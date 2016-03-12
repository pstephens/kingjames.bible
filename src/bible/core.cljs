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

(defn parse-ref [ref]
  [(ref 0) nil nil])

(defn book
  ([book-idxs]
    (go
      (try
        (let [{book-res "B"} (<? (io/resources ["B"]))]
          (book book-res book-idxs))
        (catch js/Error e
          e))))

  ([book-res book-idxs]
    (loop [acc []
           book-idxs (seq book-idxs)]
      (if book-idxs
        (let [book-idx (first book-idxs)
              next-idxs (next book-idxs)
              data (get-in book-res [:books book-idx])]
          (if data
            (recur
              (conj acc data)
              next-idxs)
            (throw (js/Error. (str "Invalid book-idx " book-idx ".")))))
        acc))))

(defn chapter
  ([chapter-idxs]
    (go
      (try
        (let [{res "B"} (<? (io/resources ["B"]))]
          (chapter res chapter-idxs))
        (catch js/Error e
          e))))

  ([res chapter-idxs]
    (loop [acc []
           chapter-idxs (seq chapter-idxs)]
      (if chapter-idxs
        (let [chapter-idx (first chapter-idxs)
              next-idxs (next chapter-idxs)
              data (get-in res [:chapters chapter-idx])]
          (if data
            (recur (conj acc data) next-idxs)
            (throw (js/Error. (str "Invalid chapter-idx " chapter-idx ".")))))
        acc))))

(defn parse-verse-ref [b r]
  (cond
    (integer? r)
    r

    :else nil))

(defn format-verse-res-id [partition-idx]
  (str "V" (if (< partition-idx 10) "0" "") partition-idx))

(defn verse
  ([verse-refs]
    (go
      (try
        (let [{b "B"} (<? (io/resources ["B"]))
              partition-size (:partition-size b)
              indexes (->> verse-refs (map #(parse-verse-ref b %)) (vec))
              unique-indexes (set indexes)
              partition-indexes (->> unique-indexes (map #(quot % partition-size)) (set))
              res-ids (->> partition-indexes (map format-verse-res-id) (vec))
              res-ids (conj res-ids "B")
              res (<? (io/resources res-ids))]
          (verse res indexes))
        (catch js/Error e
          e))))
  ([{b "B" :as all} verse-idxs]
    (let [partition-size (:partition-size b)]
      (loop [acc []
             verse-idxs (seq verse-idxs)]
        (if verse-idxs
          (let [verse-idx (first verse-idxs)
                next-idxs (next verse-idxs)
                res-idx (quot verse-idx partition-size)
                verse-offset (rem verse-idx partition-size)
                res-id (str "V" (if (< res-idx 10) "0" "") res-idx)
                res (get all res-id)
                data (get res verse-offset)]
            (if data
              (recur (conj acc {:content data}) next-idxs)
              (throw (js/Error. (str "Invalid verse-idx " verse-idx ".")))))
          acc)))))