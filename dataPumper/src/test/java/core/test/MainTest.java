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



import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import basicDatatypes.Schema;
import configuration.UnitConf;
import connection.DBMSConnection;
import core.main.DatabasePumper;
import core.main.DatabasePumperDB;
import core.main.DatabasePumperOBDA;

public class MainTest {
	
	private static String jdbcConnector = UnitConf.getInstance().jdbcConnector();
	private static String databaseUrl = UnitConf.getInstance().dbUrlToPump();
	private static String username = UnitConf.getInstance().dbUsernameToPump();
	private static String password = UnitConf.getInstance().dbPasswordToPump();
	
	private static String jdbcConnectorOriginal = UnitConf.getInstance().jdbcConnector();
	private static String databaseUrlOriginal = UnitConf.getInstance().dbUrlOriginal();
	private static String usernameOriginal = UnitConf.getInstance().dbUsernameOriginal();
	private static String passwordOriginal = UnitConf.getInstance().dbPasswordOriginal();
	
	private static String jdbcConnectorSingle = UnitConf.getInstance().jdbcConnector();
	private static String databaseUrlSingle = UnitConf.getInstance().dbUrlSingleTests();
	private static String usernameSingle = UnitConf.getInstance().dbUsernameSingleTests();
	private static String passwordSingle = UnitConf.getInstance().dbPasswordSingleTests();
	
	private static DBMSConnection db;
	private static DBMSConnection dbOriginal;
	private static DBMSConnection dbSingleTests;
	private static DBMSConnection dbSingleTestsToPump;
	
	private static Logger logger = Logger.getLogger(MainTest.class.getCanonicalName());
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		
		db = new DBMSConnection(jdbcConnector, databaseUrl, username, password);
		dbOriginal = new DBMSConnection(jdbcConnectorOriginal, databaseUrlOriginal, usernameOriginal, passwordOriginal);
		dbSingleTests = new DBMSConnection(jdbcConnectorSingle, databaseUrlSingle, usernameSingle, passwordSingle);
	
		dbSingleTestsToPump = new DBMSConnection("jdbc:mysql", "localhost/pumperTestToPump", "", "");
	}
	
	private void initTestPumpDatabase(){
		dbSingleTestsToPump.setForeignCheckOff();
		dbSingleTestsToPump.setUniqueCheckOff();
		
		PreparedStatement insertions = dbSingleTestsToPump.getPreparedStatement("INSERT INTO fKeyA VALUES (1,'ciao'), (2,'ciriciao'), (3,'ciriciriciao')");
		
		try {
			insertions.execute();
			insertions.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		PreparedStatement insertion2 = dbSingleTestsToPump.getPreparedStatement("INSERT INTO fKeyB VALUES (1,'ciao'), (2,'ciriciao'), (3,'ciriciriciao')");
		
		try {
			insertion2.execute();
			insertion2.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		insertion2 = dbSingleTestsToPump.getPreparedStatement("INSERT INTO fKeyC VALUES (1)");
		
		try {
			insertion2.execute();
			insertion2.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		dbSingleTests.setForeignCheckOff();
		dbSingleTests.setUniqueCheckOff();
		
		insertions = dbSingleTests.getPreparedStatement("INSERT INTO fKeyA VALUES (1,'ciao'), (2,'ciriciao'), (3,'ciriciriciao')");
		
		try {
			insertions.execute();
			insertions.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		insertion2 = dbSingleTests.getPreparedStatement("INSERT INTO fKeyB VALUES (1,'ciao'), (2,'ciriciao'), (3,'ciriciriciao')");
		
		try {
			insertion2.execute();
			insertion2.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		insertion2 = dbSingleTests.getPreparedStatement("INSERT INTO fKeyC VALUES (1)");
		
		try {
			insertion2.execute();
			insertion2.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void endTestPumpDatabase(){
		dbSingleTestsToPump.setForeignCheckOff();
		dbSingleTestsToPump.setUniqueCheckOff();
		
		PreparedStatement delete = dbSingleTestsToPump.getPreparedStatement("DELETE FROM fKeyA");
		
		try {
			delete.execute();
			delete.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		delete = dbSingleTestsToPump.getPreparedStatement("DELETE FROM fKeyB");
		
		try {
			delete.execute();
			delete.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		delete = dbSingleTestsToPump.getPreparedStatement("DELETE FROM fKeyC");
		
		try {
			delete.execute();
			delete.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		dbSingleTests.setForeignCheckOff();
		dbSingleTests.setUniqueCheckOff();
		
		delete = dbSingleTests.getPreparedStatement("DELETE FROM fKeyA");
		
		try {
			delete.execute();
			delete.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		delete = dbSingleTests.getPreparedStatement("DELETE FROM fKeyB");
		
		try {
			delete.execute();
			delete.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		delete = dbSingleTests.getPreparedStatement("DELETE FROM fKeyC");
		
		try {
			delete.execute();
			delete.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testPumpDatabase() {
		
		endTestPumpDatabase();
		initTestPumpDatabase();
		
		DatabasePumperDB main = new DatabasePumperDB(dbSingleTests, dbSingleTestsToPump);
		
		Schema schemaA = dbSingleTestsToPump.getSchema("fKeyA");
		Schema schemaB = dbSingleTestsToPump.getSchema("fKeyB"); 
		Schema schemaC = dbSingleTestsToPump.getSchema("selfDependency");
		
		schemaA.getColumn("value").setMaximumChaseCycles(3);
		schemaB.getColumn("id").setMaximumChaseCycles(3);
		schemaC.getColumn("id").setMaximumChaseCycles(2);
		
		main.pumpDatabase((double) 10);
		
		endTestPumpDatabase();
		
		dbSingleTestsToPump.setUniqueCheckOn();
		dbSingleTestsToPump.setForeignCheckOn();
	}
//

	@Test
	@Ignore
	public void testPumpNPDPercentage() {
		DatabasePumperDB main = new DatabasePumperDB(dbOriginal, db);		
		
		long start = System.currentTimeMillis();
	
		main.pumpDatabase((double)1);
		
		long end = System.currentTimeMillis();

		logger.info("Time elapsed to pump tables: " + (end - start) + " msec.");
//		logger.info(Statistics.printStats());
		db.setUniqueCheckOn();
		db.setForeignCheckOn();
	}
	
	@Test
	@Ignore
	public void testPumpNPDPercentageOBDAStyle() {
		DatabasePumper main = new DatabasePumperOBDA(dbOriginal, db);		
		
		long start = System.currentTimeMillis();
	
		main.pumpDatabase((double)1);
		
		long end = System.currentTimeMillis();

		logger.info("Time elapsed to pump tables: " + (end - start) + " msec.");
//		logger.info(Statistics.printStats());
		db.setUniqueCheckOn();
		db.setForeignCheckOn();
	}
	
}


