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

(ns biblecli.commands.javascript
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
    [biblecli.main.javascript :as j]
    [cljs.core.async :refer [chan put! <!]]))

(def node-fs (js/require "fs"))
(def node-path (js/require "path"))

(defn write! [dir filename content]
  (let [filePath (.join node-path dir filename)
        buff (js/Buffer. content "utf8")
        ch (chan)
        cb (fn [err res]
             (put! ch [err res]))]
    (.writeFile node-fs filePath buff cb)
    ch))

(defn wait-for [tasks]
  (go-loop [tasks tasks]
           (if-let [task (first tasks)]
             (let [[err _] (<! task)]
               (if err
                 [err nil]
                 (recur (rest tasks))))
             [nil true])))

(defn javascript
  {:summary "Generate static JavaScript."
   :doc "usage: biblecli javascript <content-dir> <output-dir>
   <content-dir>          The path to the content directory.
   <output-path>          Output directory to place the resource files."
   :async true
   :cmdline-opts {}}
[{[content-dir output-dir] :_}]
  (go
    (let [[err {:keys [original minified sourcemap]}] (<! (j/default-scripts content-dir))]
      (if err
        [err nil]
        (<! (wait-for
              (list
                (write! output-dir "script.js" original)
                (write! output-dir "script.min.js" minified)
                (write! output-dir "script.min.js.map" sourcemap)
                )))))))
