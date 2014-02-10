package core;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import basicDatatypes.Schema;
import connection.DBMSConnection;

// Speed trick 
// Connection c = DriverManager.getConnection("jdbc:mysql://host:3306/db?useServerPrepStmts=false&rewriteBatchedStatements=true", "username", "password");
// TODO Try
// Tried. Very Well.
public class Main {

	public static void main(String[] args){
		DBMSConnection dbmsConn = new DBMSConnection("jdbc:mysql", "localhost/ciao", "tir", "");
		// Get the names of all the tables
		Connection conn = dbmsConn.getConnection();
		Generator gen = new Generator(conn);
		
		Schema schema = gen.getTableSchema("example");
		gen.fillDomainBoundaries(schema);
		gen.createInsertTemplate(schema);
		gen.pumpTable("example", 100000, schema, true, 0);
		
	}
	
}
