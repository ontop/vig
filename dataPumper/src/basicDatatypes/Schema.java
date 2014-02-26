package basicDatatypes;

import java.util.ArrayList;
import java.util.List;

import basicDatatypes.MySqlDatatypes;

public class Schema{
	private List<Column> columns;
	private final String tableName;  // Final in order to avoid the well-known "mutability" problem with the <i>equals</i> method.
	private List<Column> primaryKeys;
	
	// Fields related to the pumping
	private boolean filledFlag; // It keeps the information whether this schema has been already pumped once
	private int maxDupsRepetition;

	public Schema(String tableName){
		this.tableName = tableName;
		columns = new ArrayList<Column>();
		primaryKeys = new ArrayList<Column>();
		filledFlag = false;
		maxDupsRepetition = 0;
	}
	
	public int getMaxDupsRepetition(){
		return maxDupsRepetition;
	}
	
	public void setMaxDupsRepetition(int maxDupsRepetition){
		this.maxDupsRepetition = maxDupsRepetition;
	}
	
	public void setFilled(){
		filledFlag = true;
	}
	
	public boolean isFilled(){
		return filledFlag;
	}
	
	public void addColumn(String colName, String typeString){
		
		if( typeString.startsWith("int") ) columns.add(new Column(colName, MySqlDatatypes.INT, columns.size()));
		else if( typeString.startsWith("char") ) columns.add(new Column(colName, MySqlDatatypes.CHAR, columns.size()));
		else if( typeString.startsWith("varchar") ) columns.add(new Column(colName, MySqlDatatypes.VARCHAR, columns.size()));
		else if( typeString.startsWith("text") ) columns.add(new Column(colName, MySqlDatatypes.TEXT, columns.size()));
		else if( typeString.startsWith("longtext") ) columns.add(new Column(colName, MySqlDatatypes.LONGTEXT, columns.size()));
		else if( typeString.startsWith("datetime") ) columns.add(new Column(colName, MySqlDatatypes.DATETIME, columns.size()));
		else if( typeString.startsWith("point") ) columns.add(new Column(colName, MySqlDatatypes.POINT, columns.size()));
		else if( typeString.startsWith("linestring") ) columns.add(new Column(colName, MySqlDatatypes.LINESTRING, columns.size()));
		else if( typeString.startsWith("multilinestring") ) columns.add(new Column(colName, MySqlDatatypes.MULTILINESTRING, columns.size()));
		else if( typeString.startsWith("polygon") ) columns.add(new Column(colName, MySqlDatatypes.POLYGON, columns.size()));
		else if( typeString.startsWith("multipolygon") ) columns.add(new Column(colName, MySqlDatatypes.MULTIPOLYGON, columns.size()));
	}
	
	public Column getColumn(String colName){
		for( Column col : columns ){
			if( col.getName().equals(colName) )
				return col;
		}
		return null;
	}
	/**
	 * Returns a list of all columns. Side-effects if the list is changed
	 * @return
	 */
	public List<Column> getColumns(){
		return columns;
	}	
	public String getTableName(){
		return tableName;
	}
	
	public int getNumColumns(){
		return columns.size();
	}
	
	public String toString(){
		return tableName;
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
	public List<Column> getPks(){
		if( primaryKeys.size() == 0 ){
			
			// INIT
			for( Column c : columns ){
				if( c.isPrimary() )
					primaryKeys.add(c);
			}
		}
		return primaryKeys;
	}
}