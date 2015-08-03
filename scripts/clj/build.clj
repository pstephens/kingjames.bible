(require '[cljs.build.api :as b])

(println "Building...")

(defn makeNodeOpt [name]
  {:cache-analysis true
   :target :nodejs
   :optimizations :simple
   :verbose true
   :output-to (str "out/" name ".js")
   :output-dir (str "out/" name)
   :source-map (str "out/" name ".js.map")})

(let [start (System/nanoTime)]

  (b/build (b/inputs "src/common" "src/normalizer") (makeNodeOpt "normalizer"))
  (b/build (b/inputs "src/common" "test") (makeNodeOpt "nodetests"))

  (println "... done. Elapsed" (/ (- (System/nanoTime) start) 1e9) "seconds")
  (System/exit 0))