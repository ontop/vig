package it.unibz.inf.data_pumper.column_types.intervals;

import it.unibz.inf.data_pumper.basic_datatypes.MySqlDatatypes;
import it.unibz.inf.data_pumper.column_types.ColumnPumper;
import it.unibz.inf.data_pumper.column_types.exceptions.BoundariesUnsetException;

import java.math.BigDecimal;
import java.util.List;

public class BigDecimalInterval extends Interval<BigDecimal> {

    public BigDecimalInterval(
            String key,
            MySqlDatatypes type,
            long nValues, List<ColumnPumper> involvedColumns) {
        super(key, type, nValues, involvedColumns);
    }

    @Override
    public void updateMinValueByEncoding(
            long newMin) {
        this.min = new BigDecimal(newMin);
    }

    @Override
    public void updateMaxValueByEncoding(
            long newMax) {
        this.max = new BigDecimal(newMax);
    }

    @Override
    public long getMinEncoding()
            throws BoundariesUnsetException {
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
    public Interval<? extends Object> getCopyInstance() {
        
        BigDecimalInterval result = new BigDecimalInterval(this.getKey(), this.getType(), this.nFreshsToInsert, this.intervalColumns);
        result.updateMinValueByEncoding(this.minEncoding);
        result.updateMaxValueByEncoding(this.maxEncoding);
        result.minEncoding = this.minEncoding;
        result.maxEncoding = this.maxEncoding;
        
        return result;
    }
    
}
