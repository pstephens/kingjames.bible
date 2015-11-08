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
      (is (= 31 (get-in m [:chapters 0])) "Verse count in Genesis 1")
      (is (= 26 (get-in m [:chapters 49])) "Verse count in Genesis 50"))))