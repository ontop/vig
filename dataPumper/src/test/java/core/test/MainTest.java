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

import static org.junit.Assert.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import columnTypes.ColumnPumper;
import basicDatatypes.Schema;
import basicDatatypes.Template;
import connection.DBMSConnection;
import core.main.DatabasePumper;
import core.main.DatabasePumperDB;
import core.main.DatabasePumperOBDA;

public class MainTest {
	
	private static String jdbcConnector = "jdbc:mysql";
	private static String databaseUrl = "10.7.20.39:3306/pumperTest";
	private static String username = "test";
	private static String password = "ontop2014";
	
	private static String jdbcConnector1 = "jdbc:mysql";
	private static String databaseUrl1 = "10.7.20.39:3306/pumperNpd";
	private static String username1 = "test";
	private static String password1 = "ontop2014";
	
	private static String jdbcConnectorOriginal = "jdbc:mysql";
	private static String databaseUrlOriginal = "10.7.20.39:3306/npd";
	private static String usernameOriginal = "test";
	private static String passwordOriginal = "ontop2014";
	
	private static String jdbcDbOriginalLocal = "jdbc:mysql";
	private static String databaseUrlDbOriginalLocal = "localhost/provaNpdOriginal";
	private static String usernameDbOriginalLocal = "tir";
	private static String passwordDbOriginalLocal = "";

	private static String jdbcDbLocal = "jdbc:mysql";
	private static String databaseUrlDbLocal = "localhost/provaNpd";
	private static String usernameDbLocal = "tir";
	private static String passwordDbLocal = "";
	
	private static DBMSConnection dbOriginalLocal;
	private static DBMSConnection dbLocal;
	private static DBMSConnection db;
	private static DBMSConnection db1;
	private static DBMSConnection db1Original;
	
	private static Logger logger = Logger.getLogger(MainTest.class.getCanonicalName());
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		
//		db = new DBMSConnection(jdbcConnector, databaseUrl, username, password);
		db1 = new DBMSConnection(jdbcConnector1, databaseUrl1, username1, password1);
		db1Original = new DBMSConnection(jdbcConnectorOriginal, databaseUrlOriginal, usernameOriginal, passwordOriginal);
		dbLocal = new DBMSConnection(jdbcDbLocal, databaseUrlDbLocal, usernameDbLocal, passwordDbLocal);
		dbOriginalLocal = new DBMSConnection(jdbcDbOriginalLocal, databaseUrlDbOriginalLocal, usernameDbOriginalLocal, passwordDbOriginalLocal);
	}
	
//	@Before
//	public void setUp(){
//		// INIT
//		db.setForeignCheckOff();
//		for( String sName : db.getAllTableNames() ){
//			
//			Template temp = new Template("delete from ?");
//			temp.setNthPlaceholder(1, sName);
//			
//			PreparedStatement init = db.getPreparedStatement(temp);
//			
//			try {
//				init.execute();
//			} catch (SQLException e) {
//				e.printStackTrace();
//			}
//		}
//		db.setForeignCheckOn();
//		PropertyConfigurator.configure("log4j.properties");
//	}
//
//	@AfterClass
//	public static void tearDownAfterClass() throws Exception {
//	
//		db.close();
//	}
//	
//	@After
//	public void tearDown(){
//		// INIT
//		db.setForeignCheckOff();
//		for( String sName : db.getAllTableNames() ){
//			
//			Template temp = new Template("delete from ?");
//			temp.setNthPlaceholder(1, sName);
//			
//			PreparedStatement init = db.getPreparedStatement(temp);
//			
//			try {
//				init.execute();
//			} catch (SQLException e) {
//				e.printStackTrace();
//			}
//		}
//		db.setForeignCheckOn();	
//	}
	
