package database;

import utility.PropertiesManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FunctionsRepositoryDAO {

	private static final String GOOGLE = "google cloud functions";
	private static final String AMAZON = "amazon web services lambda";

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
			"api_id varchar(5) NOT NULL, " +
			"PRIMARY KEY (function_name)" +
			")";

	private static final String INSERT_GOOGLE = "INSERT INTO " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".google (function_name, url) " +
			"VALUES (?, ?) " +
			"ON DUPLICATE KEY UPDATE function_name=VALUES(function_name), url=VALUES(url)";

	private static final String INSERT_AMAZON = "INSERT INTO " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".amazon (function_name, url, " +
			"api_id) " + "VALUES (?, ?, ?) " +
			"ON DUPLICATE KEY UPDATE function_name=VALUES(function_name), url=VALUES(url), api_id=VALUES(api_id)";

	private static final String SELECT_GOOGLE = "SELECT function_name FROM " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".google";

	private static final String SELECT_AMAZON = "SELECT function_name, api_id FROM " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".amazon";

	private static final String DROP_GOOGLE = "DROP TABLE IF EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".google";

	private static final String DROP_AMAZON = "DROP TABLE IF EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".amazon";


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

	public static void dropAmazon() {
		dropTable(AMAZON);
	}

	public static void dropGoogle() {
		dropTable(GOOGLE);
	}

	private static void dropTable(String provider) {
		try {
			Connection connection = MySQLConnect.connectDatabase();
			if (connection == null) {
				System.err.println("Could not connect to database, please check your connection");
				return;
			}

			Statement statement = connection.createStatement();

			switch (provider) {
				case GOOGLE:
					statement.executeUpdate(DROP_GOOGLE);
					break;
				case AMAZON:
					statement.executeUpdate(DROP_AMAZON);
					break;
				default:
					System.err.println("Provider not supported! Could not perform DB insertion");
			}

			statement.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void persistAmazon(String functionName, String url, String apiId) {
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
			preparedStatement.setString(3, apiId);
			preparedStatement.execute();
			preparedStatement.close();
			MySQLConnect.closeConnection(connection);
		} catch (SQLException e) {
			System.err.println("Could not perform insertion: " + e.getMessage());
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

	public static List<String> getGoogles() {
		try {
			Connection connection = MySQLConnect.connectDatabase();
			if (connection == null) {
				System.err.println("Could not connect to database, please check your connection");
				return null;
			}
			initDatabase(connection);

			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(SELECT_GOOGLE);

			List<String> result = new ArrayList<>();

			while (resultSet.next()) {
				result.add(resultSet.getString("function_name"));
			}

			statement.close();
			resultSet.close();
			return result;
		} catch (SQLException e) {
			System.err.println("Could not perform select: " + e.getMessage());
			return null;
		}
	}

	public static List<Tuple> getAmazons() {
		try {
			Connection connection = MySQLConnect.connectDatabase();
			if (connection == null) {
				System.err.println("Could not connect to database, please check your connection");
				return null;
			}
			initDatabase(connection);

			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(SELECT_AMAZON);

			List<Tuple> result = new ArrayList<>();

			while (resultSet.next()) {
				result.add(new Tuple(resultSet.getString("function_name"),
						resultSet.getString("api_id")));
			}

			statement.close();
			resultSet.close();
			return result;
		} catch (SQLException e) {
			System.err.println("Could not perform select: " + e.getMessage());
			return null;
		}
	}

}
