package it.unibz.inf.data_pumper.column_types.intervals;

import it.unibz.inf.data_pumper.basic_datatypes.MySqlDatatypes;
import it.unibz.inf.data_pumper.column_types.exceptions.BoundariesUnsetException;

public class StringInterval extends Interval<String> {

    public StringInterval(String key,
            MySqlDatatypes type,
            long nValues) {
        super(key, type, nValues);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void updateMinValueByEncoding(
            long newMin) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void updateMaxValueByEncoding(
            long newMax) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public long getMinEncoding()
            throws BoundariesUnsetException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getMaxEncoding()
            throws BoundariesUnsetException {
        // TODO Auto-generated method stub
        return 0;
    }

}
