package core.test;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import basicDatatypes.Schema;
import connection.DBMSConnection;
import core.Generator;

/**
 * TODO Make of this a proper unit test
 * @author tir
 *
 */
public class GeneratorTest {
	private static String jdbcConnector = "jdbc:mysql";
	private static String databaseUrl = "localhost/ciao";
	private static String username = "tir";
	private static String password = "";
	
	private DBMSConnection db;
	private Connection conn;
	
	@Before
	public void setUp(){
		db = new DBMSConnection(jdbcConnector, databaseUrl, username, password);
		conn = db.getConnection();
	}
	
	@After
	public void tearDown(){
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testPumpDatabase(){
		Generator gen = new Generator(conn);
		Schema schema = gen.getTableSchema("example");
		gen.fillDomainBoundaries(schema);
		gen.createInsertTemplate(schema);
		gen.pumpTable("example", 10, schema, true);
	}
	
	@Test
	public void testFillDomainBoundaries(){
		
		Generator gen = new Generator(conn);
		Schema schema = gen.getTableSchema("example");
		gen.fillDomainBoundaries(schema);
		
		System.out.println(schema.getDomain("id").max);
		System.out.println(schema.getDomain("id").min);
		
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetTableSchema() {
		
		Generator gen = new Generator(conn);
		Schema schema = gen.getTableSchema("example");
		
		System.out.println(schema);
	}
	
	@Test
	public void testCreateInsertTemplate(){		
		
		Generator gen = new Generator(conn);
		Schema schema = gen.getTableSchema("example");
		
		System.out.println(gen.createInsertTemplate(schema));
	}
	
	@Test
	public void testSomething(){
		
		Generator gen = new Generator(conn);
		Schema schema = gen.getTableSchema("allTypes");
		
		System.out.println(schema);
	}
}
