package basicDatatypes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import connection.DBMSConnection;

public class IntColumn extends Column {
	
	private List<Integer> domain;
	private int domainIndex;
	
	private int maxValue;
	private int minValue;
	
	private int lastInserted; 
	
	public IntColumn(String name, MySqlDatatypes type, int index) {
		super(name, type, index);
		domain = null;
		this.maxValue = 0;
		this.minValue = 0;
		this.lastInserted = 0;
		
		index = 0;
	}
	
	public void setLastInserted(int lastInserted){
		this.lastInserted = lastInserted;
	}
	
	public Integer getLastInserted(){
		return new Integer(lastInserted);
	}
	
	public void setDomain(List<Integer> domain){
		if( this.domain == null ){
			this.domain = domain;
			Collections.sort(domain);
		}
		
		// For memory reasons, there is an hard-limit about what the
		// rows fetched from the database and that constitute the 
		// 'domain' vector. Hence, the maximum value of the domain
		// might NOT BE the maximum in the column.
		if(domain.size() == 0) return;
		if( domain.get(domain.size() -1) < maxValue )
			domain.set(domain.size() -1, maxValue);
	}
	
	public int getCurrentMax(){
		if( domain.size() == 0 )
			return Integer.MAX_VALUE;
		return domainIndex < domain.size() ? domain.get(domainIndex) : domain.get(domainIndex -1);
	}
	
	public void nextMax(){
		++domainIndex;
	}
	
	public boolean hasNextMax(){
		return domainIndex < domain.size();
	}
	
	public void setMaxValue(int max){
		maxValue = max;
	}
	
	public int getMaxValue(){
		return maxValue;
	}
	
	public void setMinValue(int min){
		minValue = min;
	}
	
	public int getMinValue(){
		return minValue;
	}
	
	@Override
	public String getNextFreshValue(){
		int allDiffCnt = this.getLastInserted();
		
		while( ++allDiffCnt >= this.getCurrentMax() && this.hasNextMax() ) this.nextMax();
		
		this.setLastInserted(allDiffCnt);
		
		return Integer.toString(allDiffCnt);
	}
	
	@Override
	/** This method has to be called whenever information held for the column can be released **/
	public void reset(){
		if( domain != null ) domain.clear();
		domainIndex = 0;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public <T> T getCurrentMax() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T increment(T value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected <T> void setLastInserted(T value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected <T> T getLastInserted() {
		// TODO Auto-generated method stub
		return null;
	}
}
