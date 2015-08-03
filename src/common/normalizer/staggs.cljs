(ns normalizer.staggs
  (:require [clojure.string :as string]
            [bible.core]
            [normalizer.filesystem :refer [read-text]]))

(def bookNameMap {
  "Ge"   :Genesis
  "Ex"   :Exodus
  "Le"   :Leviticus
  "Nu"   :Numbers
  "De"   :Deuteronomy
  "Jos"  :Joshua
  "Jg"   :Judges
  "Ru"   :Ruth
  "1Sa"  :Samuel1
  "2Sa"  :Samuel2
  "1Ki"  :Kings1
  "2Ki"  :Kings2
  "1Ch"  :Chronicles1
  "2Ch"  :Chronicles2
  "Ezr"  :Ezra
  "Ne"   :Nehemiah
  "Es"   :Esther
  "Job"  :Job
  "Ps"   :Psalms
  "Pr"   :Proverbs
  "Ec"   :Ecclesiastes
  "So"   :SongOfSolomon
  "Isa"  :Isaiah
  "Jer"  :Jeremiah
  "La"   :Lamentations
  "Eze"  :Ezekiel
  "Da"   :Daniel
  "Ho"   :Hosea
  "Joe"  :Joel
  "Am"   :Amos
  "Ob"   :Obadiah
  "Jon"  :Jonah
  "Mic"  :Micah
  "Na"   :Nahum
  "Hab"  :Habakkuk
  "Zep"  :Zephaniah
  "Hag"  :Haggai
  "Zec"  :Zechariah
  "Mal"  :Malachi
  "Mt"   :Matthew
  "Mr"   :Mark
  "Lu"   :Luke
  "Joh"  :John
  "Ac"   :Acts
  "Ro"   :Romans
  "1Co"  :Corinthians1
  "2Co"  :Corinthians2
  "Ga"   :Galatians
  "Eph"  :Ephesians
  "Php"  :Philippians
  "Col"  :Colossians
  "1Th"  :Thessalonians1
  "2Th"  :Thessalonians2
  "1Ti"  :Timothy1
  "2Ti"  :Timothy2
  "Tit"  :Titus
  "Phm"  :Philemon
  "Heb"  :Hebrews
  "Jas"  :James
  "1Pe"  :Peter1
  "2Pe"  :Peter2
  "1Jo"  :John1
  "2Jo"  :John2
  "3Jo"  :John3
  "Jude" :Jude
  "Re"   :Revelation})

(defn transformVerse [s]
  (let [[_ book ch verse content] (re-matches #"\s+(\w+)\s+(\d+)\:(\d+)\s+(.*)" s)]
    {
      :bookId (bookNameMap book)
      :chapterNum (int ch)
      :content content
    }))

(defn transformChapter [verses]
  (let [v1 (first verses)]
    {
      "num" (v1 :chapterNum)
      "subtitle" false
      "postscript" false
      "verses"
        (->>
          verses
          (map :content)
          (vec))
    }))

(defn transformBook [verses]
  (let [
      v1 (first verses)
      bookData (bible.core/bookData (v1 :bookId))]
    {
      "name" (bookData :name)
      "num" (inc (bookData :index))
      "chapters"
        (->>
          verses
          (partition-by :chapterNum)
          (map transformChapter)
          (vec))
    }))

(defn transformBible [str]
  (->>
    str
    (string/split-lines)
    (map transformVerse)
    (filter :bookId)
    (partition-by :bookId)
    (map transformBook)
    (vec)))

(defn parser [fs path]
  (->>
    (read-text fs path)
    (transformBible)))