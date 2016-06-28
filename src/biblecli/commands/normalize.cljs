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

(ns biblecli.commands.normalize
  (:require
    [biblecli.main.utility :as u]
    [common.normalizer.core :refer [run-parser]]))

(defn normalize
  {:summary "Convert raw bible source text into a normalized format. Used to compare the quality of different kjv texts."
   :doc "usage: biblecli normalize [--parser <parser>] [--input <input-path>] <output-path>
   --parser <parser>      Parser. Defaults to '{{default-parser}}'.
   --input <input-path>   Input path. Defaults to '{{default-parser-input}}'.
   <output-path>          Output path."
   :cmdline-opts {:string ["parser" "input"]
                  :default {:parser nil
                            :input nil}}}
  [{parser :parser input :input output :_}]
  (let [parser (or parser (u/default-parser))
        input  (or input (:full-path (u/default-parser-input)))]
    (if (not= (count output) 1)
      (throw "Must have exactly one <output-path> parameter."))
    (println "parser" parser)
    (println "input" input)
    (run-parser parser input (first output))))