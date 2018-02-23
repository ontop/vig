package it.unibz.inf.data_pumper.columns.intervals;

import it.unibz.inf.data_pumper.columns.ColumnPumper;
import it.unibz.inf.data_pumper.tables.MySqlDatatypes;

import java.util.List;

public abstract class StringInterval extends Interval<String> {

    public StringInterval(String key, MySqlDatatypes type, long nValues,
	    List<ColumnPumper<String>> intervalColumns) {
	super(key, type, nValues, intervalColumns);
    }
    
    public StringInterval(String name, MySqlDatatypes type, long minEncoding,
	    long maxEncoding) {
	super(name, type, minEncoding, maxEncoding);
    }
    
    public abstract String encode(long value);
    public abstract String lowerBoundValue();
    protected abstract String upperBoundValue();

}
