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

(ns common.normalizer.staggs
  (:require [clojure.string :as string]
            [common.bible.core]
            [common.bible.model :as model]
            [common.normalizer.filesystem :refer [read-text]]))

(def book-name-map {
  "Ge"   :Genesis
  "Ex"   :Exodus
  "Le"   :Leviticus
  "Nu"   :Numbers
  "De"   :Deuteronomy
  "Jos"  :Joshua
  "Jg"   :Judges
  "Ru"   :Ruth
  "1Sa"  :Samuel1
  "2Sa"  :Samuel2
  "1Ki"  :Kings1
  "2Ki"  :Kings2
  "1Ch"  :Chronicles1
  "2Ch"  :Chronicles2
  "Ezr"  :Ezra
  "Ne"   :Nehemiah
  "Es"   :Esther
  "Job"  :Job
  "Ps"   :Psalms
  "Pr"   :Proverbs
  "Ec"   :Ecclesiastes
  "So"   :SongOfSolomon
  "Isa"  :Isaiah
  "Jer"  :Jeremiah
  "La"   :Lamentations
  "Eze"  :Ezekiel
  "Da"   :Daniel
  "Ho"   :Hosea
  "Joe"  :Joel
  "Am"   :Amos
  "Ob"   :Obadiah
  "Jon"  :Jonah
  "Mic"  :Micah
  "Na"   :Nahum
  "Hab"  :Habakkuk
  "Zep"  :Zephaniah
  "Hag"  :Haggai
  "Zec"  :Zechariah
  "Mal"  :Malachi
  "Mt"   :Matthew
  "Mr"   :Mark
  "Lu"   :Luke
  "Joh"  :John
  "Ac"   :Acts
  "Ro"   :Romans
  "1Co"  :Corinthians1
  "2Co"  :Corinthians2
  "Ga"   :Galatians
  "Eph"  :Ephesians
  "Php"  :Philippians
  "Col"  :Colossians
  "1Th"  :Thessalonians1
  "2Th"  :Thessalonians2
  "1Ti"  :Timothy1
  "2Ti"  :Timothy2
  "Tit"  :Titus
  "Phm"  :Philemon
  "Heb"  :Hebrews
  "Jas"  :James
  "1Pe"  :Peter1
  "2Pe"  :Peter2
  "1Jo"  :John1
  "2Jo"  :John2
  "3Jo"  :John3
  "Jude" :Jude
  "Re"   :Revelation})

(defn ^:private cleanup-verse-content [content]
  (->
    (str content)
    (string/replace
      #"^(ALEPH\. |BETH\. |GIMEL\. |DALETH\. |HE\. |VAU\. |ZAIN\. |CHETH\. |TETH\. |JOD\. |CAPH\. |LAMED\. |MEM\. |NUN\. |SAMECH\. |AIN\. |PE\. |TZADDI\. |KOPH\. |RESH\. |SCHIN\. |TAU\. )"
      "")
    (string/trim)))

(defn transform-verse [s]
  (let [[_ book ch verse content] (re-matches #"\s+(\w+)\s+(\d+)\:(\d+)\s+(.*)" s)
        [_ subtitle cont postscript] (re-matches #"(?:^\s*<<(.*)>>)?([^<]*)(?:<<\[(.*)\]>>\s*$)?" (str content))]
    {::model/bookId (book-name-map book)
     ::model/chapterNum (int ch)
     ::model/verse (cleanup-verse-content cont)
     ::model/subtitle subtitle
     ::model/postscript postscript}))

(defn transform-chapter [verses]
  (let [v1 (first verses)
        subtitle (::model/subtitle v1)
        vn (last verses)
        postscript (::model/postscript vn)]
    {::model/chapterNum (::model/chapterNum v1)
     ::model/bookId (::model/bookId v1)
     ::model/subtitle (some? subtitle)
     ::model/postscript (some? postscript)
     ::model/verses
       (->>
         (flatten [subtitle
                   (map ::model/verse verses)
                   postscript])
         (filter some?)
         (vec))}))

(defn transform-book [verses]
  (let [v1 (first verses)
        book-data (common.bible.core/book-data (v1 ::model/bookId))]
    {::model/bookId (book-data ::model/bookId)
     ::model/bookNum (inc (book-data :index))
     ::model/chapters
       (->>
         verses
         (partition-by ::model/chapterNum)
         (map transform-chapter)
         (vec))}))

(defn transform-bible [str]
  (->>
    str
    (string/split-lines)
    (map transform-verse)
    (filter ::model/bookId)
    (partition-by ::model/bookId)
    (map transform-book)
    (vec)))

(defn parser [fs path]
  (->>
    (read-text fs path)
    (transform-bible)))
