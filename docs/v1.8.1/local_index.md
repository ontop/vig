# VIG (Virtual Instances Generator) 
## Version: 1.8.1

VIG is a [data scaler](http://www.vldb.org/pvldb/vol4/p1470-tay.pdf) specifically designed for benchmarks of [Ontology-based Data Access (OBDA) systems](https://www.slideshare.net/guohuixiao/ontop-answering-sparql-queries-over-relational-databases) such as [Ontop](https://github.com/ontop/ontop). VIG takes as input a source database instance (currently, the source must be a __mysql__ database) of size `n` and a scale factor `s`, and produces a scaled database instance of size `n * s` that satisfies the schema constraints and that is “similar” to the source instance according to certain ad-hoc similarity measures. The produced database instance is in form of CSV files that can be effectively imported into any relational database management system (RDBMS).

## Data Scaling in OBDA

What differentiates VIG from other data scalers, and makes it more suitable for the context of OBDA benchmarks, is its ability to process an input OBDA mapping file so as to analyze the results for the queries that can be potentially submitted to the underlying RDBMS system. This feature allows a series of benefits, like providing a (limited) support to disjoint classes, reproduce fixed-domain columns in the database, or avoid empty results for queries in the mappings in the produced data. For more details please refer to the page [Characteristics of The Data Produced By VIG](Characteristics of The Data Produced By VIG).

## Quick Start Guide

Refer to [Quick Start Guide](Quick Start Guide)

## Using VIG in an OBDA Benchmark

To use VIG outside the context of the [NPD Benchmark](https://github.com/ontop/npd-benchmark), refer to the page [How To Use VIG in an OBDA Benchmark](How To Use VIG in an OBDA Benchmark).

## Restrictions

For restriction and limitations, refer to [here](Restrictions).

## Troubleshooting

Check [here](Troubleshooting) for troubleshooting.

## Coming Soon ...

For a list of the new features currently under development, refer to [Development](CHANGELOG.md) page.
