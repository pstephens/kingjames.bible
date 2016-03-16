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
"body {
  margin: 0;
  padding: 0;
}
a:link, a:visited {
  text-decoration: none;
  color: #33f;
}
a:hover, a:active {
  text-decoration: underline;
  color: #77f;
}
.content {
  font-size: 125%;
  padding: 25px;
  background-color: #FFFFFF;
}
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
  padding: 0.18em 0.25em;
  margin: 0;
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
    padding: 0.18em 0.25em 0.18em 2.25em;
    margin-bottom: 0.25em;
    margin-top: 0;
    text-align: left;
}
.hilite {
  background-color: #FFFF99;
}
.about {
  font-size: 60%;
  font-style: italic;
  text-align: center;
  margin: 0.5em 0 -0.25em 0;
}

@media (min-width: 1100px) {
  .menu {
    position: fixed;
  }
  .menu2 {
    white-space: nowrap;
    font-family: Arial, sans-serif;
    font-size: 80%;
    position: absolute;
    right: 25px;
    background-color: #FFFFFF;
    border-left: 1px solid #BBB;
    border-bottom: 1px solid #BBB;
    border-top: 1px solid #BBB;
    padding: 6px;
    border-bottom-left-radius: 6px;
    border-top-left-radius: 6px;
  }
}

@media (min-width: 680px) {
  body {
    background-color: #DDD;
    overflow-y: scroll;
  }
  .content {
    width: 600px;
    margin: 25px auto 15px auto;
    border: 1px solid #BBB;
    -webkit-box-shadow: 0px 0px 23px 3px rgba(135,135,135,0.77);
    -moz-box-shadow: 0px 0px 23px 3px rgba(135,135,135,0.77);
    box-shadow: 0px 0px 23px 3px rgba(135,135,135,0.77);
  }
}

@media (min-width: 680px) and (max-width: 1100px) {
  .menu {
    position: fixed;
    top: 0;
  }
  .menu2 {
    white-space: nowrap;
    font-family: Arial, sans-serif;
    font-size: 80%;
    background-color: #FFFFFF;
    border: 1px solid #BBB;
    text-align: right;
    padding: 6px 25px;
    position: absolute;
    left: -26px;
    width: 600px;
  }
}

@media (max-width: 680px) {
  .content {
    min-width: 280px;
    margin: 25px auto 15px auto;
  }
  .menu {
    position: fixed;
    width: 100%;
    right: 0;
    top: 0;
  }
  .menu2 {
    white-space: nowrap;
    font-family: Arial, sans-serif;
    font-size: 80%;
    background-color: #FFFFFF;
    border-bottom: 1px solid #BBB;
    text-align: right;
    padding: 6px 25px;
    position: absolute;
    right: 0;
    width: 100%;
  }
}
")

(defn js []
"// NOTE: Experimental
document.lastElem = null;

function RemoveHilite(el) {
    if(el) {
        var t = el.className;
        t = t.replace('hilite', '').trim();
        el.className = t;
    }
}

function AddHilite(el) {
    if(el) {
        var t = el.className;
        t = t.replace('hilite', '').trim();
        t += ' hilite';
        el.className = t;
    }
}

function SetHilight()
{
    var el;
    if(document.lastElem) {
        el = document.getElementById(document.lastElem);
        RemoveHilite(el);
    }

    var id = window.location.hash.substr(1);
    el = document.getElementById(id);
    if(el) {
        document.lastElem = id;
        AddHilite(el);
    }
}

document.addEventListener('DOMContentLoaded', function(e) {
    SetHilight();
    window.addEventListener('hashchange', function(e) {
        SetHilight();
    });
});

// Google Analytics
(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
})(window,document,'script','//www.google-analytics.com/analytics.js','ga');

ga('create', 'UA-75078401-1', 'auto');
ga('send', 'pageview');")

(defn robots [] "Sitemap: https://everlastingbible.com/sitemap.xml")

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

(defn book-elem-id [book-id]
  (s/replace (book-name book-id) " " "-"))

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
  [:p.tocp {:id (book-elem-id (:id b))}
    [:span.chapter (book-name (:id b))]
    " "
    (->> (:chapters b)
      (map (fn [ch]
        (list [:a {:href (rel-url (:id b) (:num ch) (count (:chapters b)))} (:num ch)] " "))))])

