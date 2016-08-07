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

(ns biblecli.commands.markdown
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
    [biblecli.main.html :as h]
    [cljs.core.async :refer [chan put! <!]]
    [cljs.nodejs :refer [require]]))

(def ^:private marked
  (let [marked (require "marked")]
    (.setOptions marked #js {})
    marked))

(def ^:private fs (require "fs"))
(def ^:private path (require "path"))

(defn ^:private readdir [dir]
  (let [chan (chan)
        cb (fn [err data]
             (put! chan [err data]))]
    (.readdir fs dir cb)
    chan))

(defn ^:private readfile [input-path]
  (let [chan (chan)
        cb (fn [err data]
             (put! chan [err data]))]
    (.readFile fs input-path "utf8" cb)
    chan))

(defn ^:private writefile [output-path data]
  (let [chan (chan)
        cb (fn [err data]
             (put! chan [err data]))]
    (.writeFile fs output-path data "utf8" cb)
    chan))

(defn ^:private markdown-to-html [input-path output-dir opts]
  (go
    (let [basename (.basename path input-path)
          extname (.extname path basename)
          name (.basename path basename extname)
          output-path (.join path output-dir name)
          [err markdown] (<! (readfile input-path))]
      (if err
        [err nil]
        (let [markup (marked markdown)
              [err _] (<! (writefile output-path markup))]
          (if err
            [err nil]
            [nil true]))))))

(defn markdown
  {:summary      "Convert markdown files to templated HTML."
   :doc          "usage: biblecli markdown [-canonical <url>] [-baseurl <url>] <input-path> <output-path>
   --canonical <url>      The canonical url. Defaults to https://kingjames.bible.
   --baseurl <url>        The base url. Defaults to https://beta.kingjames.bible.
   <input-path>           Input directory to find markdown files. Files much match the pattern *.md.
   <output-path>          Output directory to place the resource files."
   :async true
   :cmdline-opts {:string  ["input" "output" "baseurl" "canonical"]
                  :default {:baseurl   "https://beta.kingjames.bible"
                            :canonical "https://kingjames.bible"}}}
  [{[input-dir output-dir] :_ baseurl :baseurl canonical :canonical}]
  (go
    (let [[err all-files] (<! (readdir input-dir))
          opts {}]
      (if err
        [err nil]
        (let [tasks (->>
                      all-files
                      (filter #(re-find #"\.(md)|(markdown)$" %))
                      (map #(markdown-to-html
                             (.join path input-dir %)
                             output-dir
                             opts))
                      (into ()))]
          (loop [[h & t] tasks]
            (if h
              (let [[err _] (<! h)]
                (if err
                  [err nil]
                  (recur t)))
              [nil true])))))))