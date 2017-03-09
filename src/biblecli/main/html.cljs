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
            [hiccups.runtime]))

(defn join-url [base rel]
  (if (.endsWith base "/")
    (str base rel)
    (str base "/" rel)))

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
        (let [default-script (:default-script opts)]
          [:script {:type "text/javascript"}
           default-script
           (map (fn[[k v]] (str "window.kj." (name k) "=" (.stringify js/JSON (clj->js v)) ";")) (:hilighter opts))
           "window.kj.bootstrap();"])]])))

(defn menu [& inner]
 [:div.menu
  [:div.menu2
   inner]])

(defn text-button [url label]
  [:a.tbtn {:href url} label])

(defn img-button
  ([url src alt] (img-button url src alt {}))
  ([url src alt attributes]
  [:a.ibtn (dissoc (merge attributes {:href url}) :data-png)
   [:img (merge
           {:src src
            :alt alt}
           (select-keys attributes [:data-png]))]
   [:span]]))
