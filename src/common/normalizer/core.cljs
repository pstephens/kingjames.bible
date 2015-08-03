(ns normalizer.core
    (:require
      [cognitect.transit :as t]
      [normalizer.filesystem :refer [NodeFs write-text]]
      [normalizer.staggs]
      [normalizer.transit]))

(def node-fs (NodeFs.))

(def parsers {
  :staggs normalizer.staggs/parser
  :transit normalizer.transit/parser})

(defn write-bible [path bible]
  (let [w (t/writer :json-verbose)
        s (t/write w bible)]
    (write-text node-fs path s)))

(defn parse [parser path]
  (let [p (parsers (keyword parser))]
    (if p
      (p node-fs path)
      (throw (str "Failed to find parser '" parser "'.")))))

(defn run-parser [parser path outputPath]
  (write-bible outputPath (parse parser path)))