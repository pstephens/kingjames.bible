;;;;   Copyright 2015 Peter Stephens. All Rights Reserved.
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

(ns biblecli.commands.serve
  (:require
    [common.bible.resource :as res]
    [common.normalizer.core :refer [parse]]
    [cljs.nodejs :as nodejs]
    [clojure.string :as string]
    [goog.object]))

(def node-http (js/require "http"))

(defn get-header [req header]
  (let [req-headers   (goog.object/get req "headers")]
    (goog.object/get req-headers header)))

(defn test-if-none-match [req resource]
  (let [if-none-match (get-header req "if-none-match")
        resource-etag (get-in resource [:headers "ETag"])]
    (not= if-none-match resource-etag)))

(defn test-if-modified-since [req resource]
  (let [if-modified-since  (get-header req "if-modified-since")
        parse-if-mod-since (.getTime (js/Date. (str if-modified-since)))
        resource-last-mod  (get-in resource [:headers "Last-Modified"])
        parse-last-mod     (.getTime (js/Date. resource-last-mod))]
    (or
      (js/isNaN parse-if-mod-since)
      (< parse-if-mod-since parse-last-mod))))

(defn test-if-fresh [req resource]
  (cond
    (get-header req "if-none-match") (not (test-if-none-match req resource))
    (get-header req "if-modified-since") (not (test-if-modified-since req resource))
    :else false))

(defn process-response [res status-code headers content]
  (goog.object/set res "statusCode" status-code)
  (doseq [[k v] (map identity headers)]
    (.setHeader res k v))
  (if content
    (.end res content)
    (.end res)))

(defn process-not-found [req res]
  (process-response res 404 {} nil))

(defn process-not-modified [resource req res]
  (let [all-headers (:headers resource)
        subset-headers (select-keys all-headers ["ETag"])]
    (process-response res 304 subset-headers nil)))

(defn process-resource [resource req res]
  (let [headers (resource :headers)
        content (resource :content)]
    (cond
      (test-if-fresh req resource) (process-not-modified resource req res)
      :else (process-response res 200 headers content))))

(defn process-request [resources req res]
  (let [url (goog.object/get req "url")]
    (if (not (.startsWith url "/"))
      (process-not-found req res)
      (let [resource (resources (subs url 1))]
        (if (not resource)
          (process-not-found req res)
          (process-resource resource req res))))))

(defn serve [parser src]
  (let [m      (parse parser src)
        r      (res/build-resources m)
        server (.createServer node-http #(process-request r %1 %2))]
    (do
      (doseq [k (keys r)]
        (prn k))
      (prn "Listening on port 8080.")
      (.listen server 8080))))