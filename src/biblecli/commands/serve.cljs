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
    [biblecli.main.utility :refer [get-root-path]]
    [common.asset.bible :as bible-res]
    [common.asset.directory :as dir]
    [common.asset.server :as server]
    [reader.asset]
    [test.asset.testpages :as testpages]))

(def node-path (js/require "path"))

(defn serve []
  (let [root-path (get-root-path)
        rel #(.join node-path root-path %)
        bible-dir (rel "out/bible")
        bible-meta-data (bible-res/read-bible-meta-data bible-dir)]
    (server/listen [
      (bible-res/resources bible-dir bible-meta-data)
      (dir/resources (rel "out/dbg_browser") "/")
      (reader.asset/resources)
      (testpages/resources bible-meta-data)])))