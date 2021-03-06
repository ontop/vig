# VIG (Virtual Instances Generator) 
## Current Version: 1.8.0

VIG is a [data scaler](http://www.vldb.org/pvldb/vol4/p1470-tay.pdf) specifically designed for benchmarks of [Ontology-based Data Access (OBDA) systems](https://www.slideshare.net/guohuixiao/ontop-answering-sparql-queries-over-relational-databases) such as [Ontop](https://github.com/ontop/ontop). VIG takes as input a source database instance (currently, the source must be a __mysql__ database) and a scale factor, and produces a scaled database instance that satisfies the schema constraints and that is “similar” to the source instance according to certain ad-hoc similarity measures. The produced database instance is in form of csv files that can be effectively imported into any relational database management system (RDBMS).

## Data Scaling in OBDA

What differentiates VIG from other data scalers, and makes it more suitable for the context of OBDA benchmarks, is its ability to process an input OBDA mapping file so as to analyze the results for the queries that can be potentially submitted to the underlying RDBMS system. This feature allows a series of benefits, like providing a (limited) support to disjoint classes, reproduce fixed-domain columns in the database, or avoid empty results for queries in the mappings in the produced data. For more details please refer to the page [Characteristics of The Data Produced By VIG](Characteristics of The Data Produced By VIG).

## Quick Start Guide

To use VIG outside the context of the [NPD Benchmark](https://github.com/ontop/npd-benchmark), refer to the page [How To Use VIG in an OBDA Benchmark](How To Use VIG in an OBDA Benchmark).

## Restrictions

For restriction and limitations, refer to [here](Restrictions).

## Download

Clone the git repository. 

~~~~~~~~~~~~~~~~~~~
$ git clone https://github.com/ontop/vig.git
~~~~~~~~~~~~~~~~~~~

## Build and JAR
In order to build the project, simply run the following bash script.

~~~~~~~
$ ./build.sh
~~~~~~~

from the vig folder. The built jar can be then found in the vig-distribution/target/ folder.

## Launch

Vig requires a few parameters in order to run correctly, e.g. the database name. These parameters can be given by modifying the configuration file **src/main/resources/configuration.conf**.

~~~~~~
jdbc-connector jdbc:mysql              # The only connector supported, at the moment
database-url addr:port/sourceDbName    # The address, port, and name of the source database
database-user userSrc                  # The username for the access to the source database
database-pwd pwdSrc                    # The password for the access to the source database
random-gen false                       # If true, then the generator will behave as a pure random generator
obda-file resources/obda-file.obda   # The location of the mapping file. 
pumper-type ODBA                       # DB or OBDA. Since VIG 1.1, OBDA mode IS STRONGLY PREFERRED.
                                       # The NPD Benchmark, v1.8.0 onwards, should be run in OBDA mode. 
                                       # OBDA mode reads also statistics from the mappings, and 
                                       # supports fixed-domain columns. 
fixed table1.col1 table2.col2 etc.     # Manually specified fixed-domain columns
non-fixed table1.col1 table2.col2 etc. # Manually specified non fixed-domain columns. 
                                       
~~~~~~

If the pumper-type is set to OBDA, then the connection parameters must be set **ALSO** in the specified obda-file.

### Command Line Options

For a complete list of the vig command-line options, please refer to the help utility

~~~~~~~~
$ java -jar vig-distribution-1.8.0-jar-with-dependencies.jar --help
~~~~~~~~

or 

~~~~~~~~
$ java -jar vig-distribution-1.8.0-jar-with-dependencies.jar --help-verbose
~~~~~~~~

# Example of Build and Execution

1) Launch the build script from the vig directory:

~~~~~~
./build.sh
~~~~~~

2) Create the desired source database, e.g.

~~~~~
$ mysql --user="username" --host="address" --password="password" npdSource < npd-data-dump.sql
~~~~~

3) Set up the configuration files in vig-distribution/target/resources.

4) Run, specifying the location of the __resources__ folder and the __desired scaling factor__ for the target database, e.g. a scaling factor of two times---each table T in the schema will contain nRows(T)*2 rows, where nRows(T) is the number of rows in T in the source database.

~~~~~~~~
$ cd vig-distribution/target
$ java -jar vig-distribution-1.8.0-jar-with-dependencies.jar --res=resources --scale=2
~~~~~~~~

5) The csv files will be generated in the directory vig-distribution/target/src/main/resources/csvs/.
Check here for [Troubleshooting](Troubleshooting) .

## Coming Soon ...

For a list of the new features currently under development, refer to [Development](CHANGELOG.md) page.
