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

(ns test.node.common.bible.iotests
  (:require [common.bible.io :as io]
            [cljs.test :refer-macros [deftest testing is]]
            [test.node.helpers :refer [staggs-model]]))

(deftest normalized->persisted-bible
  (let [m (io/normalized->persisted-bible staggs-model)]
    (testing "Books"
      (is (= 66 (count (get-in m [:books]))) "Book count")
      (is (= 50 (get-in m [:books 0])) "Chapter count for Genesis")
      (is (= 150 (get-in m [:books 18])) "Chapter count for Psalms")
      (is (= 22 (get-in m [:books 65])) "Chapter count for Revelation"))
    (testing "Chapters"
      (is (= 1189 (count (get-in m [:chapters]))) "Chapter count")
      (is (= 31 (get-in m [:chapters 0])) "Verse count in Genesis 1")
      (is (= 26 (get-in m [:chapters 49])) "Verse count in Genesis 50")
      (is (= 18 (get-in m [:chapters 517])) "Verse count in Psalm 40")
      (is (= 176 (get-in m [:chapters 596])) "Verse count in Psalm 119")
      (is (= 26 (get-in m [:chapters 1132])) "Verse count in Philemon")
      (is (= 21 (get-in m [:chapters 1188])) "Verse count in Rev 22"))
    (testing "Chapters with subtitle"
      (is (= 115 (count (get-in m [:subtitle]))) "Subtitled chapter count")
      (is (= false (contains? (get-in m [:subtitle]) 0)) "Gen 1 is not subtitled")
      (is (= true (contains? (get-in m [:subtitle]) 517)) "Psalm 40 is subtitled")
      (is (= false (contains? (get-in m [:subtitle]) 1132)) "Phil is not subtitled"))
    (testing "Chapter with postscript"
      (is (= 5 (count (get-in m [:postscript]))) "Postscript chapter count")
      (is (= false (contains? (get-in m [:postscript]) 0)) "Gen 1 does not have postscript")
      (is (= false (contains? (get-in m [:postscript]) 517)) "Psalm 40 does not have postscript")
      (is (= true (contains? (get-in m [:postscript]) 1132)) "Phil  does have postscript"))
    (testing "Partition size"
      (is (number? (get-in m [:partition-size]))))))

(deftest normalized->persisted-verses
  (let [m (io/normalized->persisted-verses staggs-model)]
    (is (= (+ 31102 5 115) (count m)))))