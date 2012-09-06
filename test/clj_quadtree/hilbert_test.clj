(ns clj-quadtree.hilbert-test
  (:use clj-quadtree.hilbert
        midje.sweet))

(fact
 (xy->hilbert 5 2 3) => 55)

(fact
 (hilbert-plane 1) => '([0 0] [0 1] [1 1] [1 0]))