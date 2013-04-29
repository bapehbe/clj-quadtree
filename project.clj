(defproject clj-quadtree "0.1.0-SNAPSHOT"
  :description "spatial search using quadtree"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [cljts "0.2.0"]
                 [midje "1.4.0"]
                 [org.clojure/core.memoize "0.5.3"]]
  :profiles {:dev {:plugins [[lein-midje "2.0.0-SNAPSHOT"]
                             [perforate "0.3.2"]]}}
  :warn-on-reflection false)
