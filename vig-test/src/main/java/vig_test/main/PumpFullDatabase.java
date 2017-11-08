package vig_test.main;

import java.io.IOException;

import it.unibz.inf.data_pumper.core.main.Main;
import vig_test.constants.NPDConstants;
import vig_test_unit.constants.DBConstants;
import vig_test_unit.db_creation.TestDatabaseCreator;
import vig_test_unit_scripts.SQLScriptsExecuter;

public class PumpFullDatabase {

    private static final String PAR_SCALE = "--scale=1";
    private static final String PAR_NPD_OBDA_CONF = "--conf=configuration-npd-obda.conf";
    private static final String PAR_NPD_DB_CONF = "--conf=configuration-npd-db.conf";
    private static final String PAR_NPD_RAND_CONF = "--conf=configuration-npd-rand.conf";
    
    public static void main( String[] args ){
	
	npdTests();	
	bsbmTests();
    }

    private static void bsbmTests() {
	// TODO 
    }

    private static void npdTests() {	
	
//	npdTestsOfType(PAR_SCALE, PAR_NPD_DB_CONF);
//	npdTestsOfType(PAR_SCALE, PAR_NPD_OBDA_CONF);
	npdTestsOfType(PAR_SCALE, PAR_NPD_RAND_CONF);
    }

    private static void npdTestsOfType(String parScale, String parNpdDbConf) {
	DBConstants npdConsts = new NPDConstants();
	
	TestDatabaseCreator creator = new TestDatabaseCreator( npdConsts );
	creator.createTestDatabase();
	
	String[] parameters = new String[2];
	parameters[0] = parScale;
	parameters[1] = parNpdDbConf;
	
	Main.main(parameters);// That is, pump with size 1

	// Take the csvs, and load them into the database
	try {
	    SQLScriptsExecuter.loadCsvsToDB( npdConsts );
	    System.out.println("Check Fkeys for " + parameters[1]);
	    SQLScriptsExecuter.checkForeignKeys( npdConsts );
	} catch (IOException e) {
	    throw new RuntimeException(e);
	}
	
	// TODO Now, I should check the things. E
	// E.g., the cardinalities in the Columns Clusters!!! 
	// (Eh eh eh! Ma per fare questo, prima devo aggiungere queste cardinalita' al modello xml,
	// ..)
    }
}