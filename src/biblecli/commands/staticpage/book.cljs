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

(defn next-book [all-books i]
  (::model/bookId (get all-books (inc i))))

(defn prev-book [all-books i]
  (::model/bookId (get all-books (dec i))))

(defn page-content [{bookId       ::model/bookId
                     chapters     ::model/chapters
                     chapterCount ::model/chapterCount}
                    prev-book
                    next-book
                    canonical
                    default-script]
  (let [bookName (f/book-name bookId)
        bookUrl (f/book-url bookId)]
    (h/html {:title          bookName
             :desc           bookName
             :canonical      canonical
             :relurl         bookUrl
             :default-script default-script}
            [:div.content.chapters
             (h/menu
               [:div.vert
                [:ul.btncontainer (f/menu-home) (f/menu-book-arrows prev-book next-book)]
                [:ul.btncontainer.bookref (f/menu-book bookId)]]
               [:div.horz
                [:ul.btncontainer.home (f/menu-home)]
                [:ul.btncontainer (f/menu-book bookId)]
                [:ul.btncontainer (f/menu-book-arrows prev-book next-book)]])
             [:h1.book (f/book-name-nbsp bookId)]

             [:ul.chapters.btncontainer
              (->> chapters
                   (map (fn [ch]
                          [:li [:a {:href (f/chapter-url bookId (::model/chapterNum ch) chapterCount)} (::model/chapterNum ch)]])))]])))
