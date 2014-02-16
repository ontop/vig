package core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import utils.Pair;
import connection.DBMSConnection;

public class Statistics {
	private DBMSConnection dbmsConn;
	
	public Statistics(DBMSConnection dbmsConn){
		this.dbmsConn = dbmsConn;
	}
	
	/**
	 * Returns the percentage of duplicates in various fixed-size windows of the table
	 * TODO
	 * @param columnName
	 * @param tableName
	 * @return
	 */
	public float slidingWindowStrategy(String columnName, String tableName){
				
//		PreparedStatement stmt = dbmsConn.getPreparedStatement("SELECT COUNT("+columnName+") FROM "+tableName);
//		PreparedStatement stmtProj = dbmsConn.getPreparedStatement("SELECT COUNT(DISTINCT "+columnName+") FROM "+tableName);
		
		return 0; //TODO
		
	}
	
	public float naiveStrategy(String columnName, String tableName){
		
		int nRows = nRows(columnName, tableName);
		int sizeProjection = sizeProjection(columnName, tableName);
		
		return (nRows - sizeProjection) / nRows; 
	}
	
	public int nRows(String columnName, String tableName){
		PreparedStatement stmt = dbmsConn.getPreparedStatement("SELECT COUNT("+columnName+") FROM "+tableName);
		int result = 0;
		try {
			ResultSet rs = stmt.executeQuery();
			if( rs.next() )
				result = rs.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public int sizeProjection(String columnName, String tableName){
		PreparedStatement stmt = dbmsConn.getPreparedStatement("SELECT COUNT(DISTINCT "+columnName+") FROM "+tableName);
		int result = 0;
		try {
			ResultSet rs = stmt.executeQuery();
			if( rs.next() )
				result = rs.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
};
