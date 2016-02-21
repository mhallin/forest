(ns forest.class-names
  (:require [clojure.string :as s]))

(defn- truthy? [x]
  (and (not (nil? x)) (not (false? x))))

(defn- make-class-name [arg]
  (cond (not (truthy? arg)) nil

        (string? arg) arg

        (map? arg)
        (let [elems (filter (comp truthy? second) arg)]
          (if (empty? elems)
            nil
            (s/join " " (map (fn [[k _]] (make-class-name k))
                             elems))))

        (coll? arg)
        (s/join " " (filter truthy? (map make-class-name arg)))))

(defn class-names [& args]
  (or (make-class-name args) ""))
