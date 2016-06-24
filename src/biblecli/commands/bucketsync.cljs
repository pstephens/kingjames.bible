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
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [to-chan chan put! tap mult <!]]
            [cljs.nodejs :refer [require]]
            [clojure.string :as s]))

(def AWS (js/require "aws-sdk"))
(def fs (js/require "fs"))
(def path (js/require "path"))
(def process js/process)

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
  [(->> s3keys (filter (fn [[key _]] (not (contains? files key)))) (vec))
   (->> files (filter (fn [[key _]] (not (contains? s3keys key)))) (vec))
   (->> files
        (map (fn [[key path]] (let [etag (get s3keys key :notfound)] [key path etag])))
        (filter (fn [[_ _ etag]] (not= etag :notfound)))
        (vec))])

(defn bucketsync
  {:summary "Synchronize a remote Amazon S3 bucket with a local asset directory."
   :doc "usage: biblecli bucketsync [--force] [-f] [--whatif] [--bucket <bucket>] [--region <region>] [--profile <profile>] <dir>
   -f,
   --force               Upload all files, changed and unchanged, generally to refresh the http headers.
   --whatif              Print bucket changes that would be made without actually making changes.
   --bucket <bucket>     S3 bucket id. Defaults to kingjames-beta.
   --region <region>     S3 region. Defaults to us-east-1.
   --profile <profile>   S3 credentials profile. Uses the default profile if not specified.
   <dir>                 Source asset directory."
   :async true
   :cmdline-opts {:boolean ["force" "whatif"]
                  :string ["bucket" "region" "profile"]
                  :alias {:force "f"}
                  :default {:bucket "kingjames-beta"
                            :region "us-east-1"
                            :profile "default"}}}
  [{dir :_ force :force whatif :whatif bucket :bucket region :region profile :profile}]
  (go
    (if (not= (count dir) 1)
      ["<dir> parameter required." nil]
      (let [dir (first dir)
            s3 (make-s3-client profile region bucket)
            s3ObjectsTask (s3-list-objects s3)
            filesTask (readdir-recursive dir)
            [err1 s3keys] (<! s3ObjectsTask)
            [err2 files] (<! filesTask)]
        (cond
          err1 [err1 nil]
          err2 [err2 nil]
          :else
            (let [[onlyremote onlylocal both] (partition-files s3keys files)]
              (println "Remote: " (s/join onlyremote " | "))
              (println "Local: " (s/join onlylocal " | "))
              (println "Both: " (s/join both " | "))
              [nil true]))))))