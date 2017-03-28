package vig_test_unit_scripts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import vig_test_unit.constants.DBConstants;
import vig_test_unit.exceptions.AssertionFailedException;

public abstract class SQLScriptsExecuter{

    public static void initSchema( DBConstants consts ) throws IOException{
	Process p = Runtime.getRuntime().exec( consts.getBashs().initSchema() +" "+ consts.getConnParameters().getDbName() );
	printErrorAndOutputStream(p);
    }

    public static void loadCsvsToDB(DBConstants consts) throws IOException{
	
	Process p = Runtime.getRuntime().exec( consts.getBashs().pumpData() +" "+ consts.getConnParameters().getDbName() );
	String log = printErrorAndOutputStream(p);
	if( log.contains("ERROR") ){
	    throw new AssertionFailedException("Error while loading the csv");
	}
    }
    
    public static void checkForeignKeys(DBConstants consts) throws IOException{
	Process p = Runtime.getRuntime().exec( consts.getBashs().checkFks() );
	String result = printErrorAndOutputStream(p);
	if( result.contains( consts.getConnParameters().getDbName() ) ){
	    throw new AssertionFailedException("Violated Foreign Key");
	}
    }

    public static String printErrorAndOutputStream(Process p) throws IOException {
	class LocalUtils {
	    BufferedReader makeReader(boolean isError, Process p){
		InputStream str = isError ? p.getErrorStream() : p.getInputStream();
		return new BufferedReader(new InputStreamReader(str));
	    }
	    public String readFromReader(BufferedReader input) throws IOException {
		String line = null;
		StringBuilder builder = new StringBuilder();
		while ((line = input.readLine()) != null) {
		    System.out.println(line);
		    builder.append(line+"\n");
		}
		input.close();	
		return builder.toString();
	    }
	}
		
	LocalUtils utils = new LocalUtils();
	
	BufferedReader out = utils.makeReader(false, p);
	String outString = utils.readFromReader(out);
	BufferedReader err = utils.makeReader(true, p);
	String errString = utils.readFromReader(err);
	
	return outString + "\n BEGIN_STDERR \n"+ errString;
    }
}