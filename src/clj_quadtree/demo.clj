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

(defn draw-search [depth s]
  (let [result (search-quads depth s)
        line (g/line-string
              (flatten (map #(-> % shape g/centroid g/coordinates) result)))]
    (apply draw 800 800 s line (map shape result))))

;; (require '[cljts.analysis :as a])
;; (def c1 (a/buffer (g/point (g/c 43 46)) 18))
;; (draw-search 12 c1)
