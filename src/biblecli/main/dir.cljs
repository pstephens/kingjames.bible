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

(ns biblecli.main.dir
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [chan put! <!]]
            [cljs.nodejs :refer [require]]))

(def ^:private fs (require "fs"))
(def ^:private path (require "path"))

(defn ^:private readdir [dir]
  (let [chan (chan)
        cb (fn [err data]
             (put! chan [err data]))]
    (.readdir fs dir cb)
    chan))

(defn ^:private stat [path]
  (let [chan (chan)
        cb (fn [err data]
             (put! chan [err data]))]
    (.stat fs path cb)
    chan))

(defn ^:private stat-file [dir reldir filename]
  (let [fspath (.join path dir filename)
        relpath (if (empty? reldir) filename (str reldir "/" filename))
        statchan (stat fspath)]
    [relpath fspath statchan]))

(defn ^:private wait-all [filestatsasync]
  (go
    (loop [stats filestatsasync
           acc []]
      (let [head (first stats)
            rest (rest stats)]
        (if head
          (let [[relpath fspath statchan] head
                [err data] (<! statchan)]
            (if err
              [err nil]
              (recur rest (conj acc [relpath fspath data]))))
          [nil acc])))))

(defn ^:private isdir [stat]
  (.isDirectory stat))

(defn readdir-recursive
  ([dir] (readdir-recursive dir "" {}))
  ([dir reldir acc]
   (go
     (let [[err files] (<! (readdir dir))]
       (if err
         [err nil]
         (let [filestatsasync (->> files (map #(stat-file dir reldir %)) (doall))
               [err filestats] (<! (wait-all filestatsasync))]
           (if err
             [err nil]
             (loop [lst filestats acc acc]
               (let [[relpath fspath stat] (first lst)
                     rest (rest lst)]
                 (if relpath
                   (if (isdir stat)
                     (let [[err acc] (<! (readdir-recursive fspath relpath acc))]
                       (if err
                         [err nil]
                         (recur rest acc)))
                     (recur rest (conj acc [relpath fspath])))
                   [nil acc]))))))))))