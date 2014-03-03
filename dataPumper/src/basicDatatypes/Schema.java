package basicDatatypes;

import java.util.ArrayList;
import java.util.List;

import columnTypes.Column;
import columnTypes.DateTimeColumn;
import columnTypes.IntColumn;
import columnTypes.LinestringColumn;
import columnTypes.MultiLinestringColumn;
import columnTypes.MultiPolygonColumn;
import columnTypes.PointColumn;
import columnTypes.PolygonColumn;
import columnTypes.StringColumn;
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
		
		if( typeString.startsWith("int") ) columns.add(new IntColumn(colName, MySqlDatatypes.INT, columns.size()));
		else if( typeString.startsWith("char") ) columns.add(new StringColumn(colName, MySqlDatatypes.CHAR, columns.size()));
		else if( typeString.startsWith("varchar") ) columns.add(new StringColumn(colName, MySqlDatatypes.VARCHAR, columns.size()));
		else if( typeString.startsWith("text") ) columns.add(new StringColumn(colName, MySqlDatatypes.TEXT, columns.size()));
		else if( typeString.startsWith("longtext") ) columns.add(new StringColumn(colName, MySqlDatatypes.LONGTEXT, columns.size()));
		else if( typeString.startsWith("datetime") ) columns.add(new DateTimeColumn(colName, MySqlDatatypes.DATETIME, columns.size()));
		else if( typeString.startsWith("point") ) columns.add(new PointColumn(colName, MySqlDatatypes.POINT, columns.size()));
		else if( typeString.startsWith("linestring") ) columns.add(new LinestringColumn(colName, MySqlDatatypes.LINESTRING, columns.size()));
		else if( typeString.startsWith("multilinestring") ) columns.add(new MultiLinestringColumn(colName, MySqlDatatypes.MULTILINESTRING, columns.size()));
		else if( typeString.startsWith("polygon") ) columns.add(new PolygonColumn(colName, MySqlDatatypes.POLYGON, columns.size()));
		else if( typeString.startsWith("multipolygon") ) columns.add(new MultiPolygonColumn(colName, MySqlDatatypes.MULTIPOLYGON, columns.size()));
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