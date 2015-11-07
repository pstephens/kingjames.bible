(ns biblecli.commands.prepare
  (:require
    [cljs.nodejs :as nodejs]
    [cognitect.transit :as t]
    [common.normalizer.core :refer [parse]]))

(def node-fs (nodejs/require "fs"))
(def node-zlib (nodejs/require "zlib"))
(def node-crypto (nodejs/require "crypto"))
(def node-path (nodejs/require "path"))

(defn compute-hash [buf]
  (let [h (.createHash node-crypto "sha1")]
    (do
      (.update h buf)
      (.digest h "hex"))))

(defn encode-data [data]
  (let [writer    (t/writer :json-verbose)
        string    (t/write writer data)
        utf8-buf  (js/Buffer string "utf8")
        gzip-buf  (.gzipSync node-zlib utf8-buf)
        h         (compute-hash utf8-buf)]
    {:buf utf8-buf
     :gzip gzip-buf
     :hash h}))

(defn prepare-book! [dir book]
  (let [encoded (encode-data book)
        filepath (.join node-path dir (str "B" (:num book) "-" (:hash encoded)))]
    (do
      (.writeFileSync node-fs (str filepath ".d") (:buf encoded))
      (.writeFileSync node-fs (str filepath ".z") (:gzip encoded)))))

(defn prepare! [parser src output-dir]
  (let [m (parse parser src)
        book-count (count m)]
    (loop [book-index 0]
      (if (< book-index book-count)
        (do
          (prepare-book! output-dir (get m book-index))
          (recur (inc book-index)))))))