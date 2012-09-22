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
   (coords (first result)) => [0 0]
   (tile-ids result) => [0]))

(fact "search for a line intersecting 2 quads will return these quads"
 (let [search (create-search-fn {:cache-method clojure.core.memoize/memo
                                 :depth 1
                                 :tile-size 4})
       l1 (g/line-string [(g/c 2 2) (g/c 6 2)])
       result (search l1)]
   (count result) => 2
   (coords (first result)) => [0 0]
   (coords (second result)) => [4 0]
   (tile-ids result) => [0 3]))

(fact "search for a circle intersecting 4 quads will return these quads"
 (let [search (create-search-fn {:cache-method clojure.core.memoize/memo
                                 :depth 1
                                 :tile-size 4})
       c1 (a/buffer (g/point (g/c 3 3)) 2)
       result (search c1)]
   (count result) => 4
   (tile-ids result) => [[0 3]]))

(fact
 (let [p1 (g/point (g/c 443 426))
       c1 (a/buffer p1 341)
       search (create-search-fn {:cache-method clojure.core.memoize/memo
                                 :depth 15
                                 :tile-size 64})
       result (search c1)]
   (sort (map id result)) => '(9 10 11 17 18 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49 50 51 52 53 54 55 56 57 61 62 66 67 68 69 70 71 72 73 74 97 110 111 112 113 114 115 116 117 118 119 120 121 122 123 124 125 126 127 128 129 130 131 132 133 134 135 136 139 140 141 142 143 144 208 209 210 211 212 213 214 215 216 217 218 219 220 221 222 223 224 226 227 228 229 230 231 233)
   (tile-ids result) => [[9 11] [17 18] [22 57] [61 62] [66 74] 97 [110 136] [139 144] [208 224] [226 231] 233]))

(defn- random-ids [n]
  (reduce
   (fn [r k]
       (if (> 0.58 (rand))
         r
         (conj r k))) [0] (range 1 n)))

(fact
 "ids in ranges have start not= end"
 (let [ids (random-ids 1000000)]
   (every? (fn [t]
             (if (vector? t)
               (not= (first t) (second t))
               true)) (make-ranges ids)) => true))

(fact
 (let [ids (random-ids 1000000)]
   (flatten (map (fn [k]
                   (if (vector? k)
                     (range (first k) (inc (second k)))
                     k)) (make-ranges ids))) => ids))

(fact
 (make-ranges [1]) => [1])
