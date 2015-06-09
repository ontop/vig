package it.unibz.inf.data_pumper.columns_cluster;

import java.util.List;

import it.unibz.inf.data_pumper.column_types.ColumnPumper;

public abstract class ColumnsCluster<T> {
 
    // Public
    public abstract boolean hasMultiInterval();
    public abstract void classicStrategy() ;
    public abstract List<ColumnPumper<T>> getColumnPumpersInCluster();
    public abstract void adaptIntervalsFromMultiIntervalCols();
    
    // Protected
    protected abstract List<ColumnPumperInCluster<T>> getClusterCols();
    
    // Package
    abstract void registerColumnPumper(ColumnPumper<T> cP);
    abstract ColumnPumperInCluster<T> getColumnPumperInClusterWrapping(ColumnPumper<T> cP); 
}
