package it.unibz.inf.data_pumper.columns_cluster;

interface IntervalKey {
    String getKey();
    long getLwBound();
    long getUpBound();
}
