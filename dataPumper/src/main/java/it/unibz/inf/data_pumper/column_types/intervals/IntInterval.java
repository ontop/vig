package it.unibz.inf.data_pumper.column_types.intervals;

import it.unibz.inf.data_pumper.basic_datatypes.MySqlDatatypes;
import it.unibz.inf.data_pumper.column_types.exceptions.BoundariesUnsetException;

public class IntInterval extends Interval<Long> {

    public IntInterval(String key,
            MySqlDatatypes type,
            long nValues) {
        super(key, type, nValues);
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
}
