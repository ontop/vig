package core.test;

import static org.junit.Assert.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import utils.Statistics;
import basicDatatypes.Schema;
import basicDatatypes.Template;
import connection.DBMSConnection;
import core.Generator;
import core.Generator3;
import core.Main;

public class MainTest {
	
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
	
	private static Logger logger = Logger.getLogger(MainTest.class.getCanonicalName());
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		db = new DBMSConnection(jdbcConnector, databaseUrl, username, password);
		db1 = new DBMSConnection(jdbcConnector1, databaseUrl1, username1, password1);
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

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	
		db.close();
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
	public void testPumpDatabase() {
		
		logger.setLevel(Level.INFO);
		
		Main main = new Main();
		
		Schema schemaA = db.getSchema("fKeyA");
		Schema schemaB = db.getSchema("fKeyB"); 
		Schema schemaC = db.getSchema("selfDependency");
		
		schemaA.getColumn("value").setMaximumChaseCycles(1);
		schemaB.getColumn("id").setMaximumChaseCycles(1);
		schemaC.getColumn("id").setMaximumChaseCycles(3);
		
		db.setForeignCheckOff();
		db.setUniqueCheckOff();
		main.pumpDatabase(db, 1);
		db.setUniqueCheckOn();
		db.setForeignCheckOn();
	}

	@Test
	public void testPumpNPD() {
		Main main = new Main();		
		
		db1.setForeignCheckOff();
		db1.setUniqueCheckOff();
		long start = System.currentTimeMillis();
	
		main.pumpDatabase(db1, 10);
		long end = System.currentTimeMillis();

		logger.info("Time elapsed to pump "+10+" rows: " + (end - start) + " msec.");
		logger.info(Statistics.printStats());
		db1.setUniqueCheckOn();
		db1.setForeignCheckOn();
	}
}


