package databases.mysql;

import utility.PropertiesManager;

import java.sql.*;
import java.util.*;

@SuppressWarnings("DuplicatedCode")
public class FunctionsRepositoryDAO extends DAO{

	private static final String CREATE_GOOGLE_FUNCTIONS_TABLE = "CREATE TABLE IF NOT EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".google_serverless_functions (" +
			"function_name varchar(50) NOT NULL, " +
			"url varchar(100) NOT NULL, " +
			"region varchar(15) NOT NULL, " +
			"PRIMARY KEY (function_name)" +
			")";

	private static final String CREATE_AMAZON_FUNCTIONS_TABLE = "CREATE TABLE IF NOT EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".amazon_serverless_functions (" +
			"function_name varchar(50) NOT NULL, " +
			"url varchar(100) NOT NULL, " +
			"api_id varchar(50) NOT NULL, " +
			"region varchar(15) NOT NULL, " +
			"PRIMARY KEY (function_name)" +
			")";

	private static final String INSERT_GOOGLE_FUNCTION = "INSERT INTO " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".google_serverless_functions " +
			"(function_name, url, region) " + "VALUES (?, ?, ?) " +
			"ON DUPLICATE KEY UPDATE function_name=VALUES(function_name), url=VALUES(url), region=VALUES(region)";

	private static final String INSERT_AMAZON_FUNCTION = "INSERT INTO " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".amazon_serverless_functions " +
			"(function_name, url, api_id, region) " + "VALUES (?, ?, ?, ?) " +
			"ON DUPLICATE KEY UPDATE function_name=VALUES(function_name), url=VALUES(url), api_id=VALUES(api_id), " +
			"region=VALUES(region)";

	private static final String SELECT_GOOGLE_FUNCTIONS_INFO = "SELECT function_name, region FROM " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".google_serverless_functions";

	private static final String SELECT_AMAZON_FUNCTIONS_INFO = "SELECT function_name, api_id, region FROM " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".amazon_serverless_functions";

	private static final String SELECT_GOOGLE_FUNCTIONS_URL = "SELECT function_name, url FROM " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".google_serverless_functions";

	private static final String SELECT_AMAZON_FUNCTIONS_URL = "SELECT function_name, url FROM " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".amazon_serverless_functions";

	private static final String DROP_GOOGLE_FUNCTIONS = "DROP TABLE IF EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".google_serverless_functions";

	private static final String DROP_AMAZON_FUNCTIONS = "DROP TABLE IF EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".amazon_serverless_functions";


	private static void initTables(Connection connection, String provider) throws SQLException {
		if (connection != null) {

			initDatabase(connection);

			Statement statement = connection.createStatement();

			switch (provider) {
				case GOOGLE:
					statement.executeUpdate(CREATE_GOOGLE_FUNCTIONS_TABLE);
					break;
				case AMAZON:
					statement.executeUpdate(CREATE_AMAZON_FUNCTIONS_TABLE);
					break;
				default:
					statement.executeUpdate(CREATE_AMAZON_FUNCTIONS_TABLE);
					statement.executeUpdate(CREATE_GOOGLE_FUNCTIONS_TABLE);
					break;
			}
			statement.close();
		} else {
			System.err.println("Could not initialize database");
		}
	}

	public static void dropGoogle() {
		dropTable(GOOGLE);
	}

	public static void dropAmazon() {
		dropTable(AMAZON);
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
					statement.executeUpdate(DROP_GOOGLE_FUNCTIONS);
					break;
				case AMAZON:
					statement.executeUpdate(DROP_AMAZON_FUNCTIONS);
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

	public static void persistGoogle(String functionName, String url, String region) {
		try {
			Connection connection = MySQLConnect.connectDatabase();
			if (connection == null) {
				System.err.println("Could not connect to database, please check your connection");
				return;
			}
			initTables(connection, GOOGLE);

			PreparedStatement preparedStatement = connection.prepareStatement(INSERT_GOOGLE_FUNCTION);
			preparedStatement.setString(1, functionName);
			preparedStatement.setString(2, url);
			preparedStatement.setString(3, region);
			preparedStatement.execute();
			preparedStatement.close();
			MySQLConnect.closeConnection(connection);
		} catch (SQLException e) {
			System.err.println("Could not perform insertion: " + e.getMessage());
		}
	}

	public static void persistAmazon(String functionName, String url, String apiId, String region) {
		try {
			Connection connection = MySQLConnect.connectDatabase();
			if (connection == null) {
				System.err.println("Could not connect to database, please check your connection");
				return;
			}
			initTables(connection, AMAZON);

			PreparedStatement preparedStatement = connection.prepareStatement(INSERT_AMAZON_FUNCTION);
			preparedStatement.setString(1, functionName);
			preparedStatement.setString(2, url);
			preparedStatement.setString(3, apiId);
			preparedStatement.setString(4, region);
			preparedStatement.execute();
			preparedStatement.close();
			MySQLConnect.closeConnection(connection);
		} catch (SQLException e) {
			System.err.println("Could not perform insertion: " + e.getMessage());
		}
	}

	public static List<FunctionData> getGoogles() {
		try {
			Connection connection = MySQLConnect.connectDatabase();
			if (connection == null) {
				System.err.println("Could not connect to database, please check your connection");
				return null;
			}
			initTables(connection, GOOGLE);

			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(SELECT_GOOGLE_FUNCTIONS_INFO);

			List<FunctionData> result = new ArrayList<>();

			while (resultSet.next()) {
				result.add(new FunctionData(resultSet.getString("function_name"),
						resultSet.getString("region")));
			}

			statement.close();
			resultSet.close();
			return result;
		} catch (SQLException e) {
			System.err.println("Could not perform select: " + e.getMessage());
			return null;
		}
	}

	public static List<FunctionData> getAmazons() {
		try {
			Connection connection = MySQLConnect.connectDatabase();
			if (connection == null) {
				System.err.println("Could not connect to database, please check your connection");
				return null;
			}
			initTables(connection, AMAZON);

			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(SELECT_AMAZON_FUNCTIONS_INFO);

			List<FunctionData> result = new ArrayList<>();

			while (resultSet.next()) {
				result.add(new FunctionData(resultSet.getString("function_name"),
						resultSet.getString("region"), resultSet.getString("api_id")));
			}

			statement.close();
			resultSet.close();
			return result;
		} catch (SQLException e) {
			System.err.println("Could not perform select: " + e.getMessage());
			return null;
		}
	}

	public static ArrayList<FunctionURL> getUrls() {
		try {
			Connection connection = MySQLConnect.connectDatabase();
			if (connection == null) {
				System.err.println("Could not connect to database, please check your connection");
				return null;
			}
			initTables(connection, "*");

			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(SELECT_GOOGLE_FUNCTIONS_URL);

			HashMap<String, FunctionURL> dynamicResult = new HashMap<>();

			String name;
			String url;
			FunctionURL functionURL;

			while (resultSet.next()) {
				name = resultSet.getString("function_name");
				url = resultSet.getString("url");
				if (dynamicResult.containsKey(name)) {
					dynamicResult.get(name).setGoogleUrl(url);
				} else {
					functionURL = new FunctionURL(name);
					functionURL.setGoogleUrl(url);
					dynamicResult.put(name, functionURL);
				}
			}

			statement.close();
			resultSet.close();

			statement = connection.createStatement();
			resultSet = statement.executeQuery(SELECT_AMAZON_FUNCTIONS_URL);

			while (resultSet.next()) {
				name = resultSet.getString("function_name");
				url = resultSet.getString("url");
				if (dynamicResult.containsKey(name)) {
					dynamicResult.get(name).setAmazonUrl(url);
				} else {
					functionURL = new FunctionURL(name);
					functionURL.setAmazonUrl(url);
					dynamicResult.put(name, functionURL);
				}
			}

			statement.close();
			resultSet.close();

			return new ArrayList<>(dynamicResult.values());

		} catch (SQLException e) {
			System.err.println("Could not perform select: " + e.getMessage());
			return null;
		}
	}

}
