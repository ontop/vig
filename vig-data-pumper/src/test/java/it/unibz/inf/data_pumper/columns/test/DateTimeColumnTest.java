package it.unibz.inf.data_pumper.columns.test;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import vig_test_unit.constants.DBConstants;

public class DateTimeColumnTest {

    @Test
    @Ignore
    public void test() {
	fail("Not yet implemented");
    }

}

class DateTimeTestConstants extends DBConstants{
    public DateTimeTestConstants(){
	this.connPars = new DateTimeTestConnectionParameters();
	this.bashs = new DateTimeTestBashs();
	this.sqls = new DateTimeTestSQLs();
	this.type = DBConstants.DBType.MYSQL;
    }
    
    class DateTimeTestConnectionParameters extends ConnectionParameters{
	public DateTimeTestConnectionParameters(){
	    this.CONNECTOR_STRING = "jdbc:mysql://";
	    this.DB_NAME = "datetimetest";
	    this.PWD = "fish";
	    this.URL = "10.7.20.65/";
	    this.USER = "fish";
	}
    };
    
    class DateTimeTestBashs extends Bashs{
	public DateTimeTestBashs(){
	    this.INIT_SCHEMA = "src/test/resources/bashs/initDatabaseSchema.sh";
	}
    };
    
    class DateTimeTestSQLs extends SQLs{
	public DateTimeTestSQLs(){
	    this.DROP_DB = "DROP DATABASE IF EXISTS";
	    this.CREATE_DB = "CREATE DATABASE";
	}
    };
};
