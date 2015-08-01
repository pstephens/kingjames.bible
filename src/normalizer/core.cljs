(ns normalizer.core
    (:require
      [cljs.nodejs :as nodejs]
      [cognitect.transit :as t]
      [normalizer.staggs :as staggs]))

(nodejs/enable-util-print!)

(def process nodejs/process)
(def fs (nodejs/require "fs"))

(def parsers {
  :staggs staggs/parser
  })

(defn write-bible [path bible]
  (let [w (t/writer :json-verbose)
        s (t/write w bible)]
    (.writeFileSync fs path s)))

(defn run-parser [parser path outputPath]
  (let [p (parsers (keyword parser))]
    (if p
      (write-bible outputPath (p path))
      (do
        (println (str "Failed to find parser '" parser "'."))
        1))))

(defn- main [parser path output]
  (.exit process (run-parser parser path output)))

(set! *main-cli-fn* main)