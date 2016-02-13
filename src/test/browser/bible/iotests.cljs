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

(ns test.browser.bible.iotests
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [bible.io :as io]
            [cljs.core.async :refer [<!]]
            [cljs.test :refer-macros [async deftest testing is use-fixtures]]))

(deftest tryget-resources
  (let [sample-data {"A1" 1 "A2" 2 "A3" 3}]
    (is (= {"A1" 1 "A2" 2} (io/tryget-resources sample-data ["A1" "A2"])))
    (is (= {"A2" 2} (io/tryget-resources sample-data ["A2"])))
    (is (= {"A2" 2} (io/tryget-resources sample-data ["A2" "A2"])))
    (is (= nil (io/tryget-resources sample-data ["A4"])) "Returns null when a key is missing from cache.")
    (is (= nil (io/tryget-resources sample-data ["A1" "A4"])) "Returns null when a key is missing from cache. #2")
    (is (= {} (io/tryget-resources sample-data [])) "Works with empty set.")
    (is (= {} (io/tryget-resources sample-data nil)) "Works with empty nil.")))

(defprotocol MockStore
  (set-response [this resid response])
  (get-accessed [this]))

(deftype TestResourceStore [state]
  io/ResourceStore
  (get-resource [this resid cb]
    (let [r (get-in @state [:resources resid])]
      (swap! state #(update-in % [:accessed resid] (fn [x] (if x (inc x) 1))))
      (if r
        (cb r)
        (throw (js/Error. (str "Response not found for resid " resid))))))
  MockStore
  (set-response [this resid response]
    (swap! state #(assoc-in % [:resources resid] response)))
  (get-accessed [this]
    (get-in @state [:accessed])))

(defn make-mock-store
  ([] (make-mock-store {}))
  ([resources] (TestResourceStore. (atom {:resources resources :accessed {}}))))

(defn set-mock-store
  ([] (set-mock-store (make-mock-store)))
  ([store]
    (io/reset-state)
    (io/set-resource-store store)
    store))

(use-fixtures :once
  {:after
    (fn []
      (io/reset-state)
      (io/set-resource-store (io/make-xhr-manager)))})

(deftest resources
  (async done
    (go
      (testing "B-Book model"
        (is (= 66 (count (get-in (<! (io/resources ["B"])) ["B" :books]))))
        (is (= :Genesis (get-in (<! (io/resources ["B"])) ["B" :books 0 :id])))
        (is (= :Revelation (get-in (<! (io/resources ["B"])) ["B" :books 65 :id])))
        (is (= 50 (get-in (<! (io/resources ["B"])) ["B" :books 0 :chapter-cnt])))
        (is (= 22 (get-in (<! (io/resources ["B"])) ["B" :books 65 :chapter-cnt])))
        (is (= 0 (get-in (<! (io/resources ["B"])) ["B" :books 0 :chapter-idx])))
        (is (= 50 (get-in (<! (io/resources ["B"])) ["B" :books 1 :chapter-idx])))
        (is (= 0 (get-in (<! (io/resources ["B"])) ["B" :books 0 :idx])))
        (is (= 10 (get-in (<! (io/resources ["B"])) ["B" :books 10 :idx]))))
      (testing "B-Chapter model"
        (is (= 0 (get-in (<! (io/resources ["B"])) ["B" :chapters 0 :idx])))
        (is (= 300 (get-in (<! (io/resources ["B"])) ["B" :chapters 300 :idx])))
        (is (= 0 (get-in (<! (io/resources ["B"])) ["B" :chapters 0 :verse-idx])))
        (is (= 31 (get-in (<! (io/resources ["B"])) ["B" :chapters 1 :verse-idx])))
        (is (= 31 (get-in (<! (io/resources ["B"])) ["B" :chapters 0 :verse-cnt])))
        (is (= 25 (get-in (<! (io/resources ["B"])) ["B" :chapters 1 :verse-cnt])))
        (is (= nil (get-in (<! (io/resources ["B"])) ["B" :chapters 0 :subtitle])))
        (is (= true (get-in (<! (io/resources ["B"])) ["B" :chapters 578 :subtitle])))
        (is (= nil (get-in (<! (io/resources ["B"])) ["B" :chapters 50 :postscript])))
        (is (= true (get-in (<! (io/resources ["B"])) ["B" :chapters 1124 :postscript])))
        (is (= 0 (get-in (<! (io/resources ["B"])) ["B" :chapters 0 :book-idx])))
        (is (= 0 (get-in (<! (io/resources ["B"])) ["B" :chapters 49 :book-idx])))
        (is (= 1 (get-in (<! (io/resources ["B"])) ["B" :chapters 50 :book-idx])))
        (is (= 65 (get-in (<! (io/resources ["B"])) ["B" :chapters 1188 :book-idx])))
        (is (= 65 (get-in (<! (io/resources ["B"])) ["B" :chapters 1167 :book-idx])))
        (is (= 64 (get-in (<! (io/resources ["B"])) ["B" :chapters 1166 :book-idx]))))
      (testing "Multiple resources"
        (is (= 3 (count (<! (io/resources ["B" "V01" "V20"])))))
        (is (= 2 (count (<! (io/resources ["V21" "V22"])))))
        (is (= {} (<! (io/resources [])))))
      (testing "Errors should return an :err key"
        (is (contains? (<! (io/resources ["X25"])) :err))
        (is (contains? (<! (io/resources ["B" "X25"])) :err))
        (is (not (contains? (<! (io/resources ["B"])) :err))))

      (testing "Get single resource"
        (io/reset-state)
        (let [store (set-mock-store (make-mock-store {"X01" {:content {:result 1}}}))
              req1  (io/resources ["X01"])]
          (is (= {"X01" {:result 1}} (<! req1)))
          (is (= {"X01" 1} (get-accessed store)))))

      (testing "Should only fetch resource once"
        (io/reset-state)
        (let [store (set-mock-store (make-mock-store {"X01" {:content {:result 2}}}))
              req1  (io/resources ["X01"])
              req2  (io/resources ["X01"])]
          (is (= {"X01" {:result 2}} (<! req1)))
          (is (= {"X01" {:result 2}} (<! req2)))
          (is (= {"X01" 1} (get-accessed store)))))

      (testing "Should pull as much as possible out of cache"
        (io/reset-state)
        (let [store (set-mock-store
                      (make-mock-store {"X01" {:content {:result 3}}
                                        "X34" {:content {:result 4}}}))]
          (is (= {"X01" {:result 3}} (<! (io/resources ["X01"]))))
          (is (= {"X01" 1} (get-accessed store)))

          (is (= {"X01" {:result 3} "X34" {:result 4}}
            (<! (io/resources ["X01" "X34"]))))
          (is (= {"X01" 1 "X34" 1} (get-accessed store)))))

      (testing "Shouldn't fetch anything when request is empty"
        (io/reset-state)
        (let [store (set-mock-store (make-mock-store {}))]
          (is (= {} (<! (io/resources []))))
          (is (= {} (get-accessed store)))))

      (testing "Should pull multiple resources in single request"
        (io/reset-state)
        (let [store (set-mock-store
                      (make-mock-store {"X01" {:content {:result 3}}
                                        "X34" {:content {:result 4}}}))]
          (is (= {"X01" {:result 3} "X34" {:result 4}} (<! (io/resources ["X01" "X34"]))))
          (is (= {"X01" 1 "X34" 1} (get-accessed store)))))

      (testing "Should pass through error"
        (io/reset-state)
        (let [store (set-mock-store
                      (make-mock-store {"X01" {:err "Failure"}}))]
          (is (= {:err "Failure"} (<! (io/resources ["X01"]))))
          (is (= {"X01" 1} (get-accessed store)))))

      (testing "Should treat partial failure as error"
        (io/reset-state)
        (let [store (set-mock-store
                      (make-mock-store {"X05" {:content {:res 2}} "X07" {:err "Failure"}}))]
          (is (= {:err "Failure"} (<! (io/resources ["X05" "X07"]))))
          (is (= {"X05" 1 "X07" 1} (get-accessed store)))))

      (testing "Should treat partial failure as error (2)"
        (io/reset-state)
        (let [store (set-mock-store
                      (make-mock-store {"X07" {:content {:res 2}} "X05" {:err "Failure"}}))]
          (is (= {:err "Failure"} (<! (io/resources ["X05" "X07"]))))
          (is (= {"X05" 1 "X07" 1} (get-accessed store)))))

      (done))))