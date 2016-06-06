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

(ns biblecli.commands.verseoftheday
  (:require-macros [hiccups.core :as hiccups :refer [html]])
  (:require
    [clojure.string :as s]
    [common.normalizer.core :refer [parse]]
    [hiccups.runtime :as hiccupsrt]))

(def node-fs (js/require "fs"))
(def node-path (js/require "path"))

(def book-names
  ["Genesis"
   "Exodus"
   "Leviticus"
   "Numbers"
   "Deuteronomy"
   "Joshua"
   "Judges"
   "Ruth"
   "I Samuel"
   "II Samuel"
   "I Kings"
   "II Kings"
   "I Chronicles"
   "II Chronicles"
   "Ezra"
   "Nehemiah"
   "Esther"
   "Job"
   "Psalms"
   "Proverbs"
   "Ecclesiastes"
   "Song of Solomon"
   "Isaiah"
   "Jeremiah"
   "Lamentations"
   "Ezekiel"
   "Daniel"
   "Hosea"
   "Joel"
   "Amos"
   "Obadiah"
   "Jonah"
   "Micah"
   "Nahum"
   "Habakkuk"
   "Zephaniah"
   "Haggai"
   "Zechariah"
   "Malachi"
   "Matthew"
   "Mark"
   "Luke"
   "John"
   "Acts"
   "Romans"
   "I Corinthians"
   "II Corinthians"
   "Galatians"
   "Ephesians"
   "Philippians"
   "Colossians"
   "I Thessalonians"
   "II Thessalonians"
   "I Timothy"
   "II Timothy"
   "Titus"
   "Philemon"
   "Hebrews"
   "James"
   "I Peter"
   "II Peter"
   "I John"
   "II John"
   "III John"
   "Jude"
   "Revelation"])

(def book-name-to-index (reduce-kv #(assoc %1 %3 %2) {} book-names))

(defn parse-verse-ref [book chapter verse]
  (let [book-name-with-spaces (s/replace book "-" " ")
        book-idx (get book-name-to-index book-name-with-spaces)
        chapter-idx (dec (js/parseInt chapter))
        verse-idx (dec (js/parseInt verse))]
    [book-idx chapter-idx verse-idx]))

(defn map-verse-ref [r]
  (let [[_ book chapter verses] (re-matches #"\* https\://kingjames\.bible/(.*)-(\d+)\#(\d+(?:,\d+)*)" r)]
    (->>
      (map #(parse-verse-ref book chapter %) (s/split verses #","))
      (vec))))

(defn to-json [verses m]
  (let [[book-idx0 chapter-idx0 _] (nth verses 0)
        book-str (book-names book-idx0)
        book-chapter-str (str book-str " " (inc chapter-idx0))
        chapter (get-in m [book-idx0 :chapters chapter-idx0])
        delta (if (:subtitle chapter) 1 0)
        parts (flatten ["['"
                        book-chapter-str
                        "'"
                        (map (fn [[_ _ verse-idx]] (str ",'" (inc verse-idx) " " (get-in chapter [:verses (+ verse-idx delta)]) "'")) verses)
                        "]"])]
    (println verses)
    (println parts)
    (apply str parts)))

(defn expand-verses [list m]
  (->>
    list
    (map map-verse-ref)
    (map #(to-json % m))))

(defn format-verses [list m]
  (str "[\r\n  " (s/join ",\r\n  " (expand-verses list m)) "\r\n];"))

(defn write! [dir filename content]
  (let [filepath (.join node-path dir filename)
        buff (js/Buffer content "utf8")]
    (.writeFileSync node-fs filepath buff)))

(defn readlines [filepath]
  (.readFileSync node-fs filepath (js-obj "encoding" "utf8")))

(defn prepare! [parser src input-file output-dir]
  (let [m (parse parser src)
        list (->> (readlines input-file) (s/split-lines))]
    (write! output-dir "verse-list.json" (format-verses list m))))