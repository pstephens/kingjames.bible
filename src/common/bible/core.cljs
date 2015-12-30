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

(ns common.bible.core
  (:require [clojure.string :as string]))

(def book-data
  (letfn [(make [keyword index]
            {:id keyword
             :name (name keyword)
             :index index})
           (add [coll item] (assoc coll (:id item) item))]
    (->>
      (list
        (make :Genesis 0)
        (make :Exodus 1)
        (make :Leviticus 2)
        (make :Numbers 3)
        (make :Deuteronomy 4)
        (make :Joshua 5)
        (make :Judges 6)
        (make :Ruth 7)
        (make :Samuel1 8)
        (make :Samuel2 9)
        (make :Kings1 10)
        (make :Kings2 11)
        (make :Chronicles1 12)
        (make :Chronicles2 13)
        (make :Ezra 14)
        (make :Nehemiah 15)
        (make :Esther 16)
        (make :Job 17)
        (make :Psalms 18)
        (make :Proverbs 19)
        (make :Ecclesiastes 20)
        (make :SongOfSolomon 21)
        (make :Isaiah 22)
        (make :Jeremiah 23)
        (make :Lamentations 24)
        (make :Ezekiel 25)
        (make :Daniel 26)
        (make :Hosea 27)
        (make :Joel 28)
        (make :Amos 29)
        (make :Obadiah 30)
        (make :Jonah 31)
        (make :Micah 32)
        (make :Nahum 33)
        (make :Habakkuk 34)
        (make :Zephaniah 35)
        (make :Haggai 36)
        (make :Zechariah 37)
        (make :Malachi 38)
        (make :Matthew 39)
        (make :Mark 40)
        (make :Luke 41)
        (make :John 42)
        (make :Acts 43)
        (make :Romans 44)
        (make :Corinthians1 45)
        (make :Corinthians2 46)
        (make :Galatians 47)
        (make :Ephesians 48)
        (make :Philippians 49)
        (make :Colossians 50)
        (make :Thessalonians1 51)
        (make :Thessalonians2 52)
        (make :Timothy1 53)
        (make :Timothy2 54)
        (make :Titus 55)
        (make :Philemon 56)
        (make :Hebrews 57)
        (make :James 58)
        (make :Peter1 59)
        (make :Peter2 60)
        (make :John1 61)
        (make :John2 62)
        (make :John3 63)
        (make :Jude 64)
        (make :Revelation 65))
      (reduce add {}))))

(defn get-book-meta
  ([book-id] (get book-data book-id))
  ([book-id prop] (prop (get book-data book-id))))

(defn get-book
  ([m-bible book-id]
    (let [book-index (get-book-meta book-id :index)]
      (get m-bible book-index)))
  ([m-bible book-id prop]
    (let [book (get-book m-bible book-id)]
      (prop book))))

(defn get-chapters [m-bible book-id]
  (get-book m-bible book-id :chapters))

(defn get-chapter
  ([m-bible book-id chapter-num]
    (let [chapter-index (dec chapter-num)]
      (get (get-chapters m-bible book-id) chapter-index)))
  ([m-bible book-id chapter-num prop]
    (let [chapter (get-chapter m-bible book-id chapter-num)]
      (prop chapter))))

(defn get-verses [m-bible book-id chapter-num]
  (get-chapter m-bible book-id chapter-num :verses))

(defn get-verse [m-bible book-id chapter-num verse-num]
  (let [{verses :verses subtitle? :subtitle}
          (get-chapter m-bible book-id chapter-num)
        verse-index (if subtitle? verse-num (dec verse-num))]
    (get verses verse-index)))

(defn get-subtitle [m-bible book-id chapter-num]
  (let [{verses :verses subtitle? :subtitle}
          (get-chapter m-bible book-id chapter-num)
        idx (if subtitle? 0 -1)]
    (get verses idx)))

(defn get-postscript [m-bible book-id chapter-num]
  (let [{verses :verses postscript? :postscript}
          (get-chapter m-bible book-id chapter-num)
        idx (if postscript?
              (dec (count verses))
              -1)]
    (get verses idx)))