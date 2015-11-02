(ns biblecli.commands.normalize
  (:require
    [common.normalizer.core :refer [run-parser]]))

(defn normalize [parser path output]
  (run-parser parser path output))