package core.main.tableGenerator.aggregatedClasses;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import connection.DBMSConnection;
import core.tableGenerator.GeneratorColumnBased;
import columnTypes.ColumnPumper;

public class DuplicatesPicker {
	
	private ColumnPumper column;
	private ResultSet duplicatesToInsert;
	private float duplicatesRatio;
	private float nullRatio;
	
	public DuplicatesPicker(ColumnPumper column){
		this.column = column;
		this.duplicatesToInsert = null;
		this.duplicatesRatio = 0;
		this.nullRatio = 0;
	}
	
	public void setDuplicateRatio(float ratio){
		duplicatesRatio = ratio;
	}
	
	public float getDuplicateRatio(){
		return duplicatesRatio;
	}
	
	public void setNullRatio(float ratio){
		nullRatio = ratio;
	}
	
	public float getNullRatio(){
		return nullRatio;
	}
	
	public void fillDuplicates(DBMSConnection dbmsConn, String tableName, int insertedRows) {
		
		if( duplicatesToInsert != null ){
			try {
				duplicatesToInsert.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		int startIndex = insertedRows - GeneratorColumnBased.duplicatesWindowSize > 0 ? insertedRows - GeneratorColumnBased.duplicatesWindowSize : 0;
		
		String queryString = null;
		
		if( column.isGeometric() ){
			queryString = "SELECT AsWKT(" + column.getName() + ") FROM " + tableName + " "
					+ " WHERE AsWKT(" + column.getName() + ") IS NOT NULL LIMIT " + startIndex + ", " + GeneratorColumnBased.duplicatesWindowSize;
		}
		else{
			queryString = "SELECT "+column.getName()+ " FROM "+tableName+" WHERE "+column.getName()+" IS NOT NULL LIMIT "+ startIndex +", "+GeneratorColumnBased.duplicatesWindowSize;
		}
		try{
			PreparedStatement stmt = dbmsConn.getPreparedStatement(queryString);
			duplicatesToInsert = stmt.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public String pickNextDupFromDuplicatesToInsert(){
		String result = null;
		try {
			boolean stop = false;
			while( result == null && !stop ){
				if( duplicatesToInsert.next() ){
					result = duplicatesToInsert.getString(1);
				}else stop = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public void beforeFirstDuplicatesToInsert(){
		try {
			duplicatesToInsert.beforeFirst();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void reset(){
		if( duplicatesToInsert != null ){
			try {
				duplicatesToInsert.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		duplicatesToInsert = null;
	}

}
