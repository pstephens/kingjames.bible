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

(ns bible.io
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [cljs.core.async :refer [to-chan chan put! tap mult <!]]
            [cognitect.transit :as t]
            [goog.net.XhrManager :as xhr]
            [goog.object :as obj]))

(def ^:private resource-ids (atom nil))
(def ^:private resource-cache (atom {}))
(def ^:private pending-resource (atom {}))
(def ^:private resource-msg-chan (chan 10))
(def ^:private xhr-manager (goog.net.XhrManager. 3 nil nil nil 10000))

(defn ^:export set-resource-ids [resid-map]
  (if (object? resid-map)
    (recur (js->clj resid-map))
    (reset! resource-ids resid-map)))

(defn reset-resource-cache! []
  (reset! resource-cache {}))

(defn tryget-resources
  ([resids]
    (tryget-resources @resource-cache resids))
  ([res resids]
    (loop [ret {} ids (seq resids)]
      (if ids
        (if-let [entry (find res (first ids))]
          (recur
            (conj ret entry)
            (next ids))
          nil)
        ret))))

(defn ^:private url-from-resid [resid]
  (let [short-hash (@resource-ids resid)]
    (str "/" resid "-" short-hash ".d")))

(defn ^:private post-msg [msg]
  (put! resource-msg-chan msg))

(defmulti resource-event #(:type %))

(defn ^:private do-fetch-resource [resid response-ch]
  (let [ch (chan 1)
        m (mult ch)
        url (url-from-resid resid)
        cb (fn [e]
            (post-msg {:type :response
                       :resid resid
                       :content (.getResponseText (obj/get e "target"))
                       :chan ch}))]
    (swap! pending-resource #(assoc % resid m))
    (tap m response-ch)
    (.send xhr-manager resid url "GET" nil nil 1 cb)))

(defmethod resource-event :fetch [{resid :resid response-ch :response-ch}]
  (if-let [v (get @resource-cache resid)]
    (put! response-ch {:resid resid :val v})
    (if-let [m (get @pending-resource resid)]
      (tap m response-ch)
      (do-fetch-resource resid response-ch))))

(defmethod resource-event :response [{resid :resid content :content ch :chan}]
  (let [reader (t/reader :json)
        data   (t/read reader content)]
    (swap! pending-resource #(dissoc % resid))
    (swap! resource-cache #(assoc % resid data))
    (put! ch {:resid resid :val data})))

(defn ^:private resource-loop []
  (go-loop [msg (<! resource-msg-chan)]
    (resource-event msg)
    (recur (<! resource-msg-chan))))

(defn ^:private fetch-resource [resid]
  (let [ch (chan 1)]
    (post-msg
      {:type :fetch
       :resid resid
       :response-ch ch})
    ch))

(defn ^:private split-pending [res resids]
  (loop [ret {}
         pending {}
         ids (seq resids)]
    (if ids
      (let [id (first ids)
            rst (seq (next ids))
            entry (find res id)]
        (if entry
          (recur (conj ret entry) pending rst)
          (recur ret (conj pending [id (fetch-resource id)]) rst)))
      [ret pending])))

(defn ^:private park-while-pending [[ret pending]]
  (go-loop [ret ret
            pending pending]
    (if (> (count pending) 0)
      (let [[{resid :resid v :val} _] (alts! (vec (vals pending)))]
        (recur
          (assoc ret resid v)
          (dissoc pending resid)))
      ret)))

(defn resources [resids]
  (->>
    (split-pending @resource-cache resids)
    (park-while-pending)))

;; fire up the event loop
(resource-loop)