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

Vig requires a few parameters in order to run correctly. For a complete list, you can refer to the help utility

~~~~~~~~
$ java -jar vig-distribution-1.8.0-jar-with-dependencies.jar --help
~~~~~~~~

or 

~~~~~~~~
$ java -jar vig-distribution-1.8.0-jar-with-dependencies.jar --help-verbose
~~~~~~~~

This second command has the following output:

~~~
USAGE: java -jar vig.jar [OPTIONS]

CONFIGURATION OPTIONS

--conf              <string>                                               (default: resources/configuration.conf)Location of the configuration file

PUMPER OPTIONS

--random-gen        <bool>              [true -- false]                    (default: false)    Ignore database statistics, and generate with random statistics.

--scale             <double>            (0.0 -- 1.7976931348623157E308]    (default: 1.0)      It specifies the scaling factor

--cc-timeout        <int>               [0 -- 2147483647]                  (default: 60)       Timeout allowed to the columns-cluster analysis, in seconds. To be used in combination with `OBDA` mode.

--tables            <string>                                               (default: )         Restrict the generation to a list of tables. E.g., --tables=table1,table2,table3,etc.

--columns           <string>                                               (default: )         Restrict the generation to a list of columns. E.g., --columns=table1.col1,table2.col2,etc.

--jdbc              <string>                                               (default: )         Database Driver Class

--db-url            <string>                                               (default: )         Database URL.

--db-user           <string>                                               (default: )         Username for accessing the database

--db-pwd            <string>                                               (default: )         Password for accessing the database

--mappings          <string>                                               (default: )         Path to the Mappings File. This parameter is mandatory in OBDA-mode

--mode              <string>            [DB,OBDA]                          (default: OBDA)     The mode of generation. One of: Database statistics only mode (DB), or Database statistics and Mappings analysis mode (OBDA).

--fixed             <string>                                               (default: )         Space-separated list of fully qualified column names (e.g., tableName.colName) declared as `fixed domain`, that is, attribute names for which no values should be generated apart from those that can be found in the seed database instance.

--non-fixed         <string>                                               (default: )         Space-separated list of qualified attribute names (e.g., tableName.colName) for which fresh values should be generated according to the statistics analysis. Useful in case VIG sets some column as 
~~~

Command-line options overwrite the values specified through the configuration file.

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

from the vig folder.

## Setup

If the build script ran successfully, you should be able to observe an output as follows:

~~~
[SCRIPT] This is your config information:

[SCRIPT] resources folder:      ${HOME}/resources
[SCRIPT] configuration file:    ${HOME}/resources/configuration.conf
[SCRIPT] csvs folder:           ${HOME}/resources/csvs
~~~

The `configuration.conf` file contains the configuration of the generator. This file looks as follows:

~~~~~~
# ====================
# Mandatory parameters
# ====================
jdbc-connector jdbc:mysql                  # The only connector supported, at the moment.
database-url <addr:port/dbName>            # The address, port, and name of the source database.
database-user <user>                       # The username for the access to the source database.
database-pwd <pwd>                         # The password for the access to the source database.

# VIG generation mode. Either DB or OBDA (default). Since VIG 1.1, OBDA mode is preferred.
# The NPD Benchmark, v1.8.0 onwards, should be run in OBDA mode.
# OBDA mode reads also statistics from the mappings, and supports fixed-domain columns.
mode <DB|ODBA>

random-gen [true|false]                    # If true, then the generator will behave as a pure 
                                           # random generator. DB-mode only.
obda-file <path/mappings.obda>             # The location of the mapping file in .obda format.
		  								   # IMPORTANT: Connection parameters should be set 
										   # also in this file. OBDA-mode only.
scale <value>                              # Scaling factor value. Default: 1.0 

# ====================================================================================== 
# Advanced parameters. Commented out, as usually the defaults values are enough. You can 
# check what the default values are set to by running VIG with the --help option.
# ======================================================================================

# fixed <table1.col1> <table2.col2> ...    # Manually specified fixed-domain columns. OBDA-mode only.
# non-fixed <table1.col1> <table2.col2> ...# Manually specified non fixed-domain columns. OBDA-mode only.

# Time (ms) allowed for the columns-cluster analysis. Given a columns cluster {A,B,C} of columns A, B, 
# and C,VIG tries to compute the cardinality of all possible intersections between these three columns,
# namely AB, AC, BC and ABC. If timeout is reached, say, while evaluating cardinality of the intersection
# ABC, then such cardinality is assumed to be zero (hence, no value will be generated in the intersection 
# of the columns A,B, and C). OBDA-mode only.
# cc-timeout <value>                       

# tables <table1> <table2> ...             # Generate only the specified tables.
# columns <table1.col1> <table2.col2> ...  # Generate only the specified columns. 
~~~~~~

### Command Line Options

For a complete list of the vig command-line options, please refer to the help utility by running

~~~~~~~~
$ java -jar vig.jar --help
~~~~~~~~

or 

~~~~~~~~
$ java -jar vig.jar --help-verbose
~~~~~~~~

Command-line options override the parameters provided through the configuration file.

# Launch VIG

After having set up the configuration file, simply execute the jar

~~~
$ java -jar vig.jar
~~~

# Example of Build and Execution

1) Launch the build script from the vig directory:

~~~~~~
./build.sh
~~~~~~

2) Create the desired source database, e.g.

~~~~~
$ mysql --user="username" --host="address" --password="password" npdSource < npd-data-dump.sql
~~~~~

3) Set up the configuration file in `resources/configuration.conf`.

~~~
jdbc-connector jdbc:mysql
database-url localhost/db_name
database-user user
database-pwd pwd
mode OBDA
obda-file src/main/resources/npd-v2-ql_a.obda
scale 2
~~~

4) Run VIG, specifying the location of the __resources__ folder and the __desired scaling factor__ for the target database, e.g. a scaling factor of two times---each table T in the schema will contain nRows(T)*2 rows, where nRows(T) is the number of rows that T contains in the source database.

~~~~~~~~
$ java -jar vig.jar --res=resources --scale=2
~~~~~~~~

5) The csv files will be generated in the directory vig-distribution/target/src/main/resources/csvs/.
If `mode` is set to OBDA, then the connection parameters must be set **ALSO** in the specified `obda-file`.

### Command Line Options

For a complete list of the vig command-line options, please refer to the help utility

~~~~~~~~
$ java -jar vig-distribution-1.8.0-jar-with-dependencies.jar --help
~~~~~~~~

or 

~~~~~~~~
$ java -jar vig-distribution-1.8.0-jar-with-dependencies.jar --help-verbose
~~~~~~~~

Command-line options overwrite the values specified through the configuration file.

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
