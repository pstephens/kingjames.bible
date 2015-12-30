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

(ns common.normalizer.core
    (:require
      [cognitect.transit :as t]
      [common.normalizer.filesystem :refer [NodeFs write-text]]
      [common.normalizer.staggs]
      [common.normalizer.transit]))

(def node-fs (NodeFs.))

(def parsers {
  :staggs common.normalizer.staggs/parser
  :transit common.normalizer.transit/parser})

(defn write-bible [path bible]
  (let [w (t/writer :json-verbose)
        s (t/write w bible)]
    (write-text node-fs path s)))

(defn parse [parser path]
  (let [p (parsers (keyword parser))]
    (if p
      (p node-fs path)
      (throw (str "Failed to find parser '" parser "'.")))))

(defn run-parser [parser path outputPath]
  (write-bible outputPath (parse parser path)))