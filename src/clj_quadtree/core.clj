(ns clj-quadtree.core
  (:require [clj-quadtree.geom :as geom]
            [cljts.relation :as rel]
            [cljts.prep :as prep]
            [clojure.core.reducers :as r])
  (:use clj-quadtree.hilbert
        [clojure.core.memoize]))

;; (def default-config {:cache-method memo-lru
;;                      :cache-size* 10000})
(def default-config {:cache-method memo
                     :cache-size* nil
                     :depth 15
                     :tile-size 64})

(defn- create-root [^long depth ^long tile-size]
    {:level 0
     :x 0
     :y 0
     :side (geom/quadtree-side depth tile-size)})

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

(defn- search-quads* [create-node-fn ^long depth root s]
  (let [ps (prep/prepare s) ]
    (loop [quads (create-children create-node-fn root)
           result []
           lvl 1]
      (let [candidates (r/filter #(rel/intersects? ps (shape %)) quads)]
        (if (= lvl depth)
          (into result candidates)
          (let [{covered true touched false}
                (group-by #(rel/covers? ps (shape %)) candidates)
                candidates (r/flatten
                            (r/map
                             (partial create-children create-node-fn) touched))]
            ;; don't need to check the covered anymore but touched can
            ;; be subdivided
            (recur candidates (into result covered) (inc lvl))))))))

(defn- memoize-fn [fn cache-method cache-size]
  (if (nil? cache-size)
    (cache-method fn)
    (cache-method fn cache-size)))

(defn create-search-fn [{:keys [cache-method cache-size depth tile-size]}]
  (let [root (create-root depth tile-size)
        create-node-fn (memoize-fn create-node* cache-method cache-size)]
    (partial search-quads* create-node-fn depth root)))

(def search
  (create-search-fn default-config))

(defn- children-tile-ids* [parent depth tile-size]
  (let [[px py] (coords parent)
        pside (side parent)
        max-x (+ px pside)
        max-y (+ py pside)
        npx (quot px tile-size)
        npy (quot py tile-size)
        nmx (quot max-x tile-size)
        nmy (quot max-y tile-size)]
    (for [nx (range npx nmx)
          ny (range npy nmy)]
      (xy->hilbert nx ny depth))))

(defn make-ranges [ids]
  (loop [start (first ids)
         others (rest ids)
         prev start
         result []
         new-range false]
    (let [cur (first others)]
      (if (empty? others)
        (if (or new-range (= start prev))
          (conj result prev)
          (conj result [start prev]))
        (if (= 1 (- cur prev))
          (recur start (rest others) cur result false)
          (recur (first others) (rest others) cur
                 (conj result
                       (if (= start prev)
                         prev
                         [start prev])) true))))))

(defn- tile-ids [{:keys [depth tile-size]} children-tile-ids-fn r]
  (-> (map (fn [node]
                  (if (< (level node) depth)
                    (children-tile-ids-fn node depth tile-size)
                    (id node))) r) flatten sort make-ranges))

(defn create-search-ids-fn [{:keys [cache-method cache-size] :as config}]
  (let [search-fn (create-search-fn config)
        children-tile-ids-fn (memoize-fn children-tile-ids* cache-method cache-size)]
    (comp (partial tile-ids config children-tile-ids-fn) search-fn)))

(def find-ids (create-search-ids-fn default-config))
