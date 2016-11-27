(set-env!
 :source-paths #{"src"}
 :resource-paths #{"src"}
 :dependencies '[[org.clojure/clojurescript   "1.9.293"]
                 [adzerk/boot-cljs            "1.7.228-1" :scope "test"]
                 [adzerk/boot-test            "1.1.2"     :scope "test"]
                 [crisptrutski/boot-cljs-test "0.2.2"     :scope "test"]]
 :license {:name "MIT"
           :url "https://github.com/mhallin/forest/blob/master/LICENSE"})

(require '[adzerk.boot-cljs            :refer [cljs]])
(require '[adzerk.boot-test            :refer [test]])
(require '[crisptrutski.boot-cljs-test :refer [test-cljs]])

(task-options!
 pom {:project 'forest
      :version "0.2.1"
      :url "https://github.com/mhallin/forest"
      :description "CSS modules for ClojureScript"
      :scm {:developerConnection "scm:git:ssh://git@github.com:mhallin/forest.git"
            :connection "scm:git:git://github.com:mhallin/forest.git"
            :url "https://github.com/mhallin/forest"}
      :license {"MIT" "https://github.com/mhallin/forest/blob/master/LICENSE"}
      :developers {"Magnus Hallin" "mhallin@fastmail.com"}}

 push {:repo "clojars"})

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

(deftask deploy []
  (comp (pom)
        (jar)
        (push)))
