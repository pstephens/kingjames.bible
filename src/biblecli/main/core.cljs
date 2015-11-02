(ns biblecli.main.core
  (:require
    [cljs.nodejs :as nodejs]
    [biblecli.commands.normalize]))

(nodejs/enable-util-print!)

(def process nodejs/process)

(def commands
  {"normalize" biblecli.commands.normalize/normalize})

(defn- main [command & args]
  (try
    (let [cmd (commands command)]
      (if cmd
        (.exit process (apply cmd args))
        (throw (str "Failed to find command '" command "'."))))
    (catch js/Object e
      (do
        (.log js/console e)
        (.exit process 1)))))

(set! *main-cli-fn* main)