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

(ns test.browser.bible.coretests
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [bible.core :as b]
            [cljs.core.async :refer [<!]]
            [cljs.test :refer-macros [async deftest testing is]]))

(deftest parse-ref
  (is (= (b/parse-ref [:Genesis]) [:Genesis nil nil])))

(deftest book
  (testing "Pure functional model"
    (let [m {:books
              [{:id :Genesis
                :idx 0
                :chapter-cnt 50
                :chapter-idx 0}
               {:id :Exodus
                :idx 1
                :chapter-cnt 40
                :chapter-idx 50}
               {:id :Leviticus
                :idx 2
                :chapter-cnt 27
                :chapter-idx 90}]}]
      (is (= :Genesis (get-in (b/book m [0]) [0 0 :id])))
      (is (= :Leviticus (get-in (b/book m [2]) [0 0 :id])))
      (is (some? (get-in (b/book m [-1]) [1])))
      (is (some? (get-in (b/book m [3]) [1])))
      (is (= [:Genesis :Exodus :Leviticus] (vec (map :id (get-in (b/book m [0 1 2]) [0])))))
      (is (= [:Leviticus :Genesis :Exodus] (vec (map :id (get-in (b/book m [2 0 1]) [0])))))

      (is (= :Leviticus (get-in (b/book m [:Leviticus]) [0 0 :id])))
      (is (= :Exodus (get-in (b/book m [:Exodus]) [0 0 :id])))
      (is (some? (get-in (b/book m [:Foo]) [1])))
      (is (= [:Genesis :Exodus :Leviticus] (vec (map :id (get-in (b/book m [:Genesis :Exodus :Leviticus]) [0])))))
      (is (= [:Leviticus :Genesis :Exodus] (vec (map :id (get-in (b/book m [:Leviticus :Genesis :Exodus]) [0])))))))

  (async done
    (go
      (is (= :Genesis (get-in (<! (b/book [0])) [0 0 :id])))
      (is (= 65 (get-in (<! (b/book [:Revelation])) [0 0 :idx])))
      (is (= 150 (get-in (<! (b/book [:Psalms])) [0 0 :chapter-cnt])))
      (is (= 90 (get-in (<! (b/book [:Leviticus])) [0 0 :chapter-idx])))
      (is (= [:Job :Jeremiah :Matthew] (vec (map :id (get-in (<! (b/book [:Job :Jeremiah :Matthew])) [0])))))
      (is (some? (get-in (<! (b/book [:Foo :Matthew])) [1])))
      (done))))