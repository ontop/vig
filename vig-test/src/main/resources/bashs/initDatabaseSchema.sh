#!/usr/bin/env bash
dbName=$1

: ${1?"Usage: $0 DB_NAME"}

mysql --user="tir" --host="localhost" --password="gregario" $dbName < /home/tir/git/vig/vig-test/src/main/resources/sqls/npd_clean_no_spatial_schema.sql
