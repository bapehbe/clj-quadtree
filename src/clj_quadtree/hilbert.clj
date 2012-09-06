(ns clj-quadtree.hilbert)

(def ^:private hilbert-map
  {:a {'(0 0) '(0 :d) '(0 1) '(1 :a) '(1 0) '(3 :b) '(1 1) '(2 :a)}
   :b {'(0 0) '(2 :b) '(0 1) '(1 :b) '(1 0) '(3 :a) '(1 1) '(0 :c)}
   :c {'(0 0) '(2 :c) '(0 1) '(3 :d) '(1 0) '(1 :c) '(1 1) '(0 :b)}
   :d {'(0 0) '(0 :a) '(0 1) '(3 :c) '(1 0) '(1 :d) '(1 1) '(2 :d)}})

(defn xy->hilbert [x y lvl]
  (loop [sqr :a
         pos 0
         n (dec lvl)]
    (if (>= n 0)
      (let [qx (if (bit-test x n) 1 0)
            qy (if (bit-test y n) 1 0)
            [qpos sqr] (get-in hilbert-map [sqr (list qx qy)])
            pos (bit-or (bit-shift-left pos 2) qpos)]
        (recur sqr pos (dec n)))
      pos)))

(defn hilbert-plane [lvl]
  (let [n (bit-shift-left 1 lvl)]
    (sort-by
     #(xy->hilbert (first %) (second %) lvl)
     (for [x (range n) y (range n)] [x y]))))

;;; uncomment the below to get nice hilbert plane drawn
;; (require '[cljts.geom :as g])
;; (use 'clj-quadtree.demo)
;; (draw 700 700 (g/line-string (map #(apply g/c %) (hilbert-plane 6))))
