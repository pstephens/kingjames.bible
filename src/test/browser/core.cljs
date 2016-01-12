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

(ns test.browser.core
  (:require [cljs.test :refer-macros [run-tests deftest is] :as test]))

(enable-console-print!)

(deftest test-it
  (is (= 60 60))
  (is (= 61 61)))

(def colors {
  :red    "\u001b[31m"
  :green  "\u001b[32m"
  :yellow "\u001b[33m"
  :none   "\u001b[0m"
  })

(defn ^:export run [] (run-tests))

(defmethod test/report [:cljs.test/default :end-run-tests] [m]
  (if (test/successful? m)
    (do
      (println (str (colors :green) "Success!" (colors :none)))
      (println "~~EXIT(0)~~"))
    (do
      (println (str (colors :red) "FAIL" (colors :none)))
      (println "~~EXIT(1)~~"))))