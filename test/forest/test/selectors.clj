(ns forest.test.selectors
  (:require [clojure.test :refer [is are deftest testing]]

            [forest.selectors :as selectors]

            [forest.test.utils :refer [test-selector-mangler]]))

(def invalid-selector ["invalid"])

(deftest selectors
  (testing "Selector serialization"
    (are [x y] (= (selectors/serialize-selector test-selector-mangler x) y)
      ".class-name" ".test__class-name__test"
      '.class-name ".test__class-name__test"
      :.class-name ".test__class-name__test"
      "#id" "#id"
      :#id "#id"
      "element" "element"
      'element "element"
      :element "element")

    (is (thrown? Exception (selectors/serialize-selector test-selector-mangler
                                                         invalid-selector))))

  (testing "Pseudo element serialization"
    (are [x y] (= (selectors/serialize-selector test-selector-mangler x) y)
      ".class-name:hover" ".test__class-name__test:hover"
      ".class-name::before" ".test__class-name__test::before")))
