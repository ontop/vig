package connection;

import java.sql.DriverManager;
import java.sql.SQLException;

public class DBMSConnection {
	
	private String jdbcConnector;
	private String databaseUrl;
	private String username;
	private String password;
	
	private static java.sql.Connection connection;
	
	public DBMSConnection(String jdbcConnector, String database, String username, String password){
		this.jdbcConnector = jdbcConnector;
		this.databaseUrl = database;
		this.username = username;
		this.password = password;
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println("MySQL JDBC driver loaded ok");

		String url = 
				jdbcConnector + "://" + databaseUrl 
				+ "?user=" + username 
				+ "&password=" + password;
		try {
			connection = DriverManager.getConnection(url, username, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
	public java.sql.Connection getConnection(){
		return connection;
	}	
	
	public String getDbName(){
		String result = databaseUrl.substring(databaseUrl.lastIndexOf("/")+1);
		return result;
	}
};
