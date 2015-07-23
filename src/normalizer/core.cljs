(ns normalizer.core
    (:require
      [cljs.nodejs :as nodejs]
      [normalizer.staggs :as staggs]))

(nodejs/enable-util-print!)

(def process nodejs/process)
(def fs (nodejs/require "fs"))

(def parsers {
  :staggs staggs/parser
  })

(defn write-bible [output bible]
  (println output)
  (.writeFileSync fs output (str bible)))

(defn run-parser [parser path output]
  (let [p (parsers (keyword parser))]
    (if p
      (write-bible output (p path))
      (do
        (println (str "Failed to find parser '" parser "'."))
        1))))

(defn- main [parser path output]
  (.exit process (run-parser parser path output)))

(set! *main-cli-fn* main)