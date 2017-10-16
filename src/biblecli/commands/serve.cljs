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
    [test.asset.testpages :as testpages]))

(def node-path (js/require "path"))

(defn serve
  {:summary "Serve static resources using HTTP presumably for integration testing."
   :doc "usage: biblecli serve [--port <port>] [--biblepath <path>] [--respath <path>]
   --port <port>        Web server port. Defaults to 7490.
   --biblepath <path>   Path to pre-computed bible resource files. Defaults to <root>/out/bible. See the 'prepare' command.
   --respath <path>     Path to compiled javascript resources. Defaults to <root>/out/dbg-web."
   :cmdline-opts {:string ["port" "biblepath" "respath"]
                  :default {:port "7490"
                            :biblepath nil
                            :respath nil}}}
  [{port :port biblepath :biblepath respath :respath}]
  (let [root-path (get-root-path)
        rel #(.join node-path root-path %)
        bible-dir (or biblepath (rel "out/bible"))
        res-dir (or respath (rel "out/dbg-web"))
        bible-meta-data (bible-res/read-bible-meta-data bible-dir)
        port (js/parseInt port)]
    (server/listen
      [(bible-res/resources bible-dir bible-meta-data)
       (dir/resources res-dir "/")
       (testpages/resources bible-meta-data)]
      port)))
