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

import columnTypes.ColumnPumper;
import basicDatatypes.Schema;
import basicDatatypes.Template;
import connection.DBMSConnection;
import core.Main;

public class MainTest {
	
	private static String jdbcConnector = "jdbc:mysql";
	private static String databaseUrl = "10.7.20.39:3306/pumperTest";
	private static String username = "test";
	private static String password = "ontop2014";
	
//	private static String jdbcConnector1 = "jdbc:mysql";
//	private static String databaseUrl1 = "10.7.20.39:3306/pumperNpd";
//	private static String username1 = "test";
//	private static String password1 = "ontop2014";
	
	private static String jdbcConnector1 = "jdbc:mysql";
	private static String databaseUrl1 = "10.7.20.39:3306/npd_3";
	private static String username1 = "test";
	private static String password1 = "ontop2014";
	
	private static String jdbcConnectorOriginal = "jdbc:mysql";
	private static String databaseUrlOriginal = "10.7.20.39:3306/npd";
	private static String usernameOriginal = "test";
	private static String passwordOriginal = "ontop2014";
	
	private static DBMSConnection db;
	private static DBMSConnection db1;
	private static DBMSConnection db1Original;
	
	private static Logger logger = Logger.getLogger(MainTest.class.getCanonicalName());
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		db = new DBMSConnection(jdbcConnector, databaseUrl, username, password);
		db1 = new DBMSConnection(jdbcConnector1, databaseUrl1, username1, password1);
		db1Original = new DBMSConnection(jdbcConnectorOriginal, databaseUrlOriginal, usernameOriginal, passwordOriginal);
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
		
		schemaA.getColumn("value").setMaximumChaseCycles(3);
		schemaB.getColumn("id").setMaximumChaseCycles(3);
		schemaC.getColumn("id").setMaximumChaseCycles(2);
		
		db.setForeignCheckOff();
		db.setUniqueCheckOff();
		main.pumpDatabase(db, db, 1);
		db.setUniqueCheckOn();
		db.setForeignCheckOn();
	}

	@Test
	public void testPumpNPD() {
		Main main = new Main();		
		
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
	
		main.pumpDatabase(db1Original, db1, 10000);
		long end = System.currentTimeMillis();

		logger.info("Time elapsed to pump "+10000+" rows: " + (end - start) + " msec.");
		db1.setUniqueCheckOn();
		db1.setForeignCheckOn();
	}
	
	@Test
	public void testPumpNPDPercentage() {
		Main main = new Main();		
		
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
	
		main.pumpDatabase(db1Original, db1, (float)3);
		long end = System.currentTimeMillis();

		logger.info("Time elapsed to pump rows: " + (end - start) + " msec.");
//		logger.info(Statistics.printStats());
		db1.setUniqueCheckOn();
		db1.setForeignCheckOn();
	}
	
	@Test
	public void takeReferredFrom(){
		
		Schema s = db1Original.getSchema("wellbore_formation_top");
		
		ColumnPumper c = s.getColumn("lsuNpdidLithoStrat");
		
		System.err.println(c.referencedBy().size());
	}
}