//	@Test
//	public void testPumpDatabase() {
//		
//		logger.setLevel(Level.INFO);
//		
//		DatabasePumper main = new DatabasePumper();
//		
//		Schema schemaA = db.getSchema("fKeyA");
//		Schema schemaB = db.getSchema("fKeyB"); 
//		Schema schemaC = db.getSchema("selfDependency");
//		
//		schemaA.getColumn("value").setMaximumChaseCycles(3);
//		schemaB.getColumn("id").setMaximumChaseCycles(3);
//		schemaC.getColumn("id").setMaximumChaseCycles(2);
//		
//		db.setForeignCheckOff();
//		db.setUniqueCheckOff();
//		main.pumpDatabase(db, db, 1);
//		db.setUniqueCheckOn();
//		db.setForeignCheckOn();
//	}
//
//	@Test
//	public void testPumpNPD() {
//		DatabasePumper main = new DatabasePumper();		
//		
//		db1.setForeignCheckOff();
//		db1.setUniqueCheckOff();
//		
//		for( String tableName : db1.getAllTableNames() ){
//			Schema s = db1.getSchema(tableName);
//			for( ColumnPumper c : s.getColumns() ){
//				if( !c.referencesTo().isEmpty() ){
//					c.setMaximumChaseCycles(4);
//				}
//			}
//		}
//		
//		long start = System.currentTimeMillis();
//	
//		main.pumpDatabase(db1Original, db1, 10000);
//		long end = System.currentTimeMillis();
//
//		logger.info("Time elapsed to pump "+10000+" rows: " + (end - start) + " msec.");
//		db1.setUniqueCheckOn();
//		db1.setForeignCheckOn();
//	}
//	//
	@Test
	public void testPumpNPDPercentage() {
		DatabasePumperDB main = new DatabasePumperDB(db1Original, db1);		
		
		db1.setForeignCheckOff();
		db1.setUniqueCheckOff();
		
		for( String tableName : db1.getAllTableNames() ){
			Schema s = db1.getSchema(tableName);
			for( ColumnPumper c : s.getColumns() ){
				if( !c.referencesTo().isEmpty() ){
					c.setMaximumChaseCycles(4);
				}
			}
		}
		
		long start = System.currentTimeMillis();
	
		main.pumpDatabase((float)2);
		long end = System.currentTimeMillis();

		logger.info("Time elapsed to pump rows: " + (end - start) + " msec.");
//		logger.info(Statistics.printStats());
		db1.setUniqueCheckOn();
		db1.setForeignCheckOn();
	}
	
	@Test
	public void testPumpNPDPercentageOBDAStyleLOCAL() {
		DatabasePumper main = new DatabasePumperOBDA(dbOriginalLocal, dbLocal);		
		
		dbLocal.setForeignCheckOff();
		dbLocal.setUniqueCheckOff();
		
		for( String tableName : dbLocal.getAllTableNames() ){
			Schema s = dbLocal.getSchema(tableName);
			for( ColumnPumper c : s.getColumns() ){
				if( !c.referencesTo().isEmpty() ){
					c.setMaximumChaseCycles(4);
				}
			}
		}
		
		long start = System.currentTimeMillis();
	
		main.pumpDatabase((float)2);
		long end = System.currentTimeMillis();

		logger.info("Time elapsed to pump rows: " + (end - start) + " msec.");
//		logger.info(Statistics.printStats());
		dbLocal.setUniqueCheckOn();
		dbLocal.setForeignCheckOn();
	}
	
	@Test
	public void testPumpNPDPercentageOBDAStyle() {
		DatabasePumper main = new DatabasePumperOBDA(db1Original, db1);		
		
		db1.setForeignCheckOff();
		db1.setUniqueCheckOff();
		
		for( String tableName : db1.getAllTableNames() ){
			Schema s = db1.getSchema(tableName);
			for( ColumnPumper c : s.getColumns() ){
				if( !c.referencesTo().isEmpty() ){
					c.setMaximumChaseCycles(1);
				}
			}
		}
		
		long start = System.currentTimeMillis();
	
		main.pumpDatabase((float)2);
		long end = System.currentTimeMillis();

		logger.info("Time elapsed to pump rows: " + (end - start) + " msec.");
//		logger.info(Statistics.printStats());
		db1.setUniqueCheckOn();
		db1.setForeignCheckOn();
	}
	
	@Test
	public void takeReferredFrom(){
		
		Schema s = db1Original.getSchema("licence_area_poly_hst");
		
		ColumnPumper c = s.getColumn("prlAreaPolyDateValidFrom");
		
		System.err.println(c.referencedBy().size());
	}
}


