package database;

import utility.PropertiesManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnect {

	private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
	private static final String DB_URL = "jdbc:mysql://" +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_IP) + ":" +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_PORT) + "?" +
			"useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&" +
			"user=" + PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_USR) + "&" +
			"password=" + PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_PASS);

	public static Connection connectDatabase() {
		try {
			Class.forName(DB_DRIVER);
			return DriverManager.getConnection(DB_URL);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void closeConnection(Connection connection) {
		try {
			connection.close();
		} catch (SQLException throwables) {
			throwables.printStackTrace();
			System.err.println("Could not close DB connection");
		}
	}



}
