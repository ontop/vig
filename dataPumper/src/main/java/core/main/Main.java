package core.main;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import configuration.Conf;
import connection.DBMSConnection;

enum PumperType{
	DB, OBDA
}

public class Main {
	
	private static DBMSConnection dbToPump;
	private static DBMSConnection dbOriginal;
	
	private static PumperType pumperType;
	
	private static Logger logger = Logger.getLogger(Main.class.getCanonicalName());
	
	public static void main(String[] args) {
		
		if( args.length < 2 ){
			
			System.err.println("Usage: program -f percentage [--from tableName]");
			System.exit(1);
		}
		
		
		BasicConfigurator.configure();
		
		float percentage = Float.parseFloat(args[1]);
		
		pumperType = PumperType.valueOf(Conf.pumperType());
		
		dbOriginal = new DBMSConnection(Conf.jdbcConnector(), Conf.dbUrlOriginal(), Conf.dbUsernameOriginal(), Conf.dbPasswordOriginal());
		dbToPump = new DBMSConnection(Conf.jdbcConnector(), Conf.dbUrlToPump(), Conf.dbUsernameToPump(), Conf.dbPasswordToPump());
		
		DatabasePumper pumper = null;
		
		switch(pumperType){
		case DB:
			pumper = new DatabasePumperDB(dbOriginal, dbToPump);
			break;
		case OBDA:
			pumper = new DatabasePumperOBDA(dbOriginal, dbToPump);
			break;
		}
		
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
