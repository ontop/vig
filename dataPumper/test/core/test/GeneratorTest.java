package core.test;

//import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
	
	private static String jdbcConnector1 = "jdbc:mysql";
	private static String databaseUrl1 = "10.7.20.39:3306/npd";
	private static String username1 = "fish";
	private static String password1 = "fish";
	
	private DBMSConnection db;
	private DBMSConnection db1;
	private Connection conn;
	private Connection conn1;
	
	@Before
	public void setUp(){
		db = new DBMSConnection(jdbcConnector, databaseUrl, username, password);
		conn = db.getConnection();
		db1 = new DBMSConnection(jdbcConnector1, databaseUrl1, username1, password1);
		conn1 = db1.getConnection();
	}
	
	@After
	public void tearDown(){
		try {
			conn.close();
			conn1.close();
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
		gen.pumpTable("example", 10000, schema, true, 0);
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
		
		gen = new Generator(conn1);
		schema = gen.getTableSchema("baaArea");
		
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
		
		try {
			PreparedStatement stmt = conn1.prepareStatement("select TABLE_NAME,COLUMN_NAME,CONSTRAINT_NAME, REFERENCED_TABLE_NAME,REFERENCED_COLUMN_NAME from INFORMATION_SCHEMA.KEY_COLUMN_USAGE where TABLE_NAME='baaArea' and constraint_schema = 'npd' and REFERENCED_TABLE_NAME != 'null'");
						
			ResultSet rs = stmt.executeQuery();
			
			while(rs.next()){
				System.out.println(rs.getString(1) + "   " + rs.getString(2));
			}
		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
}
