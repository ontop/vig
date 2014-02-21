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

import basicDatatypes.Template;
import utils.Statistics;
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
	
//	private static String jdbcConnector1 = "jdbc:mysql";
//	private static String databaseUrl1 = "10.7.20.39:3306/npd";
//	private static String username1 = "fish";
//	private static String password1 = "fish";
	
	private static DBMSConnection db;
//	private static DBMSConnection db1;
	private static Connection conn;
//	private static Connection conn1;
	
	// Parameters
	private static int nRowsToInsert = 10;

	private static Logger logger = Logger.getLogger(GeneratorTest.class.getCanonicalName());
	
	@BeforeClass
	public static void setUpBeforeClass(){
		db = new DBMSConnection(jdbcConnector, databaseUrl, username, password);
		conn = db.getConnection();
//		db1 = new DBMSConnection(jdbcConnector1, databaseUrl1, username1, password1);
//		conn1 = db1.getConnection();
	}
	
	@AfterClass
	public static void tearDownAfterClass(){
		try {
			conn.close();
//			conn1.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Before
	public void setUp(){
		// INIT
		db.setForeignCheckOff();
		for( String sName : db.getAllTableNames() ){
			
			Template temp = new Template("delete from ?");
			temp.setNthPlaceholder(1, sName);
			
			PreparedStatement init = db.getPreparedStatement(temp);
			
			try {
				init.execute();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		db.setForeignCheckOn();
	}
	
	@After
	public void tearDown(){
		// INIT
		db.setForeignCheckOff();
		for( String sName : db.getAllTableNames() ){
			
			Template temp = new Template("delete from ?");
			temp.setNthPlaceholder(1, sName);
			
			PreparedStatement init = db.getPreparedStatement(temp);
			
			try {
				init.execute();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		db.setForeignCheckOn();	
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
		logger.info("Pumping time to insert "+nRowsToInsert+" rows: "+(endTime - startTime)+" msec.");
		
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
				
		assertEquals(6, Statistics.getIntStat("trivial.id canAdd"));
		
		try {
			init.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
	
	@Test
	public void testUnaryPkey(){
		PreparedStatement init = db.getPreparedStatement("DELETE FROM pkeyTest");
		
		try {
			init.execute();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		Generator gen = new Generator(db);
		
		gen.pumpTable(1000, db.getSchema("pkeyTest"));
		
		// Get the count of the final number of rows
		PreparedStatement check = db.getPreparedStatement("SELECT count(distinct id) FROM pkeyTest");
		
		try {
			ResultSet result = check.executeQuery();
			result.next();
			logger.info(result.getInt(1));
			assertEquals(1000, result.getInt(1));
			
			init.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testBinaryPkey(){
		
		Generator gen = new Generator(db);
		
		PreparedStatement insertions = 
				db.getPreparedStatement("INSERT INTO testBinaryKey VALUES (1, 1, 'ciao'), (1, 2, 'ciriciao'), (2, 2, 'ciriciriciao')");
		
		try {
			insertions.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		long start = System.currentTimeMillis();
		gen.pumpTable(10000, db.getSchema("testBinaryKey"));
		long end = System.currentTimeMillis();
		
		logger.info("Time elapsed to pump "+10000+" rows: " + (end - start) + " msec.");
	}
	
//	@Test
//	public void testForeignKeysBinary(){
//		// fKeyA.value -> fKeyB.value
//		// fKeyB.id -> fKeyA.id
//		
//		PreparedStatement init = db.getPreparedStatement("DELETE FROM fKeyA");
//		PreparedStatement init1 = db.getPreparedStatement("DELETE FROM fkeyB");
//		
//		try {
//			init.execute();
//			init1.execute();
//		} catch (SQLException e1) {
//			e1.printStackTrace();
//		}
//		
//		Generator gen = new Generator(db);
//		
////		gen.p TODO pumpDatabase needed.
//	}
	
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
}
