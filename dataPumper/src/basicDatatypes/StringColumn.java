package basicDatatypes;

import java.util.List;

import connection.DBMSConnection;

public class StringColumn extends Column {
	
	private List<String> domain;
	
	private long cnt;
	
	public StringColumn(String name, MySqlDatatypes type, int index){
		super(name, type, index);
		
		cnt = 0;
	}

	@Override
	public String getNextFreshValue() {
		return "randomString"+(++cnt);
	}

	@Override
	public void reset() {
		
	}

	@Override
	public void fillDomain(Schema schema, DBMSConnection db) {
		// TODO 
	}

	@Override
	public void fillDomainBoundaries(Schema schema, DBMSConnection db) {
		// TODO 
	}
	
	
}
