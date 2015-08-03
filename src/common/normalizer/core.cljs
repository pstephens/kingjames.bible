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

(defn run-parser [parser path outputPath]
  (let [p (parsers (keyword parser))]
    (if p
      (write-bible outputPath (p node-fs path))
      (do
        (println (str "Failed to find parser '" parser "'."))
        1))))