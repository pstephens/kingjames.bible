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
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
    [biblecli.main.html :as h]
    [biblecli.main.utility :as u]
    [cljs.core.async :refer [chan put! <!]]
    [clojure.string :as s]
    [common.normalizer.core :refer [parse]]))

(def node-fs (js/require "fs"))
(def node-path (js/require "path"))

(defn robots [baseurl allowrobots]
  (if allowrobots
    (str "Sitemap: " (h/join-url baseurl "sitemap.xml"))
    (str "user-agent: *\nDisallow: /")))

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

(defn book-name-nbsp [book-id]
  (let [name (book-name book-id)]
    (s/replace name #"^(I|II|III)\s+" "$1&nbsp;")))

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
  (let [chapters (:chapters b)
        chapcount (count chapters)
        id (:id b)
        bookid (book-elem-id id)]
    [:li [:a {:href (if (> chapcount 1) (str "#" bookid) bookid)} (book-name-nbsp id)]]))

(defn toc-book-details [b]
  (let [chapters (:chapters b)
        chapcount (count chapters)
        id (:id b)
        bookid (book-elem-id id)]
    [:div.book {:id (str "_" bookid)}
      [:div.back
        [:h2 (book-name-nbsp id)]
        [:a {:href "."} "Table of Contents"]]
      [:ul.chapters.btncontainer
        (->> chapters
             (map (fn [ch]
                  [:li [:a {:href (rel-url (:id b) (:num ch) chapcount)} (:num ch)]])))]]))

(defn toc [m baseurl canonical default-script]
  (h/html {:hilighter {:scrolltop true}
           :title nil
           :desc "Table of Contents"
           :canonical canonical
           :relurl ""
           :default-script default-script}
          [:div.content.toc
           [:h1 "The King James Bible"]

           [:div#_main.main.active

            [:div#votd.votd]
            [:script "(function(w,d,t,u,v,i,n,l){w['VotdObject']=v;w[v]=w[v]||{};w[v].i=i;n=d.createElement(t),l=d.getElementsByTagName(t)[0];n.async=1;n.src=u;l.parentNode.insertBefore(n,l)})(window,document,'script','votd/votd.js','votd','votd');"]

            [:h2 "The Old Testament"]
            [:ul.books.btncontainer
             (->> m
                  (take 39)
                  (map toc-book))]

            [:h2 "The New Testament"]
            [:ul.books.btncontainer
             (->> m
                  (drop 39)
                  (map toc-book))]]

           (->> m
                (map toc-book-details))

           [:div.about [:a {:href "https://github.com/pstephens/kingjames.bible/blob/master/README.md"} "About " baseurl ]]]))

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
    {:id (str "_" num)}
    {}))

(defn verse [i ch v]
  (let [tokens (tokenize v)]
    [:p
      (merge {:class (verse-class i ch)} (verse-id i ch))
      (number-markup i ch)
      (tokens-to-markup tokens)]))

(defn chapter-url [ch img alt rel]
  (let [data-png (s/replace img ".svg" ".png")]
    (if ch
      (h/img-button (rel-url ch) img alt {:rel rel :data-png data-png})
      (h/img-button "" img alt {:data-png data-png}))))

(defn menu-home []
  [:li (h/img-button "." "home.svg" "Home" {:data-png "home.png"})])

(defn menu-arrows [prev-ch next-ch]
  (list
    [:li (chapter-url prev-ch "left-arrow.svg" "Previous Chapter" "prev")]
    [:li (chapter-url next-ch "right-arrow.svg" "Next Chapter" "next")]))

(defn menu-chapter [book-id ch]
  (list
    [:li.book (h/text-button "." (book-name book-id))]
    (if (> (:chap-cnt ch) 1)
      [:li.chap (h/text-button (str ".#" (book-elem-id book-id)) (:num ch))])))

(defn chapter
  [{book-id :book-id
    verses :verses
    :as ch}
   prev-ch
   next-ch
   baseurl
   canonical
   default-script]
  (h/html {:hilighter {:centeractive true}
           :title (chapter-name ch)
           :desc (chapter-name ch)
           :canonical canonical
           :relurl (rel-url ch)
           :default-script default-script}
          [:div.content.verses
           (h/menu
             [:div.vert
               [:ul.btncontainer (menu-home) (menu-arrows prev-ch next-ch)]
               [:ul.btncontainer.bookref (menu-chapter book-id ch)]]
             [:div.horz
              [:ul.btncontainer.home (menu-home)]
              [:ul.btncontainer (menu-chapter book-id ch)]
              [:ul.btncontainer (menu-arrows prev-ch next-ch)]])
           [:h1.chap
            (chapter-name ch)]
           (map-indexed #(verse %1 ch %2) verses)
           [:div.about [:a {:href "https://github.com/pstephens/kingjames.bible/blob/master/README.md"} "About " baseurl]]]))

(defn next-chapter [all-chapters i]
  (get all-chapters (inc i)))

(defn prev-chapter [all-chapters i]
  (get all-chapters (dec i)))

(defn write! [dir filename content]
  (let [filepath (.join node-path dir filename)
        buff (js/Buffer. content "utf8")]
    (.writeFileSync node-fs filepath buff)))

(defn readfile [input-path]
  (let [chan (chan)
        cb (fn [err data]
             (put! chan [err data]))]
    (.readFile node-fs input-path "utf8" cb)
    chan))

(defn static
  {:summary "Generate static HTML, CSS, JavaScript, and other resources for the books of the bible."
   :doc "usage: biblecli static [--parser <parser>] [--input <input-path>] [-canonical <url>] [-baseurl <url>] <content-dir> <output-dir>
   --parser <parser>      Parser. Defaults to '{{default-parser}}'.
   --input <input-path>   Input path. Defaults to '{{default-parser-input}}'.
   --canonical <url>      The canonical url. Defaults to https://kingjames.bible.
   --baseurl <url>        The base url. Defaults to https://beta.kingjames.bible.
   --allowrobots          Allow robots via robots.txt. By default robots are disallowed.
   <content-dir>          The path to the content directory.
   <output-dir>           Output directory to place the resource files."
   :async true
   :cmdline-opts {:boolean ["allowrobots"]
                  :string ["parser" "input" "baseurl" "canonical"]
                  :default {:parser nil
                            :input nil
                            :contentdir nil
                            :baseurl "https://beta.kingjames.bible"
                            :canonical "https://kingjames.bible"}}}
  [{[content-dir output-dir] :_ parser :parser input :input baseurl :baseurl canonical :canonical allowrobots :allowrobots}]
  (go
    (let [parser (or parser (u/default-parser))
          input  (or input (u/default-parser-input))
          m (parse parser input)
          all-chapters (chapters m)
          [err default-script] (<! (readfile (.join node-path output-dir "script.min.js")))]
      (if err
        [err nil]
        (do
          (let [default-script (s/replace default-script #"[\s\S]//# sourceMappingURL.*$" "")]
            (write! output-dir "robots.txt" (robots baseurl allowrobots))
            (write! output-dir "7ce12f75-f371-4e85-a3e9-b7749a65f140.html" (toc m baseurl canonical default-script))
            (dorun
              (map-indexed
                ; might gain some perf by doing async writes
                #(write!
                  output-dir
                  (rel-url %2)
                  (chapter
                    %2
                    (prev-chapter all-chapters %1)
                    (next-chapter all-chapters %1)
                    baseurl
                    canonical
                    default-script))
                all-chapters))
            [nil nil]))))))
