package core.main;

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

//import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import configuration.Conf;
import connection.DBMSConnection;
import core.DoubleOption;
import core.Option;
import core.StringOption;
import ranges.DoubleRange;

enum PumperType{
	DB, OBDA
}

public class Main {
	
	private static DBMSConnection dbToPump;
	private static DBMSConnection dbOriginal;
	
	private static PumperType pumperType;
	
	private static Logger logger = Logger.getLogger(Main.class.getCanonicalName());
	
	// configuration file
	private static Conf conf;
	
	// Options
	private static DoubleOption optIncrement = new DoubleOption("--inc", "It specifies the increment ratio", "PUMPER", 2, new DoubleRange(0, Double.MAX_VALUE, false, true));	
	private static StringOption optFromTable = new StringOption("--from-table", "It starts the pumping process from the specified table", "PUMPER", null);
	public static StringOption optResources = new StringOption("--res", "Location of the resources directory", "CONFIGURATION", "src/main/resources");

	public static void main(String[] args) {
		
		// --- configuration -- //
		BasicConfigurator.configure();		
		Option.parseOptions(args);
		double percentage = optIncrement.getValue();
		String fromTable = optFromTable.getValue();
		conf = Conf.getInstance();
		
		pumperType = PumperType.valueOf(conf.pumperType());
		dbOriginal = new DBMSConnection(conf.jdbcConnector(), conf.dbUrlOriginal(), conf.dbUsernameOriginal(), conf.dbPasswordOriginal());
		dbToPump = new DBMSConnection(conf.jdbcConnector(), conf.dbUrlToPump(), conf.dbUsernameToPump(), conf.dbPasswordToPump());
		
		DatabasePumper pumper = null;
		
		switch(pumperType){
		case DB:
			pumper = new DatabasePumperDB(dbOriginal, dbToPump);
			break;
		case OBDA:
			pumper = new DatabasePumperOBDA(dbOriginal, dbToPump);
			break;
		}
		
		if( conf.pureRandomGeneration() ){
			pumper.setPureRandomGeneration();
		}
		
		if( fromTable != null ){
			pumper.pumpDatabase(percentage, fromTable);
		}
		else{
			pumper.pumpDatabase(percentage);
		}
	}
};
