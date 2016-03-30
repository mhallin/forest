(ns forest.test.integration
  (:require [cljs.test :refer-macros [deftest testing is are]]
            [forest.macros :as forest :include-macros true]
            [forest.runtime :as fr]

            [forest.test.utils :as utils]))

(forest/defstylesheet testing
  {:name-mangler [:wrap "test__" "__test"]}

  [.basic-1 {:font-weight "bold"
             :font-size "12px"}]
  [.basic-1::before {:font-size "14px"}]
  [.basic-2 {:text-transform "uppercase"}]

  [.extend-1 {:composes basic-1
              :float "left"}]
  [.extend-2 {:composes extend-1
              :background-image "url(data:1)"}]

  [.extend-multiple {:composes [basic-1 basic-2]}])


(forest/defstylesheet other-testing
  [.extend-external {:composes [basic-1 basic-2]}])


(defn compute-styles [e pseudo-element]
  (let [body (js/document.querySelector "body")]
    (.appendChild body e)
    (let [style (js/window.getComputedStyle e pseudo-element)]
      style)))

(defn element-with-class [class]
  (let [e (js/document.createElement "div")]
    (set! (.-className e) class)
    e))

(defn applied-style [class pseudo-element]
  (fr/update-stylesheet! testing)
  (let [elem (element-with-class class)
        style (compute-styles elem pseudo-element)]
    style))

(deftest environment
  (testing "Environment is sound"
    (is (some? (js/document.querySelector "body")))))

(deftest stylesheet
  (testing "Exported names"
    (is (= "test__basic-1__test" basic-1))
    (is (= "test__basic-1__test test__extend-1__test" extend-1))
    (is (= "test__basic-1__test test__extend-1__test test__extend-2__test" extend-2))
    (is (= "test__basic-1__test test__basic-2__test test__extend-multiple__test" extend-multiple))
    (is (some? testing)))

  (testing "Applied styles"
    (are [class accessor expected-value]
        (= (accessor (applied-style class nil)) expected-value)

      basic-1 .-fontWeight "bold"
      basic-1 .-fontSize "12px"
      basic-1 .-float "none"

      basic-2 .-fontWeight "normal"
      basic-2 .-textTransform "uppercase"

      extend-1 .-fontWeight "bold"
      extend-1 .-float "left"

      extend-2 .-fontWeight "bold"
      extend-2 .-float "left"
      extend-2 .-backgroundImage "url(data:1)"

      extend-multiple .-fontWeight "bold"
      extend-multiple .-float "none"
      extend-multiple .-textTransform "uppercase"

      extend-external .-fontWeight "bold"
      extend-external .-float "none"
      extend-external .-textTransform "uppercase"))

  (testing "Applied pseudo element styles"
    (are [class accessor expected-value]
        (= (accessor (applied-style class "::before")) expected-value)

      basic-1 .-fontSize "14px")))
