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
  (:require-macros [bible.macros :refer [<?]]
                   [cljs.core.async.macros :refer [go]])
  (:require [bible.core :as b]
            [bible.helpers]
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
      (is (= :Genesis (get-in (b/book m [0]) [0 :id])))
      (is (= :Leviticus (get-in (b/book m [2]) [0 :id])))
      (is (thrown? js/Error (b/book m [-1])))
      (is (thrown? js/Error (b/book m [3])))
      (is (= [:Genesis :Exodus :Leviticus] (vec (map :id (b/book m [0 1 2])))))
      (is (= [:Leviticus :Genesis :Exodus] (vec (map :id (b/book m [2 0 1])))))

      (is (= :Leviticus (get-in (b/book m [:Leviticus]) [0 :id])))
      (is (= :Exodus (get-in (b/book m [:Exodus]) [0 :id])))
      (is (thrown? js/Error (b/book m [:Foo])))
      (is (= [:Genesis :Exodus :Leviticus] (vec (map :id (b/book m [:Genesis :Exodus :Leviticus])))))
      (is (= [:Leviticus :Genesis :Exodus] (vec (map :id (b/book m [:Leviticus :Genesis :Exodus])))))))

  (testing "I/O against resource"
    (async done
      (go
        (is (= :Genesis (get-in (<? (b/book [0])) [0 :id])))
        (is (= 65 (get-in (<? (b/book [:Revelation])) [0 :idx])))
        (is (= 150 (get-in (<? (b/book [:Psalms])) [0 :chapter-cnt])))
        (is (= 90 (get-in (<? (b/book [:Leviticus])) [0 :chapter-idx])))
        (is (= [:Job :Jeremiah :Matthew] (vec (map :id (<? (b/book [:Job :Jeremiah :Matthew]))))))
        (is (thrown? js/Error (<? (b/book [:Foo :Matthew]))))
        (done)))))

(deftest chapter
  (testing "Pure functional model"
    (let [gen {:id :Geneis
               :idx 0
               :chapter-cnt 2
               :chapter-idx 0}
          exo {:id :Exodus
               :idx 1
               :chapter-cnt 3
               :chapter-idx 2}
          m {:books [gen exo]
             :chapters
              [{:idx 0 :book gen :verse-cnt 31 :verse-idx 0}
               {:idx 1 :book gen :verse-cnt 25 :verse-idx 31}
               {:idx 2 :book exo :verse-cnt 22 :verse-idx 56}
               {:idx 3 :book exo :verse-cnt 25 :verse-idx 78}
               {:idx 4 :book exo :verse-cnt 22 :verse-idx 103}]}]
      (is (= 0 (get-in (b/chapter m [0]) [0 :idx])))
      (is (= 3 (get-in (b/chapter m [3]) [0 :idx])))
      (is (thrown? js/Error (b/chapter m [-1])))
      (is (thrown? js/Error (b/chapter m [5])))
      (is (thrown? js/Error (b/chapter m ["John"])))
      (is (= [0 2 3] (vec (map :idx (b/chapter m [0 2 3])))))
      (is (= [4 3 1] (vec (map :idx (b/chapter m [4 3 1])))))

      (is (= 0 (get-in (b/chapter m [[:Genesis 1]]) [0 :idx])))
      (is (= 3 (get-in (b/chapter m [[:Exodus 2]]) [0 :idx])))
      (is (thrown? js/Error (b/chapter m [[:Genesis -1]])))
      (is (thrown? js/Error (b/chapter m [[:Genesis 0]])))
      (is (thrown? js/Error (b/chapter m [[:Genesis 3]])))
      (is (thrown? js/Error (b/chapter m [[:Exodus 0]])))
      (is (thrown? js/Error (b/chapter m [[:Exodus 4]])))
      (is (thrown? js/Error (b/chapter m [[:Exodus]])))
      (is (thrown? js/Error (b/chapter m [[:Exodus 3 10]])))
      (is (thrown? js/Error (b/chapter m [[:Foo 1]])))
      (is (= [0 2 3] (vec (map :idx (b/chapter m [[:Genesis 1] [:Exodus 1] [:Exodus 2]])))))
      (is (= [4 3 1] (vec (map :idx (b/chapter m [[:Exodus 3] [:Exodus 2] [:Genesis 2]])))))))

  (testing "I/O against resource"
    (async done
      (go
        (is (= 0 (get-in (<? (b/chapter [0])) [0 :idx])))
        (is (= 13 (get-in (<? (b/chapter [[:Job 2]])) [0 :verse-cnt])))
        (is (= 56 (get-in (<? (b/chapter [[:Genesis 3]])) [0 :verse-idx])))
        (is (= :Jude (get-in (<? (b/chapter [[:Jude 1]])) [0 :book :id])))
        (is (= [50 52 54] (vec (map :idx (<? (b/chapter [[:Exodus 1] [:Exodus 3] [:Exodus 5]]))))))
        (is (thrown? js/Error (<? (b/chapter [[:Judas 3]]))))
        (done)))))

(deftest verse
  (testing "Pure functional model"
    (let [gen {:id :Geneis
               :idx 0
               :chapter-cnt 2
               :chapter-idx 0}
          exo {:id :Exodus
               :idx 1
               :chapter-cnt 3
               :chapter-idx 2}
          b {:books [gen exo]
             :chapters
              [{:idx 0 :book gen :verse-cnt 4 :verse-idx 0}
               {:idx 1 :book gen :verse-cnt 5 :verse-idx 4}
               {:idx 2 :book exo :verse-cnt 3 :verse-idx 9}
               {:idx 3 :book exo :verse-cnt 1 :verse-idx 12}
               {:idx 4 :book exo :verse-cnt 2 :verse-idx 13}]
             :partition-size 2}
          v0 ["V1" "v2"]
          v1 ["V3" "v4"]
          v2 ["V5" "v6"]
          v3 ["V7" "v8"]
          v4 ["V9" "v10"]
          v5 ["V11" "v12"]
          v6 ["V13" "v14"]
          v7 ["V15" "v16"]
          m {"B"   b
             "V00" v0
             "V01" v1
             "V02" v2
             "V03" v3
             "V04" v4
             "V05" v5
             "V06" v6
             "V07" v7}]
      (is (= "v6" (get-in (b/verse m [5]) [0 :content]))))))