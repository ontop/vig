package it.unibz.inf.data_pumper.connection;

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

import it.unibz.inf.data_pumper.columns.ColumnPumper;
import it.unibz.inf.data_pumper.tables.Schema;
import it.unibz.inf.data_pumper.utils.Template;
import it.unibz.inf.vig_mappings_analyzer.core.utils.QualifiedName;

import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class DBMSConnection {
	
	private final String jdbcConnector;
	private final String databaseUrl;
	private final String username;
	private final String password;
	
	private java.sql.Connection connection;
	
	private Map<String, Schema> schemas; 
	
	private static Logger logger = Logger.getLogger(DBMSConnection.class.getCanonicalName());
	
	private static DBMSConnection instance = null;
	
	public static void initInstance(String jdbcConnector, String database, String username, String password) {
		DBMSConnection.instance = new DBMSConnection(jdbcConnector, database, username, password);
	}
	
	public static DBMSConnection getInstance() {
		if( DBMSConnection.instance == null ) throw new InstanceNullException("The method DBMSConnection.initInstance() has not been called yet");
		
		return DBMSConnection.instance;
	}
		
	public String getJdbcConnector(){
		return jdbcConnector;
	}
	
	public String getUsername(){
		return username;
	}
	
	public String getPassword(){
		return password;
	}
	
	public java.sql.Connection getConnection(){
		return connection;
	}	
	
	public String getDbName(){
		String result = databaseUrl.substring(databaseUrl.lastIndexOf("/")+1);
		return result;
	}
	
	public PreparedStatement getPreparedStatement(String template){
	
		try {
			return connection.prepareStatement(template); 
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void setAutoCommit(boolean b){
		try {
			connection.setAutoCommit(b);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public PreparedStatement getPreparedStatement(Template t){
		try {
			return connection.prepareStatement(t.getFilled());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Schema getSchema(String tableName){
		return schemas.get(tableName);
	}
	
	// It allows to 
	public void fillDatabaseSchemas(){
		
		for( String tableName : getAllTableNames() ){
			schemas.put(tableName, fillTableSchema(tableName));
		}
		
		// Fill the REFERENCED_BY fks
		for( String tableName : schemas.keySet() ){
			for( ColumnPumper<?> c : schemas.get(tableName).getColumns() ){
				for( QualifiedName qN : c.referencesTo() ){
					schemas.get(qN.getTableName()).getColumn(qN.getColName()).referencedBy().add(new QualifiedName(tableName, c.getName()));
				}
			}
		}
	}

	/**
	 * It retrieves all the table names from the database
	 * 
	 * @return 
	 */
	public List<String> getAllTableNames(){
		
		String dbName = getDbName();
		
		List<String> tableNames = new LinkedList<String>();
		
		// Get all the schemas
		try {
			DatabaseMetaData dbmd = connection.getMetaData();
			ResultSet rs = dbmd.getTables(dbName, null, null, null);
			
			while( rs.next() ){
				tableNames.add(rs.getString(3));
			}			
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return tableNames;
	}

	public void close() {
		try {
		    connection.close();
		} catch (SQLException e) {
		    System.exit(1);
		    e.printStackTrace();
		}
	}
	
	public void setForeignCheckOff(){
	    try(PreparedStatement stmt = connection.prepareStatement("SET FOREIGN_KEY_CHECKS=0")) {
		stmt.execute();
	    } catch (SQLException e) {
		e.printStackTrace();
		System.exit(1);
	    }
		
	}
	public void setForeignCheckOn(){
		try(PreparedStatement stmt = connection.prepareStatement("SET FOREIGN_KEY_CHECKS=1")){
		    stmt.execute();
		} catch (SQLException e){
		    e.printStackTrace();
		    System.exit(1);
		}
	}
	public void setUniqueCheckOff(){
		try(PreparedStatement stmt = connection.prepareStatement("SET FOREIGN_KEY_CHECKS=0")) {
		    stmt.execute();
		} catch (SQLException e) {
		    e.printStackTrace();
		    System.exit(1);
		}
	}
	public void setUniqueCheckOn(){
		try(PreparedStatement stmt = connection.prepareStatement("SET FOREIGN_KEY_CHECKS=1")){
		    stmt.execute();
		} catch (SQLException e){
		    e.printStackTrace();
		    System.exit(1);
		}
	}
	
	public int getNRows(String tableName){
	    int result = 0;
	    try(PreparedStatement stmt = this.getPreparedStatement("SELECT COUNT(*) FROM "+tableName)) {
		ResultSet rs = stmt.executeQuery();

		if( rs.next() ){
		    result = rs.getInt(1);
		}
	    } catch (SQLException e) {
		e.printStackTrace();
		System.exit(1);
	    }
	    return result;
	}
	
	// Private Interface
	
	private DBMSConnection(String jdbcConnector, String database, String username, String password) {
		
		this.jdbcConnector = jdbcConnector;
		this.databaseUrl = database;
		this.username = username;
		this.password = password;
		
		if( this.jdbcConnector.equals("jdbc:mysql") ){ // Mysql
			
			try {
				Class.forName("com.mysql.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
			logger.debug("MySQL JDBC driver loaded ok");
			
			String url = 
					jdbcConnector + "://" + databaseUrl 
					+ "?useServerPrepStmts=false&rewriteBatchedStatements=true&zeroDateTimeBehavior=convertToNull&user=" + username 
					+ "&password=" + password;
			try {
				connection = DriverManager.getConnection(url, username, password);
			} catch (SQLException e) {
				e.printStackTrace();
			}		
			
			schemas = new HashMap<String, Schema>();
			fillDatabaseSchemas();
		}
		else{
			throw new UnsupportedDatabaseException("The generator supports mysql, only");
		}
	}

	private Schema fillTableSchema(String tableName){

	    logger.info("Adding schema "+tableName);

	    Schema schema = new Schema(tableName);

	    try{
		PreparedStatement stmt;
		stmt = connection.prepareStatement("DESCRIBE "+tableName);
		ResultSet result = stmt.executeQuery();

		// Field - Type - Null - Default - Extra
		int index = 0;
		while(result.next()){
		    logger.debug("Adding column " + result.getString(1) + " from table " + tableName);

		    schema.addColumn(result.getString(1), result.getString(2), ++index);


		    // Primary keys need to be all different
		    logger.debug(result.getString(4));
		    if( result.getString(4).equals("PRI") ){ 
			schema.getColumn(result.getString(1)).setPrimary();
		    }		
		}
		stmt.close();

		// Retrieve columns with allDifferent()
		int cnt = 0;
		ColumnPumper<?> ref = null;
		for( ColumnPumper<?> c : schema.getColumns() ){
		    if( c.isPrimary() ){ ref = c; ++cnt; }
		}
		if( cnt == 1 ) ref.setAllDifferent();


		// Now let's retrieve foreign keys
		String informationSchemaQuery = 
			"select TABLE_NAME,COLUMN_NAME,CONSTRAINT_NAME, REFERENCED_TABLE_NAME,REFERENCED_COLUMN_NAME"
				+ " from INFORMATION_SCHEMA.KEY_COLUMN_USAGE"
				+ "	where TABLE_NAME = ? and constraint_schema = ?"
				+ " and REFERENCED_TABLE_NAME != 'null'";

		stmt = connection.prepareStatement(informationSchemaQuery);
		stmt.setString(1, tableName);
		stmt.setString(2, getDbName());

		result = stmt.executeQuery();

		while(result.next()){
		    schema.getColumn(result.getString(2))
		    .referencesTo().add(new QualifiedName(result.getString(4), result.getString(5)));
		}

		stmt.close();		
	    }
	    catch(SQLException e){
		e.printStackTrace();
		System.exit(1);
	    }

	    //		fillTuplesSchemas(schema);

	    return schema;
	}

};
