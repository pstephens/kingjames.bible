(ns biblecli.commands.prepare
  (:require
    [common.bible.resource :as res]
    [common.normalizer.core :refer [parse]]))

(def node-fs (js/require "fs"))
(def node-path (js/require "path"))

(defn write-data! [dir encoded-data]
  (let [filename (encoded-data :name)
        filepath (.join node-path dir filename)]
    (.writeFileSync node-fs filepath (encoded-data :content))))

(defn prepare! [parser src output-dir]
  (let [m (parse parser src)
        r (res/build-resources m)]
    (doseq [part (vals r)]
      (write-data! output-dir part))))