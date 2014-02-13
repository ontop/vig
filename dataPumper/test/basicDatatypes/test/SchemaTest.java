package basicDatatypes.test;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import basicDatatypes.Schema;
import connection.DBMSConnection;
import core.Generator;

public class SchemaTest {
	
	private static String jdbcConnector = "jdbc:mysql";
	private static String databaseUrl = "10.7.20.39:3306/npd";
	private static String username = "fish";
	private static String password = "fish";
	
	private DBMSConnection db;
	private Connection conn;
	private Generator gen;
	
	@Before
	public void setUp() throws Exception {
		db = new DBMSConnection(jdbcConnector, databaseUrl, username, password);
		conn = db.getConnection();
		gen = new Generator(conn);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testEqualsTo() {
		
		Schema s1 = gen.getTableSchema("baaArea");
		Schema s2 = gen.getTableSchema("baaArea");
		
		assertTrue(s1.equals(s2));
		
		List<Schema> schemas = new ArrayList<Schema>();
		schemas.add(s1);
		
		assertTrue(schemas.contains(s2));
	}

}
