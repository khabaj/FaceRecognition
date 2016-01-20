package application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.sqlite.SQLiteConfig;

public class SQLiteConnector {

	private static Connection connection;

	private SQLiteConnector() {

	}
	
	public static Connection getConnection() {
		if (connection != null)
			return connection;
		else {
			try {
				Class.forName("org.sqlite.JDBC");
				SQLiteConfig config = new SQLiteConfig();  
		        config.enforceForeignKeys(true); 
				connection = DriverManager.getConnection("jdbc:sqlite:UserDB.sqlite", config.toProperties());
				return connection;
			} catch (Exception e) {
				System.out.println(e);
				return null;
			}
		}
	}

	public static boolean isConnected() {
		if (connection != null) {
			try {
				return !connection.isClosed();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public static boolean closeConnection(){
		try {
			if(connection != null) {
				connection.close();
				return connection.isClosed();
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
}
