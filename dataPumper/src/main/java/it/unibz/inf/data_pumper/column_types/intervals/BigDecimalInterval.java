package it.unibz.inf.data_pumper.column_types.intervals;

import it.unibz.inf.data_pumper.basic_datatypes.MySqlDatatypes;
import it.unibz.inf.data_pumper.column_types.exceptions.BoundariesUnsetException;

import java.math.BigDecimal;

public class BigDecimalInterval extends Interval<BigDecimal> {

    public BigDecimalInterval(
            String key,
            MySqlDatatypes type,
            long nValues) {
        super(key, type, nValues);
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

}
