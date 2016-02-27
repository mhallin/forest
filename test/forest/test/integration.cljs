(ns forest.test.integration
  (:require [cljs.test :refer-macros [deftest testing is]]
            [forest.macros :as forest :include-macros true]
            [forest.runtime :as fr]

            [forest.test.utils :as utils]))

(forest/defstylesheet testing
  {:name-mangler [:prefix "test__"]}
  [.class-name {:font-weight "bold"}])


(defn compute-styles [e]
  (let [body (js/document.querySelector "body")]
    (.appendChild body e)
    (let [style (js/window.getComputedStyle e)]
      style)))

(defn element-with-class [class]
  (let [e (js/document.createElement "div")]
    (set! (.-className e) class)
    e))

(deftest environment
  (testing "Environment is sound"
    (is (some? (js/document.querySelector "body")))))

(deftest stylesheet
  (testing "Exported names"
    (is (= "test__class-name" class-name))
    (is (some? testing)))

  (testing "Applied styles"
    (is (do
          (fr/update-stylesheet! testing)
          (= (let [elem (element-with-class class-name)
                   style (compute-styles elem)]
               (.-fontWeight style))
             "bold")))))
