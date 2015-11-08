(ns test.node.helpers
  (:require
    [common.normalizer.core :refer [parse]]))

(def staggs-model
  (parse "staggs" "kjv-src/www.staggs.pair.com-kjbp/kjv.txt"))