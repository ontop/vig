vig (v 1.7.0)
===

Virtual Instances Generator

This repository contains the source code for the generator used in the NPD benchmark (https://github.com/ontop/npd-benchmark).

The generator is bundled as a multi-module maven (http://maven.apache.org/) project.

Build and JAR
----
Note: We highly reccomend to use the pre-compiled executable that can be found in "executableJar" folder.

VIG is bundled as a maven project, therefore it can be built using the standard maven commands. In order to save time, there is a build script that does the job as well.

$ ./build.sh

The jar (with dependencies) can then be found under the vig-distribution/target folder.

Configure and Run
----
Before running, a few things need to be configured. Please refer to the wiki page (https://github.com/ontop/vig/wiki) for more information. 

Contacts
----------

* [Davide Lanti](http://www.inf.unibz.it/~dlanti/)
