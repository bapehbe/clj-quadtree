(ns clj-quadtree.benchmarks.core
  (use perforate.core
       clj-quadtree.core)
  (require [cljts.analysis :as a]
           [cljts.geom :as g]))

(defgoal search-goal "search")
(def p1 (g/point (g/c 43 46)))
(def c1 (a/buffer p1 18))
(def search (create-search-fn {:cache-method clojure.core.memoize/memo
                               :depth 12
                               :tile-size 1}))

(defcase search-goal :point []
  (search p1))

(defcase search-goal :circle []
  (search c1))