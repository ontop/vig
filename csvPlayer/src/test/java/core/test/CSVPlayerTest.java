package core.test;

import java.util.List;

import org.junit.Test;

import core.CSVPlayer;
import exceptions.InvalidColumnIndexException;


public class CSVPlayerTest {
	
	private static String fileName = "resources/test/testCSV.csv";
	private static String separator = "\\s+";

	@Test
	public void testSearchAllOccurrencesOfTag(){
		CSVPlayer csvParser = new CSVPlayer(fileName, separator);
		
		try {
			List<String> result = csvParser.searchAllOccurrencesOfTag("Mapping:00910:ForeignKey", 1);
			
			System.out.println(result);
			
		} catch (InvalidColumnIndexException e) {
			e.printStackTrace();
		}
	}

}
