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
import geometry.Point;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

public class PointTest {
	
	private static Logger logger = Logger.getLogger(PointTest.class.getCanonicalName());

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void testConstructor() {
		Point p = new Point("Point(-2.28888388309114854e+18 1)");
		
		logger.info(p.toString());
		
		assertEquals("Point(-2.28888388309114854e+18 1.0)", p.toString());
	}

	
}