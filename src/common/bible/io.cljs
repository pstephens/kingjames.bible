(ns common.bible.io)

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
   :postscript (filtered-chapter-indexes-to-set m :postscript)})

(defn normalized->persisted-verses [m]
  (->> m
    (mapcat :chapters)
    (mapcat :verses)
    (vec)))