package columnTypes;

import basicDatatypes.Schema;
import connection.DBMSConnection;

public interface FreshValuesGenerator {
	
	public String getNextFreshValue();
	
	public void fillDomain(Schema schema, DBMSConnection db);
	public void fillDomainBoundaries(Schema schema, DBMSConnection db);

	public void reset(); // To reset the internal state

}
