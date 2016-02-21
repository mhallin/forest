(ns forest.runtime)

(defonce known-stylesheets (atom {}))

(defn- make-style-element []
  (let [elem (.createElement js/document "style")
        head (.querySelector js/document "head")]
    (assert (some? head)
            "A head element must be present for styles to be inserted")
    (.appendChild head elem)
    elem))

(defn- update-style-element! [element contents]
  (set! (.-innerHTML element) contents))

(defn- insert-style-element! [style-id contents]
  (let [elem (make-style-element)]
    (update-style-element! elem contents)
    (swap! known-stylesheets assoc style-id elem)))

(defn update-stylesheet! [stylesheet]
  (assert (some? stylesheet))
  (let [{:keys [full-name source]} stylesheet]
    (if-let [existing (@known-stylesheets full-name)]
      (update-style-element! existing source)
      (insert-style-element! full-name source))))
