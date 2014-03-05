package columnTypes;

import java.util.ArrayList;
import java.util.List;

import basicDatatypes.MySqlDatatypes;
import basicDatatypes.Schema;
import connection.DBMSConnection;

public class StringColumn extends Column {
	
	private List<String> domain;
	
	// For random generation of fixed size
	private List<Integer> rndIndexes;
	private String characters = "0123456789abcdefghijklmnopqrstuvwxyz";
	private int nChar;
	
	public StringColumn(String name, MySqlDatatypes type, int index){
		super(name, type, index);
		
		rndIndexes = new ArrayList<Integer>(datatypeLength);
		
		for( int i = 0; i < datatypeLength; ++i )
			rndIndexes.add(0);

		nChar = characters.length();
	}
	
	public StringColumn(String name, MySqlDatatypes type, int index, int datatypeLength){
		super(name, type, index);
		
		this.datatypeLength = datatypeLength;
		rndIndexes = new ArrayList<Integer>(datatypeLength);
		
		for( int i = 0; i < datatypeLength; ++i )
			rndIndexes.add(0);

		nChar = characters.length();
	}

	@Override
	public String getNextFreshValue() {
		
		if( rndIndexes.get(rndIndexes.size()-1) == characters.indexOf("A") ){
			logger.error("DEBUG");
		}
		
		for( int i = datatypeLength -1; i >= 0; --i ){
			if( rndIndexes.get(i) < nChar -1 ){
				rndIndexes.set(i, rndIndexes.get(i) + 1);
				break;
			}
			rndIndexes.set(i, 0);
		}
		
		StringBuilder builder = new StringBuilder();
		
		for( Integer i : rndIndexes ){
			builder.append(characters.charAt(i));
		}
		
		return builder.toString();
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
