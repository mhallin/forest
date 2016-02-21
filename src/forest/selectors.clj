(ns forest.selectors
  (:require [clojure.string :as s]))


(defn selector-safe [name]
  (s/replace (str name) #"[^-A-Za-z0-9]" "_"))

(defn normalize-selector [selector]
  (cond (symbol? selector) (name selector)
        (keyword? selector) (name selector)
        (string? selector) selector
        :else (throw (Exception. (str "Invalid selector: " selector)))))

(defn selector-kind [selector]
  (let [selector selector]
    (cond (and (string? selector) (= (first selector) \.)) :class
          (and (string? selector) (= (first selector) \#)) :id
          (string? selector) :element
          :else (throw (Exception. (str "Invalid selector: " selector))))))

(defn mangle-selector [mangler selector]
  (case (selector-kind selector)
    :class (str "." (selector-safe (mangler (subs selector 1))))
    :id (str "#" (selector-safe (mangler (subs selector 1))))
    selector))

(defn serialize-selector [mangler selector]
  (mangle-selector mangler (normalize-selector selector)))
