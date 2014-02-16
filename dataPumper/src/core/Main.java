package core;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import basicDatatypes.Schema;
import connection.DBMSConnection;
import core.test.GeneratorTest;

// Speed trick 
// Connection c = DriverManager.getConnection("jdbc:mysql://host:3306/db?useServerPrepStmts=false&rewriteBatchedStatements=true", "username", "password");
// TODO Try
// Tried. Very Well.
public class Main {
	
	private static String jdbcConnector1 = "jdbc:mysql";
	private static String databaseUrl1 = "10.7.20.39:3306/npd";
	private static String username1 = "fish";
	private static String password1 = "fish";
	
	private static Logger logger = Logger.getLogger(Main.class.getCanonicalName());
	
	public static void main(String[] args){
		
		DBMSConnection dbmsConn = new DBMSConnection("jdbc:mysql", "localhost/ciao", "tir", "");
		Generator gen = new Generator(dbmsConn);
		
		DBMSConnection dbmsConn1 = new DBMSConnection(jdbcConnector1, databaseUrl1, username1, password1);
		Generator gen1 = new Generator(dbmsConn1);

		long a = System.currentTimeMillis(); 
		gen.pumpTable(1000000, dbmsConn.getSchema("example"));
		long b = System.currentTimeMillis();
		System.out.println("Elapsed: " + (b - a) + "msec");
	}
}
