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

(ns test.node.common.bible.coretests
  (:require [common.bible.core :as b]
            [common.bible.model :as model]
            [cljs.test :refer-macros [deftest testing is]]))

(deftest book-metadata
  (is (= 1 (b/get-book-meta :Exodus :index)))
  (is (= "John" (b/get-book-meta :John :name)))
  (is (= :Mark (b/get-book-meta :Mark ::model/bookId)))
  (is (= nil (b/get-book-meta :NotABook :name))))
