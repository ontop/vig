#!/bin/bash

# Build and skip the unit tests
mvn -Dmaven.test.skip=true install

cd vig-distribution/

mvn assembly:single

echo
echo
echo "Installation completed. Now remember to set up the configuration files in the vig-distribution/target/resources folder!!!"
echo "For the first run, run with the --help or --help-verbose option."
