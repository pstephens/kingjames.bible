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

(ns test.node.common.normalizer.coretests
  (:require
    [cljs.test :refer-macros [deftest testing is]]
    [common.bible.model :as model]
    [test.node.helpers :refer [staggs-model]]))

(deftest test-staggs-parser
  (let [m staggs-model]
    (testing "Books"
      (is (= 66 (count (keys m))) "raw book count")
      (is (= :Genesis (get-in m [0 ::model/bookId])) "get book by keyword Genesis")
      (is (= :Thessalonians1 (get-in m [51 ::model/bookId])) "get book by keyword Thessalonians1")
      (is (= 1 (get-in m [0 ::model/bookNum])) "returned book num Genesis")
      (is (= 66 (get-in m [65 ::model/bookNum])) "returned book num Revelation"))
    (testing "Chapters"
      (is (= 50 (count (get-in m [0 ::model/chapters]))) "chapters count Genesis")
      (is (= 22 (count (get-in m [65 ::model/chapters]))) "chapters count Revelation")
      (is (= 3 (get-in m [0 ::model/chapters 2 ::model/chapterNum])) "chapter :num Genesis 3")
      (is (= 119 (get-in m [18 ::model/chapters 118 ::model/chapterNum])) "chapter :num Psalms 119"))
    (testing "Verses"
      (is (= 176 (count (get-in m [18 ::model/chapters 118 ::model/verses]))) "verse count Psalm 119")
      (is (= 6 (count (get-in m [18 ::model/chapters 0 ::model/verses]))) "verse count Psalm 1")
      (is (= "In the beginning God created the heaven and the earth."
        (get-in m [0 ::model/chapters 0 ::model/verses 0])) "verse content Gen 1:1")
      (is (= "I waited patiently for the LORD; and he inclined unto me, and heard my cry."
        (get-in m [18 ::model/chapters 39 ::model/verses 1])) "verse content Ps 40:1")
      (is (= "In the beginning was the Word, and the Word was with God, and the Word was God."
        (get-in m [42 ::model/chapters 0 ::model/verses 0])) "verse content John 1:1")
      (is (= "The grace of our Lord Jesus Christ [be] with your spirit. Amen."
        (get-in m [56 ::model/chapters 0 ::model/verses 24])) "verse content Phm 1:25")
      (is (= "The grace of our Lord Jesus Christ [be] with you all. Amen."
        (get-in m [65 ::model/chapters 21 ::model/verses 20])) "verse content Rev 22:21")
      (is (= "To God only wise, [be] glory through Jesus Christ for ever. Amen."
        (get-in m [44 ::model/chapters 15 ::model/verses 26])))
      (is (= "Blessed [are] the undefiled in the way, who walk in the law of the LORD."
        (get-in m [18 ::model/chapters 118 ::model/verses 0])) "verse content Ps 119:1")
      (is (= "And upon her forehead [was] a name written, MYSTERY, BABYLON THE GREAT, THE MOTHER OF HARLOTS AND ABOMINATIONS OF THE EARTH."
        (get-in m [65 ::model/chapters 16 ::model/verses 4])) "verse content Rev 17:5"))
    (testing "Verse Subtitle"
      (is (= false (get-in m [0 ::model/chapters 0 ::model/subtitle])) "subtitle? Gen 1")
      (is (= "To the chief Musician, A Psalm of David."
        (get-in m [18 ::model/chapters 39 ::model/verses 0])) "subtitle Ps 40")
      (is (= true (get-in m [18 ::model/chapters 39 ::model/subtitle])) "subtitle? Ps 40"))
    (testing "Verse Postscript"
      (is (= false (get-in m [0 ::model/chapters 0 ::model/postscript])) "postscript? Gen 1")
      (is (= "Written from Rome to Philemon, by Onesimus a servant."
        (get-in m [56 ::model/chapters 0 ::model/verses 25])) "postscript Phil 1")
      (is (= true (get-in m [56 ::model/chapters 0 ::model/postscript])) "postscript? Phil 1")
      (is (= "Written to the Romans from Corinthus, [and sent] by Phebe servant of the church at Cenchrea."
        (get-in m [44 ::model/chapters 15 ::model/verses 27])))
      (is (= true (get-in m [44 ::model/chapters 15 ::model/postscript]))))))
