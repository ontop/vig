package it.unibz.inf.data_pumper.column_types.intervals;

import it.unibz.inf.data_pumper.basic_datatypes.MySqlDatatypes;
import it.unibz.inf.data_pumper.column_types.ColumnPumper;
import it.unibz.inf.data_pumper.column_types.exceptions.BoundariesUnsetException;

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

    @Override
    public void updateMinEncodingAndValue(
            long newMin) {
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
    public Interval<BigDecimal> getCopyInstance() {
        
        BigDecimalInterval result = 
                new BigDecimalInterval(
                        this.getKey(), this.getType(), 
                        this.nFreshsToInsert, 
                        new LinkedList<ColumnPumper<BigDecimal>>(this.intervalColumns));
        result.updateMinEncodingAndValue(this.minEncoding);
        result.updateMaxEncodingAndValue(this.maxEncoding);
        result.minEncoding = this.minEncoding;
        result.maxEncoding = this.maxEncoding;
        
        return result;
    }
    
}
