#!/bin/bash

createConfFile () {
    cat <<EOF
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
                                           # OBDA-mode only.
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
EOF
}

# Build and skip the unit tests
mvn -Dmaven.test.skip=true install

cd vig-distribution/

mvn assembly:single
cd ..
echo 
echo
echo "[SCRIPT] Creating folder ${PWD}/csvs"
mkdir -p csvs
echo "[SCRIPT] Creating template configuration file ${PWD}/configuration.conf" 
createConfFile > configuration.conf
echo
echo "[SCRIPT] Creating file ${PWD}/vig.jar"
mv 'vig-distribution/target/vig-distribution-1.8.1-jar-with-dependencies.jar' 'vig.jar'
echo "[SCRIPT] Installation completed."
echo "[SCRIPT] This is your config information:"
echo
echo "[SCRIPT] configuration file:    ${PWD}/configuration.conf"
echo "[SCRIPT] csvs folder:           ${PWD}/csvs"
echo
echo "[SCRIPT] Before the first run, set up the configuration in ${PWD}/configuration.conf"
