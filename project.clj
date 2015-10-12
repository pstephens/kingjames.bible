(defproject everlastingbible "0.1.0-SNAPSHOT"
  :description "King James Version written as a client side single page application."
  :url "http://www.everlastingbible.com"
  :license {:name "Apache 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"
            :distribution :repo}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.122"
                  :exclusion [org.clojure/data.json]]
                 [org.clojure/data.json "0.2.6"]
                 [com.cognitect/transit-cljs "0.8.225"]]
  :npm {:dependencies [[source-map-support "0.3.2"]]}

  :jvm-opts ^:replace ["-Xmx1g" "-server"]
  :plugins [[lein-npm "0.6.1"]
            [lein-cljsbuild "1.1.0"]]

  :clean-targets ["out" "release" "target" "node_modules"]
  :target-path "target"
  :source-paths ["src"]

  :cljsbuild {
    :builds [
      {:id "test"
       :source-paths ["src"]
       :notify-command ["node" "nodetest.js"]
       :compiler {
        :output-to "out/nodetests.js"
        :output-dir "out"
        :target :nodejs
        :optimizations :none
        :source-map true}}]})