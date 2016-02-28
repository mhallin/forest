(ns forest.macros
  (:require #?(:clj [forest.compiler :as compiler])
            #?(:cljs forest.runtime)))

#?(:clj
   (def do-defstylesheet compiler/do-defstylesheet)
   :cljs
   (defn do-defstylesheet [_ _ _]))

(defmacro defstylesheet [name & body]
  (let [has-options (map? (first body))
        options (merge #?(:clj compiler/default-options :cljs {})
                       (if has-options (first body) {}))
        body (if has-options (rest body) body)]
    (do-defstylesheet name options body)))
