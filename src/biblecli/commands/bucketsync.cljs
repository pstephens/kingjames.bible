;;;;   Copyright 2016 Peter Stephens. All Rights Reserved.
;;;;
;;;;   Licensed under the Apache License, Version 2.0 (the "License");
;;;;   you may not use this file except in compliance with the License.
;;;;   You may obtain a copy of the License at
;;;;
;;;;       http://www.apache.org/licenses/LICENSE-2.0
;;;;
;;;;   Unless required by applicable law or agreed to in writing, software
;;;;   distributed under the License is distributed on an "AS IS" BASIS,
;;;;   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;;;;   See the License for the specific language governing permissions and
;;;;   limitations under the License.

(ns biblecli.commands.bucketsync
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [to-chan chan put! tap mult <! buffer onto-chan] :as async]
            [cljs.nodejs :refer [require process]]
            [clojure.string :as s]))

(def AWS (require "aws-sdk"))
(def fs (require "fs"))
(def path (require "path"))

(defn readdir [dir]
  (let [chan (chan)
        cb (fn [err data]
             (put! chan [err data]))]
    (.readdir fs dir cb)
    chan))

(defn stat [path]
  (let [chan (chan)
        cb (fn [err data]
             (put! chan [err data]))]
    (.stat fs path cb)
    chan))

(defn stat-file [dir reldir filename]
  (let [fspath (.join path dir filename)
        relpath (if (empty? reldir) filename (str reldir "/" filename))
        statchan (stat fspath)]
    [relpath fspath statchan]))

(defn wait-all [filestatsasync]
  (go
    (loop [stats filestatsasync
           acc []]
      (let [head (first stats)
            rest (rest stats)]
        (if head
          (let [[relpath fspath statchan] head
                [err data] (<! statchan)]
            (if err
              [err nil]
              (recur rest (conj acc [relpath fspath data]))))
          [nil acc])))))

(defn isdir [stat]
  (.isDirectory stat))

