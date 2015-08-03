(ns node.unittests
  (:require
    [cljs.nodejs :as nodejs]
    [cljs.test :refer-macros [deftest is run-tests] :refer [successful?]]))

(nodejs/enable-util-print!)

(def process nodejs/process)

(deftest test-something
  (is (= 1 1))
  (is (= 1 0)))

(defn- main [] (run-tests))

(defmethod cljs.test/report [:cljs.test/default :end-run-tests] [m]
  (if (successful? m)
    (do
      (println "Success!")
      (.exit process 0))
    (do
      (println "FAIL")
      (.exit process 1))))

(set! *main-cli-fn* main)