(ns common.bible.io
  (:require [clojure.string :as string]))

(defn get-chapter-count-for-book [book]
  (count (:chapters book)))

(defn get-books [m]
  (->> m
    (map get-chapter-count-for-book)
    (vec)))

(defn get-chapters [m]
  (->> m
    (mapcat :chapters)
    (map :verses)
    (map count)
    (vec)))

(defn normalized->persisted-bible [m]
  {:books (get-books m)
   :chapters (get-chapters m)})