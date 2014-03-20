package cyclesFinder.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import cyclesFinder.CyclesFinder;

public class CyclesFinderTest {
	
	@Test
	public void testLengthsOfChaseSteps(){
		List<String> v = new ArrayList<String>();
		List<String> w = new ArrayList<String>();
		
		v.add("1"); v.add("2"); v.add("3"); v.add("4"); v.add("5"); v.add("6");
		w.add("2"); w.add("2"); w.add("4"); w.add("2"); w.add("6"); w.add("6");
		
		CyclesFinder c = new CyclesFinder();
		
		List<List<String>> results = c.chasePaths(v, w);
		
		System.out.println(results);
	}

}
