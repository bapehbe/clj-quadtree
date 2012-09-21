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
                     :tile-size-p 6})

(defn- create-root [^long depth ^long tile-size-p]
  {:level 0
   :nx 0
   :ny 0
   :side (geom/quadtree-side depth tile-size-p)})

(defn- create-node* [^long level ^long nx ^long ny ^long side]
  (let [x (* side nx)
        y (* side ny)
        data  {:level level
               :nx nx
               :ny ny
               :x x
               :y y
               :side side}]
    (-> data
        (assoc :shape (geom/quad->shape x y side))
        (assoc :id (xy->hilbert nx ny level)))))

(defn id [node]
  (:id node))

(defn level [node]
  (:level node))

(defn side [node]
  (:side node))

(defn coords [node]
  [(:x node) (:y node)])

(defn norm-coords [node]
  [(:nx node) (:ny node)])

(defn shape [node]
  (:shape node))

(defn- create-children [create-node-fn parent]
  (let [clvl (-> parent level inc)
        side (bit-shift-right (side parent) 1)
        [pnx pny] (norm-coords parent)
        cnx (bit-shift-left pnx 1)
        cny (bit-shift-left pny 1)
        nwest (create-node-fn clvl cnx cny side)
        neast (create-node-fn clvl (inc cnx) cny side)
        swest (create-node-fn clvl cnx (inc cny) side)
        seast (create-node-fn clvl (inc cnx) (inc cny) side)]
    (sort-by :id [nwest neast swest seast])))

(defn- children-tiles [create-node-fn depth parent]
  (let [[npx npy] (norm-coords parent)
        plvl (level parent)
        pside (side parent)
        pnside (bit-shift-left 1 (- depth plvl))
        side (bit-shift-right pside (- depth plvl))
        nsx (* npx pnside)
        nsy (* npy pnside)
        nmx (+ nsx pnside)
        nmy (+ nsy pnside)]
    (for [nx (range nsx nmx)
          ny (range nsy nmy)]
      (create-node-fn depth nx ny side))))

(defn- search-quads* [create-node-fn ^long depth root s]
  (let [ps (prep/prepare s)]
    (loop [quads (create-children create-node-fn root)
           result (transient [])
           lvl 1]
      (let [candidates (r/filter #(rel/intersects? ps (shape %)) quads)]
        (if (= lvl depth)
          (persistent! (reduce conj! result candidates))
          (let [{covered true touched false}
                (group-by #(rel/covers? ps (shape %)) candidates)
                candidates (r/flatten
                            (r/map
                             (partial create-children create-node-fn) touched))]
            ;; don't need to check the covered anymore but touched can
            ;; be subdivided. Just need to split the covered into tiles
            (recur candidates
                   (reduce conj! result
                           (r/flatten (r/map (partial children-tiles create-node-fn depth) covered)))
                   (inc lvl))))))))

(defn- memoize-fn [fn cache-method cache-size]
  (if (nil? cache-size)
    (cache-method fn)
    (cache-method fn cache-size)))

(defn create-search-fn [{:keys [cache-method cache-size depth tile-size-p]}]
  (let [create-node-fn (memoize-fn create-node* cache-method cache-size)
        root (create-root depth tile-size-p)]
    (partial search-quads* create-node-fn depth root)))

(def search
  (create-search-fn default-config))

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

(defn tile-ids [r]
  (make-ranges (sort (map id r))))
