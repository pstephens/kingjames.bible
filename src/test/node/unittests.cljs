(ns test.node.unittests
  (:require
    [cljs.nodejs :as nodejs]
    [cljs.test :refer-macros [run-tests] :refer [successful?]]
    [test.node.common.bible.coretests]
    [test.node.common.bible.iotests]
    [test.node.common.normalizer.coretests]))

(nodejs/enable-util-print!)

(def process nodejs/process)

(defn- main []
  (run-tests 'test.node.common.normalizer.coretests
             'test.node.common.bible.coretests
             'test.node.common.bible.iotests))

(defmethod cljs.test/report [:cljs.test/default :end-run-tests] [m]
  (if (successful? m)
    (do
      (println "Success!")
      (.exit process 0))
    (do
      (println "FAIL")
      (.exit process 1))))

(set! *main-cli-fn* main)