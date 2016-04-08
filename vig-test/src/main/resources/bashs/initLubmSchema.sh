dbName=$1

: ${1?"Usage: $0 DB_NAME"}

mysql --user="tir" --host="localhost" --password="gregario" $dbName < /home/tir/git/vig2/vig/vig-test/src/main/resources/sqls/lubm/lubm_schema.sql
