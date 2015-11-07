(ns test.node.common.bible.coretests
  (:require [common.bible.core :as b]
            [cljs.test :refer-macros [deftest testing is]]))

(deftest book-metadata
  (is (= 1 (b/get-book-meta :Exodus :index)))
  (is (= "John" (b/get-book-meta :John :name)))
  (is (= :Mark (b/get-book-meta :Mark :id)))
  (is (= nil (b/get-book-meta :NotABook :name))))