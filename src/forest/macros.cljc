(ns forest.macros
  (:require #?(:clj [forest.selectors :as selectors])
            #?(:clj [forest.compiler :as compiler])
            #?(:cljs [forest.runtime :as runtime])))

(defn default-mangler [style-id x]
  (str style-id "__" x))

(defn identity-mangler [style-id x]
  x)

(defn make-prefix-mangler [prefix]
  (fn [style-id x]
    (str prefix x)))

(def default-options {:name-mangler :default})

#?(:clj
   (defn make-selector-def [mangler selector]
     (let [name (symbol (subs selector 1))
           value (selectors/serialize-selector mangler selector)]
       `(def ~name ~(subs value 1)))))

#?(:clj
   (defn make-selector-defs [mangler ruleset]
     (let [selectors (filter #(#{:class :id} (selectors/selector-kind %))
                             (map selectors/normalize-selector
                                  (butlast ruleset)))
           defs (map (partial make-selector-def mangler) selectors)]
       `(do ~@defs))))

#?(:clj
   (defn make-mangler [name-mangler]
     (cond (= name-mangler :default) default-mangler
           (= name-mangler :identity) identity-mangler

           (and (vector? name-mangler) (= (first name-mangler) :prefix))
           (make-prefix-mangler (second name-mangler))

           :else (assert false "Can't create name mangler"))))

#?(:clj
   (defn do-defstylesheet [id options stylesheet]
     (let [update-stylesheet-fn (symbol "forest.runtime" "update-stylesheet!")
           full-name (str (symbol (str *ns*) (name id)))
           mangler (partial (make-mangler (:name-mangler options)) full-name)
           selector-defs (map (partial make-selector-defs mangler)
                              stylesheet)
           stylesheet-source (compiler/compile-stylesheet mangler
                                                          stylesheet)]
       `(do
          (def ~id {:full-name ~full-name
                    :source ~stylesheet-source})
          (~update-stylesheet-fn ~id)
          ~@selector-defs)))
   :cljs
   (defn do-defstylesheet [_ _ _]))

(defmacro defstylesheet [name & body]
  (let [has-options (map? (first body))
        options (merge default-options
                       (if has-options (first body) {}))
        body (if has-options (rest body) body)]
    (do-defstylesheet name options body)))
