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


Vig requires a few parameters in order to run correctly. For a complete list, please refer to the help utility

~~~~~~~~
$ java -jar vig-distribution-1.8.0-jar-with-dependencies.jar --help
~~~~~~~~

or 

~~~~~~~~
$ java -jar vig-distribution-1.8.0-jar-with-dependencies.jar --help-verbose
~~~~~~~~

This second command has the following output:

~~~

~~~

Command-line options overwrite the values specified through the configuration file.

~~~~~~
# ====================
# Mandatory parameters
# ====================

jdbc-connector jdbc:mysql				 # The only connector supported, at the moment.
database-url <addr:port/dbName>		   	 # The address, port, and name of the source database.
database-user <user>                   	 # The username for the access to the source database.
database-pwd <pwd>                     	 # The password for the access to the source database.

# VIG generation mode. Either DB or OBDA (default). Since VIG 1.1, OBDA mode is preferred.
# The NPD Benchmark, v1.8.0 onwards, should be run in OBDA mode.
# OBDA mode reads also statistics from the mappings, and supports fixed-domain columns.
mode <DB|ODBA>

random-gen [true|false]                	 # If true, then the generator will behave as a pure 
		   								 # random generator. DB-mode only.
obda-file <path/mappings.obda>		   	 # The location of the mapping file in .obda format.
		  								 # OBDA-mode only.
scale <value> 							 # Scaling factor value. Default: 1.0 

# ============================================================================================= 
# Advanced parameters. Usually the defaults values (check through the --help option) are enough.
# =============================================================================================

fixed <table1.col1> <table2.col2> ...  	 # Manually specified fixed-domain columns. OBDA-mode only.
non-fixed <table1.col1> <table2.col2> ...# Manually specified non fixed-domain columns. OBDA-mode only.

# Time (ms) allowed for the columns-cluster analysis. Given a columns cluster {A,B,C} of columns A, B, 
# and C,VIG tries to compute the cardinality of all possible intersections between these three columns,
# namely AB, AC, BC and ABC. If timeout is reached, say, while evaluating cardinality of the intersection
# ABC, then such cardinality is assumed to be zero (hence, no value will be generated in the intersection 
# of the columns A,B, and C). OBDA-mode only.
cc-timeout <value>					  	 

tables <table1> <table2> ...			 # Generate only the specified tables.
columns <table1.col1> <table2.col2> ...  # Generate only the specified columns. 
~~~~~~

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
