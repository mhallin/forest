========
 Forest
========

.. image:: https://img.shields.io/clojars/v/forest.svg
   :target: https://clojars.org/forest

.. image:: https://travis-ci.org/mhallin/forest.svg?branch=master
   :target: https://travis-ci.org/mhallin/forest

.. image:: https://jarkeeper.com/mhallin/forest/status.svg
   :target: https://jarkeeper.com/mhallin/forest

..

   CSS modules for ClojureScript.

----

Forest is a library that provides a Clojure DSL for CSS, similar to
Garden_ but with more limited expressivity and a stronger focus on
modularity.

Usage
=====

Add the following dependency to your ``project.clj`` or ``build.boot``:

.. image:: https://clojars.org/forest/latest-version.svg

The library is intended to be used through the ``defstylesheet``
macro, which at load-time injects the compiled stylesheet into the DOM
of the browser. Additionally, the macro exposes all CSS classes and
IDs as Clojure variables with the real name of the class or ID. By
using these variables instead of string literals, each stylesheet
becomes independent and won't conflict with anything else.

.. code-block:: clojure

   (ns sample
     (:require [forest.macros :refer-macros [defstylesheet]]))

   (defstylesheet my-stylesheet
     [.my-class {:font-weight "bold"}])

   my-class ;; => "<unique class name>"

   ;; This can be used with e.g. Om
   (dom/div {:className my-class} "Bold text")


   ;; Or with something like Sablono
   (html [:div {:class my-class} "Bold text"])


Class composition/inheritance
-----------------------------

Like the ``@extend`` keyword from Sass, classes can extend each
other. Use the ``:composes`` keyword in the style declaration:

.. code-block:: clojure

   (defstylesheet my-stylesheet
     [.base {:padding-bottom "16px"}]

     [.heading {:composes base
                :font-weight "bold"}]
     [.copy {:composes base
             :font-size "16px"}])

However, unlike Sass, this does not duplicate selectors in the
stylesheet, but rather modifies the exposed class name to include all
base classes. Multiple classes can be extended by specifying a vector
or list of classes.

Since composition works on class name identifiers, you can use
Clojure's built-in features for building modules:

.. code-block:: clojure

   ;; base.cljs
   (ns base
     (:require [forest.macros :refer-macros [defstylesheet]]))

   (defstylesheet base-styles
     [.base {:padding-bottom "16px"}])

   ;; typography.cljs
   (ns typography
     (:require [base :as base]
               [forest.macros :refer-macros [defstylesheet]]))

   (defstylesheet typography-styles
     [.heading {:composes base/base
                :font-weight "bold"}]
     [.copy {:composes base/base
             :font-size "16px"}])


Class name utilities
--------------------

There's also a ``class-names`` function that can be useful for
combining classes when building interfaces:

.. code-block:: clojure

   (ns sample
     (:require [forest.macros :refer-macros [defstylesheet]]
               [forest.class-names :refer [class-names]]))

   (defstylesheet my-stylesheet
     [.list-item {:list-style "square"}]
     [.is-selected {:font-weight "italic"}])

   (html
     [:ul
       (map (fn [item]
              [:li {:class (class-names list-item
                                        {is-selected (selected? item)})}])
            items)])

``class-names`` joins together all truthy (non-nil/non-false)
arguments, flattens arrays and maps, and only picks maps with truthy
values.


Development
===========

To work on Forest, you'll need Boot_ installed somewhere on
``$PATH``. To iterate on the unit and integration tests, run:

.. code-block:: sh

   boot watch test-all


This runs all Clojure and ClojureScript tests. The main
``defstylesheet`` macro and related compiler code is written in
Clojure. The ClojureScript tests run some integration tests and test
the ``class-names`` function.

If you want to attach a REPL to the running tests, e.g. through
Cider_, run:

.. code-block:: sh

   boot repl watch test-all


.. _Garden: https://github.com/noprompt/garden
.. _Boot: https:://boot-clj.com
.. _Cider: https://github.com/clojure-emacs/cider
