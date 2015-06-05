package it.unibz.inf.data_pumper.configuration;

import java.io.IOException;

import it.unibz.inf.data_pumper.core.main.Main;

/**
 * 
 * @author Davide Lanti
 * 
 * It reads the configuration for the unit tests
 *
 */
public class UnitConf extends Conf {
	
	private static UnitConf instance = null;
	
	private UnitConf(String resourcesDir){
		super(resourcesDir);
	
		String temp = confFile.substring(0, confFile.lastIndexOf("/") + 1);
		confFile = temp + "unitTests.conf";
	}
	
	public static UnitConf getInstance(){
		if( instance == null ){
			instance = new UnitConf(Main.optResources.getValue());
		}
		return instance;
	}
	
	/** Returns the configuration scheme for the data generation 
	 * @throws IOException **/
	public  String dbUrlSingleTests() throws IOException{
		return searchTag("DbUrlSingleTests");
	}
	/** Returns the configuration scheme for the data generation 
	 * @throws IOException **/
	public  String dbUsernameSingleTests() throws IOException{
		return searchTag("DbUrlUsernameSingleTests");
	}
	
	public  String dbPasswordSingleTests() throws IOException{
		return searchTag("DbPasswordSingleTests");
	}
}
