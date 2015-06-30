package it.unibz.inf.data_pumper.columns_cluster;

import it.unibz.inf.data_pumper.columns.intervals.Interval;

 class SimpleIntervalKey implements IntervalKey{
    private final long lwBound;
    private final long upBound;
    private final String key;
    
     SimpleIntervalKey(String key, long lwBound, long upBound){
	this.key = key;	
	this.lwBound = lwBound;
	this.upBound = upBound;
    }
    
     SimpleIntervalKey(Interval<?> interval){
	this.key = interval.getKey();
	this.lwBound = interval.getMinEncoding();
	this.upBound = interval.getMaxEncoding();
    }
    
    @Override 
    public boolean equals(Object other) {
	boolean result = false;
	if (other instanceof SimpleIntervalKey) {
	    SimpleIntervalKey that = (SimpleIntervalKey) other;
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

    @Override
    public String getKey() {
	return this.key;
    }

    @Override
    public long getLwBound() {
	return this.lwBound;
    }

    @Override
    public long getUpBound() {
	return this.upBound;
    }
}
