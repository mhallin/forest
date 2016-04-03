(ns forest.test.selectors
  (:require [clojure.test :refer [is are deftest testing]]

            [forest.selectors :as selectors]

            [forest.test.utils :refer [test-selector-mangler]]))

(def invalid-selector ["invalid"])

(def serialize-selector
  (partial selectors/serialize-selector test-selector-mangler))

(deftest selectors
  (testing "Basic selector serialization"
    (are [x y] (= (serialize-selector x) y)
      ".class-name" ".test__class-name__test"
      '.class-name ".test__class-name__test"
      :.class-name ".test__class-name__test"
      "#id" "#id"
      :#id "#id"
      "element" "element"
      'element "element"
      :element "element")

    (is (thrown? Exception (serialize-selector invalid-selector))))

  (testing "Pseudo element serialization"
    (are [x y] (= (serialize-selector x) y)
      ".class-name:hover" ".test__class-name__test:hover"
      ".class-name::before" ".test__class-name__test::before"))

  (testing "Chained class selectors"
    (are [x y] (= (serialize-selector x) y)
      ".a.b" ".test__a__test.test__b__test"))

  (testing "Combinator selector serialization"
    (are [x y] (= (serialize-selector x) y)
      '(descendant .upper .lower) ".test__upper__test .test__lower__test"
      '(> .upper .lower) ".test__upper__test > .test__lower__test")))
