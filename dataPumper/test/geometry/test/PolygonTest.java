package geometry.test;

import static org.junit.Assert.*;
import geometry.Polygon;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;


public class PolygonTest {

	private static String rectanglesWKT = "Polygon((0 0,1 0,1 1,0 1,0 0),(0 0,1 0,9 8,0 0))";
	private static Logger logger = Logger.getLogger(PolygonTest.class.getCanonicalName());
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void testConstructor() {
		Polygon p = new Polygon(rectanglesWKT);
		logger.info(p);
		assertEquals(rectanglesWKT,p.toString());
	}

}
