(ns test.node.common.normalizer.coretests
  (:require [cljs.test :refer-macros [deftest testing is]]
            [common.normalizer.core :refer [parse]]))

(deftest test-staggs-parser
  (let [m (parse "staggs" "kjv-src/www.staggs.pair.com-kjbp/kjv.txt")]
    (testing "Books"
      (is (= 66 (count (keys m))) "raw book count")
      (is (= :Genesis (get-in m [0 :id])) "get book by keyword Genesis")
      (is (= :Thessalonians1 (get-in m [51 :id])) "get book by keyword Thessalonians1")
      (is (= 1 (get-in m [0 :num])) "returned book num Genesis")
      (is (= 66 (get-in m [65 :num])) "returned book num Revelation"))
    (testing "Chapters"
      (is (= 50 (count (get-in m [0 :chapters]))) "chapters count Genesis")
      (is (= 22 (count (get-in m [65 :chapters]))) "chapters count Revelation")
      (is (= 3 (get-in m [0 :chapters 2 :num])) "chapter :num Genesis 3")
      (is (= 119 (get-in m [18 :chapters 118 :num])) "chapter :num Psalms 119"))
    (testing "Verses"
      (is (= 176 (count (get-in m [18 :chapters 118 :verses]))) "verse count Psalm 119")
      (is (= 6 (count (get-in m [18 :chapters 0 :verses]))) "verse count Psalm 1")
      (is (= "In the beginning God created the heaven and the earth."
        (get-in m [0 :chapters 0 :verses 0])) "verse content Gen 1:1")
      (is (= "I waited patiently for the LORD; and he inclined unto me, and heard my cry."
        (get-in m [18 :chapters 39 :verses 1])) "verse content Ps 40:1")
      (is (= "In the beginning was the Word, and the Word was with God, and the Word was God."
        (get-in m [42 :chapters 0 :verses 0])) "verse content John 1:1")
      (is (= "The grace of our Lord Jesus Christ [be] with your spirit. Amen."
        (get-in m [56 :chapters 0 :verses 24])) "verse content Phm 1:25")
      (is (= "The grace of our Lord Jesus Christ [be] with you all. Amen."
        (get-in m [65 :chapters 21 :verses 20])) "verse content Rev 22:21"))
    (testing "Verse Subtitle"
      (is (= false (get-in m [0 :chapters 0 :subtitle])) "subtitle? Gen 1")
      (is (= "To the chief Musician, A Psalm of David."
        (get-in m [18 :chapters 39 :verses 0])) "subtitle Ps 40")
      (is (= true (get-in m [18 :chapters 39 :subtitle])) "subtitle? Ps 40")
    (testing "Verse Postscript"
      (is (= false (get-in m [0 :chapters 0 :postscript])) "postscript? Gen 1")
      (is (= "Written from Rome to Philemon, by Onesimus a servant."
        (get-in m [56 :chapters 0 :verses 25])) "postscript Phil 1")
      (is (= true (get-in m [56 :chapters 0 :postscript])) "postscript? Phil 1")))))