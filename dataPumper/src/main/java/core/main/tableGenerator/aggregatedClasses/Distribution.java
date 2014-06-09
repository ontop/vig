package core.main.tableGenerator.aggregatedClasses;

/*
 * #%L
 * dataPumper
 * %%
 * Copyright (C) 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import connection.DBMSConnection;

public class Distribution {
	private DBMSConnection dbmsConn;
	
	private static Logger logger = Logger.getLogger(Distribution.class.getCanonicalName());
	
	public Distribution(DBMSConnection dbmsConn){
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
	
	public float nullRatioNaive(String columnName, String tableName) {
		PreparedStatement st1 = dbmsConn.getPreparedStatement("SELECT COUNT("+columnName+") FROM "+tableName);
		PreparedStatement st2 = dbmsConn.getPreparedStatement("SELECT COUNT("+columnName+") FROM "+tableName+ " WHERE " + columnName + " IS NOT NULL");
		int total = 0;
		try {
			ResultSet rs = st1.executeQuery();
			if( rs.next() )
				total = rs.getInt(1);
			st1.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		int nonNull = 0;
		try {
			ResultSet rs = st2.executeQuery();
			if( rs.next() )
				nonNull = rs.getInt(1);
			st2.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (total > 0) {
			return (total - nonNull) / total;
		}
		return 0;
	}
	
	public float naiveStrategy(String columnName, String tableName){
		
		logger.setLevel(Level.INFO);
		
		int nRows = nRows(columnName, tableName);
		if( nRows == 0 ) return 0; // No rows in the table
		int sizeProjection = sizeProjection(columnName, tableName);
		
		if( nRows - sizeProjection == 0 ) return 0;
		
		float ratio = (float)(nRows - sizeProjection) / (float)nRows;
		
		logger.debug("Duplicates Ratio according to Naive Strategy: " + ratio);
		
		return ratio; 
	}
	
	public int nRows(String columnName, String tableName){
		PreparedStatement stmt = dbmsConn.getPreparedStatement("SELECT COUNT("+columnName+") FROM "+tableName);
		int result = 0;
		try {
			ResultSet rs = stmt.executeQuery();
			if( rs.next() )
				result = rs.getInt(1);
			stmt.close();
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
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
};
