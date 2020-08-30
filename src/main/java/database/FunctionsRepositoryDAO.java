package database;

import utility.PropertiesManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class FunctionsRepositoryDAO {

	private static final String CREATE_DATABASE = "CREATE DATABASE IF NOT EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB);

	private static final String CREATE_GOOGLE_TABLE = "CREATE TABLE IF NOT EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".google (" +
			"function_name varchar(50) NOT NULL, " +
			"url varchar(100) NOT NULL, " +
			"PRIMARY KEY (function_name)" +
			")";

	private static final String CREATE_AMAZON_TABLE = "CREATE TABLE IF NOT EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".amazon (" +
			"function_name varchar(50) NOT NULL, " +
			"url varchar(100) NOT NULL, " +
			"lambda_arn varchar(50) NOT NULL, " +
			"api_id varchar(50) NOT NULL, " +
			"api_parent_id varchar(50) NOT NULL, " +
			"api_resource_id varchar(50) NOT NULL, " +
			"PRIMARY KEY (function_name)" +
			")";


	private static void initDatabase(Connection connection) throws SQLException {
		if (connection != null) {
			Statement statement = connection.createStatement();
			statement.executeUpdate(CREATE_DATABASE);
			statement.executeUpdate(CREATE_AMAZON_TABLE);
			statement.executeUpdate(CREATE_GOOGLE_TABLE);
			statement.close();
		} else {
			System.err.println("Could not initialize database");
		}
	}

	public static void main(String[] args) {
		Connection connection = MySQLConnect.connectDatabase();
		try {
			initDatabase(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		MySQLConnect.closeConnection(connection);
	}
}
