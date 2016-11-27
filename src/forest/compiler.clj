(ns forest.compiler
  (:require [clojure.string :as s]

            [forest.selectors :as selectors]))

(def default-options {:name-mangler :default})

(defn compile-selectors [mangler selectors]
  (s/join ",\n"
          (map (partial selectors/serialize-selector mangler)
               selectors)))


(defn compile-declaration [declaration]
  (let [[prop value] declaration
        lhs (str "  " (name prop) ": ")]
    `(str ~lhs ~value)))


(defn valid-property? [declaration]
  (let [[prop _] declaration]
    (not (#{:composes} prop))))


(defn compile-declaration-block [block]
  `(s/join ";\n" ~(mapv compile-declaration
                        (filter valid-property? block))))


(defn validate-for-composition [selectors]
  (when (not (every? selectors/selector-valid-for-composition? selectors))
    (throw (Exception.
            (str ":composes can only be used with basic selectors, error with "
                 selectors)))))


(defn compile-ruleset [mangler ruleset]
  (let [selectors (compile-selectors mangler
                                     (butlast ruleset))
        declaration-block (compile-declaration-block (last ruleset))]
    (when (:composes (last ruleset))
      (validate-for-composition (butlast ruleset)))
    `(str ~selectors
          "\n{\n"
          ~declaration-block
          "\n}")))


(defn compile-stylesheet [mangler stylesheet]
  `(s/join "\n\n" ~(mapv (partial compile-ruleset mangler)
                         stylesheet)))


(defn default-mangler [style-id x]
  (str style-id "__" x))


(defn identity-mangler [style-id x]
  x)


(defn make-wrap-mangler [[prefix suffix]]
  (fn [style-id x]
    (str prefix x suffix)))


(defn selector-defs-in-rule [mangler composition [ident-type ident]]
  (let [name (symbol (subs ident 1))
        value (subs (selectors/serialize-selector mangler ident) 1)

        value-code
        (cond (or (vector? composition) (seq? composition))
              `(str (clojure.string/join " " ~composition) " " ~value)

              composition `(str ~composition " " ~value)
              :else value)]
    [name value-code]))


(defn selector-defs-in-ruleset [mangler ruleset]
  (let [identifiers (set
                     (apply concat (map selectors/identifiers-in-selector (butlast ruleset))))
        composition (:composes (last ruleset))]
    (map (partial selector-defs-in-rule mangler composition) identifiers)))


(defn selector-defs-in-stylesheet [mangler stylesheet]
  (reduce (fn [acc ruleset]
            (merge acc (into {}
                             (selector-defs-in-ruleset mangler ruleset))))
          {}
          stylesheet))


(defn make-selector-defs [mangler stylesheet]
  (map (fn [[name value]] `(def ~name ~value))
       (selector-defs-in-stylesheet mangler stylesheet)))


(defn make-mangler [name-mangler]
     (cond (= name-mangler :default) default-mangler
           (= name-mangler :identity) identity-mangler

           (and (vector? name-mangler) (= (first name-mangler) :wrap))
           (make-wrap-mangler (rest name-mangler))

           :else (assert false "Can't create name mangler")))


(defn do-defstylesheet [id options stylesheet]
     (let [update-stylesheet-fn (symbol "forest.runtime" "update-stylesheet!")
           full-name (str (symbol (str *ns*) (name id)))
           mangler (partial (make-mangler (:name-mangler options)) full-name)
           selector-defs (make-selector-defs mangler stylesheet)
           stylesheet-source (compile-stylesheet mangler stylesheet)]
       `(do
          ~@selector-defs
          (def ~id {:full-name ~full-name
                    :source ~stylesheet-source})
          (~update-stylesheet-fn ~id))))
