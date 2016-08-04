#!/bin/bash

DB_NAME=$1

LINE1='CREATE DATABASE IF NOT EXISTS `'$DB_NAME'` DEFAULT CHARACTER SET utf8;'
LINE2='USE `'$DB_NAME'`;'

echo ./replace_first_nlines.sh \"$LINE1\' \'$LINE2\' \'bsbm-shema.sql\'
./replace_first_nlines.sh "$LINE1" "$LINE2" "bsbm-schema.sql"
