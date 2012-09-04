(ns clj-quadtree.core-test
  (:use clojure.test
        clj-quadtree.core))

(deftest test-empty-root
  (testing "a tree with depth 1 contains only root"
    (let [root (create-tree 1 1 1)]
      (is (empty? (get-children root))))))

(deftest test-non-leaf-children
  (testing "non-leaf node contains 4 children"
    (let [root (create-tree 2 1 1)]
      (is (= 4 (-> root get-children count))))))

(deftest test-get-level
  (let [root (create-tree 2 1 1)
        achild (-> root get-children first)]
    (is (= 0 (get-level root)))
    (is (= 1 (get-level achild)))))