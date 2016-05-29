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

(ns reader.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [bible.io]))

(enable-console-print!)

(defui HelloWorld
  Object
  (render [this]
    (dom/div nil "Hello, world!")))

(def hello (om/factory HelloWorld))

(js/ReactDOM.render (hello) (gdom/getElement "app"))

(bible.io/resources ["B"
  "V00" "V01" "V02" "V03" "V04" "V05" "V06" "V07" "V08" "V09"
  "V10" "V11" "V12" "V13" "V14" "V15" "V16" "V17" "V18" "V19"
  "V20" "V21" "V22" "V23" "V24" "V25" "V26" "V27" "V28" "V29"
  "V30" "V31" "V32" "V33" "V34" "V35" "V36" "V37" "V38" "V39"])

(def reconciler
  (om/reconciler
    {:state {}
     :parser (om/parser {:read read})
     :send (todo)}))