(ns common.bible.resource
  (:require
    [cognitect.transit :as t]
    [common.bible.io :as io]
    [goog.object :as obj]))

(def node-zlib (js/require "zlib"))
(def node-crypto (js/require "crypto"))

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
    (prn "file-name" file-name)
    [{:name       (str file-name ".d")
      :content    utf8-buf
      :headers    {"Cache-Control"  "max-age=2592000, public"
                   "Content-Type"   "application/transit+json"
                   "Content-Length" (str (obj/get utf8-buf "length"))}
      :hash       h
      :short-hash short-hash
      :c-hash     h}
     {:name       (str file-name ".z")
      :content    gzip-buf
      :headers    {"Cache-Control"    "max-age=2592000, public"
                   "Content-Encoding" "gzip"
                   "Content-Type"     "application/transit+json"
                   "Content-Length"   (str (obj/get gzip-buf "length"))}
      :hash       h
      :short-hash short-hash
      :c-hash     gzip-hash}]))

(defn get-resources [m]
  [(->> m
    (io/normalized->persisted-bible)
    (encode-data "B"))])

(defn build-resources [m]
  (->> m
    (get-resources)
    (flatten)
    (reduce (fn [resources r] (assoc resources (:name r) r)) {})))