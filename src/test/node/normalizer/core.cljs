(ns test.node.normalizer.core
  (:require [common.bible.core :as b]
            [cljs.test :refer-macros [deftest testing is]]
            [common.normalizer.core :refer [parse]]))

(deftest test-staggs-parser
  (let [m (parse "staggs" "kjv-src/www.staggs.pair.com-kjbp/kjv.txt")]
    (testing "Book metadata"
      (is (= 1 (b/get-book-meta :Exodus :index)))
      (is (= "John" (b/get-book-meta :John :name)))
      (is (= :Mark (b/get-book-meta :Mark :id)))
      (is (= nil (b/get-book-meta :NotABook :name))))
    (testing "Books"
      (is (= 66 (count (keys m))) "raw model book count")
      (is (= :Genesis (:id (b/get-book m :Genesis))) "get-book by keyword")
      (is (= :Thessalonians1 (:id (b/get-book m :Thessalonians1))) "get-book by keyword 2")
      (is (= :Titus (b/get-book m :Titus :id)) "get-book by keyword w/prop :id")
      (is (= :Thessalonians2 (b/get-book m :Thessalonians2 :id)) "get-book by keyword w/prop :id 2")
      (is (= 1 (b/get-book m :Genesis :num)) "returned book num")
      (is (= 66 (b/get-book m :Revelation :num)) "returned book num"))
    (testing "Chapters"
      (is (= 50 (count (b/get-chapters m :Genesis))) "get-chapters count")
      (is (= 22 (count (b/get-chapters m :Revelation))) "get-chapters count 2")
      (is (= nil (b/get-chapter m :Psalms 0)) "get-chapter invalid chapternum")
      (is (= nil (b/get-chapter m :Psalms 151)) "get-chapter invalid chapternum 2")
      (is (= 3 (:num (b/get-chapter m :Genesis 3))) "get-chapter :num")
      (is (= 119 (:num (b/get-chapter m :Psalms 119))) "get-chapter :num 2")
      (is (= 3 (b/get-chapter m :Genesis 3 :num)) "get-chapter :num")
      (is (= 119 (b/get-chapter m :Psalms 119 :num)) "get-chapter :num 2"))
    (testing "Verses"
      (is (= 176 (count (b/get-verses m :Psalms 119))) "get-verses count")
      (is (= 6 (count (b/get-verses m :Psalms 1))) "get-verses count 2"))))