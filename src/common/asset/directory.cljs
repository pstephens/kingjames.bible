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

(ns common.asset.directory
  (:require [goog.object :as obj]))

(def node-fs (js/require "fs"))
(def node-path (js/require "path"))

(def default-mime-map
  {
    ".js"   "application/javascript"
    ".html" "text/html"
    ".css"  "text/css"
    ".map"  "application/json"
    ".json" "application/json"
  })

(declare process-dir)

(defn process-item [dir reldir name acc]
  (let [path (.join node-path dir name)
        relpath (str reldir name)
        stat (.statSync node-fs path)
        ext  (.extname node-path name)
        mime (default-mime-map ext)]
    (cond
      (.isDirectory stat) (process-dir path (str relpath "/") acc)
      (some? mime)
        (assoc acc relpath
          (delay
            (let [buff (.readFileSync node-fs path)]
              {:path relpath
               :content buff
               :headers {"Cache-Control", "max-age=0"
                         "Content-Type"   mime
                         "Content-Length" (obj/get buff "length")
                         "Last-Modified"  (.toUTCString (obj/get stat "mtime"))}})))
      :else acc)))

(defn process-dir [dir reldir acc]
  (->>
    (.readdirSync node-fs dir)
    (reduce #(process-item dir reldir %2 %1) acc)))

(defn resources [dir reldir]
  (let [dir (.resolve node-path dir)]
    (atom
      (delay
        (process-dir dir reldir {})))))