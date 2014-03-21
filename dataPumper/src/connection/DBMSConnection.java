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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import columnTypes.ColumnPumper;
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
		
		logger.setLevel(Level.INFO);
		
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
		//pstmt = conn.prepareStatement(
        //sql,
        //ResultSet.TYPE_FORWARD_ONLY,
        //ResultSet.CONCUR_READ_ONLY);
		//pstmt.setFetchSize(Integer.MIN_VALUE);
		
		try {
//			PreparedStatement stmt = connection.prepareStatement(template, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
//			stmt.setFetchSize(Integer.MIN_VALUE);
			return connection.prepareStatement(template); /*stmt;*/
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
//			case INT: {
//				stmt.setLong(columnIndex, Long.parseLong(value));
//				break;
//			}
//			case DATETIME:{
//				stmt.setDate(columnIndex, value);
//			}
			default:
				stmt.setString(columnIndex, value);
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
		insertQuery.append("INSERT IGNORE into "+s.getTableName()+" ("); // TODO Note: I use IGNORE because, from tests, I know that 
		                                                                 //      primary key duplication is extremely rare --some hard bug
		int i = 0;
		
		for( ColumnPumper col : s.getColumns() ){
			insertQuery.append(col.getName());
			if( ++i < s.getNumColumns() )
				insertQuery.append(", ");
		}
		insertQuery.append(") VALUES (");
		
		i = 0;
		for( ColumnPumper c : s.getColumns() ){
			if( c.getType() == MySqlDatatypes.POINT ||
					c.getType() == MySqlDatatypes.POLYGON ||
					c.getType() == MySqlDatatypes.MULTIPOLYGON ||
					c.getType() == MySqlDatatypes.LINESTRING ||
					c.getType() == MySqlDatatypes.MULTILINESTRING ){
				
				insertQuery.append("GeomFromText(?)");
			}
			else{
				insertQuery.append("?");
			}
			if( i++ < s.getNumColumns() -1 )
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
			for( ColumnPumper c : schemas.get(tableName).getColumns() ){
				for( QualifiedName qN : c.referencesTo() ){
					schemas.get(qN.getTableName()).getColumn(qN.getColName()).referencedBy().add(new QualifiedName(tableName, c.getName()));
				}
			}
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
				if( result.getString(4).equals("PRI") /*|| result.getString(4).equals("UNI")*/ ){ //TODO BUGFIX!!
					
					schema.getColumn(result.getString(1)).setPrimary();
				}		
			}
			stmt.close();
			
			// Retrieve columns with allDifferent()
			int cnt = 0;
			ColumnPumper ref = null;
			for( ColumnPumper c : schema.getColumns() ){
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
		}
		return schema;
	}

	/**
	 * Initializes the columns with needed values
	 * 
	 * @param schema
	 */
	public void initColumns(Schema schema){
		for( ColumnPumper column : schema.getColumns() )
			column.fillDomain(schema, this);
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
			e.printStackTrace();
		}
	}
	
	public void setForeignCheckOff(){
		try {
			PreparedStatement stmt = connection.prepareStatement("SET FOREIGN_KEY_CHECKS=0");
			stmt.execute();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void setForeignCheckOn(){
		try{
			PreparedStatement stmt = connection.prepareStatement("SET FOREIGN_KEY_CHECKS=1");
			stmt.execute();
			stmt.close();
		} catch (SQLException e){
			e.printStackTrace();
		}
	}
	public void setUniqueCheckOff(){
		try {
			PreparedStatement stmt = connection.prepareStatement("SET FOREIGN_KEY_CHECKS=0");
			stmt.execute();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void setUniqueCheckOn(){
		try{
			PreparedStatement stmt = connection.prepareStatement("SET FOREIGN_KEY_CHECKS=1");
			stmt.execute();
			stmt.close();
		} catch (SQLException e){
			e.printStackTrace();
		}
	}
	
	public int getNRows(String tableName){
		int result = 0;
		
		PreparedStatement stmt = this.getPreparedStatement("SELECT COUNT(*) FROM "+tableName);
		
		try {
			ResultSet rs = stmt.executeQuery();
			
			if( rs.next() ){
				result = rs.getInt(1);
			}

			stmt.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
};
