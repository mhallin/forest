(ns forest.compiler
  (:require [clojure.string :as s]

            [forest.selectors :as selectors]))

(defn compile-selectors [mangler selectors]
  (s/join ",\n"
          (map (partial selectors/serialize-selector mangler)
               selectors)))


(defn compile-declaration [declaration]
  (let [[prop value] declaration
        lhs (str "  " (name prop) ": ")]
    `(str ~lhs ~value)))


(defn compile-declaration-block [block]
  `(s/join ";\n" ~(mapv compile-declaration block)))


(defn compile-ruleset [mangler ruleset]
  (let [selectors (compile-selectors mangler
                                     (butlast ruleset))
        declaration-block (compile-declaration-block (last ruleset))]
    `(str ~selectors
          "\n{\n"
          ~declaration-block
          "\n}")))


(defn compile-stylesheet [mangler stylesheet]
  `(s/join "\n\n" ~(mapv (partial compile-ruleset mangler)
                         stylesheet)))
