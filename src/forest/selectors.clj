(ns forest.selectors
  (:require [clojure.string :as s]))


(defn- selector-safe [name]
  (s/replace (str name) #"[^-A-Za-z0-9:]" "_"))

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

(defn- mangle-selector [mangler [kind selector]]
  (case kind
    :class (str "." (selector-safe (mangler (subs selector 1))))
    selector))

(defn- eat-initial-whitespace [data]
  (s/replace data #"[ \t\n]*" ""))

(defn- parse-element [data]
  (when-let [match (re-find #"^[-A-Za-z0-9]+" data)]
    [:element match]))

(defn- parse-id [data]
  (when-let [match (re-find #"^#[-A-Za-z0-9]+" data)]
    [:id match]))

(defn- parse-class-name [data]
  (when-let [match (re-find #"^\.[-A-Za-z0-9]+" data)]
    [:class match]))

(defn- parse-pseudo-class [data]
  (when-let [match (re-find #"^:[-A-Za-z0-9]+" data)]
    [:pseudo-class match]))

(defn- parse-pseudo-element [data]
  (when-let [match (re-find #"^::[-A-Za-z0-9]+" data)]
    [:pseudo-element match]))

(defn- parse-any [data]
  (first (filter some? (map (fn [p] (p data))
                            [parse-element
                             parse-id
                             parse-class-name
                             parse-pseudo-class
                             parse-pseudo-element
                             ]))))

(defn- parse-selector [selector]
  (loop [selector selector
         matches []]
    (if-let [match (parse-any (eat-initial-whitespace selector))]
      (recur (subs selector (count (second match)))
             (conj matches match))
      (if (empty? (eat-initial-whitespace selector))
        matches
        (throw (Exception. (str "Invalid selector: " selector)))))))

(defn serialize-selector [mangler selector]
  (s/join ""
          (map #(mangle-selector mangler %)
               (parse-selector (normalize-selector selector)))))
