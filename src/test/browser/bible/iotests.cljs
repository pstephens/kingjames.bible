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
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [bible.io :as io]
            [cljs.core.async :refer [<!]]
            [cljs.test :refer-macros [async deftest testing is]]))

(deftest tryget-resources
  (let [sample-data {"A1" 1 "A2" 2 "A3" 3}]
    (is (= {"A1" 1 "A2" 2} (io/tryget-resources sample-data ["A1" "A2"])))
    (is (= {"A2" 2} (io/tryget-resources sample-data ["A2"])))
    (is (= {"A2" 2} (io/tryget-resources sample-data ["A2" "A2"])))
    (is (= nil (io/tryget-resources sample-data ["A4"])) "Returns null when a key is missing from cache.")
    (is (= nil (io/tryget-resources sample-data ["A1" "A4"])) "Returns null when a key is missing from cache. #2")
    (is (= {} (io/tryget-resources sample-data [])) "Works with empty set.")
    (is (= {} (io/tryget-resources sample-data nil)) "Works with empty nil.")))

(deftest resources
  (async done
    (go
      (testing "B-Book model"
        (is (= 66 (count (get-in (<! (io/resources ["B"])) ["B" :books]))))
        (is (= :Genesis (get-in (<! (io/resources ["B"])) ["B" :books 0 :id])))
        (is (= :Revelation (get-in (<! (io/resources ["B"])) ["B" :books 65 :id])))
        (is (= 50 (get-in (<! (io/resources ["B"])) ["B" :books 0 :chapter-cnt])))
        (is (= 22 (get-in (<! (io/resources ["B"])) ["B" :books 65 :chapter-cnt])))
        (is (= 0 (get-in (<! (io/resources ["B"])) ["B" :books 0 :chapter-idx])))
        (is (= 50 (get-in (<! (io/resources ["B"])) ["B" :books 1 :chapter-idx])))
        (is (= 0 (get-in (<! (io/resources ["B"])) ["B" :books 0 :idx])))
        (is (= 10 (get-in (<! (io/resources ["B"])) ["B" :books 10 :idx]))))
      (testing "B-Chapter model"
        (is (= 0 (get-in (<! (io/resources ["B"])) ["B" :chapters 0 :idx])))
        (is (= 300 (get-in (<! (io/resources ["B"])) ["B" :chapters 300 :idx])))
        (is (= 0 (get-in (<! (io/resources ["B"])) ["B" :chapters 0 :verse-idx])))
        (is (= 31 (get-in (<! (io/resources ["B"])) ["B" :chapters 1 :verse-idx])))
        (is (= 31 (get-in (<! (io/resources ["B"])) ["B" :chapters 0 :verse-cnt])))
        (is (= 25 (get-in (<! (io/resources ["B"])) ["B" :chapters 1 :verse-cnt]))))
      (done))))