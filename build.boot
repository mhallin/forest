(set-env!
 :source-paths #{"src"}
 :resource-paths #{"src"}
 :dependencies '[[org.clojure/clojurescript   "1.7.228"]
                 [adzerk/boot-cljs            "1.7.228-1" :scope "test"]
                 [adzerk/boot-test            "1.1.1"     :scope "test"]
                 [crisptrutski/boot-cljs-test "0.2.1"     :scope "test"]])

(require '[adzerk.boot-cljs            :refer [cljs]])
(require '[adzerk.boot-test            :refer [test]])
(require '[crisptrutski.boot-cljs-test :refer [test-cljs]])

(task-options! pom {:project 'forest :version "0.1.0"})

(deftask testing []
  (task-options! test-cljs {:js-env :phantom
                            :cljs-opts {:optimizations :none}
                            :namespaces [#"forest\.test\..*"]})
  (merge-env! :source-paths #{"test"})
  identity)

(deftask test-all []
  (comp (testing)
        (test)
        (test-cljs)))
