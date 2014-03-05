package columnTypes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import basicDatatypes.MySqlDatatypes;
import basicDatatypes.Schema;
import basicDatatypes.Template;
import connection.DBMSConnection;

public class IntColumn extends IncrementableColumn<Long> {
	
	public IntColumn(String name, MySqlDatatypes type, int index) {
		super(name, type, index);
		domain = null;
		this.max = Long.valueOf(0);
		this.min = Long.valueOf(0);
		this.lastInserted = Long.valueOf(0);
		
		index = 0;
	}
	
	@Override
	public String getNextFreshValue(){
		Long toInsert = this.getLastInserted();
		
		do{
			toInsert = increment(toInsert);
			
			while( toInsert.compareTo(this.getCurrentMax()) == 1 && this.hasNextMax() )
				this.nextMax();
		}
		while(toInsert.compareTo(this.getCurrentMax()) == 0);
		
//		while( ++toInsert >= this.getCurrentMax() && this.hasNextMax() ) this.nextMax();
		
		this.setLastInserted(toInsert);
		
		return Long.toString(toInsert);
	}

	@Override
	public void fillDomain(Schema schema, DBMSConnection db) {
		PreparedStatement stmt = db.getPreparedStatement("SELECT DISTINCT "+getName()+ " FROM "+schema.getTableName()+" LIMIT 100000");
		
		List<Long> values = null;
		
		try {
			ResultSet result = stmt.executeQuery();
			
			values = new ArrayList<Long>();
		
			while( result.next() ){
				values.add(result.getLong(1));
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
				setMinValue(result.getLong(1));
				setMaxValue(result.getLong(2));
				setLastInserted(result.getLong(1));
			}
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Long increment(Long toIncrement) {
		return ++toIncrement;
	}

	@Override
	public Long getCurrentMax() {
		if( domain.size() == 0 )
			return Long.MAX_VALUE;
		return domainIndex < domain.size() ? domain.get(domainIndex) : domain.get(domainIndex -1);
	}
}
