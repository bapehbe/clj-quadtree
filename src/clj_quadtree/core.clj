(ns clj-quadtree.core
  (:require [clj-quadtree.geom :as geom]
            [cljts.relation :as rel])
  (:use clj-quadtree.hilbert
        [clojure.core.memoize]))

;; (def default-config {:cache-method memo-lru
;;                      :cache-size* 10000})
(def default-config {:cache-method memo
                     :cache-size* nil})

(defn- create-root [^long depth]
  (let [side (bit-shift-left 1 depth)]
    {:level 0
     :x 0
     :y 0
     :side side}))

(defn- create-node* [^long level ^long x ^long y ^long side]
  (let [data  {:level level
               :x x
               :y y
               :side side}
        norm-x (quot x side)
        norm-y (quot y side)]
    (-> data
        (assoc :shape (geom/quad->shape data))
        (assoc :id (xy->hilbert norm-x norm-y level)))))

(defn id [node]
  (:id node))

(defn level [node]
  (:level node))

(defn side [node]
  (:side node))

(defn coords [node]
  [(:x node) (:y node)])

(defn shape [node]
  (:shape node))

(defn- create-children [create-node-fn node]
  (let [nlvl (-> node level inc)
        ns (bit-shift-right (side node) 1)
        [x y] (coords node)
        nwest (create-node-fn nlvl x y ns)
        neast (create-node-fn nlvl (+ x ns) y ns)
        swest (create-node-fn nlvl x (+ y ns) ns)
        seast (create-node-fn nlvl (+ x ns) (+ y ns) ns)]
    (sort-by :id [nwest neast swest seast])))

(defn- search-quads* [create-node-fn ^long depth s]
  (let [root (create-root depth)]
    (loop [quads (create-children create-node-fn root)
           lvl 1]
      (let [candidates (filter #(rel/intersects? s (shape %)) quads)]
        (if (= lvl depth)
          candidates
          (let [candidates (flatten
                            (map
                             (partial create-children create-node-fn) candidates))]
            (recur candidates (inc lvl))))))))

(defn- memoize-fn [fn cache-method cache-size]
  (if (nil? cache-size)
    (cache-method fn)
    (cache-method fn cache-size)))

(defn create-search-fn [{:keys [cache-method cache-size depth]}]
  (let [create-node-fn (memoize-fn create-node* cache-method cache-size)]
    (partial search-quads* create-node-fn)))

(def search-quads
  (create-search-fn default-config))
