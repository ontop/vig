package core;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import connection.DBMSConnection;

public class Main {

	public static void main(String[] args){
		DBMSConnection dbmsConn = new DBMSConnection("jdbc:mysql", "localhost/ciao", "tir", "");
		// Get the names of all the tables
		Connection conn = dbmsConn.getConnection();
		Generator gen = new Generator(conn);
		
		try {
			DatabaseMetaData dbmd = conn.getMetaData();
			
			String tableType[] = {"TABLE"};
			ResultSet rsmd = dbmd.getTables(null, dbmsConn.getDbName(), null, tableType);
			
			while(rsmd.next()){
				String tableName = rsmd.getString(3);
				System.out.println(rsmd.getString(3)); // Get table name
//				gen.pumpTable(tableName, 3);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
