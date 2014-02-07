package core.test;

import static org.junit.Assert.*;

import java.sql.Connection;

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
	
	
	@Test
	public void testGetTableSchema() {
		DBMSConnection db = new DBMSConnection(jdbcConnector, databaseUrl, username, password);
		Connection conn = db.getConnection();
		
		Generator gen = new Generator(conn);
		Schema schema = gen.getTableSchema("example");
		
		System.out.println(schema);
	}
	
	@Test
	public void testCreateInsertTemplate(){		
		DBMSConnection db = new DBMSConnection(jdbcConnector, databaseUrl, username, password);
		Connection conn = db.getConnection();
		
		Generator gen = new Generator(conn);
		
		Schema schema = gen.getTableSchema("example");
		
		System.out.println(gen.createInsertTemplate(schema));
	}
}
