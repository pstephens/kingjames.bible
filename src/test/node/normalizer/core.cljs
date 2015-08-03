(ns node.normalizer.core
  (:require [cljs.test :refer-macros [deftest testing is]]
            [normalizer.core :refer [parse]]))

(deftest test-staggs-parser
  (let [m (parse "staggs" "kjv-src/www.staggs.pair.com-kjbp/kjv.txt")]
    (testing "Books"
      (is (= 66 (count (keys m))) "Count")
      )))