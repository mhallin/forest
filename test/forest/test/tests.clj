(ns forest.test.tests
  (:require [clojure.test :refer [is are deftest testing]]

            [forest.selectors :as selectors]
            [forest.compiler :as compiler]))

(defn test-selector-mangler [selector]
  (str "test__" selector))

(def invalid-selector ["invalid"])

(deftest selectors
  (testing "Selector safe names"
    (are [x y] (= (selectors/selector-safe x) y)
      ".class-name" "_class-name"
      "a.b/item" "a_b_item"))

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
      "#id" "#test__id"
      "element" "element")

    (is (thrown? Exception (selectors/mangle-selector test-selector-mangler
                                                      invalid-selector))))

  (testing "Selector serialization"
    (are [x y] (= (selectors/serialize-selector test-selector-mangler x) y)
      ".class-name" ".test__class-name"
      '.class-name ".test__class-name"
      :.class-name ".test__class-name"
      "#id" "#test__id"
      :#id "#test__id"
      "element" "element"
      'element "element"
      :element "element")

    (is (thrown? Exception (selectors/serialize-selector test-selector-mangler
                                                         invalid-selector)))))


(deftest compiler
  (testing "Selectors"
    (are [x y] (= (compiler/compile-selectors test-selector-mangler x) y)
      `[.class-name element] ".test__class-name,\nelement"
      `[:#id element] "#test__id,\nelement"))

  (testing "Declarations"
    (are [x y] (= (compiler/compile-declaration x) y)
      `[:font-size "12px"] `(str "  font-size: " "12px")
      `[:font-size some-value] `(str "  font-size: " some-value)
      `["transition" "width 1s linear"] `(str "  transition: "
                                              "width 1s linear")))

  (testing "Declaration blocks"
    (are [x y] (= (eval (compiler/compile-declaration-block x)) y)
      `{:font-size "12px" :width "32px"}
      "  font-size: 12px;\n  width: 32px"))

  (testing "Rulesets"
    (are [x y] (= (eval (compiler/compile-ruleset test-selector-mangler x)) y)
      `[.class-name {:font-size "12px"}]
      ".test__class-name\n{\n  font-size: 12px\n}"

      `[.class-name h1 :#id {:font-size "12px"}]
      ".test__class-name,\nh1,\n#test__id\n{\n  font-size: 12px\n}")))
