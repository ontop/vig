package it.unibz.inf.data_pumper.columns.injectors;

import it.unibz.inf.data_pumper.connection.DBMSConnection;
import it.unibz.inf.data_pumper.tables.Schema;

public interface Injector<T> {
    public void createNValues(Schema schema, DBMSConnection db, long n); // TODO Remove me
    public T translate(long index, T value);
};