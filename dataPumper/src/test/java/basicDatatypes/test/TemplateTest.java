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
