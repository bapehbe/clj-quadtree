(ns clj-quadtree.core
  (:require [clojure.core.reducers :as r]
            [clj-quadtree.geom :as geom]
            [clojure.zip :as zip]
            [cljts.relation :as rel])
  (:use [clj-quadtree.hilbert]))

(defn- create-node [level x y w h]
  (let [data  {:level level
               :x x
               :y y
               :width w
               :height h}]
    (-> data
        (assoc :shape (geom/quad->shape data))
        (assoc :id (xy->hilbert x y level)))))

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
      (println id)
      (let [candidates (filter #(rel/intersects? s (shape %)) quads)]
        (println (map id candidates))
        (if (= lvl depth)
          ;; (apply sorted-set-by
          ;;        (fn [a b]
          ;;          (compare (id a) (id b)))
          ;;        candidates)
          candidates
          (let [candidates (flatten (map create-children candidates))]
            (recur candidates (inc lvl))))))))

(defn search-ids [depth w h s]
  (map id (search-quads depth w h s)))