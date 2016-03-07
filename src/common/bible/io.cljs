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

(ns common.bible.io)

(def verse-partition-size 781)

(defn- get-chapter-count-for-books [m]
  (->> m
    (map :chapters)
    (map count)
    (vec)))

(defn- get-verse-count-for-chapters [m]
  (->> m
    (mapcat :chapters)
    (map :verses)
    (map count)
    (vec)))

(defn- filtered-chapter-indexes-to-set [m f]
  (->> m
    (mapcat :chapters)
    (keep-indexed #(if (f %2) %1))
    (set)))

(defn normalized->persisted-bible [m]
  {:books (get-chapter-count-for-books m)
   :chapters (get-verse-count-for-chapters m)
   :subtitle (filtered-chapter-indexes-to-set m :subtitle)
   :postscript (filtered-chapter-indexes-to-set m :postscript)
   :partition-size verse-partition-size})

(defn normalized->persisted-verses [m]
  (->> m
    (mapcat :chapters)
    (mapcat :verses)
    (vec)))