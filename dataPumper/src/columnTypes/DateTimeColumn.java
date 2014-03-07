package columnTypes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import basicDatatypes.MySqlDatatypes;
import basicDatatypes.Schema;
import basicDatatypes.Template;
import connection.DBMSConnection;

public class DateTimeColumn extends IncrementableColumn<Timestamp>{
	
	private enum Granularity{YEAR, MONTH, DAY, HOUR, MINUTE, SECOND};
	
	// 2013-04-09 00:00:00
	
	private Granularity granularity; 
	private int skip;
	
	public DateTimeColumn(String name, MySqlDatatypes type, int index) {
		super(name, type, index);
		
		lastFreshInserted = null;
		
		granularity = Granularity.DAY;
		skip = 1;
	}

	@Override
	public void fillDomain(Schema schema, DBMSConnection db) {
		PreparedStatement stmt = db.getPreparedStatement("SELECT DISTINCT "+getName()+ " FROM "+schema.getTableName());
		
		List<Timestamp> values = null;
		
		try {
			ResultSet result = stmt.executeQuery();
			
			values = new ArrayList<Timestamp>();
		
			while( result.next() ){
				values.add(result.getTimestamp(1));
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
		try{
			ResultSet result = stmt.executeQuery();
			
			if( result.next() && (result.getTimestamp(1) != null) ){
				setMinValue(result.getTimestamp(1));
				setMaxValue(result.getTimestamp(2));
				setLastFreshInserted(result.getTimestamp(1));
			}
			else{
				setMinValue(new Timestamp(0));
				setMaxValue(new Timestamp(Long.MAX_VALUE));
				setLastFreshInserted(new Timestamp(0));
			}
			stmt.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}
	}

	@Override
	public Timestamp increment(Timestamp toIncrement) {
		
		long value = toIncrement.getTime();
		
		switch( granularity ){
		case YEAR:{
			value += 364 * 30 * 24 *3600000 * skip; // TODO This is an approximation
			break;
		}
		case MONTH:{
			value += 30 * 24 *3600000 * skip;
			break;
		}
		case DAY:{
			value += 24 *3600000 * skip;
			break;
		}
		case HOUR:{
			value += 3600000 * skip;
			break;
		}
		case MINUTE:{
			value += 60000 * skip;
			break;
		}
		case SECOND:{
			value += 10000 * skip;
			break;
		}
		default:
			break;
		
		}
		return new Timestamp(value);
	}

	@Override
	public Timestamp getCurrentMax() {
		if( domain.size() == 0 )
			return new Timestamp(Long.MAX_VALUE);
		return domainIndex < domain.size() ? domain.get(domainIndex) : domain.get(domainIndex -1);
	}

	@Override
	public String getNextChased(DBMSConnection db, Schema schema) {
		
		String result = cP.pickChase(db, schema);
		
		if( result == null ) return null;

		Timestamp chased = new Timestamp(Long.parseLong(result));
		
		if( chased.compareTo(lastFreshInserted) > 0 )
			lastFreshInserted = chased;
		
		return result;
	}
};