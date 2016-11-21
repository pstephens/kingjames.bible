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

(ns biblecli.main.html
  (:require-macros [hiccups.core :as hiccups])
  (:require [cljs.core.async :refer [chan put!]]
            [cljs.nodejs :refer [require]]
            [hiccups.runtime]))

(defn join-url [base rel]
  (if (.endsWith base "/")
    (str base rel)
    (str base "/" rel)))

(defn google-analytics-script []
  "//Google Analytics
(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)})(window,document,'script','//www.google-analytics.com/analytics.js','ga');
ga('create', 'UA-75078401-1', 'auto');
ga('send', 'pageview');")

(defn modernizr-script []
  (let [m (require "modernizr")
        ch (chan)
        cb (fn [res]
             (put! ch [nil res]))
        opts #js {:minify true
                  :options #js ["setClasses"]
                  :feature-detects #js ["test/svg/asimg"]}]
    (.build m opts cb)
    ch))

(defn html [{title     :title
             desc      :desc
             canonical :canonical
             relurl    :relurl
             :as       opts}
            inner]
  (str
    "<!DOCTYPE html>"
    (hiccups/html
      [:html {:lang "en"}
       [:head
        [:title (str title (if title " - ") "The King James Bible")]
        [:meta {:name "description" :content (str desc " - The King James Bible, the Holy Bible in English")}]
        [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
        [:link {:rel "stylesheet" :type "text/css" :href "styles.css"}]
        [:link {:rel "canonical" :href (join-url canonical relurl)}]]
       [:body
        inner
        [:script {:type "text/javascript" :src "hiliter.js"}]
        [:script {:type "text/javascript"} (google-analytics-script)]
        (if-let [modernizr (:modernizr opts)]
          [:script {:type "text/javascript"} modernizr])
        (if-let [hilighter (:hilighter opts)]
          [:script {:type "text/javascript"}
           (str "document.kj=" (.stringify js/JSON (clj->js hilighter)))])]])))

(defn menu [& inner]
 [:div.menu
  [:div.menu2
   inner]])

(defn link-label [url label]
  [:a {:href url :class "tbtn"} label])

(defn link-img
  ([url src alt] (link-img url src alt {}))
  ([url src alt attributes]
  [:a (merge attributes
             {:href url
              :class "ibtn"})
   [:img {:src src
          :alt alt}]
   [:span]]))