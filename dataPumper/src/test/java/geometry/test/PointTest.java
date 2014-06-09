package geometry.test;

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
		
//		assertEquals("Point(-2.28888388309114854e+18 1.0)", p.toString());
	}

	
}