package basicDatatypes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import connection.DBMSConnection;

public class IntColumn extends IncrementableColumn<Integer> {
	
	public IntColumn(String name, MySqlDatatypes type, int index) {
		super(name, type, index);
		domain = null;
		this.max = 0;
		this.min = 0;
		this.lastInserted = 0;
		
		index = 0;
	}
	
	@Override
	public String getNextFreshValue(){
		int toInsert = this.getLastInserted();
		
		while( ++toInsert >= this.getCurrentMax() && this.hasNextMax() ) this.nextMax();
		
		this.setLastInserted(toInsert);
		
		return Integer.toString(toInsert);
	}

	@Override
	public void fillDomain(Schema schema, DBMSConnection db) {
		PreparedStatement stmt = db.getPreparedStatement("SELECT DISTINCT "+getName()+ " FROM "+schema.getTableName()+" LIMIT 100000");
		
		List<Integer> values = null;
		
		try {
			ResultSet result = stmt.executeQuery();
			
			values = new ArrayList<Integer>();
		
			while( result.next() ){
				values.add(Integer.parseInt(result.getString(1)));
			}
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		setDomain(values);
	}

	@Override
	public void fillDomainBoundaries(Schema schema, DBMSConnection db) {
		
		Template t = new Template("select ? from "+schema.getTableName()+";");
		PreparedStatement stmt;
		
		t.setNthPlaceholder(1, "min("+getName()+"), max("+getName()+")");
		
		stmt = db.getPreparedStatement(t);
		
		ResultSet result;
		try {
			result = stmt.executeQuery();
			if( result.next() ){
				setMinValue(result.getInt(1));
				setMaxValue(result.getInt(2));
				setLastInserted(result.getInt(1));
			}
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Integer increment(Integer toIncrement) {
		return ++toIncrement;
	}

	@Override
	public Integer getCurrentMax() {
		if( domain.size() == 0 )
			return Integer.MAX_VALUE;
		return domainIndex < domain.size() ? domain.get(domainIndex) : domain.get(domainIndex -1);
	}
}
