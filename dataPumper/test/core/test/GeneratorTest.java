package core.test;

//import static org.junit.Assert.*;


import static org.junit.Assert.*;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import basicDatatypes.Schema;
import connection.DBMSConnection;
import core.Generator;

/**
 * TODO Make of this a proper unit test
 * @author tir
 *
 */
public class GeneratorTest {
	
	
	private static String jdbcConnector = "jdbc:mysql";
	private static String databaseUrl = "10.7.20.39:3306/pumperTest";
	private static String username = "test";
	private static String password = "ontop2014";
	
	private static String jdbcConnector1 = "jdbc:mysql";
	private static String databaseUrl1 = "10.7.20.39:3306/npd";
	private static String username1 = "fish";
	private static String password1 = "fish";
	
	private static DBMSConnection db;
	private static DBMSConnection db1;
	private static Connection conn;
	private static Connection conn1;
	
	// Parameters
	private static int nRowsToInsert = 10;

	private static Logger logger = Logger.getLogger(GeneratorTest.class.getCanonicalName());
	
	@BeforeClass
	
	public static void setUp(){
		db = new DBMSConnection(jdbcConnector, databaseUrl, username, password);
		conn = db.getConnection();
		db1 = new DBMSConnection(jdbcConnector1, databaseUrl1, username1, password1);
		conn1 = db1.getConnection();
	}
	
	@AfterClass
	public static void tearDown(){
		try {
			conn.close();
			conn1.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testPumpTable(){
		
		PreparedStatement init = db.getPreparedStatement("DELETE FROM trivial");
		
		try {
			init.execute();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		Generator gen = new Generator(db);
		long startTime = System.currentTimeMillis();
		gen.pumpTable(nRowsToInsert, db.getSchema("trivial"));
		long endTime = System.currentTimeMillis();
		logger.debug("Pumping time to insert "+nRowsToInsert+" columns: "+(endTime - startTime)+" msec.");
		
		String query = "SELECT count(*) FROM trivial";
		
		PreparedStatement stmt = db.getPreparedStatement(query);
		
		ResultSet result;
		try {
			result = stmt.executeQuery();
		
			if(result.next()) assertTrue(result.getInt(1) == nRowsToInsert); // Careful, it is the ''next'' which moves the cursor (how awful)
		
			init.execute(); // Re-init
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testRatioDupsPumpTable(){
		PreparedStatement init = db.getPreparedStatement("DELETE FROM trivial");
		
		try {
			init.execute();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		PreparedStatement insertions = db.getPreparedStatement("INSERT INTO trivial VALUES (1,'ciao'), (2,'ciriciao'), (1,'ciriciriciao')");
		
		try {
			insertions.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		Generator gen = new Generator(db);
		
		gen.pumpTable(20, db.getSchema("trivial"));
		
	}
	
//	@Test
//	public void testPumpDatabase(){
//		Generator gen = new Generator(conn);
//		Schema schema = gen.getTableSchema("example");
//		gen.fillDomainBoundaries(schema);
//		gen.createInsertTemplate(schema);
//		gen.pumpTable("example", 10000, schema, true, 0);
//	}
//	
//	@Test
//	public void testFillDomainBoundaries(){
//		
//		Generator gen = new Generator(conn);
//		Schema schema = gen.getTableSchema("example");
//		gen.fillDomainBoundaries(schema);
//		
//		logger.debug(schema.getDomain("id").max);
//		logger.debug(schema.getDomain("id").min);
//		
//		try {
//			conn.close();
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//	}
//	
//	@Test
//	public void testGetTableSchema() {
//		
//		Generator gen = new Generator(conn);
//		Schema schema = gen.getTableSchema("example");
//		
//		logger.debug(schema);
//		
//		gen = new Generator(conn1);
//		schema = gen.getTableSchema("baaArea");
//		
//		logger.debug(schema);
//		
//	}
//	
//	@Test
//	public void testCreateInsertTemplate(){		
//		
//		Generator gen = new Generator(conn);
//		Schema schema = gen.getTableSchema("example");
//		
//		System.out.println(gen.createInsertTemplate(schema));
//	}
	
	@Test
	public void testSomething(){
		
		try {
			PreparedStatement stmt = conn1.prepareStatement("select TABLE_NAME,COLUMN_NAME,CONSTRAINT_NAME, REFERENCED_TABLE_NAME,REFERENCED_COLUMN_NAME from INFORMATION_SCHEMA.KEY_COLUMN_USAGE where TABLE_NAME='baaArea' and constraint_schema = 'npd' and REFERENCED_TABLE_NAME != 'null'");
						
			ResultSet rs = stmt.executeQuery();
			
			while(rs.next()){
				System.out.println(rs.getString(1) + "   " + rs.getString(2));
			}
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
}
