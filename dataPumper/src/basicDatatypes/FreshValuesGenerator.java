package basicDatatypes;

import connection.DBMSConnection;

public interface FreshValuesGenerator {
	
	public <T extends Comparable<T>> String getNextFreshValue();
	
	public void fillDomain(Schema schema, DBMSConnection db);
	public void fillDomainBoundaries(Schema schema, DBMSConnection db);

	public void reset(); // To reset the internal state

}
