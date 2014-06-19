package configuration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * 
 * @author Davide Lanti
 * 
 * It reads the configuration for the unit tests
 *
 */
public class UnitConf extends Conf {
	
	/** Returns the configuration scheme for the data generation **/
	public static String dbSingleTests(){
		return searchTag("DbUrlSingleTests");
	}
	/** Returns the configuration scheme for the data generation **/
	public static String dbUsernameSingleTests(){
		return searchTag("DbUrlUsernameSingleTests");
	}
	
	public static String dbPasswordSingleTests(){
		return searchTag("DbPasswordSingleTests");
	}
	
	private static String searchTag(String tag){
		try{
			BufferedReader in = new BufferedReader(
					new FileReader("src/main/resources/unitTests.conf"));
			String s;
			String[] s2 = new String[2];
			while ((s = in.readLine()) != null){
				s2 = s.split("\\s+");
				if (s2[0].equals(tag)){ in.close(); return s2[1]; }
			}
			in.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		return "error";
	}
}
