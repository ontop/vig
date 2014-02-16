package basicDatatypes.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

//import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import basicDatatypes.Schema;
import connection.DBMSConnection;

public class SchemaTest {
	
	private static String jdbcConnector = "jdbc:mysql";
	private static String databaseUrl = "10.7.20.39:3306/npd";
	private static String username = "fish";
	private static String password = "fish";
	
//	private static Logger logger = Logger.getLogger(SchemaTest.class.getCanonicalName());
	
	private static DBMSConnection db;
	
	@BeforeClass
	public static void setUp() throws Exception {
		db = new DBMSConnection(jdbcConnector, databaseUrl, username, password);
	}

	@AfterClass
	public static void tearDown() throws Exception {
		db.close();
	}

	@Test
	public void testEqualsTo() {
		
		Schema s1 = db.getSchema("baaArea");
		Schema s2 = db.getSchema("baaArea");
		
		assertTrue(s1.equals(s2));
		
		List<Schema> schemas = new ArrayList<Schema>();
		schemas.add(s1);
		
		assertTrue(schemas.contains(s2));
	}

}
