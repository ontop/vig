package it.unibz.inf.data_pumper.columns.intervals;

import java.util.LinkedList;
import java.util.List;

import it.unibz.inf.data_pumper.columns.ColumnPumper;
import it.unibz.inf.data_pumper.columns.exceptions.BoundariesUnsetException;
import it.unibz.inf.data_pumper.core.main.exceptions.DebugException;
import it.unibz.inf.data_pumper.tables.MySqlDatatypes;

public class IntInterval extends Interval<Long> {

    public IntInterval(String key,
            MySqlDatatypes type,
            long nValues, List<ColumnPumper<Long>> involvedCols) {
        super(key, type, nValues, involvedCols);
    }

    public IntInterval(String name, MySqlDatatypes type, long minEncoding,
	    long maxEncoding) {
	super(name, type, minEncoding, maxEncoding);
    }

    @Override
    public void updateMinEncodingAndValue(long newMin) {
	
	super.updateMinEncodingAndValue(newMin);
	
        this.min = newMin;
        this.minEncoding = newMin;
    }
    
    @Override
    public void updateMaxEncodingAndValue(long newMax) {
        this.max = newMax;
        this.maxEncoding = newMax;
    }
    
    @Override
    public long getMinEncoding() {
        if( this.min == this.max ) throw new BoundariesUnsetException("Undefined interval boundaries for columns "+this.getKey());
        
        // Assert
        if( !(this.min == this.minEncoding) ){ throw new DebugException("Assertion failed: " + !(this.min == this.minEncoding)); }
        
        return minEncoding;
    }
    
    @Override
    public long getMaxEncoding() {
        if( this.min == this.max ) throw new BoundariesUnsetException("Undefined interval boundaries for Interval "+this.toString());
        
        // Assert
        if( !(this.max == this.maxEncoding) ){ throw new DebugException("Assertion failed: " + !(this.max == this.maxEncoding)); }
        
        return this.maxEncoding;
    }

    @Override
    public Interval<Long> getCopyInstance() {
        
        IntInterval result = 
                new IntInterval(
                        this.getKey(), this.getType(), 
                        this.nFreshsToInsert, new LinkedList<>(this.intervalColumns));
        result.updateMinEncodingAndValue(this.minEncoding);
        result.updateMaxEncodingAndValue(this.maxEncoding);
        
        return result;
    }

    @Override
    public void synchronizeMinMaxNFreshs() {
	// TODO Auto-generated method stub
	
    }
}
