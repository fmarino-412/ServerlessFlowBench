package database;

import utility.PropertiesManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
			"lambda_arn varchar(100) NOT NULL, " +
			"api_id varchar(50) NOT NULL, " +
			"api_parent_id varchar(50) NOT NULL, " +
			"api_resource_id varchar(50) NOT NULL, " +
			"PRIMARY KEY (function_name)" +
			")";

	private static final String INSERT_GOOGLE = "INSERT INTO " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".google (function_name, url) " +
			"VALUES (?, ?) " +
			"ON DUPLICATE KEY UPDATE function_name=VALUES(function_name), url=VALUES(url)";

	private static final String INSERT_AMAZON = "INSERT INTO " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".amazon " +
			"(function_name, url, lambda_arn, api_id, api_parent_id, api_resource_id) " +
			"VALUES (?, ?, ?, ?, ?, ?) " +
			"ON DUPLICATE KEY UPDATE function_name=VALUES(function_name), url=VALUES(url), " +
			"lambda_arn=VALUES(lambda_arn), api_id=VALUES(api_id), api_parent_id=VALUES(api_parent_id), " +
			"api_resource_id=VALUES(api_resource_id)";


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

	public static void persistGoogle(String functionName, String url) {
		try {
			Connection connection = MySQLConnect.connectDatabase();
			if (connection == null) {
				System.err.println("Could not connect to database, please check your connection");
				return;
			}
			initDatabase(connection);
			PreparedStatement preparedStatement = connection.prepareStatement(INSERT_GOOGLE);
			preparedStatement.setString(1, functionName);
			preparedStatement.setString(2, url);
			preparedStatement.execute();
			preparedStatement.close();
			MySQLConnect.closeConnection(connection);
		} catch (SQLException e) {
			System.err.println("Could not perform insertion: " + e.getMessage());
		}
	}

	public static void persistAmazon(String functionName, String url, String lambdaARN, String apiId,
									 String apiParentId, String apiResourceId) {
		try {
			Connection connection = MySQLConnect.connectDatabase();
			if (connection == null) {
				System.err.println("Could not connect to database, please check your connection");
				return;
			}
			initDatabase(connection);
			PreparedStatement preparedStatement = connection.prepareStatement(INSERT_AMAZON);
			preparedStatement.setString(1, functionName);
			preparedStatement.setString(2, url);
			preparedStatement.setString(3, lambdaARN);
			preparedStatement.setString(4, apiId);
			preparedStatement.setString(5, apiParentId);
			preparedStatement.setString(6, apiResourceId);
			preparedStatement.execute();
			preparedStatement.close();
			MySQLConnect.closeConnection(connection);
		} catch (SQLException e) {
			System.err.println("Could not perform insertion: " + e.getMessage());
		}
	}


}
