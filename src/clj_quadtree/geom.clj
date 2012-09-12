(ns clj-quadtree.geom
  (:use [cljts.geom :only [c linear-ring polygon]])
  (:import [com.vividsolutions.jts.geom.util
            AffineTransformation AffineTransformationBuilder]
           [com.vividsolutions.jts.geom Coordinate Geometry]))

(defn quad->shape [{:keys [x y side]}]
  (polygon (linear-ring (let [x1 (+ x side)
                              y1 (+ y side)
                              nw (c x y)
                              ne (c x1 y)
                              se (c x1 y1)
                              sw (c x y1)]
                          [nw ne se sw nw])) nil))

(defn- create-builder [c1 c2 c3 c4 c5 c6]
  (com.vividsolutions.jts.geom.util.AffineTransformationBuilder. c1 c2 c3 c4 c5 c6))

(defn- create-transformation [coordinates]
  (let [builder (apply create-builder (map #(apply c %) (partition 2 coordinates)))]
    (.getTransformation builder)))

(defprotocol Transformable
  (transform [this transformation]))

(extend-type Coordinate
  Transformable
  (transform [this transformation]
    (.transform transformation this (Coordinate.))))

(extend-type Geometry
  Transformable
  (transform [this transformation]
    (.transform transformation this)))

(defn- transformation-fn [tr]
  (fn [this]
      (transform this tr)))

(defn transformation [coordinates]
  (transformation-fn (create-transformation coordinates)))

(defn inverse-transformation [coordinates]
  (transformation-fn (.getInverse (create-transformation coordinates))))

(defn identity-transformation []
  (transformation-fn (.setToIdentity (AffineTransformation.))))
