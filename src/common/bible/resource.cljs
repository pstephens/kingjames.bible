;;;;   Copyright 2015 Peter Stephens. All Rights Reserved.
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

(ns common.bible.resource
  (:require
    [clojure.string :as string]
    [cognitect.transit :as t]
    [common.bible.io :as io]
    [goog.object :as obj]))

(def node-zlib (js/require "zlib"))
(def node-crypto (js/require "crypto"))

(def verse-partition-size 781)
(def hash-len 12)

(defn compute-hash [buf]
  (let [h (.createHash node-crypto "md5")]
    (do
      (.update h buf)
      (.digest h "hex"))))

(defn encode-data [name data]
  (let [writer     (t/writer :json-verbose)
        string     (t/write writer data)
        utf8-buf   (js/Buffer string "utf8")
        gzip-buf   (.gzipSync node-zlib utf8-buf)
        h          (compute-hash utf8-buf)
        gzip-hash  (compute-hash gzip-buf)
        short-hash (subs h 0 hash-len)
        file-name  (str name "-" short-hash)]
    [{:name       (str file-name ".d")
      :content    utf8-buf
      :headers    {"Cache-Control"  "max-age=2592000, public"
                   "Content-Type"   "application/transit+json"
                   "Content-Length" (str (obj/get utf8-buf "length"))
                   "Last-Modified"  (.toUTCString (js/Date.))
                   "ETag"           (str "\"" short-hash "\"")}
      :hash       h
      :short-hash short-hash
      :c-hash     h}
     {:name       (str file-name ".z")
      :content    gzip-buf
      :headers    {"Cache-Control"    "max-age=2592000, public"
                   "Content-Encoding" "gzip"
                   "Content-Type"     "application/transit+json"
                   "Content-Length"   (str (obj/get gzip-buf "length"))
                   "Last-Modified"    (.toUTCString (js/Date.))
                   "ETag"             (str "\"" short-hash "\"")}
      :hash       h
      :short-hash short-hash
      :c-hash     gzip-hash}]))

(defn format-name [name num digits]
  (let [numstr (str num)
        numzeros (- digits (count numstr))
        zeros (str (string/join (repeat numzeros "0")))]
    (str name zeros numstr)))

(defn get-resources [m]
  [(->> m
    (io/normalized->persisted-bible)
    (encode-data "B"))
   (->> m
    (io/normalized->persisted-verses)
    (partition-all verse-partition-size)
    (map-indexed #(encode-data (format-name "V" %1 2) %2))
    (vec))])

(defn build-resources [m]
  (->> m
    (get-resources)
    (flatten)
    (reduce (fn [resources r] (assoc resources (:name r) r)) {})))