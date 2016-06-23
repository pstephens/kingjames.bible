;;;;   Copyright 2015 Peter Stephens. All Rights Reserved.
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

(ns biblecli.commands.prepare
  (:require
    [biblecli.main.utility :as u]
    [common.bible.resource :as res]
    [common.normalizer.core :refer [parse]]))

(def node-fs (js/require "fs"))
(def node-path (js/require "path"))

(defn write-data! [dir encoded-data]
  (let [filename (subs (encoded-data :path) 1)
        filepath (.join node-path dir filename)]
    (.writeFileSync node-fs filepath (encoded-data :content))))

(defn write-metadata! [dir buffer]
  (let [filepath (.join node-path dir "bible-meta.json")]
    (.writeFileSync node-fs filepath buffer)))

(defn prepare
  {:summary "Write bible content in web browser ready chunks of json/transit data."
   :doc "usage: biblecli prepare [--parser <parser>] [--input <input-path>] <output-path>
   --parser <parser>     Parser. Defaults to '{{default-parser}}'.
   --input <input-path>  Input path. Defaults to '{{default-parser-input}}'.
   <output-path>         Output directory to place data files."
   :cmdline-opts {:string ["parser" "input"]
                  :default {:parser nil
                            :input nil}}}
  [{parser :parser src :input output-dir :_}]
  (if (not= (count output-dir) 1)
    (throw "Must have exactly one <output-path> parameter."))
  (let [parser (or parser (u/default-parser))
        src  (or src (u/default-parser-input))
        m (parse parser src)
        r (res/build-resources m)
        metadata (res/metadata->buffer r)
        output-dir (first output-dir)]
    (write-metadata! output-dir metadata)
    (doseq [part (vals r)]
      (write-data! output-dir part))))