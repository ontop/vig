package vig_test.constants;

public class NPDConstants extends DBConstants{
    public NPDConstants(){
	this.connPars = new NPDConnectionParameters();
	this.bashs = new NPDBashs();
	this.sqls = new NPDSQLs();
	this.type = DBConstants.DBType.MYSQL;
    }
    
    class NPDConnectionParameters extends ConnectionParameters{
	public NPDConnectionParameters(){
	    this.CONNECTOR_STRING = "jdbc:mysql://";
	    this.DB_NAME = "npd_clean_to_pump";
	    this.PWD = "gregario";
	    this.URL = "localhost/";
	    this.USER = "tir";
	}
    };
    
    class NPDBashs extends Bashs{
	public NPDBashs(){
	    this.INIT_SCHEMA = "src/main/resources/bashs/initDatabaseSchema.sh";
	    this.PUMP_DATA = "src/main/resources/bashs/pumpDataToNpd.sh";
	    this.CHECK_FKs = "src/main/resources/bashs/executeSql.sh src/main/resources/sqls/check_fkeys.sql npd_clean_to_pump tir gregario mysql";
	}
    };
    
    class NPDSQLs extends SQLs{
	public NPDSQLs(){
	    this.DROP_DB = "DROP DATABASE IF EXISTS";
	    this.CREATE_DB = "CREATE DATABASE";
	}
    };
};