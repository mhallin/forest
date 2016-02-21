(ns forest.macros
  (:require #?(:clj [forest.selectors :as selectors])
            #?(:clj [forest.compiler :as compiler])
            #?(:cljs [forest.runtime :as runtime])))

(defn mangler [style-id x]
  (str style-id "__" x))

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
   (defn do-defstylesheet [id stylesheet]
     (let [update-stylesheet-fn (symbol "forest.runtime" "update-stylesheet!")
           full-name (str (symbol (str *ns*) (name id)))
           mangler (partial mangler full-name)
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
   (defn do-defstylesheet [_ _]))

(defmacro defstylesheet [name & body]
  (do-defstylesheet name body))
