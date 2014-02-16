package utils;

import static org.junit.Assert.*;

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

}
