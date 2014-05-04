package mappings;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import ch.qos.logback.classic.Logger;
import utils.MyHashMapList;

public class TupleTemplateImpl extends TupleTemplate{
	private final int id;
	private final String templatesString;
	private final Tuple belongsTo;
	private final MyHashMapList<String, String> mTableName_Columns;
	
	/**
	 * Sets up all the sub-structures
	 * @param id
	 */
	TupleTemplateImpl(String templatesString, 
			MyHashMapList<String, String> mTableName_Columns,
			Tuple belongsTo, int id){ // It can be created only in this package
		this.templatesString = templatesString;
		this.mTableName_Columns = mTableName_Columns;
		this.belongsTo = belongsTo;
		this.id = id;
		
		int first = 0;
		
		for( String tableName : mTableName_Columns.keyset() ){
			if( first == 0 ) first = mTableName_Columns.get(tableName).size();

			assert first == mTableName_Columns.get(tableName).size();
		}
	}
	
	public int belongsToTuple(){
		return belongsTo.getId();
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
		
		builder.append("Tuple: " + this.belongsTo.getFunctName() + "\n");
		builder.append("Template: " + templatesString + "\n");
		builder.append("Tables: " + mTableName_Columns.keyset().toString() + "\n");
		
		return builder.toString();
	}
	
	@Override 
	public boolean equals(Object other) {
		boolean result = false;
		if( other == null ) return false;
		if ( !(other instanceof TupleTemplateImpl) && !(other instanceof TupleTemplateDecorator) ){
			return false;
		}
	
		TupleTemplate that = (TupleTemplate) other;
		result = (this.belongsToTuple() == that.belongsToTuple() && this.getID() == that.getID());
		return result;
	}
	
	@Override
	public int hashCode(){
		return this.belongsToTuple() * 43 + this.getID();
	}
	
	@Override
	public int getID() {
		return id;
	}
};