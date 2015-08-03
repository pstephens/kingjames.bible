(ns main.core
  (:require
    [cljs.nodejs :as nodejs]
    [normalizer.core :refer [run-parser]]))

(nodejs/enable-util-print!)

(def process nodejs/process)

(defn- main [parser path output]
  (.exit process (run-parser parser path output)))

(set! *main-cli-fn* main)