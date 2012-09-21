(ns clj-quadtree.geom
  (:use [cljts.geom :only [c linear-ring polygon]]
        [cljts.transform :only [transformation inverse-transformation]])
  (:import [com.vividsolutions.jts.geom Coordinate Geometry]))

(defn quadtree-side [^long depth ^long tile-size-p]
  (bit-shift-left 1 (+ depth tile-size-p)))

(defn quad->shape [x y side]
  (polygon (linear-ring (let [x1 (+ x side)
                              y1 (+ y side)
                              nw (c x y)
                              ne (c x1 y)
                              se (c x1 y1)
                              sw (c x y1)]
                          [nw ne se sw nw])) nil))


(defn control-vectors [depth tile-size-p upper-left lower-left upper-right]
  (let [side (quadtree-side depth tile-size-p)]
    [(c 0 0) (c 0 side) (c side 0)
     upper-left lower-left upper-right]))

(defn from-binary-transform [depth tile-size-p upper-left lower-left upper-right]
  "creates a function for transforming coordinates from the quadtree coordinate
system to your coordinate system. You will need to provide the quadtree depth and tile size as well as coordinates of three corners of your coordinate system"
  (transformation
   (control-vectors depth tile-size-p upper-left lower-left upper-right)))

(defn to-binary-transform [depth tile-size-p upper-left lower-left upper-right]
  (inverse-transformation
   (control-vectors depth tile-size-p upper-left lower-left upper-right)))
