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

import core.main.Main;

/**
 * Reads the configuration info from a configuration file.
 * @author Davide Lanti
 *
 */
public class Conf {
	
	protected String confFile;
	private static Conf instance = null;
	
	protected Conf(String resourcesDir){
		this.confFile = resourcesDir + "/configuration.conf";
	};
	
	public static Conf getInstance(){
		if( instance == null ){
			instance = new Conf(Main.optResources.getValue());
		}
		return instance;
	}
	
	
	/** Returns the name of the database driver **/
	public String jdbcConnector(){
		return searchTag("JdbcConnector");
	}
	/** Returns the url of the original database (this will not be pumped. A copy of it will) **/
	public  String dbUrlOriginal(){
		return searchTag("DbUrlOriginal");
	}
	/** Returns the username for the original database (this will not be pumped. A copy of it will) **/
	public  String dbUsernameOriginal(){
		return searchTag("DbUsernameOriginal");
	}
	/** Returns the password for the original database (this will not be pumped. A copy of it will) **/
	public  String dbPasswordOriginal(){
		return searchTag("DbPasswordOriginal");
	}
	
	/** Returns the url of the database to be pumped **/
	public  String dbUrlToPump(){
		return searchTag("DbUrlToPump");
	}
	/** Returns the username of the database to be pumped **/
	public  String dbUsernameToPump(){
		return searchTag("DbUsernameToPump");
	}
	/** Returns the password of the database to be pumped **/
	public  String dbPasswordToPump(){
		return searchTag("DbPasswordToPump");
	}
	public  boolean pureRandomGeneration(){
		String randomValue = searchTag("randomGen");
		return randomValue.equals("true");
	}
	/** Returns the obda file containing the mappings **/
	public  String mappingsFile(){
		return searchTag("obdaFile");
	}
	/** Returns the configuration scheme for the data generation **/
	public  String pumperType(){
		return searchTag("pumperType");
	}
	
	protected  String searchTag(String tag){
		try{
			BufferedReader in = new BufferedReader(
					new FileReader(confFile));
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
