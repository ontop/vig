package connection.test;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.junit.Test;

import java.sql.Statement;

import connection.DBMSConnection;

public class ConnectionTest {
	
//	private static String jdbcConnector = "jdbc:mysql";
//	private static String databaseUrl = "10.7.20.39:3306/npd";
//	private static String username = "fish";
//	private static String password = "fish";

	private static String jdbcConnector = "jdbc:mysql";
	private static String databaseUrl = "localhost/ciao";
	private static String username = "tir";
	private static String password = "";
	
	@Test
	public void testGetConnection() {
		DBMSConnection database = new DBMSConnection(jdbcConnector, databaseUrl, username, password);
		
		Connection conn = database.getConnection();
		
		assertNotEquals(conn, null);
		
		PreparedStatement stmt;
		try {
			stmt = conn.prepareStatement("select * from ciao.example;");
		
		ResultSet result = stmt.executeQuery();
		
		while(result.next()){
			System.out.println(result.getString(1) + " " + result.getInt(2));
		}
		
		conn.close();
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testInsertTuples() {
		DBMSConnection database = new DBMSConnection(jdbcConnector, databaseUrl, username, password);
		
		Connection conn = database.getConnection();
		
		assertNotEquals(conn, null);
		
		DatabaseMetaData metaData;
		try {
			metaData = conn.getMetaData();
		
		
			String tableType[] = {"TABLE"};
		    
			StringBuilder builder = new StringBuilder();
			
			ResultSet result = metaData.getTables(null,"ciao",null,tableType);
			
			ResultSetMetaData rsMetadata;
			rsMetadata = result.getMetaData();
			while( result.next() ){
				String tableName = result.getString(3);
				String catalog = result.getString(1);
				String schema = result.getString(2);
				
				Statement stmt = conn.createStatement();
				
				
			
				ResultSet res = stmt.executeQuery("select * from example where 1=2");
								
				ResultSetMetaData rsmd=res.getMetaData();
				System.out.println(rsmd.getColumnTypeName(1));
				System.out.println(rsmd.getColumnTypeName(2));
				rsmd.getColumnType(1);
				rsmd.getColumnLabel(1);
				rsmd.getColumnDisplaySize(1);
				
				System.out.println(result.getString(7));
				
				System.out.println(catalog); // Prints the name of the db
				System.out.println(tableName);
				System.out.println(schema);
			}
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetDbName(){
		DBMSConnection database = new DBMSConnection(jdbcConnector, databaseUrl, username, password);
		System.out.println(database.getDbName());
		//TODO Remove this ugly sysout
	}

}
