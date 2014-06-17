package geometry.test;

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
import geometry.Polygon;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;


public class PolygonTest {

	private static String rectanglesWKT = "Polygon((0 0,1 -2.28888388309114854E+18,1 1,0 1,0 0),(0 0,1 0,9 8,0 0))";
	private static Logger logger = Logger.getLogger(PolygonTest.class.getCanonicalName());
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void testConstructor() {
		Polygon p = new Polygon(rectanglesWKT);
		logger.info(p);
		
		
//		assertEquals(rectanglesWKT,p.toString());
	}

}
