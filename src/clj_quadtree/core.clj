(ns clj-quadtree.core
  (:require [clojure.core.reducers :as r]))

(defn- create-node [level x y w h]
  {:level level
   :x x
   :y y
   :width w
   :height h})

(defn get-level [node]
  (:level node))

(defn get-width [node]
  (:width node))

(defn get-height [node]
  (:height node))

(defn get-coords [node]
  [(:x node) (:y node)])

(defn get-children [node]
  (:children node))
  
(defn- create-children [node]
  (let [nlvl (-> node get-level inc)
        nw (-> node get-width (/ 2))
        nh (-> node get-height (/ 2))
        [x y] (get-coords node)
        nwest (create-node nlvl x y nw nh)
        neast (create-node nlvl (+ x nw) y nw nh)
        swest (create-node nlvl x (+ y nh) nw nh)
        seast (create-node nlvl (+ x nw) (+ y nh) nw nh)]
    [nwest neast swest seast]))

(defn- create-level [parent lvl maxdepth]
  (if (>= lvl maxdepth)
    parent
    (assoc parent :children
           (r/reduce conj [] (r/map #(create-level % (inc lvl) maxdepth) (create-children parent))))))

(defn create-tree [depth w h]
  (let [root (create-node 0 0 0 w h)]
    (create-level root 0 (dec depth))))
