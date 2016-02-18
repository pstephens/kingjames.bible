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
  ([book-refs]
    (go
      (try
        (let [{book-res "B"} (<? (io/resources ["B"]))]
          (book book-res book-refs))
        (catch js/Error e
          e))))

  ([book-res book-refs]
    (loop [acc []
           book-refs (seq book-refs)]
      (if book-refs
        (let [book-ref (first book-refs)
              next-refs (next book-refs)
              idx (if (keyword? book-ref)
                    (bible.meta/book-id-to-idx book-ref)
                    book-ref)
              data (get-in book-res [:books idx])]
          (if data
            (recur
              (conj acc data)
              next-refs)
            (throw (js/Error. (str "Invalid book-ref " book-ref ".")))))
        acc))))