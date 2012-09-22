[![Build Status](https://secure.travis-ci.org/bapehbe/clj-quadtree.png)](http://travis-ci.org/bapehbe/clj-quadtree)

# clj-quadtree

Library for spatial indexing and search using quadtrees.
Check out [wiki](https://github.com/bapehbe/clj-quadtree/wiki/IAQ) for
detailed description.

## Usage

```clojure
;; Create a function for searching on a square divided into 2^15 tiles,
;; each side of which is 64
(use 'clj-quadtree.core)
(def search (create-search-fn {:cache-method clojure.core.memoize/memo
                  :depth 15
                  :tile-size 64}))

;; let's create some geometric shapes
(require '[cljts.geom :as g])
(require '[cljts.analysis :as a])
(def p1 (g/point (g/c 443 426))) ;; a point
(def c1 (a/buffer p1 341)) ;; a circle

;; let's do some search
;; find Hilbert coordinate of a tile containing the point defined above 
(tile-ids (search p1))
; user> [40]
;; find Hilbert coordinates of tiles which intersect the circle
;; defined above
(tile-ids (search c1))
; user> [[9 11] [17 18] [22 57] [61 62] [66 74] 97 [110 136] [139 144] [208 224] [226 231] 233]
;; the result contains coordinates (like 97 above) or ranges of
;; coordinates. For example, [22 57] means all positive integers from
;; 22 to 57 inclusive

;; See the search above in a picture which shows the tiles, the Hilbert
;; curve and the circle
(use 'clj-quadtree.demo)
(run-demo)
```
... to get this
![demo](https://github.com/bapehbe/clj-quadtree/raw/master/demo.png)
## License

Use it any way you want to, just make sure you send me bug fixes and
improvements, OK?
