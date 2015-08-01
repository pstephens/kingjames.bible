(ns normalizer.transit
  (:require [cognitect.transit :as t]
            [normalizer.filesystem :refer [read-text]]))

(defn parser [fs path]
  (let [r (t/reader :json)]
  (->>
    (read-text fs path)
    (t/read r))))