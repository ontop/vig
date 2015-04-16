package it.unibz.inf.data_pumper.column_types.intervals;

import java.util.LinkedList;
import java.util.List;

import it.unibz.inf.data_pumper.basic_datatypes.MySqlDatatypes;
import it.unibz.inf.data_pumper.column_types.ColumnPumper;
import it.unibz.inf.data_pumper.column_types.exceptions.BoundariesUnsetException;

public class IntInterval extends Interval<Long> {

    public IntInterval(String key,
            MySqlDatatypes type,
            long nValues, List<ColumnPumper> involvedCols) {
        super(key, type, nValues, involvedCols);
    }

    @Override
    public void updateMinValueByEncoding(long newMin) {
        this.min = newMin;
    }
    
    @Override
    public void updateMaxValueByEncoding(long newMax) {
        this.max = newMax;
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
    public Interval<? extends Object> getCopyInstance() {
        
        IntInterval result = new IntInterval(this.getKey(), this.getType(), this.nFreshsToInsert, this.intervalColumns);
        result.updateMinValueByEncoding(this.minEncoding);
        result.updateMaxValueByEncoding(this.maxEncoding);
        result.minEncoding = this.minEncoding;
        result.maxEncoding = this.maxEncoding;
        
        return result;
    }

    @Override
    public List<Interval<? extends Object>> split(
            Interval<? extends Object> toInsert) {
        
        List<Interval<? extends Object>> result = new LinkedList<Interval<? extends Object>>();
         
        return result;
    }
}
