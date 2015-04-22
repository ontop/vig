package it.unibz.inf.vig_mappings_analyzer.core;

import junit.framework.TestCase;

/**
 * The class <code>JoinableColumnsFinderTest</code> contains tests for the
 * class {@link <code>JoinableColumnsFinder</code>}
 *
 * @pattern JUnit Test Case
 *
 * @generatedBy CodePro at 3/13/15 11:29 AM
 *
 * @author Davide Lanti
 *
 * @version $Revision$
 */
public class JoinableColumnsFinderTest extends TestCase {

	/**
	 * Construct new test instance
	 *
	 * @param name the test name
	 */
	public JoinableColumnsFinderTest(String name) {
		super(name);
	}

	/**
	 * Run the Set<FunctionTemplate> findFunctionTemplates() method test
	 */
	public void testFindFunctionTemplatesWithAliases() {
		try {
			JoinableColumnsFinder a = new JoinableColumnsFinder("src/test/resources/npd-v2-ql_a_aliases.obda");
			
			String expected = "[\n" +
					"TEMPLATE: http://sws.ifi.uio.no/data/npd-v2/wellbore/{}/point\n" +
					"ARGS: [[wellbore_coordinates.wlbnpdidwellbore]], \n"+
					"TEMPLATE: http://www.w3.org/2001/XMLSchema#integer()\n"+
					"ARGS: [[wellbore_coordinates.wlbnsmin]]]";
			
			assertEquals(expected, a.findFunctionTemplates().toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

/*$CPS$ This comment was generated by CodePro. Do not edit it.
 * patternId = com.instantiations.assist.eclipse.pattern.testCasePattern
 * strategyId = com.instantiations.assist.eclipse.pattern.testCasePattern.junitTestCase
 * additionalTestNames = 
 * assertTrue = false
 * callTestMethod = true
 * createMain = false
 * createSetUp = false
 * createTearDown = false
 * createTestFixture = false
 * createTestStubs = false
 * methods = findFunctionTemplates()
 * package = it.unibz.inf.vig_mappings_analyzer.core
 * package.sourceFolder = vig-mappings-analyzer/src/main/java
 * superclassType = junit.framework.TestCase
 * testCase = JoinableColumnsFinderTest
 * testClassType = it.unibz.inf.vig_mappings_analyzer.core.JoinableColumnsFinder
 */