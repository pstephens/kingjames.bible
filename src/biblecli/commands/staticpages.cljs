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

(ns biblecli.commands.staticpages
  (:require-macros [hiccups.core :as hiccups :refer [html]])
  (:require
    [clojure.string :as s]
    [common.normalizer.core :refer [parse]]
    [hiccups.runtime :as hiccupsrt]))

(def node-fs (js/require "fs"))
(def node-path (js/require "path"))

(defn style []
"
.ref {
  display: none;
}
.ps {
  text-indent: 0;
  margin-top: 0.5em;
  font-size: 85%;
  text-align: center;
}
.intro {
  text-indent: 0;
  margin-top: 0.5em;
  margin-bottom: 0.35em;
  font-size: 85%;
  text-align: center;
}
.chap {
  text-align: center;
  page-break-before: always;
}
.verse {
  margin-top: 0.125em;
  margin-bottom: 0.125em;
  line-height: 1.2em;
  text-indent: 0;
}
.ps119h {
  margin-top: 0.6em;
  margin-bottom: 0.3em;
  text-indent: 0;
  text-align: center;
}
.chapter {
    font-size: 120%;
    font-weight: bold;
}
.tocp {
    text-indent: -2em;
    padding-left: 2em;
    margin-bottom: 0.25em;
    margin-top: 0;
    text-align: left;
}
")

(defn book-name [book-id]
  (let [m {
    :Genesis        "Genesis"
    :Exodus         "Exodus"
    :Leviticus      "Leviticus"
    :Numbers        "Numbers"
    :Deuteronomy    "Deuteronomy"
    :Joshua         "Joshua"
    :Judges         "Judges"
    :Ruth           "Ruth"
    :Samuel1        "I Samuel"
    :Samuel2        "II Samuel"
    :Kings1         "I Kings"
    :Kings2         "II Kings"
    :Chronicles1    "I Chronicles"
    :Chronicles2    "II Chronicles"
    :Ezra           "Ezra"
    :Nehemiah       "Nehemiah"
    :Esther         "Esther"
    :Job            "Job"
    :Psalms         "Psalms"
    :Proverbs       "Proverbs"
    :Ecclesiastes   "Ecclesiastes"
    :SongOfSolomon  "Song of Solomon"
    :Isaiah         "Isaiah"
    :Jeremiah       "Jeremiah"
    :Lamentations   "Lamentations"
    :Ezekiel        "Ezekiel"
    :Daniel         "Daniel"
    :Hosea          "Hosea"
    :Joel           "Joel"
    :Amos           "Amos"
    :Obadiah        "Obadiah"
    :Jonah          "Jonah"
    :Micah          "Micah"
    :Nahum          "Nahum"
    :Habakkuk       "Habakkuk"
    :Zephaniah      "Zephaniah"
    :Haggai         "Haggai"
    :Zechariah      "Zechariah"
    :Malachi        "Malachi"
    :Matthew        "Matthew"
    :Mark           "Mark"
    :Luke           "Luke"
    :John           "John"
    :Acts           "Acts"
    :Romans         "Romans"
    :Corinthians1   "I Corinthians"
    :Corinthians2   "II Corinthians"
    :Galatians      "Galatians"
    :Ephesians      "Ephesians"
    :Philippians    "Philippians"
    :Colossians     "Colossians"
    :Thessalonians1 "I Thessalonians"
    :Thessalonians2 "II Thessalonians"
    :Timothy1       "I Timothy"
    :Timothy2       "II Timothy"
    :Titus          "Titus"
    :Philemon       "Philemon"
    :Hebrews        "Hebrews"
    :James          "James"
    :Peter1         "I Peter"
    :Peter2         "II Peter"
    :John1          "I John"
    :John2          "II John"
    :John3          "III John"
    :Jude           "Jude"
    :Revelation     "Revelation"}]
    (get m book-id)))

(defn chapter-name
  ([book-id chap-num chap-count]
    (if (> chap-count 1)
      (str (book-name book-id) " " chap-num)
      (book-name book-id)))
  ([{book-id :book-id
     chap-num :num
     chap-cnt :chap-cnt}]
    (chapter-name book-id chap-num chap-cnt)))

(defn rel-url
  ([book-id chap-num chap-count]
    (s/replace (chapter-name book-id chap-num chap-count) " " "-"))
  ([{book-id :book-id
     chap-num :num
     chap-cnt :chap-cnt}]
    (rel-url book-id chap-num chap-cnt)))

(defn toc-book [b]
  [:p {:class "tocp"}
    [:span {:class "chapter"} (book-name (:id b))]
    " "
    (->> (:chapters b)
      (map (fn [ch]
        (list [:a {:href (rel-url (:id b) (:num ch) (count (:chapters b)))} (:num ch)] " "))))])

(defn toc [m]
  (html
    [:html
      [:head
        [:title "The King James Bible"]
        [:link {:rel "stylesheet" :type "text/css" :href "styles.css"}]]
      [:body
        [:h1 "The King James Bible"]

        [:h2 "The Old Testament"]
        (->> m
          (take 39)
          (map toc-book))

        [:h2 "The New Testament"]
        (->> m
          (drop 39)
          (map toc-book))]]))

(defn chapters [m]
  (->> m
    (mapcat
      (fn [b]
        (map
          (fn [ch]
            (merge ch
              {:book-id (:id b)
               :book-num (:num b)
               :chap-cnt (count (:chapters b))}))
          (:chapters b))))))

(defn verse [i ch v]
  [:p {:class "foo"} v])

(defn chapter
  [{book-id :book-id
    chap-num :num
    chap-cnt :chap-cnt
    verses :verses
    :as ch}]
  (html
    [:html
      [:head
        [:title (str (chapter-name ch) " - The King James Bible")]
        [:link {:rel "stylesheet" :type "text/css" :href "styles.css"}]]
      [:body
        [:h2 {:class "chap"}
          (chapter-name ch)]
        (map-indexed #(verse %1 ch %2) verses)]]))

(defn write! [dir filename content]
  (let [filepath (.join node-path dir filename)
        buff (js/Buffer content "utf8")]
    (.writeFileSync node-fs filepath buff)))

(defn prepare! [parser src output-dir]
  (let [m (parse parser src)]
    (write! output-dir "styles.css" (style))
    (write! output-dir "7ce12f75-f371-4e85-a3e9-b7749a65f140.html" (toc m))
    (doseq [ch (chapters m)]
      (write! output-dir (rel-url ch) (chapter ch)))))