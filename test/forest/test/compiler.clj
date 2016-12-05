(ns forest.test.compiler
  (:require [clojure.test :refer [is are deftest testing]]

            [forest.compiler :as compiler]

            [forest.test.utils :refer [test-selector-mangler]]))


(deftest compiler
  (testing "Selectors"
    (are [x y] (= (compiler/compile-selectors test-selector-mangler x) y)
      `[.class-name element] ".test__class-name__test,\nelement"
      `[:#id element] "#id,\nelement"))

  (testing "Declarations"
    (are [x y] (= (compiler/compile-declaration x) y)
      `[:font-size "12px"] "  font-size: 12px"
      `[:font-size some-value] `(str "  font-size: " some-value)
      `["transition" "width 1s linear"] "  transition: width 1s linear"))

  (testing "Declaration blocks"
    (are [x y] (= (eval (compiler/compile-declaration-block x)) y)
      `{:font-size "12px" :width "32px"}
      "  font-size: 12px;\n  width: 32px"))

  (testing "Rulesets"
    (are [x y] (= (eval (compiler/compile-ruleset test-selector-mangler x)) y)
      `[.class-name {:font-size "12px"}]
      ".test__class-name__test\n{\n  font-size: 12px\n}"

      `[.class-name h1 :#id {:font-size "12px"}]
      ".test__class-name__test,\nh1,\n#id\n{\n  font-size: 12px\n}"

      `[.class-name {:font-size "12px" :composes class-name}]
      ".test__class-name__test\n{\n  font-size: 12px\n}")

    (is (thrown-with-msg?
         Exception
         #":composes can only be used with basic selectors"
         (compiler/compile-ruleset
          test-selector-mangler
          `[(">" .base .sub) {:font-size "12px" :composes class-name}])))))
