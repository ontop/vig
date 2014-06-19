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



import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import configuration.UnitConf;
import connection.DBMSConnection;
import core.main.DatabasePumper;
import core.main.DatabasePumperDB;
import core.main.DatabasePumperOBDA;

public class MainTest {
	
	private static String jdbcConnector = UnitConf.jdbcConnector();
	private static String databaseUrl = UnitConf.dbUrlToPump();
	private static String username = UnitConf.dbUsernameToPump();
	private static String password = UnitConf.dbPasswordToPump();
	
	private static String jdbcConnectorOriginal = UnitConf.jdbcConnector();
	private static String databaseUrlOriginal = UnitConf.dbUrlOriginal();
	private static String usernameOriginal = UnitConf.dbUsernameOriginal();
	private static String passwordOriginal = UnitConf.dbPasswordOriginal();
	
	private static DBMSConnection db;
	private static DBMSConnection dbOriginal;
	
	private static Logger logger = Logger.getLogger(MainTest.class.getCanonicalName());
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		
		db = new DBMSConnection(jdbcConnector, databaseUrl, username, password);
		dbOriginal = new DBMSConnection(jdbcConnectorOriginal, databaseUrlOriginal, usernameOriginal, passwordOriginal);
	}
	
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


