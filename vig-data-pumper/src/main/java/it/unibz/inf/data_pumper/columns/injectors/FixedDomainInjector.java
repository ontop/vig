package it.unibz.inf.data_pumper.columns.injectors;

import it.unibz.inf.data_pumper.connection.DBMSConnection;
import it.unibz.inf.data_pumper.tables.Schema;

public class FixedDomainInjector<T> implements Injector<T> {

    @Override
    public void createNValues(Schema schema, DBMSConnection db, long n) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public T translate(long index, T value) {
	// TODO Auto-generated method stub
	return null;
    }
    
}
