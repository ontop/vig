package it.unibz.inf.data_pumper.utils_test;

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
import it.unibz.inf.data_pumper.utils.TrivialQueue;

import org.junit.Before;
import org.junit.Test;

public class TrivialQueueTest {	
	
	TrivialQueue<Integer> queue;
	
	@Before
	public void setUp() throws Exception {
		queue = new TrivialQueue<Integer>();
	}

	@Test
	public void testEnqueueDequeue(){
		
		queue.enqueue(1);
		queue.enqueue(2);
		queue.enqueue(3);
		
		assertTrue(queue.toString().equals("[1, 2, 3]"));
		
		int element = queue.dequeue();
		
		assertEquals(element, 1);
		
		assertTrue(queue.toString().equals("[2, 3]"));
	}

	@Test
	public void testContains(){
		queue.enqueue(1);
		queue.enqueue(2);
		queue.enqueue(3);
		
		queue.contains(1);
		
		assertTrue(queue.contains(1));
	}
}
