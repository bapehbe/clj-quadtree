(ns clj-quadtree.hilbert-test
  (:use clj-quadtree.hilbert
        midje.sweet))

(fact
 (xy->hilbert 5 2 3) => 55)

(fact
 (hilbert-plane 1) => '([0 0] [0 1] [1 1] [1 0]))

(fact
 (let [d 8
       n (bit-shift-left 1 d)]
   (sort (for [x (range n)
               y (range n)] (xy->hilbert x y d))) => (range (* n n))))