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

(ns bible.core
  (:require-macros [bible.macros :refer [<?]]
                   [cljs.core.async.macros :refer [go]])
  (:require [bible.helpers]
            [bible.io :as io]
            [bible.meta]))

(defn ^:private binary-search [value comp v]
  (let [cnt (count v)]
    (if (<= cnt 0)
      nil
      (let [i (quot cnt 2)
            value2 (get v i)
            cmp (comp value value2)]
        (cond
          (< cmp 0) (recur value comp (subvec v 0 i))
          (> cmp 0) (recur value comp (subvec v (inc i)))
          :else value2)))))

(defn ^:private compare-verse-idx-to-chapter
  [verse-idx {first-verse-idx :verse-idx verse-cnt :verse-cnt}]
    (cond
      (< verse-idx first-verse-idx) -1
      (>= verse-idx (+ first-verse-idx verse-cnt)) 1
      :else 0))

(defn ^:private format-verse-resid [partition-idx]
  (str "V" (if (< partition-idx 10) "0" "") partition-idx))

(defn ^:private accumulate [f idxs]
  (loop [acc []
         idxs (seq idxs)]
    (if idxs
      (let [idx (first idxs)
            next-idxs (next idxs)
            data (f idx)]
        (if data
          (recur (conj acc data) next-idxs)
          (throw (js/Error. (str "Invalid index " idx ".")))))
      acc)))

(defn ^:private fetch-resources-and-accumulate [f-acc f-fetch idxs]
  (go
    (try
      (let [res (<? (f-fetch idxs))]
        (f-acc res idxs))
      (catch js/Error e
        e))))

(defn book
  ([book-idxs]
    (fetch-resources-and-accumulate
      book
      #(io/resources ["B"])
      book-idxs))

  ([{res "B"} book-idxs]
    (accumulate
      #(get-in res [:books %])
      book-idxs)))

(defn chapter
  ([chapter-idxs]
    (fetch-resources-and-accumulate
      chapter
      #(io/resources ["B"])
      chapter-idxs))

  ([{res "B"} chapter-idxs]
    (accumulate
      #(get-in res [:chapters %])
      chapter-idxs)))

(defn verse
  ([verse-idxs]
    (go
      (try
        (let [{{partition-size :partition-size} "B"} (<? (io/resources ["B"]))
              partition-idxs
                (->>
                  verse-idxs
                  (map #(quot % partition-size))
                  (set))
              res-ids
                (->>
                  partition-idxs
                  (map format-verse-resid)
                  (concat ["B"])
                  (vec))
              res (<? (io/resources res-ids))]
          (verse res verse-idxs))
        (catch js/Error e
          e))))

  ([{{partition-size :partition-size
      chapters :chapters} "B" :as all} verse-idxs]
    (accumulate
      #(let [partition-idx (quot % partition-size)
             verse-offset (rem % partition-size)
             content (get-in all [(format-verse-resid partition-idx) verse-offset])]
        (if content
          {:content content
           :chapter (binary-search % compare-verse-idx-to-chapter chapters)
           :idx %}
          nil))
      verse-idxs)))