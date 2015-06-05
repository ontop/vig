package it.unibz.inf.data_pumper.column_types.aggregate_types.constraintProgram;

import it.unibz.inf.data_pumper.column_types.intervals.Interval;

public class CPIntervalKey extends IntervalKey {
    public final String colName;

    public CPIntervalKey(IntervalKey iK, String colName){
	super(iK.key, iK.lwBound, iK.upBound);
	this.colName = colName;
    }
    
    public CPIntervalKey(Interval<?> interval, String colName) {
	super(interval);
	this.colName = colName;
    }

    public CPIntervalKey(String colName, String intervalKey, long lwBound, long upBound){
	super(intervalKey, lwBound, upBound);
	this.colName = colName;
    }
    
    @Override 
    public boolean equals(Object other) {
	boolean result = false;
	if (other instanceof CPIntervalKey) {
	    CPIntervalKey that = (CPIntervalKey) other;
	    result = that.toString().equals(this.toString());
	}
	return result;
    }
    
    @Override
    public String toString(){
	String result = this.colName + super.toString();
	return result;
    }
};
