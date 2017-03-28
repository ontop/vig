package vig_test_unit.constants;

public abstract class DBConstants{
    protected Bashs bashs;
    protected SQLs sqls;
    protected ConnectionParameters connPars;
    protected DBType type;
     
    public DBType getType(){
	return this.type;
    }
    
    public Bashs getBashs(){
	return this.bashs;
    }
    
    public SQLs getSQLs(){
	return this.sqls;
    }
    
    public ConnectionParameters getConnParameters(){
	return this.connPars;
    }
    
    public enum DBType{
	MYSQL, POSTGRES
    };
    
    public abstract class ConnectionParameters{
	protected String USER;
	protected String DB_NAME;
	protected String PWD;
	protected String CONNECTOR_STRING;
	protected String URL;
	
	public String getUser() {
	    return USER;
	}
	public String getDbName() {
	    return DB_NAME;
	}
	public String getPwd() {
	    return PWD;
	}
	public String getConnectorString() {
	    return CONNECTOR_STRING;
	}
	public String getURL() {
	    return URL;
	}
    }
    
    public abstract class Bashs{
	protected String INIT_SCHEMA;
	protected String PUMP_DATA;
	protected String CHECK_FKs;
	
	public String initSchema(){
	    return this.INIT_SCHEMA;
	}
	public String pumpData(){
	    return this.PUMP_DATA;
	}
	public String checkFks(){
	    return this.CHECK_FKs;
	}
    };
    
    public abstract class SQLs{
	protected String DROP_DB;
	protected String CREATE_DB;
	
	public String dropDB(){
	    return this.DROP_DB;
	}
	
	public String createDB(){
	    return this.CREATE_DB;
	}
    }
};