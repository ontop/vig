package it.unibz.inf.data_pumper.columns.intervals;

import it.unibz.inf.data_pumper.columns.ColumnPumper;
import it.unibz.inf.data_pumper.columns.exceptions.BoundariesUnsetException;
import it.unibz.inf.data_pumper.tables.MySqlDatatypes;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

public class BigDecimalInterval extends Interval<BigDecimal> {

    public BigDecimalInterval(
            String key,
            MySqlDatatypes type,
            long nValues, List<ColumnPumper<BigDecimal>> involvedColumns) {
        super(key, type, nValues, involvedColumns);
    }
    
    public BigDecimalInterval(
	    String key, 
	    MySqlDatatypes type,
	    long minEncoding,
	    long maxEncoding){
	super(key, type, minEncoding, maxEncoding);
    }

    @Override
    public void updateMinEncodingAndValue(
            long newMin) {
	
	super.updateMinEncodingAndValue(newMin);
	
        this.min = new BigDecimal(newMin);
        this.minEncoding = newMin;
    }

    @Override
    public void updateMaxEncodingAndValue(
            long newMax) {
        this.max = new BigDecimal(newMax);
        this.maxEncoding = newMax;
    }

    @Override
    public long getMinEncoding(){
        if( this.min == this.max ) throw new BoundariesUnsetException("Undefined interval boundaries");
        return min.longValue();
    }

    @Override
    public long getMaxEncoding()
            throws BoundariesUnsetException {
        if( this.min == this.max ) throw new BoundariesUnsetException("Undefined interval boundaries");
        return this.max.longValue();
    }

    @Override
    public Interval<BigDecimal> getCopyInstance() {
        
        BigDecimalInterval result = 
                new BigDecimalInterval(
                        this.getKey(), this.getType(), 
                        this.nFreshsToInsert, 
                        new LinkedList<ColumnPumper<BigDecimal>>(this.intervalColumns));
        result.updateMinEncodingAndValue(this.minEncoding);
        result.updateMaxEncodingAndValue(this.maxEncoding);
        
        return result;
    }

    @Override
    public void synchronizeMinMaxNFreshs() {
	
    }
    
}
