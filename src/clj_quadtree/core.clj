(ns clj-quadtree.core
  (:require [clj-quadtree.geom :as geom]
            [cljts.relation :as rel])
  (:use [clj-quadtree.hilbert]))

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

(defn- create-root [w h]
  (create-node 0 0 0 w h))

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

(defn- create-children [node]
  (let [nlvl (-> node level inc)
        nw (-> node width (/ 2))
        nh (-> node height (/ 2))
        [x y] (coords node)
        nwest (create-node nlvl x y nw nh)
        neast (create-node nlvl (+ x nw) y nw nh)
        swest (create-node nlvl x (+ y nh) nw nh)
        seast (create-node nlvl (+ x nw) (+ y nh) nw nh)]
    (sort-by :id [nwest neast swest seast])))

(defn search-quads [depth w h s]
  (let [root (create-node 0 0 0 w h)]
    (loop [quads (create-children root)
           lvl 1]
      (let [candidates (filter #(rel/intersects? s (shape %)) quads)]
        (if (= lvl depth)
          candidates
          (let [candidates (flatten (map create-children candidates))]
            (recur candidates (inc lvl))))))))

(defn search-ids [depth w h s]
  (map id (search-quads depth w h s)))