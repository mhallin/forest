=========
 Changes
=========

Unreleased_
===========

0.2.0_ – 2016-05-06
===================

Features:

* Composed selectors, inital support for descendant (i.e. the "space"
  operator) and immediate child (i.e. the ">" operator).


0.1.4_ – 2016-03-31
===================

Features:

* Support for pseudo elements and pseudo classes.

Bugfixes:

* The ``composes`` property removed from the generated CSS output.
* Improve stability of CSS selector name mangling


0.1.3_ – 2016-02-28
===================

Bugfixes:

* The runtime library was not automatically imported into client
  applications when using the ``defstylesheet`` macro.


0.1.2_ – 2016-02-27
===================

New features:

* Class name composition/inheritance through the ``:composes``
  keyword.
* Support for custom CSS class name manglers in the ``defstylesheet``
  macro.

Bugfixes:

* Remove ID selector name mangling


0.1.1_ – 2016-02-22
===================

Internal fixes:

* Add license file to repository
* Update test library dependency


0.1.0_ – 2016-02-22
===================

Initial release:

* Dynamic class name generator for conditional classes
* Basic CSS module support


.. _Unreleased: https://github.com/mhallin/forest/compare/release/v0.2.0...HEAD
.. _0.2.0: https://github.com/mhallin/forest/compare/release/v0.1.4...release/v0.2.0
.. _0.1.4: https://github.com/mhallin/forest/compare/release/v0.1.3...release/v0.1.4
.. _0.1.3: https://github.com/mhallin/forest/compare/release/v0.1.2...release/v0.1.3
.. _0.1.2: https://github.com/mhallin/forest/compare/release/v0.1.1...release/v0.1.2
.. _0.1.1: https://github.com/mhallin/forest/compare/release/v0.1.0...release/v0.1.1
.. _0.1.0: https://github.com/mhallin/forest/commits/release/v0.1.0
