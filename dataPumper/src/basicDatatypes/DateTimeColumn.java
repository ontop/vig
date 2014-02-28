package basicDatatypes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import connection.DBMSConnection;

public class DateTimeColumn extends Column{
	
	private enum Granularity{YEAR, MONTH, DAY, HOUR, MINUTE, SECOND};
	
	// 2013-04-09 00:00:00
	
	private Timestamp minDatetime;
	private Timestamp maxDatetime;
	
	private List<Timestamp> domain; 
	
	private int domainIndex;
	
	private Granularity granularity; //TODO Maybe in future we want to support this
	private int skip;
	
	private Timestamp lastInserted;
	
	public DateTimeColumn(String name, MySqlDatatypes type, int index) {
		super(name, type, index);
		
		domain = null;
		domainIndex = 0;
		lastInserted = null;
		
		granularity = Granularity.SECOND;
		skip = 1;
	}
	
	public Timestamp getLastInserted(){
		return lastInserted;
	}
	
	public void setLastInserted(Timestamp lastInserted){
		this.lastInserted = lastInserted;
	}
	
	public void setDomain(List<Timestamp> domain){
		if( this.domain == null ){
			this.domain = domain;
			Collections.sort(domain);
		}
		
		// For memory reasons, there is an hard-limit about what the
		// rows fetched from the database and that constitute the 
		// 'domain' vector. Hence, the maximum value of the domain
		// might NOT BE the maximum in the column.
		if( domain.size() == 0 ) return;
		if( domain.get(domain.size() -1).before(maxDatetime) )
			domain.set(domain.size() -1, maxDatetime);
	}	
	
	public long getCurrentMax(){
		if( domain.size() == 0 )
			return Long.MAX_VALUE;
		return domainIndex < domain.size() ? domain.get(domainIndex).getTime() : domain.get(domainIndex -1).getTime();
	}
	
	public void nextMax(){
		++domainIndex;
	}
	
	public boolean hasNextMax(){
		return domain == null ? false : domainIndex < domain.size();
	}
	
	public void setMaxValue(Timestamp max){
		maxDatetime = max;
	}
	
	public Timestamp getMaxValue(){
		return maxDatetime;
	}
	
	public void setMinValue(Timestamp min){
		minDatetime = min;
	}
	
	public Timestamp getMinValue(){
		return minDatetime;
	}
	
	private long increase(long time){
		switch( granularity ){
		case YEAR:{
			time += 364 * 30 * 24 *360000 * skip; // TODO This is an approximation
			break;
		}
		case MONTH:{
			time += 30 * 24 *360000 * skip;
			break;
		}
		case DAY:{
			time += 24 *360000 * skip;
			break;
		}
		case HOUR:{
			time += 360000 * skip;
			break;
		}
		case MINUTE:{
			time += 6000 * skip;
			break;
		}
		case SECOND:{
			time += 1000 * skip;
			break;
		}
		default:
			break;
		
		}
		return time;
	}
	
	@Override
	/** This method has to be called whenever information held for the column can be released **/
	public void reset(){
		if( domain != null ) domain.clear();
		domainIndex = 0;
	}

	@Override
	public String getNextFreshValue() {
		long toInsert = getLastInserted().getTime();
		
		toInsert = increase(toInsert);
		
		while( toInsert >= getCurrentMax() && hasNextMax() ) nextMax();
		
		Timestamp toReturn = new Timestamp(toInsert);
		setLastInserted(toReturn);
		
		return toReturn.toString();
	}

	@Override
	public void fillDomain(Schema schema, DBMSConnection db) {
		PreparedStatement stmt = db.getPreparedStatement("SELECT DISTINCT "+getName()+ " FROM "+schema.getTableName()+" LIMIT 100000");
		
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
		
		Template t = new Template("select ? from "+schema.getTableName()+";");
		PreparedStatement stmt;
		
		t.setNthPlaceholder(1, "min("+getName()+"), max("+getName()+")");
		
		stmt = db.getPreparedStatement(t);
		try{
			ResultSet result = stmt.executeQuery();
			
			if( result.next() ){
				setMinValue(result.getTimestamp(1));
				setMaxValue(result.getTimestamp(2));
				setLastInserted(result.getTimestamp(1));
			}
			stmt.close();
		}
		catch(SQLException e){
			e.printStackTrace();
		}
	}	
};