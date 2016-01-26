;;;;   Copyright 2016 Peter Stephens. All Rights Reserved.
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

(ns test.browser.bible.iotests
  (:require [bible.io :as io]
            [cljs.test :refer-macros [deftest testing is]]))

(deftest tryget-resources
  (let [sample-data {"A1" 1 "A2" 2 "A3" 3}]
    (is (= {"A1" 1 "A2" 2} (io/tryget-resources sample-data ["A1" "A2"])))
    (is (= {"A2" 2} (io/tryget-resources sample-data ["A2"])))
    (is (= {"A2" 2} (io/tryget-resources sample-data ["A2" "A2"])))
    (is (= nil (io/tryget-resources sample-data ["A4"])) "Returns null when a key is missing from cache.")
    (is (= nil (io/tryget-resources sample-data ["A1" "A4"])) "Returns null when a key is missing from cache. #2")
    (is (= {} (io/tryget-resources sample-data [])) "Works with empty set.")
    (is (= {} (io/tryget-resources sample-data nil)) "Works with empty nil.")))