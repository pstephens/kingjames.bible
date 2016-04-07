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

(ns reader.asset
  (:require-macros [hiccups.core :as hiccups :refer [html]])
  (:require
    [goog.object :as obj]
    [hiccups.runtime :as hiccupsrt]))

(defn scripts []
  '(
    [:script {:type "text/javascript" :src "goog/base.js"}]
    [:script {:type "text/javascript" :src "debug_refs.js"}]
    [:script {:type "text/javascript"}
          "goog.require(\"reader.core\");"]
  ))

(defn page-template []
  (str "<!DOCTYPE html>"
    (html
      [:html {:lang "en"}
        [:head
          [:title "The King James Bible"]
          [:meta {:name "description" :content "The King James Bible, the Holy Bible in English"}]
          [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]]
        [:body
          [:div.app]
          (scripts)]])))

(defn html-resource [p content-fn]
  [p
    (delay
      (let [buff (js/Buffer (content-fn) "utf8")]
        {:path    p
         :content buff
         :headers {"Cache-Control"  "max-age=600, public"
                   "Content-Type"   "text/html"
                   "Content-Length" (str (obj/get buff "length"))
                   "Last-Modified"  (.toUTCString (js/Date.))}}))])

(defn resources []
  (->>
    [(html-resource "/" page-template)]
    (reduce conj {})))