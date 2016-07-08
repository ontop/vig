#!/bin/bash

# ./generate is the command to invoke the Berlin SPARQL Benchmark Generator

cd ..
./generate -s sql -pc 10000
rm -rf bsbm_10000products
mv dataset bsbm_10000products

./generate -s sql -pc 100000
rm -rf bsbm_100000products
mv dataset bsbm_100000products

./generate -s sql -pc 1000000
rm -rf bsbm_1000000products
mv dataset bsbm_1000000products
