;;;;   Copyright 2017 Peter Stephens. All Rights Reserved.
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

(ns biblecli.commands.staticpage.chapter
  (:require [biblecli.main.html :as h]
            [biblecli.commands.staticpage.common :as f]
            [clojure.string :as s]
            [common.bible.model :as model]))

(defn chapter-url [ch img alt rel]
  (let [data-png (s/replace img ".svg" ".png")]
    (if ch
      (h/img-button (f/chapter-url ch) img alt {:rel rel :data-png data-png})
      (h/img-button "" img alt {:data-png data-png}))))

(defn menu-home []
  [:li (h/img-button "." "home.svg" "Home" {:data-png "home.png"})])

(defn menu-arrows [prev-ch next-ch]
  (list
    [:li (chapter-url prev-ch "left-arrow.svg" "Previous Chapter" "prev")]
    [:li (chapter-url next-ch "right-arrow.svg" "Next Chapter" "next")]))

(defn menu-chapter [book-id ch]
  (list
    [:li.book (h/text-button "." (f/book-name book-id))]
    (if (> (::model/chapterCount ch) 1)
      [:li.chap (h/text-button (str ".#" (f/book-elem-id book-id)) (::model/chapterNum ch))])))


(def ^:private beginbracket [:beginbracket "["])
(def ^:private endbracket [:endbracket "]"])
(def ^:private whitespace [:whitespace " "])

(defn next-chapter [all-chapters i]
  (get all-chapters (inc i)))

(defn prev-chapter [all-chapters i]
  (get all-chapters (dec i)))

(defn subtitle? [i {subtitle ::model/subtitle}]
  (and subtitle (= i 0)))

(defn postscript? [i {postscript ::model/postscript verses ::model/verses}]
  (and postscript (= i (dec (count verses)))))

(defn tokens-to-markup
  ([tokens acc]
   (let [tokens (seq tokens)]
     (if-not tokens
       [nil (reverse acc)]
       (let [[tok-type tok-val] (first tokens)
             remaining (rest tokens)]
         (case tok-type
           :whitespace (recur remaining (conj acc " "))
           :uppercase (recur remaining (conj acc tok-val))
           :mixed (recur remaining (conj acc tok-val))
           :punctuation (recur remaining (conj acc tok-val))
           :beginbracket
           (let [[remaining part] (tokens-to-markup remaining '())]
             (recur remaining (conj acc [:i part])))
           :endbracket
           [remaining (reverse acc)]
           (throw (js/Error. "Invalid token type.")))))))
  ([tokens]
   (let [[_ acc] (tokens-to-markup tokens '())]
     acc)))

(defn verse-num [i {chapter-has-subtitle ::model/subtitle :as ch}]
  (cond
    (postscript? i ch) nil
    (subtitle? i ch) nil
    :else
    (let [delta (if chapter-has-subtitle 0 1)
          verse-num (+ i delta)]
      verse-num)))

(defn verse-class [i ch]
  (cond
    (subtitle? i ch) "intro"
    (postscript? i ch) "ps"
    :else "verse"))

(defn tokenize [content]
  (->>
    (re-seq #"\s+|[,\[\].!?;:,'()-]|[A-Za-z]+" content)
    (map
      #(cond
         (re-matches #"\s+" %) whitespace
         (re-matches #"^[A-Z]+$" %) [:uppercase %]
         (re-matches #"^[A-Za-z]+$" %) [:mixed %]
         (re-matches #"\[" %) beginbracket
         (re-matches #"\]" %) endbracket
         (re-matches #"[,.!?;:,'(\)-]" %) [:punctuation %]
         :else (throw (js/Error. "Invalid token."))))))



(defn number-markup [i ch]
  (if-let [num (verse-num i ch)]
    (list [:a {:href (str (f/chapter-url ch) "#" num)} num] " ")
    ""))

(defn verse-id [i ch]
  (if-let [num (verse-num i ch)]
    {:id (str "_" num)}
    {}))


(defn verse [i ch v]
  (let [tokens (tokenize v)]
    [:p
     (merge {:class (verse-class i ch)} (verse-id i ch))
     (number-markup i ch)
     (tokens-to-markup tokens)]))

(defn chapter-list [m]
  (->> m
       (mapcat
         (fn [book]
           (map
             (fn [chapter]
               (merge chapter
                      {::model/chapterCount (count (::model/chapters book))}))
             (::model/chapters book))))
       (vec)))

(defn page-content
  [{bookId  ::model/bookId
    verses  ::model/verses
    :as     ch}
   prev-ch
   next-ch
   baseurl
   canonical
   default-script]
  (h/html {:hilighter      {:centeractive true}
           :title          (f/chapter-name ch)
           :desc           (f/chapter-name ch)
           :canonical      canonical
           :relurl         (f/chapter-url ch)
           :default-script default-script}
          [:div.content.verses
           (h/menu
             [:div.vert
              [:ul.btncontainer (menu-home) (menu-arrows prev-ch next-ch)]
              [:ul.btncontainer.bookref (menu-chapter bookId ch)]]
             [:div.horz
              [:ul.btncontainer.home (menu-home)]
              [:ul.btncontainer (menu-chapter bookId ch)]
              [:ul.btncontainer (menu-arrows prev-ch next-ch)]])
           [:h1.chap
            (f/chapter-name ch)]
           (map-indexed #(verse %1 ch %2) verses)
           [:div.about [:a {:href "https://github.com/pstephens/kingjames.bible/blob/master/README.md"} "About " baseurl]]]))
