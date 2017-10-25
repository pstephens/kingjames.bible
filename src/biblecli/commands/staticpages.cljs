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
    [biblecli.commands.staticpage.book :as book]
    [biblecli.commands.staticpage.chapter :as chapter]
    [biblecli.commands.staticpage.common :as f]
    [biblecli.commands.staticpage.robots :as robots]
    [biblecli.commands.staticpage.toc :as toc]
    [biblecli.main.utility :as u]
    [cljs.core.async :refer [chan put! <!]]
    [clojure.string :as s]
    [common.bible.model :as model]
    [common.normalizer.core :refer [parse]]))

(def node-fs (js/require "fs"))
(def node-path (js/require "path"))

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
          all-chapters (chapter/chapter-list m)
          all-books (book/book-list m)
          [err default-script] (<! (readfile (.join node-path output-dir "script.min.js")))]
      (if err
        [err nil]
        (do
          (let [default-script (s/replace default-script #"[\s\S]//# sourceMappingURL.*$" "")]
            (write! output-dir "robots.txt" (robots/page-content baseurl allowrobots))
            (write! output-dir "7ce12f75-f371-4e85-a3e9-b7749a65f140.html" (toc/page-content m canonical default-script))

            (dorun
              (->>
                all-books
                (map-indexed (fn [index book] {:index index :book book}))
                (filter #(> (get-in % [:book ::model/chapterCount]) 1))
                (map
                  (fn [{book :book index :index}]
                    (write!
                      output-dir
                      (f/book-url (::model/bookId book))
                      (book/page-content
                        book
                        (book/prev-book all-books index)
                        (book/next-book all-books index)
                        canonical
                        default-script))))))

            (dorun
              (->>
                all-chapters
                (map-indexed
                  ; might gain some perf by doing async writes
                  #(write!
                     output-dir
                     (f/chapter-url %2)
                     (chapter/page-content
                       %2
                       (chapter/prev-chapter all-chapters %1)
                       (chapter/next-chapter all-chapters %1)
                       canonical
                       default-script))))
            [nil nil])))))))
