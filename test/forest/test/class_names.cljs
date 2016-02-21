(ns forest.test.class-names
  (:require [cljs.test :refer-macros [is are deftest testing]]

            [forest.class-names :as class-names]))

(deftest classnames
  (testing "Joining string arguments"
    (are [x y] (= (apply class-names/class-names x) y)
      [] ""
      ["cls1"] "cls1"
      ["cls1" "cls2"] "cls1 cls2"))

  (testing "Joining nil/false arguments"
    (are [x y] (= (apply class-names/class-names x) y)
      [false] ""
      [nil] ""
      ["cls1" nil false] "cls1"))

  (testing "Joining a string -> boolean map"
    (are [x y] (= (apply class-names/class-names x) y)
      [{"cls1" true "cls2" false}] "cls1"
      [{"cls1" true} {"cls2" true}] "cls1 cls2"
      [{"cls1" false} {"cls2" true}] "cls2"))

  (testing "Flattening arrayed arguments"
    (are [x y] (= (apply class-names/class-names x) y)
      [[]] ""
      [[nil]] ""
      [["cls1"] ["cls2"]] "cls1 cls2"
      [[[[{["cls1" nil "cls2"] true} {"cls3" false}]]]] "cls1 cls2")))
