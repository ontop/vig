package it.unibz.inf.data_pumper.configuration;

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

import it.unibz.inf.data_pumper.core.main.Main;

import java.io.*;

/**
 * Reads the configuration info from a configuration file.
 * @author Davide Lanti
 *
 */
public class Conf {
	
	protected String confFile;
	protected String confDir;
	private static Conf instance = null;
	
	protected Conf(String resourcesDir){
		this.confFile = resourcesDir + "/configuration.conf";
		this.confDir = resourcesDir;
	};
	
	public static Conf getInstance(){
		if( instance == null ){
			instance = new Conf(Main.optResources.getValue());
		}
		return instance;
	}
	
	public String confDir() {
	    return this.confDir;
	}
	
	
	/** Returns the name of the database driver **/
	public String jdbcConnector(){
		return searchTag("jdbc-connector");
	}
	/** Returns the url of the original database (this will not be pumped. A copy of it will) **/
	public  String dbUrl(){
		return searchTag("database-url");
	}
	/** Returns the username for the original database (this will not be pumped. A copy of it will) **/
	public  String dbUser(){
		return searchTag("database-user");
	}
	/** Returns the password for the original database (this will not be pumped. A copy of it will) **/
	public  String dbPwd(){
		return searchTag("database-pwd");
	}
	public  boolean pureRandomGeneration(){
		String randomValue = searchTag("random-gen");
		return randomValue.equals("true");
	}
	/** Returns the obda file containing the mappings **/
	public  String mappingsFile(){
		return searchTag("obda-file");
	}
	/** Returns the configuration scheme for the data generation **/
	public  String pumperType(){
		return searchTag("pumper-type");
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
