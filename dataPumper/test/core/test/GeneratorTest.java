package core.test;

//import static org.junit.Assert.*;


import static org.junit.Assert.*;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import columnTypes.Column;
import basicDatatypes.Schema;
import basicDatatypes.Template;
import utils.Statistics;
import connection.DBMSConnection;
import core.Generator;
import core.Generator4;

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
	private static String databaseUrl1 = "localhost/provaNpd";
	private static String username1 = "tir";
	private static String password1 = "";
	
	private static DBMSConnection db;
	private static DBMSConnection db1;
	private static Connection conn;
//	private static Connection conn1;
	
	// Parameters
	private static int nRowsToInsert = 1000;

	private static Logger logger = Logger.getLogger(GeneratorTest.class.getCanonicalName());
	
	@BeforeClass
	public static void setUpBeforeClass(){
		db = new DBMSConnection(jdbcConnector, databaseUrl, username, password);
		conn = db.getConnection();
		db1 = new DBMSConnection(jdbcConnector1, databaseUrl1, username1, password1);
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
		
		Generator4 gen = new Generator4(db);
		
		for( Column c : db.getSchema("trivial").getColumns() ){
			c.fillDomain(db.getSchema("trivial"), db);
			c.fillDomainBoundaries(db.getSchema("trivial"), db);
		}
		
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
		
		Schema schema = db.getSchema("trivial");
		
		for( Column c : schema.getColumns() ){
			c.fillDomain(schema, db);
			c.fillDomainBoundaries(schema, db);
		}
		
		Generator4 gen = new Generator4(db);
		
		long start = System.currentTimeMillis();
		gen.pumpTable(nRowsToInsert, db.getSchema("trivial"));
		long end = System.currentTimeMillis();

		logger.info("Time elapsed to pump "+nRowsToInsert+" rows: " + (end - start) + " msec.");
		logger.info(Statistics.printStats());
				
//		assertEquals(6, Statistics.getIntStat("trivial.id canAdd"));
		
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
		
		Generator4 gen = new Generator4(db);
		
		long start = System.currentTimeMillis();
		gen.pumpTable(nRowsToInsert, db.getSchema("pkeyTest"));
		long end = System.currentTimeMillis();

		logger.info("Time elapsed to pump "+nRowsToInsert+" rows: " + (end - start) + " msec.");
		logger.info(Statistics.printStats());
		
		// Get the count of the final number of rows
		PreparedStatement check = db.getPreparedStatement("SELECT count(distinct id) FROM pkeyTest");
		
		try {
			ResultSet result = check.executeQuery();
			result.next();
			logger.info(result.getInt(1));
			assertEquals(nRowsToInsert, result.getInt(1));
			
			init.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testBinaryPkey(){
		
		PreparedStatement init = db.getPreparedStatement("DELETE FROM testBinaryKey");
		
		try {
			init.execute();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		PreparedStatement insertions = 
				db.getPreparedStatement("INSERT INTO testBinaryKey VALUES (1, 1, 'ciao'), (2, 1, 'ciriciao'), (3, 1, 'ciriciriciao'), (3, 2, 'ciriciri'), (4, 2, 'ciriciri')");
		try {
			insertions.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		Schema schema = db.getSchema("testBinaryKey");
		
		for( Column c : schema.getColumns() ){
			c.fillDomain(schema, db);
			c.fillDomainBoundaries(schema, db);
		}

//		db.fillDatabaseSchemas(); // Refill schemas
		
		Generator4 gen = new Generator4(db);
		
		long start = System.currentTimeMillis();
		gen.pumpTable(nRowsToInsert, db.getSchema("testBinaryKey"));
		long end = System.currentTimeMillis();
		
		logger.info("Time elapsed to pump "+nRowsToInsert+" rows: " + (end - start) + " msec.");
		logger.info(Statistics.printStats());
	}
	
	@Test
	public void testGenerateDatetime(){
		
		// Init
		PreparedStatement init = db.getPreparedStatement("insert into datetimeTest values (1, '2005-11-23 12:56:00')");
		
		try{
			init.execute();
		}
		catch(SQLException e){
			e.printStackTrace();
		}
		
		Schema schema = db.getSchema("datetimeTest");
		
		for( Column c : schema.getColumns() ){
			c.fillDomain(schema, db);
			c.fillDomainBoundaries(schema, db);
		}
		
//		db.fillDatabaseSchemas();
		
		Generator4 gen = new Generator4(db);
		
		long start = System.currentTimeMillis();
		gen.pumpTable(nRowsToInsert, db.getSchema("datetimeTest"));
		long end = System.currentTimeMillis();
		
		logger.info("Time elapsed to pump "+nRowsToInsert+" rows: " + (end - start) + " msec.");
		logger.info(Statistics.printStats());
	}
	
	@Test
	public void testGenerateDatetime_1(){
				
		Generator4 gen = new Generator4(db);
		
		long start = System.currentTimeMillis();
		gen.pumpTable(nRowsToInsert, db.getSchema("datetimeTest"));
		long end = System.currentTimeMillis();
		
		logger.info("Time elapsed to pump "+nRowsToInsert+" rows: " + (end - start) + " msec.");
		logger.info(Statistics.printStats());
	}
	
	@Test
	public void testGeneratePoint(){
		
		Schema schema = db.getSchema("pointTest");
		
		for( Column c : schema.getColumns() ){
			c.fillDomain(schema, db);
			c.fillDomainBoundaries(schema, db);
		}
		
		Generator4 gen = new Generator4(db);
		
		long start = System.currentTimeMillis();
		gen.pumpTable(nRowsToInsert, db.getSchema("pointTest"));
		long end = System.currentTimeMillis();
		
		logger.info("Time elapsed to pump "+nRowsToInsert+" rows: " + (end - start) + " msec.");
		logger.info(Statistics.printStats());
	}
	
	@Test
	public void testGenerateLinestring(){
		
		Schema schema = db.getSchema("testLinestring");
		
		for( Column c : schema.getColumns() ){
			c.fillDomain(schema, db);
			c.fillDomainBoundaries(schema, db);
		}
		
		Generator4 gen = new Generator4(db);
		
		long start = System.currentTimeMillis();
		gen.pumpTable(nRowsToInsert, db.getSchema("testLinestring"));
		long end = System.currentTimeMillis();
		
		logger.info("Time elapsed to pump "+nRowsToInsert+" rows: " + (end - start) + " msec.");
		logger.info(Statistics.printStats());
	}	
	
	@Test
	public void testGeneratePolygon(){
		
		Schema schema = db.getSchema("testPolygon");
		
		for( Column c : schema.getColumns() ){
			c.fillDomain(schema, db);
			c.fillDomainBoundaries(schema, db);
		}
		
		Generator4 gen = new Generator4(db);
		
		long start = System.currentTimeMillis();
		gen.pumpTable(nRowsToInsert, db.getSchema("testPolygon"));
		long end = System.currentTimeMillis();
		
		logger.info("Time elapsed to pump "+nRowsToInsert+" rows: " + (end - start) + " msec.");
		logger.info(Statistics.printStats());
	}	
	
	@Test
	public void testGenerateMultiLinestring(){
		
		Schema schema = db.getSchema("testMultilinestring");
		
		for( Column c : schema.getColumns() ){
			c.fillDomain(schema, db);
			c.fillDomainBoundaries(schema, db);
		}

		Generator4 gen = new Generator4(db);
		
		long start = System.currentTimeMillis();
		gen.pumpTable(nRowsToInsert, db.getSchema("testMultilinestring"));
		long end = System.currentTimeMillis();
		
		logger.info("Time elapsed to pump "+nRowsToInsert+" rows: " + (end - start) + " msec.");
		logger.info(Statistics.printStats());
	}
	
	@Test
	public void testGenerateMultiPolygon(){
		
		Schema schema = db.getSchema("testMultipolygon");
		
		for( Column c : schema.getColumns() ){
			c.fillDomain(schema, db);
			c.fillDomainBoundaries(schema, db);
		}

		Generator4 gen = new Generator4(db);
		
		long start = System.currentTimeMillis();
		gen.pumpTable(nRowsToInsert, db.getSchema("testMultipolygon"));
		long end = System.currentTimeMillis();
		
		logger.info("Time elapsed to pump "+nRowsToInsert+" rows: " + (end - start) + " msec.");
		logger.info(Statistics.printStats());
	}
	
	@Test
	public void testAutoincrement(){
		
		Schema schema = db.getSchema("testAutoincrement");
		
		for( Column c : schema.getColumns() ){
			c.fillDomain(schema, db);
			c.fillDomainBoundaries(schema, db);
		}

		Generator4 gen = new Generator4(db);
		
		long start = System.currentTimeMillis();
		gen.pumpTable(nRowsToInsert, db.getSchema("testAutoincrement"));
		long end = System.currentTimeMillis();
		
		logger.info("Time elapsed to pump "+nRowsToInsert+" rows: " + (end - start) + " msec.");
		logger.info(Statistics.printStats());
	}
	
	@Test
	public void testAutoincrement_1(){
		
		// Init
		PreparedStatement init = db.getPreparedStatement("insert into testAutoincrement values (1, 100)");
		
		try{
			init.execute();
		}
		catch(SQLException e){
			e.printStackTrace();
		}
		
		Schema schema = db.getSchema("testAutoincrement");
		
		for( Column c : schema.getColumns() ){
			c.fillDomain(schema, db);
			c.fillDomainBoundaries(schema, db);
		}
		
		Generator4 gen = new Generator4(db);
		
		long start = System.currentTimeMillis();
		gen.pumpTable(nRowsToInsert, db.getSchema("testAutoincrement"));
		long end = System.currentTimeMillis();
		
		logger.info("Time elapsed to pump "+nRowsToInsert+" rows: " + (end - start) + " msec.");
		logger.info(Statistics.printStats());
	}
	
	@Test
	public void testGenerateDate(){
		
		// Init
		PreparedStatement init = db.getPreparedStatement("insert into dateTest values (1, '2005-11-23')");
		
		try{
			init.execute();
		}
		catch(SQLException e){
			e.printStackTrace();
		}
		
		Schema schema = db.getSchema("dateTest");
		
		for( Column c : schema.getColumns() ){
			c.fillDomain(schema, db);
			c.fillDomainBoundaries(schema, db);
		}
		
//		db.fillDatabaseSchemas();
		
		Generator4 gen = new Generator4(db);
		
		long start = System.currentTimeMillis();
		gen.pumpTable(nRowsToInsert, db.getSchema("dateTest"));
		long end = System.currentTimeMillis();
		
		logger.info("Time elapsed to pump "+nRowsToInsert+" rows: " + (end - start) + " msec.");
		logger.info(Statistics.printStats());
	}
	
//	@Test
//	public void testPumpWithNullInGeometry(){
//				
//		Generator gen = new Generator4(db1);
//		
//		long start = System.currentTimeMillis();
//		gen.pumpTable(nRowsToInsert, db1.getSchema("apaAreaNet"));
//		long end = System.currentTimeMillis();
//		
//		logger.info("Time elapsed to pump "+nRowsToInsert+" rows: " + (end - start) + " msec.");
//		logger.info(Statistics.printStats());
//	}
//	
//	@Test
//	public void checkPrimaryKeyIntegerSkips(){
//		Generator gen = new Generator4(db1);
//		
//		long start = System.currentTimeMillis();
//		gen.pumpTable(nRowsToInsert, db1.getSchema("bsns_arr_area"));
//		long end = System.currentTimeMillis();
//		
//		logger.info("Time elapsed to pump "+nRowsToInsert+" rows: " + (end - start) + " msec.");
//		logger.info(Statistics.printStats());
//	}
//	
	@Test
	public void provetta(){
		
		Schema schema = db1.getSchema("wellbore_development_all");
		
		for( Column c : schema.getColumns() ){
			c.fillDomain(schema, db1);
			c.fillDomainBoundaries(schema, db1);
		}
		
		Generator4 gen = new Generator4(db1);
		
		db1.setForeignCheckOff();
		
		long start = System.currentTimeMillis();
		List<Schema> schemas = gen.pumpTable(100000, db1.getSchema("wellbore_development_all"));
		long end = System.currentTimeMillis();
		
		while( !schemas.isEmpty() ){
			Schema cur = schemas.remove(0);
			for( Column c : cur.getColumns() ){
				c.fillDomain(cur, db1);
				c.fillDomainBoundaries(cur, db1);
			}
			
			schemas.addAll(gen.pumpTable(0, cur));
		}
		
		db1.setForeignCheckOn();
		
		logger.info("Time elapsed to pump "+nRowsToInsert+" rows: " + (end - start) + " msec.");
		logger.info(Statistics.printStats());
	}
}
