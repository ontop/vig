package it.unibz.inf.data_pumper.column_types.intervals;

import java.util.LinkedList;
import java.util.List;

import it.unibz.inf.data_pumper.basic_datatypes.MySqlDatatypes;
import it.unibz.inf.data_pumper.column_types.ColumnPumper;
import it.unibz.inf.data_pumper.column_types.exceptions.BoundariesUnsetException;

public class IntInterval extends Interval<Long> {

    public IntInterval(String key,
            MySqlDatatypes type,
            long nValues, List<ColumnPumper<Long>> involvedCols) {
        super(key, type, nValues, involvedCols);
    }

    @Override
    public void updateMinEncodingAndValue(long newMin) {
        this.min = newMin;
        this.minEncoding = newMin;
    }
    
    @Override
    public void updateMaxEncodingAndValue(long newMax) {
        this.max = newMax;
        this.maxEncoding = newMax;
    }
    
    @Override
    public long getMinEncoding() throws BoundariesUnsetException {
        if( this.min == this.max ) throw new BoundariesUnsetException("Undefined interval boundaries");
        return min.longValue();
    }
    
    @Override
    public long getMaxEncoding() throws BoundariesUnsetException {
        if( this.min == this.max ) throw new BoundariesUnsetException("Undefined interval boundaries");
        return this.max.longValue();
    }

    @Override
    public Interval<Long> getCopyInstance() {
        
        IntInterval result = 
                new IntInterval(
                        this.getKey(), this.getType(), 
                        this.nFreshsToInsert, new LinkedList<>(this.intervalColumns));
        result.updateMinEncodingAndValue(this.minEncoding);
        result.updateMaxEncodingAndValue(this.maxEncoding);
        result.minEncoding = this.minEncoding;
        result.maxEncoding = this.maxEncoding;
        
        return result;
    }
}
