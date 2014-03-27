package core;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import configuration.Conf;
import connection.DBMSConnection;

public class Main {
	
	private static DBMSConnection dbToPump;
	private static DBMSConnection dbOriginal;
	
	private static Logger logger = Logger.getLogger(Main.class.getCanonicalName());
	
	public static void main(String[] args) {
		
		if( args.length < 2 ){
			
			System.err.println("Usage: program -f percentage [--from tableName]");
			System.exit(1);
		}
		
		
		BasicConfigurator.configure();
		
		float percentage = Float.parseFloat(args[1]);
		
		dbOriginal = new DBMSConnection(Conf.jdbcConnector(), Conf.dbUrlOriginal(), Conf.dbUsernameOriginal(), Conf.dbPasswordOriginal());
		dbToPump = new DBMSConnection(Conf.jdbcConnector(), Conf.dbUrlToPump(), Conf.dbUsernameToPump(), Conf.dbPasswordToPump());
		
		DatabasePumper pumper = new DatabasePumper(dbOriginal, dbToPump);
		
		if( Conf.pureRandomGeneration() ){
			pumper.setPureRandomGeneration();
		}
		
		if( args.length == 4 && args[2].equals("--from") ){
			pumper.pumpDatabase(percentage, args[3]);
		}
		else{
			pumper.pumpDatabase(percentage);
		}
	}
};
