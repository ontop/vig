package basicDatatypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import basicDatatypes.MySqlDatatypes;

public class Schema{
	private Map<String, MySqlDatatypes> map;
	private Map<String, Boolean> mAllDifferentFlags;
	private Map<String, Domain<?>> domains;
	private String tableName;
	private List<String> orderedKeys = null;
	
	public Schema(){
		map = new HashMap<String, MySqlDatatypes>();
	}
	
	public void addField(String colName, String fieldName){
		if( fieldName.startsWith("int") ) map.put(colName, MySqlDatatypes.INT);
		else if( fieldName.startsWith("char") ) map.put(colName, MySqlDatatypes.CHAR);
		else if( fieldName.startsWith("varchar") ) map.put(colName, MySqlDatatypes.VARCHAR);
		else if( fieldName.startsWith("text") ) map.put(colName, MySqlDatatypes.TEXT);
		else if( fieldName.startsWith("longtext") ) map.put(colName, MySqlDatatypes.LONGTEXT);
		else if( fieldName.startsWith("datetime") ) map.put(colName, MySqlDatatypes.DATETIME);
		else if( fieldName.startsWith("point") ) map.put(colName, MySqlDatatypes.POINT);
		else if( fieldName.startsWith("linestring") ) map.put(colName, MySqlDatatypes.LINESTRING);
		else if( fieldName.startsWith("multilinestring") ) map.put(colName, MySqlDatatypes.MULTILINESTRING);
		else if( fieldName.startsWith("polygon") ) map.put(colName, MySqlDatatypes.POLYGON);
		else if( fieldName.startsWith("multipolygon") ) map.put(colName, MySqlDatatypes.MULTIPOLYGON);
	}
	public void setDomains(Map<String, Domain<?>> domains){
		this.domains = domains;
	}
	public Domain<?> getDomain(String colName){
		if( domains.containsKey(colName) )
			return domains.get(colName);
		return null;
	}
	public MySqlDatatypes getType(String colName){
		return map.get(colName);
	}
	/**
	 * Returns a list of the column names
	 * @return
	 */
	public List<String> getColNames(){
		if( orderedKeys == null ){
			orderedKeys = new ArrayList<String>(map.keySet());
		}
		return orderedKeys;
	}
	
	public void setTableName(String tableName){
		this.tableName = tableName;
	}
	
	public String getTableName(){
		return tableName;
	}
	
	public int getNumColumns(){
		return map.keySet().size();
	}
	
	public boolean allDifferent(String colName){
		return mAllDifferentFlags.get(colName);
	}
	
	public String toString(){
		return map.toString();
	}
	
	public boolean hasDomain(String colName){
		return domains.containsKey(colName);
	}

	public boolean isIndependent() {
		// TODO Auto-generated method stub
		return false;
	}
}