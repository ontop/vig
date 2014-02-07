package basicDatatypes;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Schema{
	private Map<String, Integer> map;
	private Map<String, Boolean> mAllDifferentFlags;
	private String tableName;
	
	public Schema(){
		map = new HashMap<String, Integer>();
	}
	
	public void addMap(String colName, int colType){
		map.put(colName, colType);
	}
	
	public int getType(String colName){
		return map.get(colName);
	}
	
	public Set<String> getColNames(){
		return map.keySet();
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
}