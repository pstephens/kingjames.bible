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

(ns biblecli.commands.sitemap
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [biblecli.main.dir :refer [readdir-recursive]]
            [cljs.core.async :refer [<!]]))

(def ^:private path (js/require "path"))
(def ^:private fs (js/require "fs"))

(defn join-url [base rel]
  (if (.endsWith base "/")
    (str base rel)
    (str base "/" rel)))

(defn sitemap-line [rel-url freq priority baseurl]
  (str
    "<url><loc>"
    (join-url baseurl rel-url)
    "</loc><changefreq>"
    freq
    "</changefreq><priority>"
    priority
    "</priority></url>
"))

(defn ^:private html? [filename]
  (re-find #"^[^.]+$" filename))

(defn calc-sitemap [files baseurl]
  (apply str
         (flatten [
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">
"
                   (sitemap-line "" "daily" "0.8" baseurl)
                   (->>
                     (keys files)
                     (filter html?)
                     (sort)
                     (map #(sitemap-line % "monthly" "1.0" baseurl)))
                   "</urlset>"])))

(defn write! [filepath content]
  (let [buff (.from js/Buffer content "utf8")]
    (.writeFileSync fs filepath buff)))

(defn sitemap
  {:summary "Generates the sitemap for a directory and all sub-directories."
   :doc "usage: biblecli sitemap [--output <output-path>] [--baseurl <url>] <input-path>
   <input-path>           Input directory to scan recursively.
   --output <output-path> Output path to save the sitemap file to. Defaults to 'sitemap.xml' relative to the input directory.
   --baseurl <url>        The base url. Defaults to https://beta.kingjames.bible."
   :async true
   :cmdline-opts {:string ["output" "baseurl"]
                  :default {:output nil
                            :baseurl "https://beta.kingjames.bible"}}}
  [{output-path :output input-dir :_ baseurl :baseurl}]
  (go
    (if (not= (count input-dir) 1)
      ["Must have exactly one <input-path> parameter." nil]
      (let [input-dir (first input-dir)
            output-path (or output-path (.join path input-dir "sitemap.xml"))
            [err files] (<! (readdir-recursive input-dir))]
        (if err
          [err nil]
          (do
            (write! output-path (calc-sitemap files baseurl))
            [nil true]))))))
