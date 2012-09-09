(ns clj-quadtree.core
  (:require [clj-quadtree.geom :as geom]
            [cljts.relation :as rel])
  (:use clj-quadtree.hilbert
        [clojure.core.memoize]))

(def ^:dynamic *cache-method* memo-lru)
(def ^:dynamic *cache-size* 10000)

(defn- create-node [level x y w h]
  (let [data  {:level level
               :x x
               :y y
               :width w
               :height h}
        norm-x (/ x w)
        norm-y (/ y h)]
    (-> data
        (assoc :shape (geom/quad->shape data))
        (assoc :id (xy->hilbert norm-x norm-y level)))))

(def ^:private memo-create-node (*cache-method* create-node *cache-size*))

(defn- create-root [depth]
  (let [side (bit-shift-left 1 depth)]
    (memo-create-node 0 0 0 side side)))

(defn id [node]
  (:id node))

(defn level [node]
  (:level node))

(defn width [node]
  (:width node))

(defn height [node]
  (:height node))

(defn coords [node]
  [(:x node) (:y node)])

(defn shape [node]
  (:shape node))

(defn- halve [n]
  (bit-shift-right n 1))

(defn- create-children [node]
  (let [nlvl (-> node level inc)
        nw (halve (width node))
        nh (halve (height node))
        [x y] (coords node)
        nwest (memo-create-node nlvl x y nw nh)
        neast (memo-create-node nlvl (+ x nw) y nw nh)
        swest (memo-create-node nlvl x (+ y nh) nw nh)
        seast (memo-create-node nlvl (+ x nw) (+ y nh) nw nh)]
    (sort-by :id [nwest neast swest seast])))

(defn search-quads [depth s]
  (let [root (create-root depth)]
    (loop [quads (create-children root)
           lvl 1]
      (let [candidates (filter #(rel/intersects? s (shape %)) quads)]
        (if (= lvl depth)
          candidates
          (let [candidates (flatten (map create-children candidates))]
            (recur candidates (inc lvl))))))))

(defn search-ids [depth s]
  (map id (search-quads depth s)))