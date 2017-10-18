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

(ns biblecli.commands.staticpage.book
  (:require [biblecli.main.html :as h]
            [biblecli.commands.staticpage.common :as f]
            [common.bible.model :as model]))

(defn book-list [m]
  (->> m
       (map
         (fn [book]
           {::model/bookId       (::model/bookId book)
            ::model/chapters     (::model/chapters book)
            ::model/chapterCount (count (::model/chapters book))}))
       (vec)))

(defn page-content [{bookId       ::model/bookId
                     chapters     ::model/chapters
                     chapterCount ::model/chapterCount
                     :as b}
                    canonical
                    default-script]
  (let [bookName (f/book-name bookId)
        bookUrl (f/book-url bookId)]
    (h/html {:title          bookName
             :desc           bookName
             :canonical      canonical
             :relurl         bookUrl
             :default-script default-script}
            [:div.content.toc
             [:h1 "The King James Bible"]
             [:div.book
              [:div.back
               [:h2 (f/book-name-nbsp bookId)]
               [:a {:href "."} "Table of Contents"]]
              [:ul.chapters.btncontainer
               (->> chapters
                    (map (fn [ch]
                           [:li [:a {:href (f/chapter-url bookId (::model/chapterNum ch) chapterCount)} (::model/chapterNum ch)]])))]]])))
