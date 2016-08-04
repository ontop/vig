#!/bin/bash


#bsbm_1000000products
#bsbm_100000products
#bsbm_10000products
#bsbm_100_of10000products_vig1_8 
#bsbm_10_of10000products_vig1_8
#bsbm_1_of10000products_vig1_8


# NPD Tests
for i in 1 
do
 
DB_NAME=bsbm_${i}_of10000products_vig1_8

echo Script: Doing $DB_NAME

./replace-n-lines.sh "db-url 10.7.20.39/$DB_NAME" 1 src/main/resources/configuration.conf
./replace-n-lines.sh "connectionUrl   jdbc:mysql://10.7.20.39/$DB_NAME" 14 src/main/resources/bsbm-mysql-vig.obda

java -jar mixer.jar --runs=1

cp src/main/resources/statsMixer.txt . 

done
