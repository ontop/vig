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
	public BigDecimalColumn(String name, MySqlDatatypes type, int index) {
		super(name, type, index);
		domain = null;
		this.max = BigDecimal.ZERO;
		this.min = BigDecimal.ZERO;
		this.lastInserted = BigDecimal.ZERO;
		
		index = 0;
	}
	
//	@Override
//	public String getNextFreshValue(){
//		
//		BigDecimal toInsert = this.getLastInserted();
//		
//		if( toInsert == null ) logger.error(this.toString() +" toInsert is NULL");
//
//		do{
//			toInsert = increment(toInsert);
//			
//			while( toInsert.compareTo(this.getCurrentMax()) == 1 && this.hasNextMax() )
//				this.nextMax();
//		}
//		while(toInsert.compareTo(this.getCurrentMax()) == 0);
//		
////		while( (toInsert = increment(toInsert)).compareTo(this.getCurrentMax()) > -1 && this.hasNextMax() ) this.nextMax(); 
//		
//		this.setLastInserted(toInsert);
//		
//		return toInsert.toString();
//	}
	
//	@Override
//	public String getNextFreshValue(){
//		BigDecimal toInsert = this.getLastInserted();
//		
//		while( (toInsert = toInsert + BigDecimal.ONE) >= this.getCurrentMax() && this.hasNextMax() ) this.nextMax();
//		
//		this.setLastInserted(toInsert);
//		
//		return Double.toString(toInsert);
//	}

	@Override
	public void fillDomain(Schema schema, DBMSConnection db) {
		PreparedStatement stmt = db.getPreparedStatement("SELECT DISTINCT "+getName()+ " FROM "+schema.getTableName()+" LIMIT 100000");
		
		List<BigDecimal> values = null;
		
		try {
			ResultSet result = stmt.executeQuery();
			
			values = new ArrayList<BigDecimal>();
		
			while( result.next() ){
				values.add(BigDecimal.valueOf(result.getDouble(1)));
//				values.add(new BigDecimal(result.getString(1)));
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
//				setMinValue(new BigDecimal(result.getString(1)));
//				setMaxValue(new BigDecimal(result.getString(2)));
//				setLastInserted(new BigDecimal(result.getString(1)));
				setMinValue(BigDecimal.valueOf(result.getDouble(1)));
				setMaxValue(BigDecimal.valueOf(result.getDouble(2)));
				setLastInserted(BigDecimal.valueOf(result.getDouble(1)));
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
}
