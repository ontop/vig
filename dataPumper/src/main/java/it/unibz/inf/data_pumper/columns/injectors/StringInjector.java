package it.unibz.inf.data_pumper.columns.injectors;

import java.util.ArrayList;
import java.util.List;

import it.unibz.inf.data_pumper.columns.CyclicGroupGenerator;
import it.unibz.inf.data_pumper.columns.intervals.Interval;
import it.unibz.inf.data_pumper.connection.DBMSConnection;
import it.unibz.inf.data_pumper.tables.Schema;

public class StringInjector extends StandardInjector<String> {
    
    public StringInjector(long numFreshs, long numNulls, CyclicGroupGenerator gen, List<Interval<?>> intervals)  {
	super(numFreshs, numNulls, gen, intervals);
    }
    
    @Override
    public void createNValues(Schema schema, DBMSConnection db, long n) {
    }

    @Override
    public String translate(long index, String value) {
	// TODO Auto-generated method stub
	return null;
    }
}
