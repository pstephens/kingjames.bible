(ns biblecli.commands.serve
  (:require
    [common.bible.resource :as res]
    [common.normalizer.core :refer [parse]]
    [cljs.nodejs :as nodejs]
    [clojure.string :as string]
    [goog.object]))

(def node-http (nodejs/require "http"))

(defn process-request [r req res]
  (let [resource (r (subs (goog.object/get req "url") 1))]
    (if resource
      (do
        (goog.object/set res "statusCode" 200)
        (doseq [[k v] (map identity (resource :headers))]
          (.setHeader res k v))
        (.end res (get resource :content)))
      (do
        (goog.object/set res "statusCode" 404)
        (.end res)))))

(defn serve [parser src]
  (let [m      (parse parser src)
        r      (res/build-resources m)
        server (.createServer node-http #(process-request r %1 %2))]
    (.listen server 8080)))