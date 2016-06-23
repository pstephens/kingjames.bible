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
    [biblecli.commands.verseoftheday]
    [biblecli.main.utility :as u]
    [clojure.string :as s]))

(declare commands)

(defn printallcommands []
  (let [maxchars (reduce #(max %1 (count %2)) 0 (keys commands))]
    (println "All commands:")
    (doseq [[command fn] commands]
      (let [padleft (- maxchars (count command))
            ws (apply str (repeat padleft " "))
            summary (:summary (meta fn))]
        (println
          (str "  " command ws "   " summary))))))

(defn printcommandnotfound [command]
  (println (str "Command '" command "' not found.")))

(defn replace-doc-params [str]
  (let [f #(case %1
                 "{{default-parser}}" (u/default-parser)
                 "{{default-parser-input}}" (u/default-parser-input-rel))]
    (if str
      (s/replace str #"\{\{[^\}]+\}\}" f)
      nil)))

(defn printcommandhelp [command]
  (let [fn (get commands command)
        meta (meta fn)
        docs (:doc meta)]
    (cond
      (not fn) (printcommandnotfound command)
      (not docs) (println (str "No documentation for command '" command "'."))
      :else (println (replace-doc-params docs)))))

(defn printhelp
  {:summary "Print out help for a command."
   :doc "usage: biblecli help [<command>]
   <command>   Print help information for this command. Lists all commands if none are specified."}
  [{commands :_}]
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
   "prepare"       #'biblecli.commands.prepare/prepare
   "serve"         #'biblecli.commands.serve/serve
   "static"        #'biblecli.commands.staticpages/static
   "verseoftheday" #'biblecli.commands.verseoftheday/verseoftheday})

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
        (do
          (printcommandnotfound command)
          (printallcommands))))
    (catch :default e
      (do
        (prn e)
        (prn (.-stack e))
        (.exit process 1)))))

(set! *main-cli-fn* main)