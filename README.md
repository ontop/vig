vig (v 1.8.1)
===

Virtual Instances Generator

This repository contains the source code for the generator used in the NPD benchmark (https://github.com/ontop/npd-benchmark).

The generator is bundled as a multi-module maven (http://maven.apache.org/) project.

Build and JAR
----

VIG is bundled as a maven project, therefore it can be built using the standard maven commands. In order to save time, there is a build script that does the job as well.

$ ./build.sh

The jar (with dependencies) can then be found under the vig-distribution/target folder.

Configure and Run
----
Before running, a few things need to be configured. Please refer to the wiki page (https://github.com/ontop/vig/wiki) for more information. 

Publications
----

* **Davide Lanti, Guohui Xiao, Diego Calvanese:
Fast and Simple Data Scaling for OBDA Benchmarks. BLINK@ISWC 2016
   Available at http://ceur-ws.org/Vol-1700/paper-06.pdf

* **Data Scaling in OBDA Benchmarks: The VIG Approach**. Davide Lanti, Guohui Xiao, Diego Calvanese. 2016 
   Available at https://arxiv.org/abs/1607.06343.

* **Davide Lanti, Guohui Xiao, Diego Calvanese:
An Evaluation of VIG with the BSBM Benchmark. International Semantic Web Conference (Posters & Demos) 2016
   Available at http://ceur-ws.org/Vol-1690/paper82.pdf

Contacts
----------

* [Davide Lanti](http://www.inf.unibz.it/~dlanti/)
