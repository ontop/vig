package cyclesFinder.test;

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

import org.apache.log4j.Logger;
import org.junit.Test;

import cyclesFinder.CyclesFinder;

public class CyclesFinderTest {
	
	private static Logger logger = Logger.getLogger(CyclesFinderTest.class.getCanonicalName());
	
	@Test
	public void testLengthsOfChaseSteps(){
		List<String> v = new ArrayList<String>();
		List<String> w = new ArrayList<String>();
		
		v.add("1"); v.add("2"); v.add("3"); v.add("4"); v.add("5"); v.add("6");
		w.add("2"); w.add("2"); w.add("4"); w.add("2"); w.add("6"); w.add("6");
		
		CyclesFinder c = new CyclesFinder();
		
		List<List<String>> results = c.chasePaths(v, w);
		
		assertEquals("[[1, 2], [2], [3, 4, 2], [4, 2], [5, 6], [6]]", results.toString());
		
		logger.debug(results.toString());
	}

}
