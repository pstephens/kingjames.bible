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

; TODO: implement as part of book chapter index
(defn page-content [book]
  (let [chapters (::model/chapters book)
        chapcount (count chapters)
        id (::model/bookId book)]
    (h/html {}
            [:div.content.toc
             [:h1 "The King James Bible"]
             [:div.book
              [:div.back
               [:h2 (f/book-name-nbsp id)]
               [:a {:href "."} "Table of Contents"]]
              [:ul.chapters.btncontainer
               (->> chapters
                    (map (fn [ch]
                           [:li [:a {:href (f/chapter-url (::model/bookId book) (::model/chapterNum ch) chapcount)} (::model/chapterNum ch)]])))]]])))
