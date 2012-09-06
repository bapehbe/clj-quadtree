(ns clj-quadtree.geom
  (:use [cljts.geom :only [c linear-ring polygon]]))

(defn quad->shape [{:keys [x y width height]}]
  (polygon (linear-ring (let [x1 (+ x width)
                              y1 (+ y height)
                              nw (c x y)
                              ne (c x1 y)
                              se (c x1 y1)
                              sw (c x y1)]
                          [nw ne se sw nw])) nil))
