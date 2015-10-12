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
      (is (= 66 (count (keys m))) "book count")
      (is (= :Genesis (:id (b/get-book m :Genesis))) "get by keyword")
      (is (= :Thessalonians1 (:id (b/get-book m :Thessalonians1))) "get by keyword 2")
      (is (= 1 (:num (b/get-book m :Genesis))) "book num"))))