(defn readdir-recursive
  ([dir] (readdir-recursive dir "" {}))
  ([dir reldir acc]
   (go
     (let [[err files] (<! (readdir dir))]
       (if err
         [err nil]
         (let [filestatsasync (->> files (map #(stat-file dir reldir %)) (doall))
               [err filestats] (<! (wait-all filestatsasync))]
           (if err
             [err nil]
             (loop [lst filestats acc acc]
               (let [[relpath fspath stat] (first lst)
                     rest (rest lst)]
                 (if relpath
                   (if (isdir stat)
                     (let [[err acc] (<! (readdir-recursive fspath relpath acc))]
                       (if err
                         [err nil]
                         (recur rest acc)))
                     (recur rest (conj acc [relpath fspath])))
                   [nil acc]))))))))))

(defn s3-list-objects-1000 [s3 continuationToken]
  (let [chan (chan)
        opts (cond-> {}
               continuationToken (assoc :ContinuationToken continuationToken))
        cb (fn [err data]
             (put! chan [err data]))]
    (.listObjectsV2 s3 (clj->js opts) cb)
    chan))

(defn s3-list-objects [s3]
  (go
    (loop [acc {}
           continuationToken nil]
      (let [[err data] (<! (s3-list-objects-1000 s3 continuationToken))]
        (if err
          [err nil]
          (let [{contents :Contents isTruncated :IsTruncated nextToken :NextContinuationToken} (js->clj data :keywordize-keys true)
                acc (->> contents
                         (reduce (fn [coll {key :Key etag :ETag}] (assoc coll key etag)) acc))]
            (if isTruncated
              (recur acc nextToken)
              [nil acc])))))))

(defn make-s3-client [profile region bucket]
  (let [cfg
          (cond-> {:region region
                   :sslEnabled true
                   :params {:Bucket bucket}}
              (not= profile "default")
              (assoc :credentials (AWS.SharedIniFileCredentials. (clj->js {:profile profile}))))]
    (AWS.S3. (clj->js cfg))))

(defn partition-files [s3keys files]
  (let [to-delete (->> s3keys (filter (fn [[key _]] (not (contains? files key)))) (vec))
        to-copy   (->> files (filter (fn [[key _]] (not (contains? s3keys key)))) (vec))
        to-update (->> files
                    (map (fn [[key path]] (let [etag (get s3keys key :notfound)] [key path etag])))
                    (filter (fn [[_ _ etag]] (not= etag :notfound)))
                    (vec))]
    [to-delete to-copy to-update]))

(defn log-action [pred whatif verbose & parts]
  (if pred
    (let [parts (cond->> (list* parts)
                         whatif (cons "Whatif: ")
                         verbose (list* (.toISOString (js/Date.)) " "))]
      (println (apply str parts)))))

(defn perform-action [chan]
  (go-loop []
    (if-let [f (<! chan)]
      (let [[err _] (<! (f))]
        (if err
          [err nil]
          (recur)))
      [nil true])))

(defn perform-actions [n coll]
  (go
    (let [ch (to-chan coll)
          results
          (->>
            (range n)
            (map (fn [_] (perform-action ch)))
            (vec)
            (async/merge)
            (async/into [])
            (<!)
            (map (fn [[err _]] err)))
          err (reduce #(or %1 %2) nil results)
          res (if err nil true)]
      [err res])))

(defn object-props [content-type max-age]
  {:content-type content-type
   :max-age max-age})

(defn calc-object-props [key]
  (cond
    ; should increase the max-age after things stabilize
    (or (re-find #"\.html$" key) (re-find #"^[^.]+$" key)) (object-props "text/html;charset=utf-8" 600)
    (re-find #"\.css$" key) (object-props "text/css;charset=utf-8" 600)
    (re-find #"\.js$" key) (object-props "text/javascript" 600)
    (re-find #"\.png" key) (object-props "image/png" 600)
    (re-find #"\.txt$" key) (object-props "text/plain" 600)
    (re-find #"\.xml$" key) (object-props "application/xml" 600)
    :else nil))

(defn calc-md5-digest [buffer]
  (let [crypto (require "crypto")
        hash (.createHash crypto "md5")]
    (.update hash buffer)
    (.digest hash)))

(defn put-s3-object [s3 key buffer md5buffer]
  (let [props (calc-object-props key)
        content-md5 (.toString md5buffer "base64")
        ch (chan)
        cb (fn [err data] (put! ch [err data]))]
    (if props
      (.putObject
        s3
        #js {:ACL "public-read"
             :Body buffer
             :CacheControl (str "max-age=" (:max-age props))
             :ContentMD5 content-md5
             :ContentType (:content-type props)
             :Key key
             :StorageClass "STANDARD"}
        cb)
      (cb (str "Could not resolve content-type for '" key "'.") nil))
    ch))

(defn delete-s3-objects [s3 keys]
  (let [params (clj->js {:Delete
                         {:Objects (->> keys
                                        (map (fn [key] {:Key key}))
                                        (vec))}})
        ch (chan)
        cb (fn [err data] (put! ch [err data]))]
    (.deleteObjects s3 params cb)
    ch))

(defn read-local-file [path]
  (let [ch (chan)
        cb (fn [err data] (put! ch [err data]))]
    (.readFile fs path cb)
    ch))

(defn make-delete-file-action [s3 keys should-delete whatif verbose]
  (fn delete []
    (go
      (let [msg (if verbose
                  (str "deleting S3 object(s) '" (s/join "', '" keys) "'")
                  (str "deleting " (count keys) " S3 object(s)"))]
        (log-action (not should-delete) whatif verbose "Skipped " msg ". Use the --delete option.")
        (log-action should-delete whatif verbose "Started " msg ".")
        (let [[err _] (if (or whatif (not should-delete))
                        [nil true]
                        (<! (delete-s3-objects s3 keys)))]
          (if err
            (do
              (log-action true whatif verbose "Failed while " msg ". " err)
              [err nil])
            (do
              (log-action (and should-delete verbose) whatif verbose "Finished " msg ".")
              [nil true])))))))

(defn make-copy-file-action [s3 [relpath filepath] whatif verbose]
  (fn copy []
    (go
      (let [msg (str "copying '" relpath "' to S3")]
        (log-action true whatif verbose "Started " msg ".")
        (let [[err buff] (<! (read-local-file filepath))]
          (if err
            (do
              (log-action true whatif verbose "Failed to read local file '" filepath "'. " err)
              [err nil])
            (let [md5buff (calc-md5-digest buff)
                  [err _] (if whatif
                            [nil true]
                            (<! (put-s3-object s3 relpath buff md5buff)))]
              (if err
                (do
                  (log-action true whatif verbose "Failed while " msg ". " err)
                  [err nil])
                (do
                  (log-action verbose whatif verbose "Finished " msg ".")
                  [nil true])))))))))

(defn make-update-file-action [s3 [relpath filepath etag] force whatif verbose]
  (fn update []
    (go
      (let [msg (str "updating '" relpath "' to S3")]
        (log-action verbose whatif verbose "Comparing local '" relpath "' with remote.")
        (let [[err buff] (<! (read-local-file filepath))]
          (if err
            (do
              (log-action true whatif verbose "Failed to read local file '" filepath "'. " err)
              [err nil])
            (let [md5buff (calc-md5-digest buff)
                  sourceMd5str (str "\"" (.toUpperCase (.toString md5buff "hex")) "\"")
                  destMd5str (.toUpperCase etag)]
              (if (or force (not= sourceMd5str destMd5str))
                (do
                  (log-action true whatif verbose "Started " msg ".")
                  (let [[err _] (if whatif
                                 [nil true]
                                 (<! (put-s3-object s3 relpath buff md5buff)))]
                    (if err
                      (do
                        (log-action true whatif verbose "failed while " msg ". " err)
                        [err nil])
                      (do
                        (log-action verbose whatif verbose "Finished " msg ".")
                        [nil true]))))))))))))

(def remote-key-white-list #"^(BingSiteAuth\.xml)|(google.*\.html)$")

(defn bucketsync
  {:summary "Synchronize a remote Amazon S3 bucket with a local asset directory."
   :doc "usage: biblecli bucketsync [--force] [-f] [--whatif] [--delete] [--verbose] [--bucket <bucket>] [--region <region>] [--profile <profile>] <dir>
   -f,
   --force               Upload all files, changed and unchanged, generally to refresh the http headers.
   --whatif              Print bucket changes that would be made without actually making changes.
   --delete              Remote files that do not exist locally will be deleted.
   --verbose             Print verbose status messages.
   --bucket <bucket>     S3 bucket id. Defaults to kingjames-beta.
   --region <region>     S3 region. Defaults to us-east-1.
   --profile <profile>   S3 credentials profile. Uses the default profile if not specified.
   <dir>                 Source asset directory."
   :async true
   :cmdline-opts {:boolean ["force" "whatif" "delete" "verbose"]
                  :string ["bucket" "region" "profile"]
                  :alias {:force "f" :verbose "v"}
                  :default {:bucket "kingjames-beta"
                            :region "us-east-1"
                            :profile "default"}}}
  [{dir :_ force :force whatif :whatif delete :delete bucket :bucket region :region profile :profile verbose :verbose}]
  (go
    (if (not= (count dir) 1)
      ["<dir> parameter required." nil]
      (let [dir (first dir)
            s3 (make-s3-client profile region bucket)
            s3ObjectsTask (s3-list-objects s3)
            filesTask (readdir-recursive dir)
            [err1 s3keys] (<! s3ObjectsTask)
            [err2 files] (<! filesTask)
            firstErr (or err1 err2)]
        (if firstErr
          [firstErr nil]
          (let [[onlyremote onlylocal both] (partition-files s3keys files)
                actions (->>
                          (concat
                            (map #(make-copy-file-action s3 % whatif verbose) onlylocal)
                            (->> onlyremote
                                 (map (fn [[key _]] key))
                                 (filter #(not (re-find remote-key-white-list %)))
                                 (partition-all 1000)
                                 (map #(make-delete-file-action s3 % delete whatif verbose)))
                            (map #(make-update-file-action s3 % force whatif verbose) both))
                          (vec))]
            (<! (perform-actions 8 actions))))))))