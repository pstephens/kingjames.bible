(defproject kingjames.bible "0.1.0-SNAPSHOT"
  :description "King James Version for the web and beyond."
  :url "https://kingjames.bible"
  :license {:name "Apache 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"
            :distribution :repo}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.456"
                  :exclusion [org.clojure/data.json]]
                 [org.clojure/core.async "0.2.395"]
                 [org.clojure/data.json "0.2.6"]
                 [com.cognitect/transit-cljs "0.8.239"]
                 [hiccups "0.3.0"]]

  :npm {:dependencies [[aws-sdk "2.9.0"]
                       [del "2.2.2"]
                       [gulp "gulpjs/gulp#4.0"]
                       [gulp-uglify "2.0.1"]
                       [jasmine "2.5.3"]
                       [less "2.7.2"]
                       [lodash "4.17.4"]
                       [marked "0.3.6"]
                       [minimist "1.2.0"]
                       [mkdirp "0.5.1"]
                       [modernizr "3.3.1"]
                       [phantomjs2 "2.2.0"]
                       [source-map-support "0.4.11"]]}

  :jvm-opts ^:replace ["-Xmx2g" "-server"]
  :plugins [[lein-npm "0.6.2"]
            [lein-cljsbuild "1.1.5"]]

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
          :target :nodejs
          :optimizations :none
          :source-map true}}]})
