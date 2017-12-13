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

import it.unibz.inf.data_pumper.core.main.exceptions.DebugException;

import java.io.*;
import java.util.Arrays;
import java.util.List;

/**
 * Reads the configuration info from a configuration file.
 * @author Davide Lanti
 *
 */
public class ConfParser {

    protected String confFile;
    private static ConfParser instance = null;

    private ConfParser(String confFile){
	this.confFile = confFile;
    };
        
    public static ConfParser makeInstance(String confFile) {
	if( instance == null ) {
	    instance = new ConfParser(confFile);
	}
	return instance;
    }
    
    public static ConfParser getInstance(){
	if( instance == null ){
	    throw new DebugException("ConfParser.getInstance() should be called after a call to the method makeInstance()"); 
	}
	return instance;
    }

    /** Returns the name of the database driver **/
    public String jdbcConnector() throws IOException {
	return searchTag("jdbc-connector");
    }
    /** Returns the url of the original database (this will not be pumped. A copy of it will) 
     * @throws IOException **/
    public  String dbUrl() throws IOException{
	return searchTag("database-url");
    }
    /** Returns the username for the original database (this will not be pumped. A copy of it will) 
     * @throws IOException **/
    public  String dbUser() throws IOException{
	return searchTag("database-user");
    }
    /** Returns the password for the original database (this will not be pumped. A copy of it will) 
     * @throws IOException **/
    public  String dbPwd() throws IOException{
	return searchTag("database-pwd");
    }
    public String pureRandomGeneration() throws IOException{
	String randomValue = searchTag("random-gen");
	return randomValue;
    }
    /** Returns the obda file containing the mappings 
     * @throws IOException **/
    public  String mappingsFile() throws IOException{
	return searchTag("obda-file");
    }
    /** Returns the configuration scheme for the data generation **/
    public String mode() throws IOException{
	return searchTag("mode");
    }

    public String fixed() throws IOException{
	return searchTag("fixed");
    }

    public String nonFixed() throws IOException{
	return searchTag("non-fixed");
    }

    protected String searchTag(String tag) throws IOException{
	try(BufferedReader in = new BufferedReader(
		new FileReader(confFile))){
	    String s;
	    while ((s = in.readLine()) != null){
		List<String> s2 = Arrays.asList(s.split("\\s+"));
		if (s2.get(0).equals(tag)){
		    in.close();
		    StringBuilder resultBuilder = new StringBuilder();
		    for( int i = 1; i < s2.size(); ++i ){
			if( s2.get(i).startsWith("#") ) break; // Comment
			if( i > 1 ) resultBuilder.append(" ");
			resultBuilder.append( s2.get(i) );
		    }
		    return resultBuilder.toString();
		}
	    }
	    in.close();
	}catch(IOException e){
	    e.printStackTrace();
	}
	return "error";
    }

    public String ccAnalysisTimeout() throws IOException {
	return searchTag("ccAnalysisTimeout");
    }
    
    public String scale() throws IOException {
	return searchTag("scale");
    }
    
    public String resources() throws IOException {
	return searchTag("resources");
    }
    
    public String tables() throws IOException {
	return searchTag("tables");
    }
    
    public String columns() throws IOException {
	return searchTag("columns");
    }
}
