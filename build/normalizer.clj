(require 'cljs.build.api)

(cljs.build.api/build "src" 
  {:main 'normalizer.core
   :output-to "normalizer.js"
   :target :nodejs})