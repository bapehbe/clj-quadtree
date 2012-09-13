(ns clj-quadtree.geom-test
  (:use clj-quadtree.geom
        midje.sweet
        [cljts.geom :only [c linear-ring polygon]]))

(fact
 (quad->shape {:x 0 :y 0 :side 10}) =>
 (polygon (linear-ring [(c 0 0)
                                 (c 10 0)
                                 (c 10 10)
                                 (c 0 10)
                                 (c 0 0)]) nil))

(fact
 (let [quad->mercator
       (from-binary-transform 15 64
                              (c -20037508.342789244 20037508.342789244)
                              (c -20037508.342789244 -20037508.342789244)
                              (c 20037508.342789244 20037508.342789244))
       quad-coordinate (c 1267712 655616)
       mercator-coordinate (quad->mercator quad-coordinate)]
   (.x mercator-coordinate) => (roughly 4187526.157575097)
   (.y mercator-coordinate) => (roughly 7509173.658735715)))

(fact
 (let [mercator->quad
       (to-binary-transform 15 64
                              (c -20037508.342789244 20037508.342789244)
                              (c -20037508.342789244 -20037508.342789244)
                              (c 20037508.342789244 20037508.342789244))
       mercator-coordinate (c 4187594.340763207 7509144.10858847)
       quad-coordinate (mercator->quad mercator-coordinate)]
   (.x quad-coordinate) => (roughly 1267715.568071111)
   (.y quad-coordinate) => (roughly 655617.5463786547)))
