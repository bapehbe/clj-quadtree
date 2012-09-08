(ns clj-quadtree.core-test
  (:use clj-quadtree.core
        midje.sweet))

(fact
 (let [root (#'clj-quadtree.core/create-root 1000 1500)
       lvl1 (#'clj-quadtree.core/create-children root)
       lvl2 (flatten (map #(#'clj-quadtree.core/create-children %) lvl1))]
   (map id lvl1) => '(0 1 2 3)
   (map id lvl2) => (seq (range 16))))
