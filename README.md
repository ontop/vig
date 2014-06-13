vig
===

Virtual Instances Generator

This repository contains the source code for the generator used in the NPD benchmark (https://github.com/ontop/npd-benchmark).

The generator is bundled as a multi-module maven (http://maven.apache.org/) project.

Build
----

Please skip the unit tests, since at the moment they access a database that cannot be accessed from the outside.

mvn -Dmaven.test.skip=true install

Contacts
----------

* [Davide Lanti](http://www.inf.unibz.it/~dlanti/)
