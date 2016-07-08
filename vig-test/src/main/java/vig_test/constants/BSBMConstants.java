package vig_test.constants;

public class BSBMConstants extends DBConstants {
    public BSBMConstants(){
	this.connPars = new BSBMConnectionParameters();
	this.bashs = new BSBMBashs();
	this.sqls = new BSBMSQLs();
	this.type = DBConstants.DBType.MYSQL;
    }
    
    class BSBMConnectionParameters extends ConnectionParameters{
	public BSBMConnectionParameters(){
	    this.CONNECTOR_STRING = "jdbc:mysql://";
	    this.DB_NAME = "npd_clean_to_pump";
	    this.PWD = "gregario";
	    this.URL = "localhost/";
	    this.USER = "tir";
	}
    };
    
    class BSBMBashs extends Bashs{
	public BSBMBashs(){
	    this.INIT_SCHEMA = "src/main/resources/bashs/initDatabaseSchema.sh";
	    this.PUMP_DATA = "src/main/resources/bashs/pumpDataToNpd.sh";
	    this.CHECK_FKs = "src/main/resources/bashs/executeSql.sh src/main/resources/sqls/check_fkeys.sql npd_clean_to_pump tir gregario mysql";
	}
    };
    
    class BSBMSQLs extends SQLs{
	public BSBMSQLs(){
	    this.DROP_DB = "DROP DATABASE IF EXISTS";
	    this.CREATE_DB = "CREATE DATABASE";
	}
    };
}
