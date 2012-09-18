(ns clj-quadtree.demo
  (import com.vividsolutions.jts.awt.ShapeWriter
          [com.vividsolutions.jts.geom Geometry Polygon])
  (:use clj-quadtree.core)
  (:require [cljts.geom :as g]))

(defn apply-attributes [g options]
  (when-not (nil? options)
    (let [paint (:paint options)
          font (:font options)
          stroke (:stroke options)
          transform (:transform options)
          composite (:composite options)
          clip (:clip options)]
      (when paint (fn [paint] (.setPaint g paint)))
      (when font (fn [font] (.setFont g font)))
      (when stroke (fn [stroke] (.setStroke g stroke)))
      (when transform (fn [transform] (.setTransform g transform)))
      (when composite (fn [composite] (.setComposite g composite)))
      (when clip (fn [clip] (.setClip g clip))))))

(defprotocol Drawable
  (draw [this g options]))

(extend-type java.awt.Shape
  Drawable
  (draw [this g options]
    (.draw g this)))

(extend-type String
  Drawable
  (draw [this g options]
    (.drawString g this (-> options :x float) (-> options :y float))))

(def ^:private shape-writer (ShapeWriter.))

(defn drawable [shape]
  (.toShape shape-writer shape))

(defn stack-trace [e]
  (let [sw (java.io.StringWriter.)
        pw (java.io.PrintWriter. sw)]
    (do
      (.printStackTrace e pw)
      (.toString sw))))

(defn quad->label [quad]
  (let [[x y] (coords quad)
        c (g/centroid (shape quad))]
    (vector (str (id quad)) {:x (.getX c) :y (.getY c)})))

(defn with-graphics
  "restores graphics attributes after applying a function to it"
  [g f]
  (let [paint (.getPaint g)
        font (.getFont g)
        stroke (.getStroke g)
        transform (.getTransform g)
        composite (.getComposite g)
        clip (.getClip g)]
    (f g)
    (doto g
      (.setPaint paint)
      (.setFont font)
      (.setStroke stroke)
      (.setTransform transform)
      (.setComposite composite)
      (.setClip clip))))

(defn show [w h & drawables]
  (let [frame (javax.swing.JFrame. "demo")]
    (do 
      (.setContentPane frame
                       (doto (proxy [javax.swing.JPanel] []
                               (paintComponent [^java.awt.Graphics g]
                                 (let [g (doto ^java.awt.Graphics2D (.create g)
                                               (.setStroke (java.awt.BasicStroke. 0.4)))]
                                   (try
                                     (doseq [[drawable options] drawables]
                                       (with-graphics g (fn [g]
                                                          (apply-attributes g options)
                                                          (draw drawable g options))))
                                     (catch Exception e
                                       (javax.swing.JOptionPane/showMessageDialog
                                        frame (stack-trace e)))))))
                         (.setPreferredSize (java.awt.Dimension. w h))))
      (.pack frame)
      (.setVisible frame true))))

(defn ids-line
  "generates a line shape which connects centers of quads in the order of their ids"
  [qs]
  (g/line-string (flatten (map #(-> % shape g/centroid g/coordinates) qs))))

;; (require '[cljts.analysis :as a])
;; (let [p1 (g/point (g/c 443 426))
;;       c1 (a/buffer p1 341)
;;       search (create-search-fn {:cache-method clojure.core.memoize/memo
;;                                 :depth 15
;;                                 :tile-size 64})
;;       result (search c1)
;;       c1-draw [(drawable c1) {:paint java.awt.Color/RED}]
;;       quads-draw (map #(vector (-> % shape drawable) {:stroke (java.awt.BasicStroke. 1.0)}) result)
;;       labels-draw (map quad->label result)]
;;   (apply show 800 800 (conj (into quads-draw labels-draw) c1-draw)))
  
