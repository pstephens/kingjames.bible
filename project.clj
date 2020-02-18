(defproject kingjames.bible "0.1.0-SNAPSHOT"
  :description "King James Version for the web and beyond."
  :url "https://kingjames.bible"
  :license {:name "Apache 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"
            :distribution :repo}

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.597"
                  :exclusion [org.clojure/data.json]]
                 [org.clojure/core.async "0.7.559"]
                 [org.clojure/data.json "0.2.7"]
                 [com.cognitect/transit-cljs "0.8.256"]
                 [hiccups "0.3.0"]]

  :npm {:dependencies [[aws-sdk "2.619.0"]
                       [del "5.1.0"]
                       [gulp "4.0.2"]
                       [gulp-uglify "3.0.2"]
                       [jasmine "3.5.0"]
                       [less "3.11.1"]
                       [lodash "4.17.15"]
                       [marked "0.8.0"]
                       [minimist "1.2.0"]
                       [mkdirp "1.0.3"]
                       [modernizr "3.9.0"]
                       [puppeteer "2.1.1"]
                       [source-map-support "0.5.16"]
                       [uglify-js "3.7.7"]]}

  :jvm-opts ^:replace ["-Xmx2g" "-server"]
  :plugins [[lein-npm "0.6.2"]
            [lein-cljsbuild "1.1.7"]]

  :clean-targets ["out" "release" "target" "node_modules"]
  :target-path "target"
  :source-paths ["src"]

  :cljsbuild {
    :builds [
      {
        :id "dbg"
        :source-paths ["src"]
        :notify-command ["node" "scripts/write-empty-file.js" "out/dbg/last-compiled.txt"]
        :compiler {
          :output-to "out/dbg/debug_refs.js"
          :output-dir "out/dbg"
          :main  "biblecli.main.core"
          :target :nodejs
          :optimizations :none
          :source-map true}}
       {:id "browser"
        :source-paths ["src"]
        :compiler {
          :output-to "out/dbg-web/debug_refs.js"
          :output-dir "out/dbg-web"
          :optimizations :none
          :source-map true}}]})
