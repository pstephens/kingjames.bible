(ns normalizer.filesystem
  (:require [cljs.nodejs :as nodejs]))

(def node-fs (nodejs/require "fs"))

(defprotocol FileSystem
  (read-text [fs path])
  (write-text [fs path text]))

(deftype NodeFs []
  FileSystem
  (read-text [fs path] 
    (.readFileSync node-fs path (js-obj "encoding" "utf8")))
  (write-text [fs path text] 
    (.writeFileSync node-fs path text (js-obj "encoding" "utf8"))))