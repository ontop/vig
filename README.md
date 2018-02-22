VIG (v 1.8.0)
===

# Virtual Instances Generator (VIG)

VIG is a data scaler for OBDA benchmarks. It takes as input a source data instance and a scale factor, and produces a scaled data instance that satisfies the schema constraints and that is “similar” to the source instance according to certain ad-hoc similarity measures. The produced data instance is in form of csv files that can be effectively imported into any relational database management system (RDBMS).

VIG is currently the official data scaler of the [NPD benchmark](https://github.com/ontop/npd-benchmark). 


Build JAR
----

VIG is bundled as a maven project, therefore it can be built using the standard maven commands. We provide a bash script to save time:

$ bash build.sh

The jar (with dependencies) containing the application will be generated under the vig-distribution/target folder.

Configure and Run
----
Before running, a few things need to be configured. Please refer to the documentation (http://ontop.github.io/vig/ or `docs` folder) for more information. 

Publications
----
The following list contains a few publications describing VIG. We suggest to skim through them, so as to save time and understand *exactly* how VIG works and what data it can generate for you. 

- [Longest Technical Description and Evaluation of VIG (Submission to SWJ)](http://www.semantic-web-journal.net/content/vig-data-scaling-obda-benchmarks-0)
- [Long Technical Description and Evaluation of VIG (@BLINK '16)](http://ceur-ws.org/Vol-1700/paper-06.pdf)
- [Short Technical Description of VIG](https://arxiv.org/abs/1607.06343)
- [Evaluation of VIG with the BSBM Benchmark (@ISWC Posters '16)](http://ceur-ws.org/Vol-1690/paper82.pdf)


Experimental Evaluations
----

Checkout to the `evaluations/results` branch. Evaluations will be in the "evaluations" folder.

Contacts
----------

* [Davide Lanti](http://www.inf.unibz.it/~dlanti/)
