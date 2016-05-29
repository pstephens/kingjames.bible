(ns reader.test)

(defn foo
  ;;#?(:cljs {:tag boolean})
  [a]
  a
  )