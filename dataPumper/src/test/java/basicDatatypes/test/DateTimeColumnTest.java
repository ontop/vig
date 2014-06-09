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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import basicDatatypes.Template;
import connection.DBMSConnection;
import core.test.GeneratorDBTest;

public class DateTimeColumnTest {
	
	private static String jdbcConnector = "jdbc:mysql";
	private static String databaseUrl = "10.7.20.39:3306/pumperTest";
	private static String username = "test";
	private static String password = "ontop2014";
	
	private static DBMSConnection db;
	
	private static Logger logger = Logger.getLogger(DateTimeColumnTest.class.getCanonicalName());
	
	@BeforeClass
	public static void setUpBeforeClass(){
		db = new DBMSConnection(jdbcConnector, databaseUrl, username, password);
	}
	
	@AfterClass
	public static void tearDownAfterClass(){
		db.close();
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
	public void testSomething(){
		
		PreparedStatement init = db.getPreparedStatement("insert into datetimeTest values (1, '2005-11-23 12:56:00')");
				
		try{
			init.execute();
		}
		catch(SQLException e){
			e.printStackTrace();
		}
		
		PreparedStatement stmt = db.getPreparedStatement("SELECT date FROM datetimeTest");
		
		Timestamp time = null;
		try {
			ResultSet rs = stmt.executeQuery();
			while( rs.next() ){
				logger.info("ciao");
				time = rs.getTimestamp(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		System.out.println(time);
		logger.info(time.getTime());
	}
	
}
