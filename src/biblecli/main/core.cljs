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