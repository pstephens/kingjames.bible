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
    [clojure.string :as string]
    [biblecli.commands.staticpage.common :as f]))

(def ^:private marked
  (let [marked (js/require "marked")]
    (.setOptions marked #js {})
    marked))

(def ^:private fs (js/require "fs"))
(def ^:private path (js/require "path"))

(defn ^:private readdir [dir]
  (let [chan (chan)
        cb (fn [err data]
             (put! chan [err data]))]
    (.readdir fs dir cb)
    chan))

(defn ^:private parse-markdown-props [data]
  (let [lines (string/split-lines data)]
    (loop [acc {}
           i 0]
      (let [line (lines i)]
        (if line
          (let [[_ k v] (re-find #"^:([^ ]+)\s+(.*)$" line)]
            (if k
              (recur (assoc acc (keyword k) v) (inc i))
              (let [[_ title] (re-find #"^#\s+(.*)$" line)
                    remaining (string/join "\r\n" (subvec lines i))]
                (if title
                  [remaining (assoc acc :title title)]
                  [remaining acc])))))))))

(defn ^:private writefile [output-path data]
  (let [chan (chan)
        cb (fn [err data]
             (put! chan [err data]))]
    (.writeFile fs output-path data "utf8" cb)
    chan))

(defn ^:private readfile [input-path]
  (let [chan (chan)
        cb (fn [err data]
             (put! chan [err data]))]
    (.readFile fs input-path "utf8" cb)
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
        (let [[markdown opts2] (parse-markdown-props markdown)
              opts (merge opts opts2)
              markup (marked markdown)
              markup (h/html (merge opts {:relurl name})
                             [:div.content
                              (h/menu
                                [:div.vert
                                 [:ul.btncontainer (f/menu-home)]
                                 [:ul.btncontainer.misc [:li (h/text-button "." "Read")]]
                                 [:ul.btncontainer.misc [:li (h/text-button "about" "About")]]
                                 [:ul.btncontainer.misc [:li (h/text-button "authority" "Authority")]]
                                 [:ul.btncontainer.misc [:li (h/text-button "salvation" "Salvation")]]
                                 [:ul.btncontainer.misc [:li (h/text-button "profit" "Profit")]]]
                                [:div.horz
                                 [:ul.btncontainer.home (f/menu-home)]
                                 [:ul.btncontainer.misc [:li (h/text-button "." "Read")]]
                                 [:ul.btncontainer.misc [:li (h/text-button "about" "About")]]
                                 [:ul.btncontainer.misc [:li (h/text-button "authority" "Authority")]]
                                 [:ul.btncontainer.misc [:li (h/text-button "salvation" "Salvation")]]
                                 [:ul.btncontainer.misc [:li (h/text-button "profit" "Profit")]]])
                              markup])
              [err _] (<! (writefile output-path markup))]
          (if err
            [err nil]
            [nil true]))))))

(defn markdown
  {:summary      "Convert markdown files to templated HTML."
   :doc          "usage: biblecli markdown [-canonical <url>] [-baseurl <url>] <content-dir> <output-dir>
   --canonical <url>      The canonical url. Defaults to https://kingjames.bible.
   --baseurl <url>        The base url. Defaults to https://beta.kingjames.bible.
   <content-dir>          Input directory for content. Files must match the pattern <contentdir>/*.md.
   <output-dir>           Output directory to place the HTML resource files."
   :async true
   :cmdline-opts {:string  ["baseurl" "canonical"]
                  :default {:baseurl   "https://beta.kingjames.bible"
                            :canonical "https://kingjames.bible"}}}
  [{[content-dir output-dir] :_ baseurl :baseurl canonical :canonical}]
  (go
    (let [ch1 (readdir content-dir)
          ch2 (readfile (.join path output-dir "script.min.js"))
          [err1 all-files] (<! ch1)
          [err2 default-script] (<! ch2)
          opts {:baseurl        baseurl
                :canonical      canonical
                :default-script (string/replace default-script #"[\s\S]//# sourceMappingURL.*$" "")}]
      (if-let [err (or err1 err2)]
        [err nil]
        (let [tasks (->>
                      all-files
                      (filter #(re-find #"\.(md)|(markdown)$" %))
                      (map #(markdown-to-html
                             (.join path content-dir %)
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
