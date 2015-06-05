package it.unibz.inf.data_pumper.column_types.aggregate_types.constraintProgram;

import it.unibz.inf.data_pumper.column_types.intervals.Interval;

public class IntervalKey {
    public final long lwBound;
    public final long upBound;
    public final String key;
    
    public IntervalKey(String key, long lwBound, long upBound){
	this.key = key;	
	this.lwBound = lwBound;
	this.upBound = upBound;
    }
    
    public IntervalKey(Interval<?> interval){
	this.key = interval.getKey();
	this.lwBound = interval.getMinEncoding();
	this.upBound = interval.getMaxEncoding();
    }

    @Override 
    public boolean equals(Object other) {
	boolean result = false;
	if (other instanceof IntervalKey) {
	    IntervalKey that = (IntervalKey) other;
	    result = that.toString().equals(this.toString());
	}
	return result;
    }
    
    @Override 
    public int hashCode() {
	return this.toString().hashCode();
    }
    
    @Override
    public String toString(){
	return this.key;
    }
}
