package columnTypes;

import basicDatatypes.Schema;
import connection.DBMSConnection;

public interface FreshValuesGenerator {
	
	public String getNextFreshValue();
	public String getNextChased(DBMSConnection db, Schema schema);
	public String getFromReferenced(DBMSConnection db, Schema schema);
	
	public void fillDomain(Schema schema, DBMSConnection db);
	public void fillDomainBoundaries(Schema schema, DBMSConnection db);
	
	public int getCurrentChaseCycle();
	public void incrementCurrentChaseCycle();
	public int getMaximumChaseCycles();
	public void setMaximumChaseCycles(int maximumChaseCycles);
	public abstract boolean hasNextChase();
	public abstract void refillCurChaseSet(DBMSConnection conn, Schema s);
	
	// Duplicates handling
	public abstract void fillDuplicates(DBMSConnection dbmsConn, Schema schema, int insertedRows);
	public abstract String pickNextDupFromDuplicatesToInsert();
	public abstract void beforeFirstDuplicatesToInsert();

	public void reset(); // To reset the internal state

}
