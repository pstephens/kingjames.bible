;;;;   Copyright 2015 Peter Stephens. All Rights Reserved.
;;;;
;;;;   Licensed under the Apache License, Version 2.0 (the "License");
;;;;   you may not use this file except in compliance with the License.
;;;;   You may obtain a copy of the License at
;;;;
;;;;       http://www.apache.org/licenses/LICENSE-2.0
;;;;
;;;;   Unless required by applicable law or agreed to in writing, software
;;;;   distributed under the License is distributed on an "AS IS" BASIS,
;;;;   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;;;;   See the License for the specific language governing permissions and
;;;;   limitations under the License.

(ns test.node.unittests
  (:require
    [cljs.nodejs :as nodejs]
    [cljs.test :refer-macros [run-tests] :refer [successful?]]
    [test.node.common.bible.coretests]
    [test.node.common.bible.iotests]
    [test.node.common.normalizer.coretests]))

(nodejs/enable-util-print!)

(def process nodejs/process)

(defn runtests []
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
