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

public class DoubleColumn extends IncrementableColumn<Double>{
	
	public DoubleColumn(String name, MySqlDatatypes type, int index) {
		super(name, type, index);
		domain = null;
		this.max = 0.0;
		this.min = 0.0;
		this.lastInserted = 0.0;
		
		index = 0;
	}
	
	@Override
	public String getNextFreshValue(){
		double toInsert = this.getLastInserted();
		
		while( ++toInsert >= this.getCurrentMax() && this.hasNextMax() ) this.nextMax();
		
		this.setLastInserted(toInsert);
		
		return Double.toString(toInsert);
	}

	@Override
	public void fillDomain(Schema schema, DBMSConnection db) {
		PreparedStatement stmt = db.getPreparedStatement("SELECT DISTINCT "+getName()+ " FROM "+schema.getTableName()+" LIMIT 100000");
		
		List<Double> values = null;
		
		try {
			ResultSet result = stmt.executeQuery();
			
			values = new ArrayList<Double>();
		
			while( result.next() ){
				values.add(result.getDouble(1));
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
				setMinValue(result.getDouble(1));
				setMaxValue(result.getDouble(2));
				setLastInserted(result.getDouble(1));
			}
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Double increment(Double toIncrement) {
		return ++toIncrement;
	}

	@Override
	public Double getCurrentMax() {
		if( domain.size() == 0 )
			return Double.MAX_VALUE;
		return domainIndex < domain.size() ? domain.get(domainIndex) : domain.get(domainIndex -1);
	}
}
