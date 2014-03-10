package basicDatatypes;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import columnTypes.BigDecimalColumn;
import columnTypes.ColumnPumper;
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
	private List<ColumnPumper> columns;
	private final String tableName;  // Final in order to avoid the well-known "mutability" problem with the <i>equals</i> method.
	private List<ColumnPumper> primaryKeys;
	
	// Fields related to the pumping
	private boolean filledFlag; // It keeps the information whether this schema has been already pumped once
	private int maxDupsRepetition;
	
	private static Logger logger = Logger.getLogger(Schema.class.getCanonicalName());
	
	public Schema(String tableName){
		this.tableName = tableName;
		columns = new ArrayList<ColumnPumper>();
		primaryKeys = new ArrayList<ColumnPumper>();
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
	
	public void addColumn(String colName, String typeString, int index){
		
		if( typeString.startsWith("int") ) columns.add(new BigDecimalColumn(colName, MySqlDatatypes.DOUBLE, index));
		else if( typeString.startsWith("decimal") ) columns.add(new IntColumn(colName, MySqlDatatypes.INT, index, TypeStringParser.getFirstBinaryDatatypeSize(typeString), TypeStringParser.getSecondBinaryDatatypeSize(typeString)));
//		else if( typeString.startsWith("decimal") ) columns.add(new BigDecimalColumn(colName, MySqlDatatypes.DOUBLE, columns.size(), TypeStringParser.getFirstBinaryDatatypeSize(typeString) - TypeStringParser.getSecondBinaryDatatypeSize(typeString)));
		else if( typeString.startsWith("bigint") ) columns.add(new BigDecimalColumn(colName, MySqlDatatypes.DOUBLE, index));
		else if( typeString.startsWith("char") ) columns.add(new StringColumn(colName, MySqlDatatypes.VARCHAR, index, TypeStringParser.getUnaryDatatypeSize(typeString)));
		else if( typeString.startsWith("varchar") )	columns.add(new StringColumn(colName, MySqlDatatypes.VARCHAR, index, TypeStringParser.getUnaryDatatypeSize(typeString)));
		else if( typeString.startsWith("text") ) columns.add(new StringColumn(colName, MySqlDatatypes.VARCHAR, index));
		else if( typeString.startsWith("longtext") ) columns.add(new StringColumn(colName, MySqlDatatypes.VARCHAR, index));
		else if( typeString.startsWith("datetime") ) columns.add(new DateTimeColumn(colName, MySqlDatatypes.DATETIME, index));
		else if( typeString.startsWith("date") ) columns.add(new DateTimeColumn(colName, MySqlDatatypes.DATETIME, index));
		else if( typeString.startsWith("point") ) columns.add(new PointColumn(colName, MySqlDatatypes.POINT, index));
		else if( typeString.startsWith("linestring") ) columns.add(new LinestringColumn(colName, MySqlDatatypes.LINESTRING, index));
		else if( typeString.startsWith("multilinestring") ) columns.add(new MultiLinestringColumn(colName, MySqlDatatypes.MULTILINESTRING, index));
		else if( typeString.startsWith("polygon") ) columns.add(new PolygonColumn(colName, MySqlDatatypes.POLYGON, index));
		else if( typeString.startsWith("multipolygon") ) columns.add(new MultiPolygonColumn(colName, MySqlDatatypes.MULTIPOLYGON, index));
		else{
			logger.error("SUPPORT FOR TYPE: "+ typeString +" IS MISSING.");
		}
	}
	
	public ColumnPumper getColumn(String colName){
		for( ColumnPumper col : columns ){
			if( col.getName().equals(colName) )
				return col;
		}
		return null;
	}
	/**
	 * Returns a list of all columns. Side-effects if the list is changed
	 * @return
	 */
	public List<ColumnPumper> getColumns(){
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
	public List<ColumnPumper> getPks(){
		if( primaryKeys.size() == 0 ){
			
			// INIT
			for( ColumnPumper c : columns ){
				if( c.isPrimary() )
					primaryKeys.add(c);
			}
		}
		return primaryKeys;
	}
}
class TypeStringParser{
	
	static int getFirstBinaryDatatypeSize(String toParse){
		int indexStart = toParse.indexOf("(") + 1;
		int indexEnd = toParse.indexOf(",");
		
		return Integer.parseInt(toParse.substring(indexStart, indexEnd));
	}
	
	static int getSecondBinaryDatatypeSize(String toParse){
		int indexStart = toParse.indexOf(",") + 1;
		int indexEnd = toParse.indexOf(")");
		
		return Integer.parseInt(toParse.substring(indexStart, indexEnd));
	}

	static int getUnaryDatatypeSize(String toParse){
		int indexStart = toParse.indexOf("(") + 1;
		int indexEnd = toParse.indexOf(")");
				
		return Integer.parseInt(toParse.substring(indexStart, indexEnd));
	}
}