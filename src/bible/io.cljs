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
  (:require [cljs.core.async :refer [chan]]
            [goog.net.XhrManager :as xhr]))

(def ^:private resource-ids (atom nil))
(def ^:private resource-cache (atom {}))

(defn set-resource-ids [resid-map]
  (if (object? resid-map)
    (recur (js->clj resid-map))
    (reset! resource-ids resid-map)))

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

(defn resources [resids]
  )