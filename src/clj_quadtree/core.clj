(ns clj-quadtree.core
  (:require [clj-quadtree.geom :as geom]
            [cljts.relation :as rel])
  (:use clj-quadtree.hilbert
        [clojure.core.memoize]))

(def ^:dynamic *cache-method* memo-lru)
(def ^:dynamic *cache-size* 10000)

(defn- create-node [^long level ^long x ^long y ^long side]
  (let [data  {:level level
               :x x
               :y y
               :side side}
        norm-x (quot x side)
        norm-y (quot y side)]
    (-> data
        (assoc :shape (geom/quad->shape data))
        (assoc :id (xy->hilbert norm-x norm-y level)))))

(def ^:private memo-create-node (*cache-method* create-node *cache-size*))

(defn- create-root [^long depth]
  (let [side (bit-shift-left 1 depth)]
    (memo-create-node 0 0 0 side)))

(defn id [node]
  (:id node))

(defn level [node]
  (:level node))

(defn side [node]
  (:side node))

(defn coords [node]
  [(:x node) (:y node)])

(defn shape [node]
  (:shape node))

(defn- create-children [node]
  (let [nlvl (-> node level inc)
        ns (bit-shift-right (side node) 1)
        [x y] (coords node)
        nwest (memo-create-node nlvl x y ns)
        neast (memo-create-node nlvl (+ x ns) y ns)
        swest (memo-create-node nlvl x (+ y ns) ns)
        seast (memo-create-node nlvl (+ x ns) (+ y ns) ns)]
    (sort-by :id [nwest neast swest seast])))

(defn search-quads [^long depth s]
  (let [root (create-root depth)]
    (loop [quads (create-children root)
           lvl 1]
      (let [candidates (filter #(rel/intersects? s (shape %)) quads)]
        (if (= lvl depth)
          candidates
          (let [candidates (flatten (map create-children candidates))]
            (recur candidates (inc lvl))))))))

(defn search-ids [^long depth s]
  (map id (search-quads depth s)))