# VIG (Virtual Instances Generator) Version 0.0.1

### This wiki refers to branch "version/0.0.1"

VIG is a database instances generator. Currently it supports only __mysql__, but support to others RDBMSs will be added in future.

## Download

Clone the git repository. 

~~~~~~~~~~~~~~~~~~~
$ git clone https://github.com/ontop/vig.git
~~~~~~~~~~~~~~~~~~~

## Set Up Unit Tests

Several unit tests require the access to test databases. In case you want to ignore the unit tests during the build phase, you can do so in the standard maven way:

~~~~~~~~~~~~~~~~~~
mvn -Dmaven.test.skip=true install
~~~~~~~~~~~~~~~~~~

Another possibility is to configure the test databases on a machine. The unit tests require two databases, a database called **pumperTest** (whose sql dump can be found in the __databases/__ folder) and the npd database (whose sql dump can be found in [FactPages](http://sws.ifi.uio.no/project/npd-v2/)).

### Unit Tests Configuration File
The unit tests can be instructed to access the aforementioned databases through the configuration file **src/main/resources/unitTests.conf**. 

~~~~~~~~~
JdbcConnector jdbc:mysql
DbUrlOriginal host:port/npd
DbUsernameOriginal user
DbPasswordOriginal pwd
DbUrlToPump host:port/pumperNpd
DbUsernameToPump user
DbPasswordToPump pwd
randomGen false
obdaFile src/main/resources/npd-v2-ql_a.obda
pumperType DB
DbUrlSingleTests host:port/pumperTest
DbUrlUsernameSingleTests user
DbPasswordSingleTests pwd
~~~~~~~~~

The database **pumperNpd** is a copy of the database **npd** that is used by some (currently ignored) unit tests. 

## Build and JAR
In order to build the project, simply run the following bash script.

~~~~~~~
$ ./build.sh
~~~~~~~

from the vig folder. The built jar can be then found in the vig-distribution/target/ folder.

## Launch

Vig requires a few parameters in order to run correctly, e.g. the database name. These parameters can be given by modifying the configuration file **src/main/resources/configuration.conf**.

~~~~~~
JdbcConnector jdbc:mysql             # The only connector supported, at the moment
DbUrlOriginal addr:port/sourceDbName # The address, port, and name of the source database
DbUsernameOriginal userSrc           # The username for the access to the source database
DbPasswordOriginal pwdSrc            # The password for the access to the source database
DbUrlToPump addr:port/targetDbName   # The address, port, and name of the target database
DbUsernameToPump userTgt             # The username for the access to the target database
DbPasswordToPump pwdTgt              # The password for the access to the target database
randomGen false                      # If true, then the generator will behave as a pure random generator
obdaFile resources/npd-v2-ql_a.obda  # The location of the mapping file. 
pumperType DB                        # At the moment, DB is the only value available for this option
~~~~~~
### Command Options

For a complete list of the vig command-line options, please refer to the help utility

~~~~~~~~
$ java -jar vig-distribution-X.Y.Z-SNAPSHOT-jar-with-dependencies.jar --help
~~~~~~~~

or 

~~~~~~~~
$ java -jar vig-distribution-X.Y.Z-SNAPSHOT-jar-with-dependencies.jar --help-verbose
~~~~~~~~

# Example of Build and Execution

1) Launch the build script from the vig directory:

~~~~~~
./build.sh
~~~~~~

2) Create the desired source and target databases, e.g.

~~~~~
$ mysql --user="username" --host="address" --password="password" npdSource < npd-data-dump.sql
$ mysql --user="username" --host="address" --password="password" npdTarget < npd-data-dump.sql
~~~~~

3) Set up the configuration files in vig-distribution/target/resources.

4) Run, specifying the location of the __resources__ folder and the __desired increment__ for the target database, e.g. an increment of 2 times

~~~~~~~~
$ cd vig-distribution/target
$ java -jar vig-distribution-0.1.0-SNAPSHOT-jar-with-dependencies.jar --res=resources --inc=2
~~~~~~~~
