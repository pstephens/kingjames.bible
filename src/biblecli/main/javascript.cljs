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

(ns biblecli.main.javascript
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [chan put! <!]]))

(def node-fs (js/require "fs"))
(def node-path (js/require "path"))
(def uglify (js/require "uglify-js"))
(def modernizr (js/require "modernizr"))

(defn google-analytics-script []
  "//Google Analytics
(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)})(window,document,'script','//www.google-analytics.com/analytics.js','ga');
ga('create', 'UA-75078401-1', 'auto');
ga('send', 'pageview');")

(defn modernizr-script []
  (let [ch (chan)
        cb (fn [res]
             (put! ch [nil res]))
        opts #js {:minify false
                  :options #js ["setClasses"]
                  :feature-detects #js
                              ["test/svg/asimg"
                               "test/css/flexbox"]}]
    (.build modernizr opts cb)
    ch))

(defn collect-scripts [channels]
  (go-loop [chs channels
            acc []]
           (if-let [ch (first chs)]
             (let [[err res] (<! ch)]
               (if err
                 [err nil]
                 (recur (rest chs) (conj acc res))))
             [nil acc])))

(defn do-minify [scripts]
  (let [opts #js {:warnings false
                  :fromString true
                  :compress #js {}}
        strs (clj->js scripts)
        result (js->clj (.minify uglify strs opts) :keywordize-keys true)]
    (:code result)))

(defn minify-scripts [channels]
  (go
    (let [[err scripts] (<! (collect-scripts channels))]
      (if err
        [err nil]
        [nil (do-minify scripts)]))))

(defn load-script [path]
  (let [ch (chan)
        cb (fn [err data]
             (put! ch [err data]))]
    (.readFile node-fs path "utf8" cb)
    ch))

(defn provide-script [script]
  (let [ch (chan)]
    (put! ch [nil script])
    ch))

(defn default-scripts [content-dir]
  (minify-scripts
    (list
      (modernizr-script)
      (provide-script (google-analytics-script))
      (load-script (.join node-path content-dir "js/domready.js"))
      (load-script (.join node-path content-dir "js/kj.js")))))
