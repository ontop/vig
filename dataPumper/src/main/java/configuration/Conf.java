package configuration;

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
	/** Returns the configuration scheme for the data generation **/
	public static String pumperType(){
		return searchTag("pumperType");
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
