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

import vig_test.constants.DBConstants;
import vig_test.constants.NPDConstants;
import exceptions.AssertionFailedException;

public class PumpFullDatabase {

    private static final String PAR_SCALE = "--scale=10";
    private static final String PAR_NPD_CONF = "--conf=configuration.conf";
    
    public static void main( String[] args ){
	
	DBConstants npdConsts = new NPDConstants();
	
	TestDatabaseCreator creator = new TestDatabaseCreator( npdConsts );
	creator.createTestDatabase();

	String[] parameters = new String[2];
	parameters[0] = PAR_SCALE;
	parameters[1] = PAR_NPD_CONF;
	
	Main.main(parameters);// That is, pump with size 1

	// Take the csvs, and load them into the database
	try {
	    SQLScriptsExecuter.loadCsvsToDB( npdConsts );
	    SQLScriptsExecuter.checkForeignKeys( npdConsts );
	} catch (IOException e) {
	    throw new RuntimeException(e);
	}
	
	// TODO Now, I should check the things. E
	// E.g., the cardinalities in the Columns Clusters!!! 
	// (Eh eh eh! Ma per fare questo, prima devo aggiungere queste cardinalita' al modello xml,
	// altrimenti stocazzo...)
    }
}

abstract class SQLScriptsExecuter{

    static void initSchema( DBConstants consts ) throws IOException{
	Process p = Runtime.getRuntime().exec( consts.getBashs().initSchema() +" "+ consts.getConnParameters().getDbName() );
	printErrorAndOutputStream(p);
    }

    static void loadCsvsToDB(DBConstants consts) throws IOException{
	Process p = Runtime.getRuntime().exec( consts.getBashs().pumpData() +" "+ consts.getConnParameters().getDbName() );
	String log = printErrorAndOutputStream(p);
	if( log.contains("ERROR") ){
	    throw new AssertionFailedException("Error while loading the csv");
	}
    }
    
    static void checkForeignKeys(DBConstants consts) throws IOException{
	Process p = Runtime.getRuntime().exec( consts.getBashs().checkFks() );
	String result = printErrorAndOutputStream(p);
	if( result.contains( consts.getConnParameters().getDbName() ) ){
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
    
    private DBConstants consts;
    
    public TestDatabaseCreator(DBConstants consts){
	this.consts = consts;
    }
    
    public void createTestDatabase(){
	SchemaCreator creator = new SchemaCreator();
	creator.createDB(new DatabaseCreator());
    }

    private class DatabaseCreator{

	private GenericConnectionGetter gCN;

	void dropAndcreateDatabase(){
	    switch(consts.getType()){
	    case MYSQL:
		gCN = GenericConnectionGetter.getInstance( DriverRegistererFactory.getMySqlDriverRegisterer(), consts );
		break;
	    case POSTGRES:
		break;
	    default:
		break;
	    
	    }
	    try(Connection conn = gCN.getConnection() ){
		PreparedStatement stmt = conn.prepareStatement(consts.getSQLs().dropDB() +" "+consts.getConnParameters().getDbName());
		stmt.executeUpdate();
		stmt = conn.prepareStatement( consts.getSQLs().createDB()+" "+consts.getConnParameters().getDbName() );
		stmt.executeUpdate();
	    } catch (SQLException e) {
		e.printStackTrace();
		throw new RuntimeException(e);
	    }
	}
    };

    private class SchemaCreator{
	void createDB(DatabaseCreator dbCreator){

	    dbCreator.dropAndcreateDatabase();
	    try {
		SQLScriptsExecuter.initSchema(consts);
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

class GenericConnectionGetter implements ConnectionGetter{

    private DBConstants consts;
    
    @Override
    public Connection getConnection() throws SQLException{

	String url = this.consts.getConnParameters().getConnectorString() + 
		this.consts.getConnParameters().getURL() +this.consts.getConnParameters().getDbName();
	String user = this.consts.getConnParameters().getUser();
	String pwd = this.consts.getConnParameters().getPwd(); 
	
	Connection conn = DriverManager.getConnection( url, user, pwd );
	return conn;
    }
    
    private GenericConnectionGetter(DriverRegisterer reg, DBConstants consts){
	reg.registerDriver();
	this.consts = consts;
    }

    public static GenericConnectionGetter getInstance(DriverRegisterer reg, DBConstants consts){
	GenericConnectionGetter instance = new GenericConnectionGetter(reg, consts);
	return instance;
    }    
};