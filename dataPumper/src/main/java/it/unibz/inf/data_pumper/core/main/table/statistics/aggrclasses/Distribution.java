package it.unibz.inf.data_pumper.core.main.table.statistics.aggrclasses;

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

import it.unibz.inf.data_pumper.connection.DBMSConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

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
		int nRows = nRows(tableName);
		if( nRows == 0 ) return 0; // No rows in the table
		
		int nNulls = nRows - nValues(columnName, tableName);
		
		if( nNulls <= 0 ) return 0;
		
		float ratio = (float)(nNulls) / (float)nRows;
		
		logger.debug("Nulls Ratio according to Naive Strategy: " + ratio);
		
		return ratio; 
	}
	
	
	public float dupsRatioNaive(String columnName, String tableName){
		
		int nRows = nRows(tableName);
		if( nRows == 0 ) return 0; // No rows in the table
		
		int nNulls = nRows - nValues(columnName, tableName);
		int sizeProjection = sizeProjection(columnName, tableName);
		
		int nDups = nRows - nNulls - sizeProjection;
		
		if( nDups <= 0 ) return 0;
		
		float ratio = (float)(nDups) / (float)nRows;
		
		logger.debug("Duplicates Ratio according to Naive Strategy: " + ratio);
		
		return ratio; 
	}
	
	public int nValues(String colName, String tableName){
		PreparedStatement stmt = dbmsConn.getPreparedStatement("SELECT COUNT("+colName+") FROM "+tableName+ " WHERE " + colName + " IS NOT NULL");
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
	
	public int nRows(String tableName){
		PreparedStatement stmt = dbmsConn.getPreparedStatement("SELECT COUNT(*) FROM "+tableName);
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

	// mysql> select count(distinct A.wlbNpdidWellbore) from wellbore_shallow_all A join wellbore_development_all B on A.wlbNpdidWellbore = B.wlbNpdidWellbore;
	public int sharedDistinctRows(String colName, String colTableName, String refName, String refTableName) throws SQLException{
		
		int result = 0;
		
		String projPart = "SELECT COUNT(DISTINCT A."+colName+") FROM "+ colTableName + " A JOIN " + refTableName + " B";
		String onClause = " ON A."+colName +"=B."+refName;
		String query = projPart + onClause;
		
		PreparedStatement stmt = dbmsConn.getPreparedStatement(query);
		
		ResultSet rs = stmt.executeQuery();
		if( rs.next() ){
			result = rs.getInt(1);
		}
					
		return result;
	}
};
