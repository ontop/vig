package it.unibz.inf.data_pumper.basic_datatypes.test;

import static org.junit.Assert.*;
import it.unibz.inf.data_pumper.utils.Template;

import org.junit.BeforeClass;
import org.junit.Test;

public class TemplateTest {

	@Test
	public void testTemplateString() {
		Template temp = new Template("XX?XX");
		assertNotNull(temp);
	}

	@Test
	public void testTemplateStringString() {
		Template temp = new Template("XX?XX", "?");
		assertNotNull(temp);
	}

	@Test
	public void testGetFilled() {
		Template temp = new Template("XX?XX");
		String result = temp.getFilled();
		
		assertNotNull(result);
	}

	@Test
	public void testGetNumPlaceholders() {
		Template temp = new Template("XX?XX? ", "?");
		assertEquals(temp.getNumPlaceholders(), 2);
	}
};
