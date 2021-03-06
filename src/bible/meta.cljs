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

(ns bible.meta)

(def books
  [:Genesis
   :Exodus
   :Leviticus
   :Numbers
   :Deuteronomy
   :Joshua
   :Judges
   :Ruth
   :Samuel1
   :Samuel2
   :Kings1
   :Kings2
   :Chronicles1
   :Chronicles2
   :Ezra
   :Nehemiah
   :Esther
   :Job
   :Psalms
   :Proverbs
   :Ecclesiastes
   :SongOfSolomon
   :Isaiah
   :Jeremiah
   :Lamentations
   :Ezekiel
   :Daniel
   :Hosea
   :Joel
   :Amos
   :Obadiah
   :Jonah
   :Micah
   :Nahum
   :Habakkuk
   :Zephaniah
   :Haggai
   :Zechariah
   :Malachi
   :Matthew
   :Mark
   :Luke
   :John
   :Acts
   :Romans
   :Corinthians1
   :Corinthians2
   :Galatians
   :Ephesians
   :Philippians
   :Colossians
   :Thessalonians1
   :Thessalonians2
   :Timothy1
   :Timothy2
   :Titus
   :Philemon
   :Hebrews
   :James
   :Peter1
   :Peter2
   :John1
   :John2
   :John3
   :Jude
   :Revelation])

(def ^:private book-id-to-idx-map
   (delay
      (->>
         books
         (map-indexed (fn [idx id] [id idx]))
         (reduce conj {}))))

(defn book-id-to-idx [book-id]
   (get @book-id-to-idx-map book-id))