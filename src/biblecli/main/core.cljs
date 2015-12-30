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

(ns biblecli.main.core
  (:require
    [cljs.nodejs :as nodejs]
    [biblecli.commands.normalize]
    [biblecli.commands.prepare]
    [biblecli.commands.serve]))

(nodejs/enable-util-print!)

(def process nodejs/process)

(def commands
  {"normalize" biblecli.commands.normalize/normalize
   "prepare"   biblecli.commands.prepare/prepare!
   "serve"     biblecli.commands.serve/serve})

(defn- main [command & args]
  (try
    (let [cmd (commands command)]
      (if cmd
        (apply cmd args)
        (throw (str "Failed to find command '" command "'."))))
    (catch :default e
      (do
        (prn e)
        (prn (.-stack e))
        (.exit process 1)))))

(set! *main-cli-fn* main)