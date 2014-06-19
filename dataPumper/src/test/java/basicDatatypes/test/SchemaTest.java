package basicDatatypes.test;

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

import java.util.ArrayList;
import java.util.List;


//import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import basicDatatypes.Schema;
import configuration.UnitConf;
import connection.DBMSConnection;

public class SchemaTest {
	
	private static String jdbcConnector = UnitConf.jdbcConnector();
	private static String databaseUrl = UnitConf.dbUrlToPump();
	private static String username = UnitConf.dbUsernameToPump();
	private static String password = UnitConf.dbPasswordToPump();
		
	private static DBMSConnection db;
	
	@BeforeClass
	public static void setUp() throws Exception {
		db = new DBMSConnection(jdbcConnector, databaseUrl, username, password);
	}

	@AfterClass
	public static void tearDown() throws Exception {
		db.close();
	}
	
	// It tests the equality for schemas
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
