(ns clj-quadtree.core-test
  (:use clj-quadtree.core
        midje.sweet)
  (:require [cljts.geom :as g]
            [cljts.analysis :as a]))

(fact "quads are ordered by their hilbert id"
 (let [create-children (partial #'clj-quadtree.core/create-children
                                #'clj-quadtree.core/create-node*)
       root (#'clj-quadtree.core/create-root 4 2)
       lvl1 (create-children root)
       lvl2 (flatten (map #(create-children %) lvl1))]
   (map id lvl1) => '(0 1 2 3)
   (map id lvl2) => (seq (range 16))))

(fact
 (:side (#'clj-quadtree.core/create-root 4 2)) => 32)

(fact "search for a point inside a quad will return this quad"
 (let [search (create-search-fn {:cache-method clojure.core.memoize/memo
                                 :depth 1
                                 :tile-size 4})
       p1 (g/point (g/c 2 2))
       result (search p1)]
   (count result) => 1
   (coords (first result)) => '[0 0]))

(fact "search for a line intersecting 2 quads will return these quads"
 (let [search (create-search-fn {:cache-method clojure.core.memoize/memo
                                 :depth 1
                                 :tile-size 4})
       l1 (g/line-string [(g/c 2 2) (g/c 6 2)])
       result (search l1)]
   (count result) => 2
   (coords (first result)) => '[0 0]
   (coords (second result)) => '[4 0]))

(fact "search for a circle intersecting 4 quads will return these quads"
 (let [search (create-search-fn {:cache-method clojure.core.memoize/memo
                                 :depth 1
                                 :tile-size 4})
       c1 (a/buffer (g/point (g/c 3 3)) 2)
       result (search c1)]
   (count result) => 4))