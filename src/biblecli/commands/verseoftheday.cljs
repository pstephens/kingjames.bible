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
  (:require-macros [hiccups.core :refer [html]])
  (:require
    [clojure.string :as s]
    [common.normalizer.core :refer [parse]]
    [hiccups.runtime]
    [biblecli.main.utility :as u]))

(def node-fs (js/require "fs"))
(def node-path (js/require "path"))

(def base-url "https://kingjames.bible/")

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
        parts (flatten ["[\""
                        book-chapter-str
                        "\""
                        (map (fn [[_ _ verse-idx]] (str ",\"" (inc verse-idx) " " (get-in chapter [:verses (+ verse-idx delta)]) "\"")) verses)
                        "]"])]
    (apply str parts)))

(defn expand-verses [list m]
  (->>
    list
    (map map-verse-ref)
    (map #(to-json % m))))

(defn format-verses [list m]
  (str "[\r\n  " (s/join ",\r\n  " (expand-verses list m)) "\r\n];"))

(defn votd-js [verse-json base-url]
  (str
"(function(window, document) {
    var votdVar = window['VotdObject'] || 'votd';
    var votd = window[votdVar] = window[votdVar] || {};
    votd.verses = "
verse-json

"

    // A Javascript implementaion of Richard Brent's Xorgens xor4096 algorithm.
    // http://arxiv.org/pdf/1004.3115v1.pdf
    // https://github.com/davidbau/seedrandom/blob/released/lib/xor4096.js
    !function(a,b,c){function d(a){function b(a,b){var c,d,e,f,g,h=[],i=128;for(b===(0|b)?(d=b,b=null):(b+=\"\\x00\",d=0,i=Math.max(i,b.length)),e=0,f=-32;i>f;++f)b&&(d^=b.charCodeAt((f+32)%b.length)),0===f&&(g=d),d^=d<<10,d^=d>>>15,d^=d<<4,d^=d>>>13,f>=0&&(g=g+1640531527|0,c=h[127&f]^=d+g,e=0==c?e+1:0);for(e>=128&&(h[127&(b&&b.length||0)]=-1),e=127,f=512;f>0;--f)d=h[e+34&127],c=h[e=e+1&127],d^=d<<13,c^=c<<17,d^=d>>>15,c^=c>>>12,h[e]=d^c;a.w=g,a.X=h,a.i=e}var c=this;c.next=function(){var a,b,d=c.w,e=c.X,f=c.i;return c.w=d=d+1640531527|0,b=e[f+34&127],a=e[f=f+1&127],b^=b<<13,a^=a<<17,b^=b>>>15,a^=a>>>12,b=e[f]=b^a,c.i=f,b+(d^d>>>16)|0},b(c,a)}function e(a,b){return b.i=a.i,b.w=a.w,b.X=a.X.slice(),b}function f(a,b){null==a&&(a=+new Date);var c=new d(a),f=b&&b.state,g=function(){return(c.next()>>>0)/4294967296};return g[\"double\"]=function(){do var a=c.next()>>>11,b=(c.next()>>>0)/4294967296,d=(a+b)/(1<<21);while(0===d);return d},g.int32=c.next,g.quick=g,f&&(f.X&&e(f,c),g.state=function(){return e(c,{})}),g}b&&b.exports?b.exports=f:c&&c.amd?c(function(){return f}):a.xor4096=f}(votd,\"object\"==typeof module&&module,\"function\"==typeof define&&define);

    function hashedIndex(s, maxEx) {
        var prng = votd.xor4096(s);
        return Math.floor(prng.double() * maxEx);
    }

    function calcUrl(baseUrl, chap) {
        return baseUrl + chap.replace(/\\s/g, \"-\");
    }

    votd.getVerseFromDateAndHostname = function getVerseFromDateAndHostname(dt, hostname, cnt) {
        var dayNum = dt.getFullYear() * 10000 + dt.getMonth() * 100 + dt.getDate()
        return hashedIndex(hostname + dayNum, cnt);
    };

    votd.renderVerses = function renderVerses(baseUrl, verses) {
        var b = \"\",
            url = calcUrl(baseUrl, verses[0]),
            i, num, matches, txt;

        for(i = 1; i < verses.length; ++i) {
            matches = verses[i].match(/^(\\d+) (.*)$/);
            num = parseInt(matches[1]);
            txt = matches[2];
            b += '<p><a href=\"' + url + '#' + num + '\">';
            if(i == 1) {
                b += verses[0] + ':';
            }
            b += num + '</a> ';
            b += txt.replace(/\\[/g, \"<i>\").replace(/\\]/g, \"</i>\");
            b += '</p>';
        }

        return b;
    };

    votd.renderVersesToElement = function renderVersesToElement(id, allVerses, dt, baseUrl, hostname) {
        var el, i, v, html;

        if(!id) {
            return;
        }

        el = document.getElementById(id);
        if(!el) {
            return;
        }

        i = votd.getVerseFromDateAndHostname(dt, hostname, allVerses.length);
        v = allVerses[i];
        html = votd.renderVerses(baseUrl, v);

        el.innerHTML = html;
    }

    votd.renderVersesToElement(
        votd.i,
        votd.verses,
        new Date(),
        \""
base-url
"\",
        (document.location || {}).hostname || \"\");

})(window, document);"))

(defn client-html [base-url]
  (str "<!DOCTYPE html>"
    (html
      [:html {:lang "en"}
        [:body {:style "background-color: white;"}
          [:div#votd-el.votd]
          [:script
            (str "(function(w,d,t,u,v,i,n,l){w['VotdObject']=v;w[v]=w[v]||{};w[v].i=i;n=d.createElement(t),l=d.getElementsByTagName(t)[0];n.async=1;n.src=u;l.parentNode.insertBefore(n,l)})(window,document,'script','"
              base-url "votd/votd.js','votd','votd-el');")
          ]]])))

(defn write! [dir filename content]
  (let [filepath (.join node-path dir filename)
        buff (js/Buffer. content "utf8")]
    (.writeFileSync node-fs filepath buff)))

(defn readlines [filepath]
  (.readFileSync node-fs filepath (js-obj "encoding" "utf8")))

(defn verseoftheday
  {:summary "Generate the 'Verse of the Day' embeddable JavaScript program given a list of verses. Includes a sample 'client.html'."
   :doc "usage: biblecli verseoftheday [--parser <parser>] [--input <input-path>] <verselist> <output-path>
   --parser <parser>      Parser. Defaults to '{{default-parser}}'.
   --input <input-path>   Input path. Defaults to '{{default-parser-input}}'.
   <verselist>            The verse list. Each line should follow the form '* https://kingjames.bible/Genesis-1#1'.
   <output-path>          Output directory to place the resource files."
   :cmdline-opts {:string ["parser" "input"]
                  :default {:parser nil
                            :input nil}}}
  [{parser :parser src :input paths :_}]
  (if (< (count paths) 2)
    (throw "The <verselist> and <output-path> parameters are required."))
  (let [parser (or parser (u/default-parser))
        src  (or src (u/default-parser-input))
        input-verse-list (paths 0)
        output-dir (paths 1)
        m (parse parser src)
        verse-refs (->> (readlines input-verse-list) (s/split-lines))
        verse-data (format-verses verse-refs m)]
    (write! output-dir "votd.js" (votd-js verse-data base-url))
    (write! output-dir "client.html" (client-html base-url))))
