package connection;

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

import basicDatatypes.Column;
import basicDatatypes.MySqlDatatypes;
import basicDatatypes.QualifiedName;
import basicDatatypes.Schema;
import basicDatatypes.Template;

public class DBMSConnection {
	
	private String jdbcConnector;
	private String databaseUrl;
	private String username;
	private String password;
	
	private java.sql.Connection connection;
	
	private Map<String, Schema> schemas; 
	
	private static Logger logger = Logger.getLogger(DBMSConnection.class.getCanonicalName());
	
	public DBMSConnection(String jdbcConnector, String database, String username, String password){
		this.jdbcConnector = jdbcConnector;
		this.databaseUrl = database;
		this.username = username;
		this.password = password;
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		logger.debug("MySQL JDBC driver loaded ok");

		String url = 
				jdbcConnector + "://" + databaseUrl 
				+ "?useServerPrepStmts=false&rewriteBatchedStatements=true&user=" + username 
				+ "&password=" + password;
		try {
			connection = DriverManager.getConnection(url, username, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		
		schemas = new HashMap<String, Schema>();
		fillDatabaseSchemas();
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
	
	public void setter(PreparedStatement stmt, int columnIndex, MySqlDatatypes type, String value){
		try{
			
			switch(type){
			case INT: {
				stmt.setInt(columnIndex, Integer.parseInt(value));
				break;
			}
			case CHAR:
				break;
			case DATETIME:
				break;
			case LINESTRING:
				break;
			case LONGTEXT:
				break;
			case MULTILINESTRING:
				break;
			case MULTIPOLYGON:
				break;
			case POINT:
				break;
			case POLYGON:
				break;
			case TEXT:
				break;
			case VARCHAR : {
				stmt.setString(columnIndex, value);
				break;
			}
			
			default:
				break;
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	public void commit(){
		try {
			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @author tir
	 * @param s: Schema of the table for which an insert query has to be created
	 * @return A template insert query with a suitable number of place-holders
	 * 
	 * TODO: is this good for autoincrement cols? -- Sol. just keep track of autoincrement cols
	 */
	public String createInsertTemplate(Schema s){
		StringBuilder insertQuery = new StringBuilder();
		insertQuery.append("INSERT into "+s.getTableName()+" (");
		int i = 0;
		
		for( Column col : s.getColumns() ){
			insertQuery.append(col.getName());
			if( ++i < s.getNumColumns() )
				insertQuery.append(", ");
		}
		insertQuery.append(") VALUES (");
		
		for( i = 0; i < s.getNumColumns(); ++i ){
			insertQuery.append("?");
			if( i < s.getNumColumns() - 1 )
				insertQuery.append(", ");
		}
		insertQuery.append(")");
		
		return insertQuery.toString();
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
			for( Column c : schemas.get(tableName).getColumns() ){
				for( QualifiedName qN : c.referencesTo() ){
					schemas.get(qN.getTableName()).getColumn(qN.getColName()).referencedBy().add(new QualifiedName(tableName, c.getName()));
				}
			}
		}
	}
	
	private Schema fillTableSchema(String tableName){
		Schema schema = new Schema(tableName);
		
		try{
			PreparedStatement stmt;
			stmt = connection.prepareStatement("DESCRIBE "+tableName);
			ResultSet result = stmt.executeQuery();
			
			// Field - Type - Null - Default - Extra
			while(result.next()){
				schema.addColumn(result.getString(1), result.getString(2));
				
				// Primary keys need to be all different
				logger.info(result.getString(4));
				if( result.getString(4).equals("PRI") ){
					
					schema.getColumn(result.getString(1)).setPrimary();
				}
			}
			
			// Retrieve columns with allDifferent()
			int cnt = 0;
			Column ref = null;
			for( Column c : schema.getColumns() ){
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
		
			// Now, let's fill the Min & Max information
			fillDomainBoundaries(schema);
		
		}
		catch(SQLException e){
			e.printStackTrace();
		}
		return schema;
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
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return tableNames;
	}

	public void close() {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * TODO Complete the switch
	 * TODO Since I have dates and polygons, maybe it is better to use String fields rather than doubles
	 * @param s
	 * @return
	 * 
	 * Types in FactPages db
	 * int, datetime, varchar, decimal, char, text, longtext, point, linestring, polygon, multipolygon, multilinestring
	 */
	private void fillDomainBoundaries(Schema s){
		
		try {
			Template t = new Template("select ? from "+s.getTableName()+";");
			PreparedStatement stmt;
						
			for( Column c : s.getColumns() ){
				String colName = c.getName();
				
				switch(c.getType()){
				case INT : {
										
					t.setNthPlaceholder(1, "min("+colName+"), max("+colName+")");
					
					stmt = connection.prepareStatement(t.getFilled());
					ResultSet result = stmt.executeQuery();
										
					if( result.next() ){
						c.setMinValue(result.getInt(1));
						c.setMaxValue(result.getInt(2));
						c.setLastInserted(result.getInt(2));
					}
					break;
				}
				case CHAR : {
					break;
				}
				case VARCHAR : {
					break;
				}
				case TEXT : {
					break;
				}
				case LONGTEXT : {
					break;
				}
				case DATETIME : {
					// Not sure whether etc.
					break;
				}
				case POINT : {
					break;
				}
				case LINESTRING : {
					break;
				}
				case MULTILINESTRING : {
					break;
				}
				case POLYGON : {
					break;
				}
				case MULTIPOLYGON : {
					break;
				}
				}
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
	}	
	public void setForeignCheckOff(){
		try {
			PreparedStatement stmt = connection.prepareStatement("SET FOREIGN_KEY_CHECKS=0");
			stmt.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void setForeignCheckOn(){
		try{
			PreparedStatement stmt = connection.prepareStatement("SET FOREIGN_KEY_CHECKS=1");
			stmt.execute();
		} catch (SQLException e){
			e.printStackTrace();
		}
	}
	public void setUniqueCheckOff(){
		try {
			PreparedStatement stmt = connection.prepareStatement("SET FOREIGN_KEY_CHECKS=0");
			stmt.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void setUniqueCheckOn(){
		try{
			PreparedStatement stmt = connection.prepareStatement("SET FOREIGN_KEY_CHECKS=1");
			stmt.execute();
		} catch (SQLException e){
			e.printStackTrace();
		}
	}
};
