package it.unibz.inf.data_pumper.column_types.aggregate_types.constraintProgram;


public class CPIntervalKey extends IntervalKeyDecorator {
    
    private final String colName;
    private IntervalKey instance;
    
    private CPIntervalKey(IntervalKey iK, String colName){
	this.colName = colName;
	this.instance = iK;
    }
    
    public static CPIntervalKey promote(IntervalKey iK, String colName) {
	return new CPIntervalKey(iK, colName);
    } 
    
    public String getColName(){
	return this.colName;
    }
    
    public String getKey(){
	return instance.getKey();
    }
    
    public long getLwBound(){
	return instance.getLwBound();
    }
    
    public long getUpBound(){
	return instance.getUpBound();
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
	String result = this.colName + instance.toString();
	return result;
    }
};
