package vig_test_unit.db_creation;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import vig_test_unit.constants.DBConstants;
import vig_test_unit_scripts.SQLScriptsExecuter;

public class TestDatabaseCreator{

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