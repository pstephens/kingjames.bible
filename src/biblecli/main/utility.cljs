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

(ns biblecli.main.utility)

(def path (js/require "path"))

(def root-path (atom ""))

(defn get-root-path [] @root-path)

(defn set-root-path! [path] (reset! root-path path))

(defn default-parser [] "staggs")

(defn default-parser-input []
  (.join path (get-root-path) "./kjv-src/www.staggs.pair.com-kjbp/kjv.txt"))

(defn default-parser-input-rel []
  (.relative path "" (default-parser-input)))