(defn toc [m]
  (str "<!DOCTYPE html>"
    (html
      [:html {:lang "en"}
        [:head
          [:title "The King James Bible"]
          [:meta {:name "description" :content "The King James Bible, the Holy Bible in English - Table of Contents"}]
          [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
          [:link {:rel "stylesheet" :type "text/css" :href "styles.css"}]]
        [:body
          [:div.content
            [:h1 "The King James Bible"]

            [:h2 "The Old Testament"]
            (->> m
              (take 39)
              (map toc-book))

            [:h2 "The New Testament"]
            (->> m
              (drop 39)
              (map toc-book))

            [:div.about [:a {:href "https://github.com/pstephens/EverlastingBible/blob/master/README.md"} "About EverlastingBible.com"]]]
          [:script {:type "text/javascript" :src "hiliter.js"}]]])))

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
          (:chapters b))))
    (vec)))

(def ^:private beginbracket [:beginbracket "["])
(def ^:private endbracket [:endbracket "]"])
(def ^:private whitespace [:whitespace " "])

(defn subtitle? [i {subtitle :subtitle}]
  (and subtitle (= i 0)))

(defn postscript? [i {postscript :postscript verses :verses}]
  (and postscript (= i (dec (count verses)))))

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

(defn verse-num [i {chapter-has-subtitle :subtitle :as ch}]
  (cond
    (postscript? i ch) nil
    (subtitle? i ch) nil
    :else
    (let [delta (if chapter-has-subtitle 0 1)
          verse-num (+ i delta)]
      verse-num)))

(defn number-markup [i ch]
  (if-let [num (verse-num i ch)]
    (list [:a {:href (str (rel-url ch) "#" num)} num] " ")
    ""))

(defn verse-id [i ch]
  (if-let [num (verse-num i ch)]
    {:id num}
    {}))

(defn verse [i ch v]
  (let [tokens (tokenize v)]
    [:p
      (merge {:class (verse-class i ch)} (verse-id i ch))
      (number-markup i ch)
      (tokens-to-markup tokens)]))

(defn chapter-url [ch inner]
  (if ch
    [:a {:href (rel-url ch)} inner]
    inner))

(defn chapter
  [{book-id :book-id
    chap-num :num
    chap-cnt :chap-cnt
    verses :verses
    :as ch}
    prev-ch
    next-ch]
  (str
    "<!DOCTYPE html>"
    (html
      [:html {:lang "en"}
        [:head
          [:title (str (chapter-name ch) " - The King James Bible")]
          [:meta {:name "description" :content (str "The King James Bible, the Holy Bible in English - " (chapter-name ch))}]
          [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
          [:link {:rel "stylesheet" :type "text/css" :href "styles.css"}]]
        [:body
          [:div.content
            [:div.menu
              [:div.menu2
                [:a {:href (str ".#" (book-elem-id book-id))} (chapter-name ch)]
                "&nbsp; "
                (chapter-url prev-ch "&lt;&lt;")
                "&nbsp; "
                (chapter-url next-ch "&gt;&gt;")]]
            [:h1.chap
              (chapter-name ch)]
            (map-indexed #(verse %1 ch %2) verses)
            [:div.about [:a {:href "https://github.com/pstephens/EverlastingBible/blob/master/README.md"} "About EverlastingBible.com"]]
            [:script {:type "text/javascript" :src "hiliter.js"}]]]])))

(defn next-chapter [all-chapters i]
  (get all-chapters (inc i)))

(defn prev-chapter [all-chapters i]
  (get all-chapters (dec i)))

(defn sitemap-line [rel-url freq priority]
  (str
    "<url><loc>https://everlastingbible.com/"
    rel-url
    "</loc><changefreq>"
    freq
    "</changefreq><priority>"
    priority
    "</priority></url>
"))

(defn sitemap [chapters]
  (apply str
    (flatten [
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">
"
(sitemap-line "" "daily" "0.8")
(map #(sitemap-line (rel-url %) "monthly" "1.0") chapters)
"</urlset>"])))

(defn write! [dir filename content]
  (let [filepath (.join node-path dir filename)
        buff (js/Buffer content "utf8")]
    (.writeFileSync node-fs filepath buff)))

(defn prepare! [parser src output-dir]
  (let [m (parse parser src)
        all-chapters (chapters m)]
    (write! output-dir "styles.css" (style))
    (write! output-dir "hiliter.js" (js))
    (write! output-dir "robots.txt" (robots))
    (write! output-dir "sitemap.xml" (sitemap all-chapters))
    (write! output-dir "7ce12f75-f371-4e85-a3e9-b7749a65f140.html" (toc m))
    (dorun
      (map-indexed
        #(write!
          output-dir
          (rel-url %2)
          (chapter %2
            (prev-chapter all-chapters %1)
            (next-chapter all-chapters %1)))
        all-chapters))))