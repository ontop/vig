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
database-url db_host/db_name
database-user user
database-pwd pwd
mode OBDA
obda-file resources/npd-v2-ql_a.obda
scale 2
~~~

In our configuration, we have set up the scaling factor to 2 through the parameter `scale 2`.

5) If we have set the mode to OBDA, as in our example, then we need to put mappings in the location we specified in the configuration file (`resources/npd-v2-ql_a.obda` in our example). Morover, we need to set up the connection parameters in the mappings file:

~~~
[SourceDeclaration]
sourceUri	http://sws.ifi.uio.no/vocab/npd-v2
connectionUrl	jdbc:mysql://db_host/db_name
username	user
password	pwd
driverClass	com.mysql.jdbc.Driver
~~~

4) We are now ready to run VIG, specifying the location of the __resources__ folder.

~~~~~~~~
$ java -jar vig.jar --res="resources"
~~~~~~~~

5) The csv files will be generated in the directory `resources/csvs`.

6) Import the csv files into the RDBMS.
