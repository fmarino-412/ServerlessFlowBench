package databases.mysql;

import utility.PropertiesManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * A MySQL database client offering functionalities to connect and disconnect database
 */
public class MySQLConnect {

	/**
	 * Database connection properties
	 */
	private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
	private static final String DB_URL = "jdbc:mysql://" +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_IP) + ":" +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_PORT) + "?" +
			"useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&" +
			"user=" + PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_USR) + "&" +
			"password=" + PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_PASS);

	/**
	 * Connects to MySQL database
	 * @return instance of db connection
	 */
	public static Connection connectDatabase() {
		try {
			Class.forName(DB_DRIVER);
			return DriverManager.getConnection(DB_URL);
		} catch (ClassNotFoundException | SQLException e) {
			System.err.println("Could not open DB connection: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Closes a MySQL database connection
	 * @param connection MySQL connection to close
	 */
	public static void closeConnection(Connection connection) {
		try {
			connection.close();
		} catch (SQLException e) {
			System.err.println("Could not close DB connection: " + e.getMessage());
		}
	}



}
