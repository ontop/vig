package basicDatatypes;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import basicDatatypes.MySqlDatatypes;

public class Schema{
	private Map<String, MySqlDatatypes> map;
	private Map<String, Boolean> mAllDifferentFlags;
	private Map<String, Domain<?>> domains = null;
	private Map<String, String> mForeignKeys;
	private Map<String, Integer> mMaximumChaseCreation;
	private final String tableName;  // Final in order to avoid the well-known "mutability" problem with the <i>equals</i> method.
	private List<String> orderedKeys = null;
	public Schema(String tableName){
		this.tableName = tableName;
		map = new HashMap<String, MySqlDatatypes>();
		mAllDifferentFlags = new HashMap<String, Boolean>();
		mForeignKeys = new HashMap<String, String>();
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
	public String getTableName(){
		return tableName;
	}
	
	public int getNumColumns(){
		return map.keySet().size();
	}
	
	public void setAllDifferent(String colName, boolean value){
		mAllDifferentFlags.put(colName, value);
	}
	public boolean allDifferent(String colName){
		return mAllDifferentFlags.get(colName);
	}
	
	public String toString(){
		String fks = "\nFOREIGN KEYS: " + mForeignKeys.toString();
		return map.toString() + fks;
	}
	
	public boolean hasDomain(String colName){
		return domains.containsKey(colName);
	}
	
	public String getReferencedTable(String fk){
		String tableDotColumn = mForeignKeys.get(fk);
		return tableDotColumn.substring(0, tableDotColumn.indexOf("."));
	}
	
	public String getReferencedColumn(String fk){
		String tableDotColumn = mForeignKeys.get(fk);
		return tableDotColumn.substring(tableDotColumn.indexOf(".") + 1, tableDotColumn.length());
	}
	
	public void setForeignKey(String fk, String refColumn, String refTable){
		mForeignKeys.put(fk, refTable + "." + refColumn);
	}
	
	public List<String> getFks(){
		return new ArrayList<String>(mForeignKeys.keySet());
	}

	public boolean isIndependent() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean equals(Object s){
		if(! (s instanceof Schema) ) return false;
		
		return this.getTableName().equals(((Schema)s).getTableName());
	}
	@Override
	public int hashCode(){
		return this.getTableName().hashCode();
	}
}