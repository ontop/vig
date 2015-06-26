dbName=$1

: ${1?"Usage: $0 DB_NAME"}

mysql --user="tir" --host="localhost" --password="gr3g4r10" $dbName < /home/tir/git/vig2/vig/vig-test/src/main/resources/sqls/npd_clean_no_spatial_schema.sql
