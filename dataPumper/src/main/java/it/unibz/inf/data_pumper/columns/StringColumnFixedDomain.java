package it.unibz.inf.data_pumper.columns;

import java.util.List;

import it.unibz.inf.data_pumper.columns.intervals.Interval;
import it.unibz.inf.data_pumper.columns.intervals.StringInterval;
import it.unibz.inf.data_pumper.tables.MySqlDatatypes;
import it.unibz.inf.data_pumper.tables.Schema;

public class StringColumnFixedDomain extends StringColumn {

    
    public StringColumnFixedDomain(String name, MySqlDatatypes type, int index,
	    int datatypeLength, Schema schema) {
	super(name, type, index, datatypeLength, schema);
    }

    @Override
    protected Interval<String> getIntervalInstance(
	    String qualifiedName, List<ColumnPumper<String>> involvedCols){
	Interval<String> interval = new StringInterval(qualifiedName, this.getType(), this.numFreshsToInsert, this.datatypeLength, involvedCols);
	return interval;
    }
    
}
