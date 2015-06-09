package it.unibz.inf.data_pumper.columns_cluster;


class CPIntervalKey implements IntervalKeyDecorator {

    private final String colName;
    private IntervalKey instance;

    private CPIntervalKey(IntervalKey iK, String colName){
	this.colName = colName;
	this.instance = iK;
    }

    static CPIntervalKey promote(IntervalKey iK, String colName) {
	return new CPIntervalKey(iK, colName);
    } 

    String getFullName(){
	return this.colName;
    }

    @Override
    public String getKey(){
	return instance.getKey();
    }

    @Override
    public long getLwBound(){
	return instance.getLwBound();
    }

    @Override
    public long getUpBound(){
	return instance.getUpBound();
    }

    @Override
    public int hashCode() {
	return this.toString().hashCode();
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
	String result = this.colName + "___" + instance.toString();
	return result;
    }
};
