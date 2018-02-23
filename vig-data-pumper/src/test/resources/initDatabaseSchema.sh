dbName=$1

: ${1?"Usage: $0 DB_NAME"}

mysql --user="fish" --host="10.7.20.65" --password="fish" $dbName < src/test/resources/sqls/create_dateTimeColumnTestDB.sql
