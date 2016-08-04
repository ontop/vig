#!/bin/bash

NUM_ARGS=$((${#}-1))

echo $NUM_ARGS

for ((i=1;i<=$NUM_ARGS;i++))
do
    echo c
done