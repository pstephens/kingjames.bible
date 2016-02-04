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
  (:require [bible.meta]
            [cljs.core.async :refer [to-chan chan put! tap mult <!]]
            [cognitect.transit :as t]
            [goog.net.XhrManager :as xhr]
            [goog.object :as obj]))

(def ^:private state (atom {:eventloop (chan 10)}))

(defprotocol ResourceStore
  (get-resource [this resid cb] "The cb should have this signature: (fn [{content :content err :err}] ...)."))

(defn set-resource-store [store]
  (swap! state #(assoc % :store store)))

(defmulti resource-event #(:type %))

(defmulti resource-transform #(nth % 0))

(defmethod resource-transform :default [[resid v]]
  v)

(defn ^:private url-from-resid [resid-map resid]
  (let [short-hash (get resid-map resid)]
    (str "/" resid "-" short-hash ".d")))

(deftype XhrManagerSource [mgr]
  ResourceStore
  (get-resource [this resid cb]
    (let [url (url-from-resid (:hash @state) resid)
          reader (t/reader :json)
          parse-body
            (fn [e]
              (let [target (obj/get e "target")]
                (if (.isSuccess target)
                  (try
                    {:content
                      (t/read reader (.getResponseText target))}
                    (catch js/Object e
                      {:err {:ex e}}))
                  {:err {:status (.getStatus target)
                         :statusText (.getStatusText target)}})))
          on-response (fn [e] (cb (parse-body e)))]
      (.send mgr resid url "GET" nil nil 1 on-response))))

(defn make-xhr-manager []
  (XhrManagerSource. (goog.net.XhrManager. 3 nil nil nil 10000)))

(defn ^:export set-resource-ids [resid-map]
  (if (object? resid-map)
    (recur (js->clj resid-map))
    (swap! state #(assoc % :hash resid-map))))

(defn ^:private post-msg [eventloop msg] (put! eventloop msg))

(defn ^:private fetch-resource-from-store [resid response-ch m cb-ch store eventloop]
  (let [cb (fn [{content :content err :err}]
             (post-msg eventloop
               {:type :response
                :resid resid
                :content content
                :err err
                :chan cb-ch}))]
    (tap m response-ch)
    (get-resource store resid cb)))

(defmethod resource-event :fetch [{resid :resid response-ch :response-ch}]
  (loop [st @state]
    (if-let [v (get-in st [:cache resid])]
      (put! response-ch {:resid resid :val v})
      (if-let [m (get-in st [:pending resid])]
        (tap m response-ch)
        (let [ch        (chan 1)
              m2        (mult ch)
              st2       (assoc-in st [:pending resid] m2)
              store     (:store st2)
              eventloop (:eventloop st2)]
          (if (compare-and-set! state st st2)
            (fetch-resource-from-store resid response-ch m2 ch store eventloop)
            (recur @state)))))))

(defmethod resource-event :response [{resid :resid data :content err :err ch :chan}]
  (if err
    (do
      (swap! state (fn [st] (update-in st [:pending] #(dissoc % resid))))
      (put! ch {:resid resid :err err}))
    (do
      (let [data (resource-transform [resid data])]
        (swap! state
          (fn [st]
            (-> st
              (update-in [:pending] #(dissoc % resid))
              (update-in [:cache] #(assoc % resid data)))))
        (put! ch {:resid resid :val data})))))

(defn ^:private fetch-resource [resid eventloop]
  (let [ch (chan 1)]
    (post-msg eventloop
      {:type :fetch
       :resid resid
       :response-ch ch})
    ch))

(defn ^:private resource-loop []
  (go-loop [msg (<! (:eventloop @state))]
    (resource-event msg)
    (recur (<! (:eventloop @state)))))

(defn ^:private transform-books [books]
  (let
    [[_ _ transformed-books]
      (->>
        books
        (map (fn [id chapter-cnt] [chapter-cnt id]) bible.meta/books)
        (reduce
          (fn [[chapter-idx idx acc] [chapter-cnt id]]
            [(+ chapter-idx chapter-cnt)
             (inc idx)
             (conj acc
               {:id id
                :idx idx
                :chapter-cnt chapter-cnt
                :chapter-idx chapter-idx})])
          [0 0 []]))]
    transformed-books))

(defn ^:private transform-chapters [chapters]
  (let
    [[_ _ transformed-chapters]
      (reduce
        (fn [[verse-idx idx acc] verse-cnt]
          [(+ verse-idx verse-cnt)
           (inc idx)
           (conj acc
             {:idx idx
              :verse-idx verse-idx
              :verse-cnt verse-cnt
              ;; :book-idx
              ;; :subtitle
              ;; :postscript
              })])
        [0 0 []]
        chapters)]
    transformed-chapters))

(defmethod resource-transform "B" [[resid v]]
  {:books (transform-books (:books v))
   :chapters (transform-chapters (:chapters v))})

(defn ^:private split-pending [resids cache eventloop]
  (loop [ret {}
         pending {}
         ids (seq resids)]
    (if ids
      (let [id (first ids)
            rst (seq (next ids))
            entry (find cache id)]
        (if entry
          (recur (conj ret entry) pending rst)
          (recur ret (conj pending [id (fetch-resource id eventloop)]) rst)))
      [ret pending])))

(defn ^:private park-while-pending [[ret pending]]
  (go-loop [ret ret
            pending pending]
    (if (> (count pending) 0)
      (let [d (alts! (vec (vals pending)))
            [{resid :resid v :val err :err} _] d]
        (if err
          {:err err}
          (recur
            (assoc ret resid v)
            (dissoc pending resid))))
      ret)))

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

(defn resources [resids]
  (let [st @state
        cache (:cache st)
        eventloop (:eventloop st)]
    (->>
      (split-pending resids cache eventloop)
      (park-while-pending))))

;; default configuration
(set-resource-store (make-xhr-manager))

;; fire up the event loop
(resource-loop)