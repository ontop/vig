package columnTypes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import basicDatatypes.MySqlDatatypes;
import basicDatatypes.Schema;
import connection.DBMSConnection;
import core.ChasePicker;

public class StringColumn extends IncrementableColumn<String> {
	
	// For random generation of fixed size
	private List<Integer> rndIndexes;
	private String characters = "0123456789abcdefghijklmnopqrstuvwxyz"; // Ordered from the least to the bigger (String.compareTo)
	
	private int backupIndex = 0;
	
	public StringColumn(String name, MySqlDatatypes type, int index){
		super(name, type, index);
		
		rndIndexes = new ArrayList<Integer>(datatypeLength);
		
		for( int i = 0; i < datatypeLength; ++i )
			rndIndexes.add(0);

		cP = new ChasePicker(this);
	}
	
	public StringColumn(String name, MySqlDatatypes type, int index, int datatypeLength){
		super(name, type, index);
		
		this.datatypeLength = datatypeLength;
		rndIndexes = new ArrayList<Integer>(datatypeLength);
		
		for( int i = 0; i < datatypeLength; ++i )
			rndIndexes.add(0);

		cP = new ChasePicker(this);
	}

	@Override
	public void fillDomain(Schema schema, DBMSConnection db) {
		PreparedStatement stmt = db.getPreparedStatement("SELECT DISTINCT "+getName()+ " FROM "+schema.getTableName()+" WHERE CHAR_LENGTH("+getName()+")="+datatypeLength);
		
		List<String> values = null;
		
		try {
			ResultSet result = stmt.executeQuery();
			
			values = new ArrayList<String>();
			
			while( result.next() ){
				values.add(result.getString(1));
			}
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		setDomain(values);
	}

	@Override
	public void fillDomainBoundaries(Schema schema, DBMSConnection db) {
		
		if( domain == null )
			fillDomain(schema, db);
		
		if( lastFreshInserted != null ) return;
		
		if( domain.size() != 0 ){
			min = domain.get(0);
			max = domain.get(domain.size()-1);
		}
		else{
			min = lowerBoundValue();
			max = upperBoundValue();
		}
		setLastFreshInserted(min);
	}

	@Override
	public String getNextChased(DBMSConnection db, Schema schema) {
		
		String result = cP.pickChase(db, schema);
		
		if( result == null ) return null;
		
		if( result.compareTo(lastFreshInserted) > 0 )
			lastFreshInserted = result;
		
		return result;
	}

	@Override
	public String increment(String toIncrement) {
		
		StringBuilder builder = new StringBuilder(toIncrement);
		
		for( int i = toIncrement.length() -1; i >= 0; --i ){
			
			if( toIncrement.substring(i, i+1).compareTo(upperBoundValue().substring(0, 1)) < 0 ){
				builder.replace(i, i+1, characters.charAt(characters.indexOf(toIncrement.charAt(i)) + 1)+"");
				return builder.toString();
			}
			int j = i;
			
			while( j >= 0 && toIncrement.charAt(j) == upperBoundValue().charAt(0) ){
				builder.replace(j, j+1, lowerBoundValue().substring(0, 1));
				--j;
			}
			if( j >= 0 ){
				builder.replace(j, j+1, characters.charAt(characters.indexOf(toIncrement.charAt(j)) + 1)+"");
				return builder.toString();
			}
		} // Available symbols are finished. Put a duplicate.
		logger.info("NOT POSSIBLE TO ADD A FRESH VALUE. RE-GENERATING");
		
		return characters.charAt(++backupIndex % characters.length())+"";
	}
	
	@Override
	public String getCurrentMax() {
		if( domain.size() == 0 )
			return upperBoundValue();
		return domainIndex < domain.size() ? domain.get(domainIndex) : domain.get(domainIndex -1);
	}
	
	private String lowerBoundValue(){
		StringBuilder builder = new StringBuilder();
		
		for( int i = 0; i < datatypeLength; ++i ){
			builder.append(characters.charAt(0)); // Minimum
		}
		
		return builder.toString();
	}
	
	private String upperBoundValue(){
		StringBuilder builder = new StringBuilder();
		
		for( int i = 0; i < datatypeLength; ++i ){
			builder.append(characters.charAt(characters.length()-1)); // Maximum
		}
		
		return builder.toString();
	}
	
}
