(ns forest.test.selectors
  (:require [clojure.test :refer [is are deftest testing]]

            [forest.selectors :as selectors]

            [forest.test.utils :refer [test-selector-mangler]]))

(def invalid-selector ["invalid"])

(deftest selectors
  (testing "Selector safe names"
    (are [x y] (= (selectors/selector-safe x) y)
      ".class-name" "_class-name"
      "a.b/item" "a_b_item"
      ".class-name:hover" "_class-name:hover"))

  (testing "Normalized selectors"
    (are [x y] (= (selectors/normalize-selector x) y)
      ".class-name" ".class-name"
      '.class-name ".class-name"
      :#id "#id"
      :element "element")

    (is (thrown? Exception (selectors/normalize-selector invalid-selector))))

  (testing "Selector classification"
    (are [x y] (= (selectors/selector-kind x) y)
      ".class-name" :class
      "#id" :id
      "element" :element)

    (is (thrown? Exception (selectors/selector-kind invalid-selector))))

  (testing "Selector mangling"
    (are [x y] (= (selectors/mangle-selector test-selector-mangler x) y)
      ".class-name" ".test__class-name"
      "#id" "#id"
      "element" "element")

    (is (thrown? Exception (selectors/mangle-selector test-selector-mangler
                                                      invalid-selector))))

  (testing "Selector serialization"
    (are [x y] (= (selectors/serialize-selector test-selector-mangler x) y)
      ".class-name" ".test__class-name"
      '.class-name ".test__class-name"
      :.class-name ".test__class-name"
      "#id" "#id"
      :#id "#id"
      "element" "element"
      'element "element"
      :element "element")

    (is (thrown? Exception (selectors/serialize-selector test-selector-mangler
                                                         invalid-selector)))))
