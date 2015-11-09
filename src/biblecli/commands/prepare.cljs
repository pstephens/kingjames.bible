(ns biblecli.commands.prepare
  (:require
    [cljs.nodejs :as nodejs]
    [clojure.string :as string]
    [cognitect.transit :as t]
    [common.normalizer.core :refer [parse]]
    [common.bible.io :as io]))

(def node-fs (nodejs/require "fs"))
(def node-zlib (nodejs/require "zlib"))
(def node-crypto (nodejs/require "crypto"))
(def node-path (nodejs/require "path"))

(def verse-partition-size 781)
(def hash-len 12)

(defn compute-hash [buf]
  (let [h (.createHash node-crypto "sha1")]
    (do
      (.update h buf)
      (.digest h "hex"))))

(defn encode-data [name data]
  (let [writer    (t/writer :json-verbose)
        string    (t/write writer data)
        utf8-buf  (js/Buffer string "utf8")
        gzip-buf  (.gzipSync node-zlib utf8-buf)
        h         (compute-hash utf8-buf)]
    {:name name
     :buf utf8-buf
     :gzip gzip-buf
     :hash h}))

(defn format-name [name num digits]
  (let [numstr (str num)
        numzeros (- digits (count numstr))
        zeros (str (string/join (repeat numzeros "0")))]
    (str name zeros numstr)))

(defn write-data! [dir encoded-data]
  (let [filename (str (:name encoded-data) "-" (subs (:hash encoded-data) 0 hash-len))
        filepath (.join node-path dir filename)]
    (do
      (.writeFileSync node-fs (str filepath ".d") (:buf encoded-data))
      (.writeFileSync node-fs (str filepath ".z") (:gzip encoded-data)))))

(defn prepare! [parser src output-dir]
  (let [m (parse parser src)]
    (do
      (->> m
        (io/normalized->persisted-bible)
        (encode-data "B")
        (write-data! output-dir))

      (doseq [[i part] (->> m
                  (io/normalized->persisted-verses)
                  (partition-all verse-partition-size)
                  (map-indexed #(vector %1 (vec %2))))]
        (->> part
          (encode-data (format-name "V" i 2))
          (write-data! output-dir))))))