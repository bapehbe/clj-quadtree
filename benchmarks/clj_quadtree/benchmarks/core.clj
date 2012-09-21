(ns clj-quadtree.benchmarks.core
  (use perforate.core
       clj-quadtree.core)
  (require [cljts.analysis :as a]
           [cljts.geom :as g]))

(defgoal search-goal "search")
(def p1 (g/point (g/c 43 46)))
(def c1 (a/buffer p1 18))

(def config {:cache-method clojure.core.memoize/memo
             :depth 12
             :tile-size-p 0})
(def no-cache-config {:cache-method identity
                      :depth 12
                      :tile-size-p 0})
(def cache-search (create-search-fn config))
(def no-cache-search (create-search-fn no-cache-config))

(defcase search-goal :point []
  (cache-search p1))

(defcase search-goal :circle []
  (cache-search c1))

(defcase search-goal :no-cache []
  (no-cache-search c1))

(defcase search-goal :tile-ids []
  (tile-ids (cache-search c1)))
