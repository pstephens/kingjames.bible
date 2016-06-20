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
    [cljs.nodejs :refer [process require enable-util-print!]]
    [biblecli.commands.bucketsync]
    [biblecli.commands.normalize]
    [biblecli.commands.prepare]
    [biblecli.commands.serve]
    [biblecli.commands.staticpages]
    [biblecli.commands.verseoftheday]))

(declare commands)

(defn printallcommands []
  (let [maxchars (reduce #(max %1 (count %2)) 0 (keys commands))]
    (println "All commands:")
    (doseq [[command fn] commands]
      (let [padleft (- maxchars (count command))
            ws (apply str (repeat padleft " "))
            summary (:summary (meta fn))]
        (println
          (str "  " command ws " " summary))))))

(defn printcommandhelp [command]
  (let [fn (get commands command)
        meta (meta fn)
        docs (:doc meta)]
    (cond
      (not fn) (println (str "Command '" command "' not found."))
      (not docs) (println (str "No documentation for command '" command "'."))
      :else (println docs))))

(defn printhelp [{commands :_}]
  (if (<= (count commands) 0)
    (do
      (println "usage: biblecli <command> [<args>]")
      (printallcommands))
    (doseq [command commands]
      (printcommandhelp command))))

(def commands
  {"bucketsync"    #'biblecli.commands.bucketsync/bucketsync
   "help"          #'printhelp
   "normalize"     #'biblecli.commands.normalize/normalize
   "prepare"       #'biblecli.commands.prepare/prepare!
   "serve"         #'biblecli.commands.serve/serve
   "static"        #'biblecli.commands.staticpages/prepare!
   "verseoftheday" #'biblecli.commands.verseoftheday/prepare!})

(defn parse-commandline [args opts]
  (let [opts (if opts opts #js{})
        args (if (nil? args)
               #js[]
               (clj->js args))
        minimist (require "minimist")
        processed-args (minimist args opts)]
    (js->clj processed-args :keywordize-keys true)))

(defn- main [command & args]
  (enable-util-print!)
  (try
    (let [fn (commands command)
          opts (clj->js (:cmdline-opts (meta fn)))
          args (parse-commandline args opts)]
      (if fn
        (fn args)
        (throw (str "Failed to find command '" command "'."))))
    (catch :default e
      (do
        (prn e)
        (prn (.-stack e))
        (.exit process 1)))))

(set! *main-cli-fn* main)