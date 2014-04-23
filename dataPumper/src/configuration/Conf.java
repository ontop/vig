package configuration;

import java.io.*;

/**
 * Reads the configuration info from a configuration file.
 * @author tir
 *
 */
public class Conf {
		
	/** Returns the name of the database driver **/
	public static String jdbcConnector(){
		return searchTag("JdbcConnector");
	}
	/** Returns the url of the original database (this will not be pumped. A copy of it will) **/
	public static String dbUrlOriginal(){
		return searchTag("DbUrlOriginal");
	}
	/** Returns the username for the original database (this will not be pumped. A copy of it will) **/
	public static String dbUsernameOriginal(){
		return searchTag("DbUsernameOriginal");
	}
	/** Returns the password for the original database (this will not be pumped. A copy of it will) **/
	public static String dbPasswordOriginal(){
		return searchTag("DbPasswordOriginal");
	}
	
	/** Returns the url of the database to be pumped **/
	public static String dbUrlToPump(){
		return searchTag("DbUrlToPump");
	}
	/** Returns the username of the database to be pumped **/
	public static String dbUsernameToPump(){
		return searchTag("DbUsernameToPump");
	}
	/** Returns the password of the database to be pumped **/
	public static String dbPasswordToPump(){
		return searchTag("DbPasswordToPump");
	}
	public static boolean pureRandomGeneration(){
		String randomValue = searchTag("randomGen");
		return randomValue.equals("true");
	}
	/** Returns the obda file containing the mappings **/
	public static String mappingsFile(){
		return searchTag("obdaFile");
	}
	
	private static String searchTag(String tag){
		try{
			BufferedReader in = new BufferedReader(
					new FileReader("configuration.conf"));
			String s;
			String[] s2 = new String[2];
			while ((s = in.readLine()) != null){
				s2 = s.split(" ");
				if (s2[0].equals(tag)){ in.close(); return s2[1]; }
			}
			in.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		return "error";
	}
}
