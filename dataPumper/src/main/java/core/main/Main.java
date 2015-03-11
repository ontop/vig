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
import it.unibz.inf.vig_options.core.DoubleOption;
import it.unibz.inf.vig_options.core.Option;
import it.unibz.inf.vig_options.core.StringOption;
import it.unibz.inf.vig_options.ranges.DoubleRange;

import org.apache.log4j.BasicConfigurator;

import configuration.Conf;
import connection.DBMSConnection;
import connection.exceptions.UnsupportedDatabaseException;

enum PumperType{
	DB, OBDA
}

public class Main {
	
	private static PumperType pumperType;
	
	// configuration file
	private static Conf conf;
	
	// Options
	private static DoubleOption optScaling = new DoubleOption("--scale", "It specifies the scaling factor", "PUMPER", 1, new DoubleRange(0, Double.MAX_VALUE, false, true));	
	public static StringOption optResources = new StringOption("--res", "Location of the resources directory", "CONFIGURATION", "src/main/resources");

	public static void main(String[] args) {
		
		// --- configuration -- //
		BasicConfigurator.configure();		
		Option.parseOptions(args);
		double percentage = optScaling.getValue();
		conf = Conf.getInstance();
		
		pumperType = PumperType.valueOf(conf.pumperType());
		try {
			DBMSConnection.initInstance(conf.jdbcConnector(), conf.dbUrl(), conf.dbUser(), conf.dbPwd());
		} catch (UnsupportedDatabaseException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		DatabasePumper pumper = null;
		
		switch(pumperType){
		case DB:
			pumper = new DatabasePumperDB();
			break;
		case OBDA:
			pumper = new DatabasePumperDB();
			break;
		}
		
		if( conf.pureRandomGeneration() ){
			pumper.setPureRandomGeneration();
		}	
		pumper.pumpDatabase(percentage);
	}
};
