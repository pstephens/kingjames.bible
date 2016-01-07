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

(ns common.asset.bible
  (:require [cognitect.transit :as t]
            [common.bible.resource :as res]))

(def node-fs (js/require "fs"))
(def node-path (js/require "path"))

(defn process-item [dir item]
  (delay
    (let [filename (subs (:path item) 1)
          path (.join node-path dir filename)
          buff (.readFileSync node-fs path)]
      (assoc item :content buff))))

(defn resources [dir]
  (let [dir           (.resolve node-path dir)
        meta-filename (.join node-path dir "bible-meta.json")
        meta-buff     (.readFileSync node-fs meta-filename)
        meta-data     (res/buffer->metadata meta-buff)]
    (->>
      (vals meta-data)
      (reduce #(assoc %1 (:path %2) (process-item dir %2)) {}))))