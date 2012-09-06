(ns clj-quadtree.demo
  (import com.vividsolutions.jts.awt.ShapeWriter))

(defn draw [w h shape]
  (let [shape-writer (ShapeWriter.)]
    (doto (javax.swing.JFrame. "demo")
      (.setContentPane
       (doto (proxy [javax.swing.JPanel] []
               (paintComponent [^java.awt.Graphics g]
                 (let [g (doto ^java.awt.Graphics2D (.create g)
                               (.scale 10 10)
                               (.translate 1.5 1.5)
                               (.setStroke (java.awt.BasicStroke. 0.4)))]
                   (.draw g (.toShape shape-writer shape)))))
         (.setPreferredSize (java.awt.Dimension. w h))))
      .pack
      (.setVisible true))))
