(ns forest.selectors
  (:require [clojure.string :as s]))


(defn- selector-safe [name]
  (s/replace (str name) #"[^-A-Za-z0-9]" "_"))

(defn- normalize-selector [selector]
  (cond (symbol? selector) (name selector)
        (keyword? selector) (name selector)
        (string? selector) selector
        :else (throw (Exception. (str "Invalid selector: " selector)))))

(defn- selector-kind [selector]
  (let [selector selector]
    (cond (and (string? selector) (= (first selector) \.)) :class
          (and (string? selector) (= (first selector) \#)) :id
          (string? selector) :element
          :else (throw (Exception. (str "Invalid selector: " selector))))))

(defn- mangle-selector [mangler selector]
  (condp = [(:type selector) (:operator selector)]
    [:basic :class] (str "." (selector-safe (mangler (subs (:argument selector) 1))))
    [:basic :sequence] (s/join "" (map (partial mangle-selector mangler) (:argument selector)))
    [:combined 'descendant] (s/join " " (map (partial mangle-selector mangler) (:arguments selector)))
    [:combined '>] (s/join " > " (map (partial mangle-selector mangler) (:arguments selector)))
    (:argument selector)))

(defn- extract-identifiers [selector]
  (cond (and (= (:type selector) :basic)
             (#{:class :id} (:operator selector)))
        [[(:operator selector) (:argument selector)]]

        (= [(:type selector) (:operator selector)]
           [:basic :sequence])
        (apply concat (map extract-identifiers (:argument selector)))

        (= (:type selector) :combined)
        (apply concat (map extract-identifiers (:arguments selector)))))

(defn- eat-initial-whitespace [data]
  (s/replace data #"[ \t\n]*" ""))

(defn- parse-element [data]
  (when-let [match (re-find #"^[-A-Za-z0-9]+" data)]
    {:type :basic :operator :element :argument match}))

(defn- parse-id [data]
  (when-let [match (re-find #"^#[-A-Za-z0-9]+" data)]
    {:type :basic :operator :id :argument match}))

(defn- parse-class-name [data]
  (when-let [match (re-find #"^\.[-A-Za-z0-9]+" data)]
    {:type :basic :operator :class :argument match}))

(defn- parse-pseudo-class [data]
  (when-let [match (re-find #"^:[-A-Za-z0-9]+" data)]
    {:type :basic :operator :pseudo-class :argument match}))

(defn- parse-pseudo-element [data]
  (when-let [match (re-find #"^::[-A-Za-z0-9]+" data)]
    {:type :basic :operator :pseudo-element :argument match}))

(defn- parse-any [data]
  (first (filter some? (map (fn [p] (p data))
                            [parse-element
                             parse-id
                             parse-class-name
                             parse-pseudo-class
                             parse-pseudo-element
                             ]))))

(defn- parse-basic-selector [selector]
  (loop [selector (eat-initial-whitespace selector)
         matches []]
    (if-let [match (parse-any selector)]
      (recur (subs selector (count (:argument match)))
             (conj matches match))
      (if (empty? (eat-initial-whitespace selector))
        {:type :basic :operator :sequence :argument matches}
        (throw (Exception. (str "Invalid selector: " selector)))))))

(defn- parse-combined-selector [selector]
  (let [[op & args] selector]
    (when-not (#{'descendant '>} op)
      (throw (Exception. (str "Invalid selector combinator: " op))))
    {:operator op
     :type :combined
     :arguments (mapv #(parse-basic-selector (normalize-selector %))
                      args)}))

(defn- parse-selector [selector]
  (if (coll? selector)
    (parse-combined-selector selector)
    (parse-basic-selector (normalize-selector selector))))

(defn serialize-selector [mangler selector]
  (s/join ""
          (mangle-selector mangler (parse-selector selector))))

(defn identifiers-in-selector [selector]
  (extract-identifiers (parse-selector selector)))

(comment
  (serialize-selector identity ["invalid"])
  (serialize-selector #(str "X_" % "_X") '(> ".a" ".b"))
  (serialize-selector #(str "X_" % "_X") '(descendant ".a" ".b"))
  (serialize-selector #(str "X_" % "_X") ".class-name")

  (identifiers-in-selector ".class-name#hej")
  (identifiers-in-selector '(descendant ".a" ".b"))
  )
