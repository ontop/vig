package it.unibz.inf.vig_mappings_analyzer.datatypes;


public class Field{
	public final String tableName;
	public final String colName;
	
	public Field(String tableName, String colName){
		this.tableName = tableName;
		this.colName = colName;
	}
	
	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if( obj instanceof Field ){
			Field other = (Field) obj;
			result = (this.tableName.equals(other.tableName)) && (this.colName.equals(other.colName));
		}
		return result;
	}
	
	@Override
	public int hashCode() {
		return (this.tableName + "." + this.colName).hashCode();
	}
	
	@Override
	public String toString(){
		return this.tableName + "." + this.colName;
	}
};
