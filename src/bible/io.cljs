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

(def ^:private state (atom {:eventloop (chan 10)}))

(defprotocol ResourceStore
  (get-resource [this resid cb] "The cb should have this signature: (fn [{content :content err :err}] ...)."))

(defn ^:private url-from-resid [state resid]
  (let [short-hash (get-in state [:hash resid])]
    (str "/" resid "-" short-hash ".d")))

(deftype XhrManagerSource [mgr]
  ResourceStore
  (get-resource [this resid cb]
    (let [url (url-from-resid @state resid)
          on-response
            (fn [e]
              (cb {:content (.getResponseText (obj/get e "target"))}))]
      (.send mgr resid url "GET" nil nil 1 on-response))))

(defn set-resource-store-to-xhr-manager []
  (swap! state #(assoc % :store (XhrManagerSource. (goog.net.XhrManager. 3 nil nil nil 10000)))))

(defn ^:export set-resource-ids [resid-map]
  (if (object? resid-map)
    (recur (js->clj resid-map))
    (swap! state #(assoc % :hash resid-map))))

(defn tryget-resources
  ([resids]
    (tryget-resources (:cache @state) resids))
  ([cache resids]
    (loop [ret {} ids (seq resids)]
      (if ids
        (if-let [entry (find cache (first ids))]
          (recur
            (conj ret entry)
            (next ids))
          nil)
        ret))))

(defn ^:private post-msg
  ([msg] (post-msg (:eventloop @state) msg))
  ([eventloop msg] (put! eventloop msg)))

(defmulti resource-event #(:type %))

(defn ^:private do-fetch-resource [resid response-ch]
  (let [ch (chan 1)
        m (mult ch)
        url (url-from-resid @state resid)
        cb (fn [{content :content err :err}]
            (post-msg {:type :response
                       :resid resid
                       :content content
                       :chan ch}))]
    (swap! state #(assoc-in % [:pending resid] m))
    (tap m response-ch)
    (get-resource (:store @state) resid cb)))

(defmethod resource-event :fetch [{resid :resid response-ch :response-ch}]
  (if-let [v (get-in @state [:cache resid])]
    (put! response-ch {:resid resid :val v})
    (if-let [m (get-in @state [:pending resid])]
      (tap m response-ch)
      (do-fetch-resource resid response-ch))))

(defmethod resource-event :response [{resid :resid content :content ch :chan}]
  (let [reader (t/reader :json)
        data   (t/read reader content)]
    (swap! state
      (fn [st]
        (-> st
          (update-in [:pending] #(dissoc % resid))
          (update-in [:cache] #(assoc % resid data)))))
    (put! ch {:resid resid :val data})))

(defn ^:private resource-loop []
  (go-loop [msg (<! (:eventloop @state))]
    (resource-event msg)
    (recur (<! (:eventloop @state)))))

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
    (split-pending (:cache @state) resids)
    (park-while-pending)))

;; fire up the event loop
(set-resource-store-to-xhr-manager)
(resource-loop)