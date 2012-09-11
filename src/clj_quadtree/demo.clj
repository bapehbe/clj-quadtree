(ns clj-quadtree.demo
  (import com.vividsolutions.jts.awt.ShapeWriter)
  (:use clj-quadtree.core)
  (:require [cljts.geom :as g]))

(defn draw [w h & shapes]
  (let [shape-writer (ShapeWriter.)]
    (doto (javax.swing.JFrame. "demo")
      (.setContentPane
       (doto (proxy [javax.swing.JPanel] []
               (paintComponent [^java.awt.Graphics g]
                 (let [g (doto ^java.awt.Graphics2D (.create g)
                               (.scale 10 10)
                               (.translate 1.5 1.5)
                               (.setStroke (java.awt.BasicStroke. 0.4)))]
                   (doseq [shape shapes]
                     (.draw g (.toShape shape-writer shape))))))
         (.setPreferredSize (java.awt.Dimension. w h))))
      .pack
      (.setVisible true))))

(defn ids-line
  "generates a line shape which connects centers of quads in the order of their ids"
  [qs]
  (g/line-string (flatten (map #(-> % shape g/centroid g/coordinates) qs))))

;; (require '[cljts.analysis :as a])
;; (def c1 (a/buffer (g/point (g/c 43 46)) 18))
;; (draw-quads (search-quads c1))
