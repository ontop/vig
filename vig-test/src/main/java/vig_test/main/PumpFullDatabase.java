package vig_test.main;

import java.io.IOException;

import it.unibz.inf.data_pumper.core.main.VigMain;
import vig_test.constants.NPDConstants;
import vig_test_unit.constants.DBConstants;
import vig_test_unit.db_creation.TestDatabaseCreator;
import vig_test_unit_scripts.SQLScriptsExecuter;

public class PumpFullDatabase {

  private static final String PAR_SCALE = "--scale=1";
  private static final String PAR_NPD_OBDA_CONF = "--res=src/main/resources/npd-obda";
  private static final String PAR_NPD_DB_CONF = "--res=src/main/resources/npd-db";
  private static final String PAR_NPD_RAND_CONF = "--res=src/main/resources/npd-rand";

  public static void main( String[] args ){

    npdTests();
    bsbmTests();
  }

  private static void bsbmTests() {
    // TODO
  }

  private static void npdTests() {

//    npdTestsOfType(PAR_SCALE, PAR_NPD_DB_CONF);
	npdTestsOfType(PAR_SCALE, PAR_NPD_OBDA_CONF);
//  npdTestsOfType(PAR_SCALE, PAR_NPD_RAND_CONF);
  }

  private static void npdTestsOfType(String parScale, String parNpdDbConf) {
    DBConstants npdConsts = new NPDConstants();

    TestDatabaseCreator creator = new TestDatabaseCreator( npdConsts );
    creator.createTestDatabase();

    String[] parameters = new String[2];
    parameters[0] = parScale;
    parameters[1] = parNpdDbConf;
	
    VigMain.main(parameters);// That is, pump with size 1

    // Take the csvs, and load them into the database
    try {
      switch( parNpdDbConf ){
        case PAR_NPD_DB_CONF :
          Process p = Runtime.getRuntime().exec("ls");
          SQLScriptsExecuter.printErrorAndOutputStream(p);
          p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "cp src/main/resources/npd-db/csvs/* src/main/resources/csvs/"});
          SQLScriptsExecuter.printErrorAndOutputStream(p);
          break;
        case PAR_NPD_OBDA_CONF :
          p = Runtime.getRuntime().exec( "cp src/main/resources/npd-obda/csvs/* src/main/resources/csvs/" );
          SQLScriptsExecuter.printErrorAndOutputStream(p);
          break;
        case PAR_NPD_RAND_CONF :
          p = Runtime.getRuntime().exec( "cp src/main/resources/npd-rand/csvs/* src/main/resources/csvs/" );
          SQLScriptsExecuter.printErrorAndOutputStream(p);
          break;
        default :

      }
      SQLScriptsExecuter.loadCsvsToDB( npdConsts );
      System.out.println("Check Fkeys for " + parameters[1]);
      SQLScriptsExecuter.checkForeignKeys( npdConsts );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // TODO Now, I should check the things.
    // E.g., the cardinalities in the Columns Clusters!!!
    // To do so, first I need to add such cardinalities to the XML model
  }
}