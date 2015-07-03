package vig_test.main;

import it.unibz.inf.data_pumper.core.main.Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import exceptions.AssertionFailedException;

abstract class Constants{
    
    static final String CSVS_PATH = "src/main/resources/csvs";
    static final String DB_NAME = "npd_clean_to_pump";

    static abstract class Bashs{
	static final String INIT_SCHEMA = "src/main/resources/bashs/initDatabaseSchema.sh";
	static final String PUMP_DATA = "src/main/resources/bashs/pumpDataToNpd.sh";
	static final String CHECK_FKs = "src/main/resources/bashs/executeSql.sh src/main/resources/sqls/check_fkeys.sql npd_clean_to_pump tir gr3g4r10 mysql";
    }
    
    abstract class SQLs{
	static final String DROP_DB = "DROP DATABASE IF EXISTS";
	static final String CREATE_DB = "CREATE DATABASE";
    }
}

public class PumpFullDatabase {

    private static String parameterScale = "--scale=10";
    
    public static void main(String[] args){

	TestDatabaseCreator creator = new TestDatabaseCreator();
	creator.createTestDatabase();

	String[] parameters = new String[1];
	parameters[0] = parameterScale;
	
	Main.main(parameters);// That is, pump with size 1

	// Take the csvs, and load them into the database
	try {
	    SQLScriptsExecuter.loadCsvsToDB();
	    SQLScriptsExecuter.checkForeignKeys();
	} catch (IOException e) {
	    throw new RuntimeException(e);
	}
	
	// Now, I should check the things. E.g., is the r
    }
}

class SQLScriptsExecuter{

    static void initNPDSchema() throws IOException{
	Process p = Runtime.getRuntime().exec(Constants.Bashs.INIT_SCHEMA +" "+Constants.DB_NAME);
	printErrorAndOutputStream(p);
    }

    static void loadCsvsToDB() throws IOException{
	Process p = Runtime.getRuntime().exec(Constants.Bashs.PUMP_DATA+" "+Constants.DB_NAME);
	String log = printErrorAndOutputStream(p);
	if( log.contains("ERROR") ){
	    throw new AssertionFailedException("Error while loading the csv");
	}
    }
    
    static void checkForeignKeys() throws IOException{
	Process p = Runtime.getRuntime().exec(Constants.Bashs.CHECK_FKs);
	String result = printErrorAndOutputStream(p);
	if( result.contains("npd_clean_to_pump") ){
	    throw new AssertionFailedException("Violated Foreign Key");
	}
    }

    private static String printErrorAndOutputStream(Process p) throws IOException {
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

class TestDatabaseCreator{
    
    public void createTestDatabase(){
	SchemaCreator creator = new SchemaCreator();
	creator.createDB(new DatabaseCreator());
    }

    private class DatabaseCreator{

	private GenericConnectionGetter gCN;

	void createDatabase(){
	    gCN = GenericConnectionGetter.setInstance(DriverRegistererFactory.getMySqlDriverRegisterer());
	    try(Connection conn = gCN.getConnection() ){
		PreparedStatement stmt = conn.prepareStatement(Constants.SQLs.DROP_DB +" "+Constants.DB_NAME);
		stmt.executeUpdate();
		stmt = conn.prepareStatement(Constants.SQLs.CREATE_DB+" "+Constants.DB_NAME);
		stmt.executeUpdate();
	    } catch (SQLException e) {
		e.printStackTrace();
		throw new RuntimeException(e);
	    }
	}
    };

    private class SchemaCreator{
	void createDB(DatabaseCreator dbCreator){

	    dbCreator.createDatabase();
	    try {
		SQLScriptsExecuter.initNPDSchema();
	    } catch (IOException e) {
		throw new RuntimeException(e);
	    }
	}
    }
};

interface DriverRegisterer{
    public void registerDriver();
}

abstract class DriverRegistererFactory implements DriverRegisterer{
    static final MySQLDriverRegisterer getMySqlDriverRegisterer(){
	return MySQLDriverRegisterer.INSTANCE;
    }    

    private enum MySQLDriverRegisterer implements DriverRegisterer{

	INSTANCE;
	
	private static final String DRIVER_NAME = "com.mysql.jdbc.Driver";

	@Override
	public void registerDriver() {
	    try {
		Class.forName(DRIVER_NAME);
	    } catch (ClassNotFoundException e) {
		throw new RuntimeException(e);
	    }
	}
    }
}

interface ConnectionGetter{    
    public Connection getConnection() throws SQLException;
}

/**
 * 
 * @author Make a class tunable
 *
 */
abstract class SecretelyTunableConnectionGetter implements ConnectionGetter {

    @Override
    public Connection getConnection() throws SQLException{
	return getConnection(new Tuning(){

	    @Override
	    protected String getDBString() {
		return "";
	    }});
    }

    protected Connection getConnection(Tuning tuning) throws SQLException{
	Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/"+tuning.getDBString(), "tir", "gr3g4r10");
	return conn;
    }

    protected abstract class Tuning{
	protected abstract String getDBString();
    }
}

class GenericConnectionGetter extends SecretelyTunableConnectionGetter {

    private static GenericConnectionGetter instance = null;

    private GenericConnectionGetter(DriverRegisterer reg){
	reg.registerDriver();
    }

    public static GenericConnectionGetter setInstance(DriverRegisterer reg){
	instance = new GenericConnectionGetter(reg);
	return instance;
    }    

}

class DBConnectionGetter extends SecretelyTunableConnectionGetter {

    private static DBConnectionGetter instance = null;
    private GenericConnectionGetter wrapped;

    private final String DB_NAME;

    private DBConnectionGetter(GenericConnectionGetter toWrap, String dbName){
	DB_NAME = dbName;
	this.wrapped = toWrap;
    }

    public static DBConnectionGetter promoteGenericConnectionGetter(GenericConnectionGetter toWrap, String dbName) {
	instance = new DBConnectionGetter(toWrap, dbName);
	return instance;
    }

    @Override
    public Connection getConnection() throws SQLException {

	class DBStringTuning extends Tuning{

	    @Override
	    protected String getDBString() {
		return DB_NAME;
	    }

	}

	return wrapped.getConnection(new DBStringTuning());
    }
}