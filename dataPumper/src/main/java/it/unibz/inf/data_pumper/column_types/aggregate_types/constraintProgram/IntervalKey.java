package it.unibz.inf.data_pumper.column_types.aggregate_types.constraintProgram;

public interface IntervalKey {
    public String getKey();
    public long getLwBound();
    public long getUpBound();
}
