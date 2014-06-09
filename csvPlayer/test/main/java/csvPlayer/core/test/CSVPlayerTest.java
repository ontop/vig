package main.java.csvPlayer.core.test;

import static org.junit.Assert.*;

import java.util.List;

import main.java.csvPlayer.core.CSVPlayer;
import main.java.csvPlayer.exceptions.InvalidColumnIndexException;

import org.junit.BeforeClass;
import org.junit.Test;

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
