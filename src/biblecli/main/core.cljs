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
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.core.async :refer [<!]]
    [cljs.nodejs :refer [process enable-util-print!]]
    [biblecli.commands.bucketsync]
    [biblecli.commands.javascript]
    [biblecli.commands.markdown]
    [biblecli.commands.normalize]
    [biblecli.commands.prepare]
    [biblecli.commands.serve]
    [biblecli.commands.sitemap]
    [biblecli.commands.staticpages]
    [biblecli.commands.unittest]
    [biblecli.commands.verseoftheday]
    [biblecli.commands.unittest]
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
   "javascript"    #'biblecli.commands.javascript/javascript
   "markdown"      #'biblecli.commands.markdown/markdown
   "normalize"     #'biblecli.commands.normalize/normalize
   "prepare"       #'biblecli.commands.prepare/prepare
   "serve"         #'biblecli.commands.serve/serve
   "sitemap"       #'biblecli.commands.sitemap/sitemap
   "static"        #'biblecli.commands.staticpages/static
   "unittest"      #'biblecli.commands.unittest/runtests
   "verseoftheday" #'biblecli.commands.verseoftheday/verseoftheday})

(defn parse-commandline [args opts]
  (let [opts (if opts opts #js{})
        args (if (nil? args)
               #js[]
               (clj->js args))
        minimist (js/require "minimist")
        processed-args (minimist args opts)]
    (js->clj processed-args :keywordize-keys true)))

(defn run-command [fn isAsync args]
  (if isAsync
    (go
      (let [[err _] (<! (fn args))]
        (if err
          (do
            (println err)
            (.exit process 1)))))
    (fn args)))

(defn- main [command & args]
  (enable-util-print!)
  (try
    (let [fn (commands command)
          fnmeta (meta fn)
          opts (clj->js (:cmdline-opts fnmeta))
          isAsync (:async fnmeta)
          args (parse-commandline args opts)]
      (if fn
        (run-command fn isAsync args)
        (do
          (printcommandnotfound command)
          (printallcommands)
          (.exit process 1))))
    (catch :default e
      (do
        (println e)
        (println (.-stack e))
        (.exit process 1)))))

(set! *main-cli-fn* main)
