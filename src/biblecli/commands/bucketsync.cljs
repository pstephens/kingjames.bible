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

(ns biblecli.commands.bucketsync
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [to-chan chan put! tap mult <!]]))

(def AWS (js/require "aws-sdk"))

(defn list-objects-1000 [s3 continuationToken]
  (let [chan (chan)
        opts (cond-> {}
               continuationToken (assoc :ContinuationToken continuationToken))
        cb (fn [err data]
             (put! chan [err data]))]
    (.listObjectsV2 s3 (clj->js opts) cb)
    chan))

(defn list-objects [s3]
  (go
    (loop [acc {}
           continuationToken nil]
      (let [[err data] (<! (list-objects-1000 s3 continuationToken))]
        (if err {:err err}
                (let [{contents :Contents isTruncated :IsTruncated nextToken :NextContinuationToken} (js->clj data :keywordize-keys true)
                      acc (->> contents
                               (reduce (fn [coll {key :Key etag :ETag}] (assoc coll key etag)) acc))]
                  (if isTruncated
                    (recur acc nextToken)
                    acc)))))))

(defn make-s3 [profile region bucket]
  (let [cfg
          (cond-> {:region region
                   :sslEnabled true
                   :params {:Bucket bucket}}
              (not= profile "default")
              (assoc :credentials (AWS.SharedIniFileCredentials. (clj->js {:profile profile}))))]
    (AWS.S3. (clj->js cfg))))

(defn sync! [dir profile region bucket]
  (let [s3 (make-s3 profile region bucket)]
    (go
      (let [keys (<! (list-objects s3))]
        (println (count keys))))))