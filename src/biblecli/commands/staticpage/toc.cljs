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

(ns biblecli.commands.staticpage.toc
  (:require [biblecli.commands.staticpage.common :as f]
            [biblecli.main.html :as h]
            [common.bible.model :as model]))

(defn toc-book [{bookId ::model/bookId}]
  [:li [:a {:href (f/book-url bookId)} (f/book-name-nbsp bookId)]])

(defn page-content [m canonical default-script]
  (h/html {:hilighter      {:scrolltop true}
           :title          nil
           :desc           "Table of Contents"
           :canonical      canonical
           :relurl         ""
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
                  (map toc-book))]

            [:h2 "Miscellany"]
            [:ul.btncontainer.misc
             [:li (h/text-button "About" "About")]
             [:li (h/text-button "Help" "Help")]]]]))
