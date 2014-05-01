package mappings;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import utils.MyHashMapList;

public class TupleTemplate{
	private final String templatesString;
	
	private final MyHashMapList<String, String> mTableName_Columns;
	
	/**
	 * Sets up all the sub-structures
	 * @param id
	 */
	TupleTemplate(String templatesString, MyHashMapList<String, String> mTableName_Columns){ // It can be created only in this package
		this.templatesString = templatesString;
		this.mTableName_Columns = mTableName_Columns;
	}
	
	public String getTemplatesString(){
		return templatesString;
	}
	
	public Set<String>getReferredTables(){
		return Collections.unmodifiableSet(mTableName_Columns.keyset());
	}
	
	public List<String> getColumnsInTable(String tableName){
		if(this.mTableName_Columns.containsKey(tableName)){
			return Collections.unmodifiableList(mTableName_Columns.get(tableName));
		}
		else return null;
	}
	
	public String toString(){
		StringBuilder builder = new StringBuilder();
		
		builder.append("Template: " + templatesString + "\n");
		builder.append("Tables: " + mTableName_Columns.keyset().toString() + "\n");
		
		return builder.toString();
	}
};