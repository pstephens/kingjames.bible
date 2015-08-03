(defproject everlastingbible "0.1.0-SNAPSHOT"
  :description "King James Version written as a client side single page application."
  :url "http://www.everlastingbible.com"
  :license {:name "Apache 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"
            :distribution :repo}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.28" :classifier "aot"
                  :exclusion [org.clojure/data.json]]
                 [org.clojure/data.json "0.2.6" :classifier "aot"]
                 [com.cognitect/transit-cljs "0.8.220"]]
  :jvm-opts ^:replace ["-Xmx1g" "-server"]
  :node-dependencies [[source-map-support "0.3.2"]]
  :plugins [[lein-npm "0.5.0" :exclusions [org.clojure/clojure]]]
  :clean-targets ["out" "release"]
  :target-path "target")