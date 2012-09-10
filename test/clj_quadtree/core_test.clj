(ns clj-quadtree.core-test
  (:use clj-quadtree.core
        midje.sweet))

(fact
 (let [create-children (partial #'clj-quadtree.core/create-children
                                #'clj-quadtree.core/create-node*)
       root (#'clj-quadtree.core/create-root 4)
       lvl1 (create-children root)
       lvl2 (flatten (map #(create-children %) lvl1))]
   (map id lvl1) => '(0 1 2 3)
   (map id lvl2) => (seq (range 16))))
