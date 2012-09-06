(ns clj-quadtree.geom-test
  (:use clj-quadtree.geom
        midje.sweet
        [cljts.geom :only [c linear-ring polygon]]))

(fact
 (quad->shape {:x 0 :y 0 :width 10 :height 10}) =>
 (polygon (linear-ring [(c 0 0)
                        (c 10 0)
                        (c 10 10)
                        (c 0 10)
                        (c 0 0)]) nil))