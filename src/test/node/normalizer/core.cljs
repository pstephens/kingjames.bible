(ns node.normalizer.core
  (:require [bible.core :as b]
            [cljs.test :refer-macros [deftest testing is]]
            [normalizer.core :refer [parse]]))

(deftest test-staggs-parser
  (let [m (parse "staggs" "kjv-src/www.staggs.pair.com-kjbp/kjv.txt")]
    (testing "Book metadata"
      (is (= 1 (b/get-book-meta :Exodus :index))))
    (testing "Books"
      (is (= 66 (count (keys m))) "count")
      (is (= :Genesis (:id (b/get-book m :Genesis))) "get by keyword")
      )))