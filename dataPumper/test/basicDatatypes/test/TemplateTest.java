package basicDatatypes.test;

import static org.junit.Assert.*;

import org.junit.Test;

import basicDatatypes.Template;

public class TemplateTest {
	private String templateString = "select ? from ? where ?";
	
	
	@Test
	public void testSetNthPlaceholder() {
		String temp = templateString;
		Template t = new Template(temp);
		t.setNthPlaceholder(1, "max(x,y,z)");
		t.setNthPlaceholder(2, "pappappero");
		t.setNthPlaceholder(3, "ciao = ciao");
		
		assertEquals("select max(x,y,z) from pappappero where ciao = ciao", t.getFilled());
	}

}
