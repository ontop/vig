package core.test;

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

import columnTypes.ColumnPumper;
import basicDatatypes.Schema;
import basicDatatypes.Template;
import utils.Statistics;
import configuration.UnitConf;
import connection.DBMSConnection;
import core.tableGenerator.GeneratorDB;

/**
 * @author tir
 *
 */
public class GeneratorDBTest {
	
	// For changing these parameters, please
	// modify the file src/main/resources/unitTests.conf
	private static String jdbcConnector = UnitConf.jdbcConnector();
	private static String databaseUrl = UnitConf.dbSingleTests();
	private static String username = UnitConf.dbUsernameSingleTests();
	private static String password = UnitConf.dbPasswordSingleTests();

	
	private static DBMSConnection db;
	private static Connection conn;
	
	// Parameters
	private static int nRowsToInsert = 1000;

	
	private static Logger logger = Logger.getLogger(GeneratorDBTest.class.getCanonicalName());
	
	@BeforeClass
	public static void setUpBeforeClass(){
		db = new DBMSConnection(jdbcConnector, databaseUrl, username, password);
		conn = db.getConnection();
	}
	
	@AfterClass
	public static void tearDownAfterClass(){
		try {
			conn.close();
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
	
	/**
	 * Tries to pump an empty table
	 */
	@Test
	public void testPumpTable(){
		
		GeneratorDB gen = new GeneratorDB(db);
		
		for( ColumnPumper c : db.getSchema("trivial").getColumns() ){
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
		
			if(result.next()) assertTrue(result.getInt(1) == nRowsToInsert); 
			else assertTrue(false);
		
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testRatioDupsPumpTable(){
		
		PreparedStatement insertions = db.getPreparedStatement("INSERT INTO trivial VALUES (1,'ciao'), (2,'ciriciao'), (1,'ciriciriciao')");
		
		try {
			insertions.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		Schema schema = db.getSchema("trivial");
		
		for( ColumnPumper c : schema.getColumns() ){
			c.fillDomain(schema, db);
			c.fillDomainBoundaries(schema, db);
		}
		
		GeneratorDB gen = new GeneratorDB(db);
		
		long start = System.currentTimeMillis();
		gen.pumpTable(nRowsToInsert, db.getSchema("trivial"));
		long end = System.currentTimeMillis();

		logger.info("Time elapsed to pump "+nRowsToInsert+" rows: " + (end - start) + " msec.");
		
		assertEquals(new Float(0.33333334), new Float(Statistics.getFloatStat("trivial.id dups ratio")));
		logger.info(Statistics.printStats());
					
	}
	
	@Test
	public void testUnaryPkey(){
		
		Schema schema = db.getSchema("pkeyTest");
		
		for( ColumnPumper c : schema.getColumns() ){
			c.fillDomain(schema, db);
			c.fillDomainBoundaries(schema, db);
		}

		GeneratorDB gen = new GeneratorDB(db);
		
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
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testBinaryPkey(){
		
		PreparedStatement insertions = 
				db.getPreparedStatement("INSERT INTO testBinaryKey VALUES (1, 1, 'ciao'), (2, 1, 'ciriciao'), (3, 1, 'ciriciriciao'), (3, 2, 'ciriciri'), (4, 2, 'ciriciri')");
		try {
			insertions.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		Schema schema = db.getSchema("testBinaryKey");
		
		for( ColumnPumper c : schema.getColumns() ){
			c.fillDomain(schema, db);
			c.fillDomainBoundaries(schema, db);
		}

		GeneratorDB gen = new GeneratorDB(db);
		
		long start = System.currentTimeMillis();
		gen.pumpTable(nRowsToInsert, db.getSchema("testBinaryKey"));
		long end = System.currentTimeMillis();
		
		assertEquals(nRowsToInsert, Statistics.getIntStat("testBinaryKey.id1 Adding a duplicate from initial database values") + 
				Statistics.getIntStat("testBinaryKey.id1 fresh values"));
		
		logger.info("Time elapsed to pump "+nRowsToInsert+" rows: " + (end - start) + " msec.");
		logger.info(Statistics.printStats());
	}
	
	@Test
	public void testGeneratePoint(){
		
		Schema schema = db.getSchema("pointTest");
		
		for( ColumnPumper c : schema.getColumns() ){
			c.fillDomain(schema, db);
			c.fillDomainBoundaries(schema, db);
		}
		
		GeneratorDB gen = new GeneratorDB(db);
		
		long start = System.currentTimeMillis();
		gen.pumpTable(nRowsToInsert, db.getSchema("pointTest"));
		long end = System.currentTimeMillis();
		
		assertEquals(nRowsToInsert, Statistics.getIntStat("pointTest.id fresh values"));
		
		logger.info("Time elapsed to pump "+nRowsToInsert+" rows: " + (end - start) + " msec.");
		logger.info(Statistics.printStats());
	}
	
	@Test
	public void testGenerateLinestring(){
		
		Schema schema = db.getSchema("testLinestring");
		
		for( ColumnPumper c : schema.getColumns() ){
			c.fillDomain(schema, db);
			c.fillDomainBoundaries(schema, db);
		}
		
		GeneratorDB gen = new GeneratorDB(db);
		
		long start = System.currentTimeMillis();
		gen.pumpTable(nRowsToInsert, db.getSchema("testLinestring"));
		long end = System.currentTimeMillis();
		
		assertEquals(nRowsToInsert, Statistics.getIntStat("testLinestring.id fresh values"));
		
		logger.info("Time elapsed to pump "+nRowsToInsert+" rows: " + (end - start) + " msec.");
		logger.info(Statistics.printStats());
	}	
	
	@Test
	public void testGeneratePolygon(){
		
		Schema schema = db.getSchema("testPolygon");
		
		for( ColumnPumper c : schema.getColumns() ){
			c.fillDomain(schema, db);
			c.fillDomainBoundaries(schema, db);
		}
		
		GeneratorDB gen = new GeneratorDB(db);
		
		long start = System.currentTimeMillis();
		gen.pumpTable(nRowsToInsert, db.getSchema("testPolygon"));
		long end = System.currentTimeMillis();
		
		assertEquals(nRowsToInsert, Statistics.getIntStat("testPolygon.id fresh values"));
		
		logger.info("Time elapsed to pump "+nRowsToInsert+" rows: " + (end - start) + " msec.");
		logger.info(Statistics.printStats());
	}	
	
	@Test
	public void testGenerateMultiLinestring(){
		
		Schema schema = db.getSchema("testMultilinestring");
		
		for( ColumnPumper c : schema.getColumns() ){
			c.fillDomain(schema, db);
			c.fillDomainBoundaries(schema, db);
		}

		GeneratorDB gen = new GeneratorDB(db);
		
		long start = System.currentTimeMillis();
		gen.pumpTable(nRowsToInsert, db.getSchema("testMultilinestring"));
		long end = System.currentTimeMillis();
		
		assertEquals(nRowsToInsert, Statistics.getIntStat("testMultilinestring.id fresh values"));
		
		logger.info("Time elapsed to pump "+nRowsToInsert+" rows: " + (end - start) + " msec.");
		logger.info(Statistics.printStats());
	}
	
	@Test
	public void testGenerateMultiPolygon(){
		
		Schema schema = db.getSchema("testMultipolygon");
		
		for( ColumnPumper c : schema.getColumns() ){
			c.fillDomain(schema, db);
			c.fillDomainBoundaries(schema, db);
		}

		GeneratorDB gen = new GeneratorDB(db);
		
		long start = System.currentTimeMillis();
		gen.pumpTable(nRowsToInsert, db.getSchema("testMultipolygon"));
		long end = System.currentTimeMillis();
		
		assertEquals(nRowsToInsert, Statistics.getIntStat("testMultipolygon.id fresh values"));
		
		logger.info("Time elapsed to pump "+nRowsToInsert+" rows: " + (end - start) + " msec.");
		logger.info(Statistics.printStats());
	}
	
	@Test
	public void testAutoincrement(){
		
		Schema schema = db.getSchema("testAutoincrement");
		
		for( ColumnPumper c : schema.getColumns() ){
			c.fillDomain(schema, db);
			c.fillDomainBoundaries(schema, db);
		}

		GeneratorDB gen = new GeneratorDB(db);
		
		long start = System.currentTimeMillis();
		gen.pumpTable(nRowsToInsert, db.getSchema("testAutoincrement"));
		long end = System.currentTimeMillis();
		
		
		logger.info("Time elapsed to pump "+nRowsToInsert+" rows: " + (end - start) + " msec.");
		logger.info(Statistics.printStats());

		assertEquals(nRowsToInsert, Statistics.getIntStat("testAutoincrement.id fresh values"));
	}
	
	@Test
	public void testGenerateDate(){
		
		// Init
		PreparedStatement init = db.getPreparedStatement("insert into dateTest values (1, NULL), (2, '2005-06-06')");
		
		try{
			init.execute();
		}
		catch(SQLException e){
			e.printStackTrace();
		}
		
		Schema schema = db.getSchema("dateTest");
		
		for( ColumnPumper c : schema.getColumns() ){
			c.fillDomain(schema, db);
			c.fillDomainBoundaries(schema, db);
		}
		
		GeneratorDB gen = new GeneratorDB(db);
		
		long start = System.currentTimeMillis();
		gen.pumpTable(nRowsToInsert, db.getSchema("dateTest"));
		long end = System.currentTimeMillis();
		
		assertEquals(nRowsToInsert, Statistics.getIntStat("dateTest.id fresh values"));
		
		logger.info("Time elapsed to pump "+nRowsToInsert+" rows: " + (end - start) + " msec.");
		logger.info(Statistics.printStats());
	}
	
}
