package columnTypes;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import basicDatatypes.MySqlDatatypes;
import basicDatatypes.Schema;
import basicDatatypes.Template;
import connection.DBMSConnection;

public class BigDecimalColumn extends IncrementableColumn<BigDecimal>{
		
	public BigDecimalColumn(String name, MySqlDatatypes type, int index, int datatypeFirstLength) {
		super(name, type, index);
		domain = null;
		this.max = null;
		this.min = null;
		this.lastFreshInserted = null;
	}
	
	public BigDecimalColumn(String name, MySqlDatatypes type, int index) {
		super(name, type, index);
		domain = null;
		this.max = null;
		this.min = null;
		this.lastFreshInserted = null;
	}
	
	@Override
	public void fillDomain(Schema schema, DBMSConnection db) {
		PreparedStatement stmt = db.getPreparedStatement("SELECT DISTINCT "+getName()+ " FROM "+schema.getTableName()+ " WHERE "+getName()+" IS NOT NULL");
		
		List<BigDecimal> values = null;
		
		try {
			ResultSet result = stmt.executeQuery();
			
			values = new ArrayList<BigDecimal>();
		
			while( result.next() ){
				values.add(BigDecimal.valueOf(result.getDouble(1)));
			}
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		setDomain(values);
	}

	@Override
	public void fillDomainBoundaries(Schema schema, DBMSConnection db) {
		
		if( lastFreshInserted != null ) return; // Boundaries already filled
		
		Template t = new Template("select ? from "+schema.getTableName()+";");
		PreparedStatement stmt;
		
		t.setNthPlaceholder(1, "min("+getName()+"), max("+getName()+")");
		
		stmt = db.getPreparedStatement(t);
		
		ResultSet result;
		try {
			result = stmt.executeQuery();
			if( result.next() ){
				setMinValue(BigDecimal.valueOf(result.getDouble(1)));
				setMaxValue(BigDecimal.valueOf(result.getDouble(2)));
				setLastFreshInserted(BigDecimal.valueOf(result.getDouble(1)));
			}
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public BigDecimal increment(BigDecimal toIncrement) {
		
		toIncrement = toIncrement.add(BigDecimal.ONE);
		
		return toIncrement;
	}

	@Override
	public BigDecimal getCurrentMax() {
		if( domain.size() == 0 )
			return BigDecimal.valueOf(Double.MAX_VALUE);
		return domainIndex < domain.size() ? domain.get(domainIndex) : domain.get(domainIndex -1);
	}
	
	@Override
	public String getNextChased(DBMSConnection db, Schema schema) {
		
		String result = cP.pickChase(db, schema);
		
		if( result == null ) return null;
		
		BigDecimal resultBD = new BigDecimal(result);
		if( resultBD.compareTo(lastFreshInserted) > 0 ){
			lastFreshInserted = resultBD;
		}
		
		return result;
	}
}
