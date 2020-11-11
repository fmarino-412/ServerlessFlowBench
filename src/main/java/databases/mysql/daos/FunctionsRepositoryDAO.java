package databases.mysql.daos;

import databases.mysql.CloudEntityData;
import databases.mysql.FunctionalityURL;
import databases.mysql.MySQLConnect;
import utility.PropertiesManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Data Access Object for serverless function related information
 */
@SuppressWarnings({"DuplicatedCode", "SqlResolve", "RedundantSuppression"})
public class FunctionsRepositoryDAO extends DAO {

	/**
	 * Queries
	 */
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

	private static final String CREATE_OPENWHISK_FUNCTIONS_TABLE = "CREATE TABLE IF NOT EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".openwhisk_serverless_functions (" +
			"function_name varchar(50) NOT NULL, " +
			"url varchar(100) NOT NULL, " +
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

	private static final String INSERT_OPENWHISK_FUNCTION = "INSERT INTO " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".openwhisk_serverless_functions " +
			"(function_name, url) " + "VALUES (?, ?) " +
			"ON DUPLICATE KEY UPDATE function_name=VALUES(function_name), url=VALUES(url)";

	private static final String SELECT_GOOGLE_FUNCTIONS_INFO = "SELECT function_name, region FROM " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".google_serverless_functions";

	private static final String SELECT_AMAZON_FUNCTIONS_INFO = "SELECT function_name, api_id, region FROM " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".amazon_serverless_functions";

	private static final String SELECT_OPENWHISK_FUNCTIONS_INFO = "SELECT function_name FROM " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".openwhisk_serverless_functions";

	private static final String SELECT_GOOGLE_FUNCTIONS_URL = "SELECT function_name, url FROM " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".google_serverless_functions";

	private static final String SELECT_AMAZON_FUNCTIONS_URL = "SELECT function_name, url FROM " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".amazon_serverless_functions";

	private static final String SELECT_OPENWHISK_FUNCTIONS_URL = "SELECT function_name, url FROM " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".openwhisk_serverless_functions";

	private static final String DROP_GOOGLE_FUNCTIONS = "DROP TABLE IF EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".google_serverless_functions";

	private static final String DROP_AMAZON_FUNCTIONS = "DROP TABLE IF EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".amazon_serverless_functions";

	private static final String DROP_OPENWHISK_FUNCTIONS = "DROP TABLE IF EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".openwhisk_serverless_functions";


	/**
	 * Initializes tables
	 * @param connection database connection to use
	 * @param provider select which provider is needed to initialize corresponding tables
	 * @throws SQLException query definition and execution related problems
	 */
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
				case OPENWHISK:
					statement.executeUpdate(CREATE_OPENWHISK_FUNCTIONS_TABLE);
					break;
				default:
					statement.executeUpdate(CREATE_GOOGLE_FUNCTIONS_TABLE);
					statement.executeUpdate(CREATE_AMAZON_FUNCTIONS_TABLE);
					statement.executeUpdate(CREATE_OPENWHISK_FUNCTIONS_TABLE);
					break;
			}
			statement.close();
		} else {
			System.err.println("Could not initialize database");
		}
	}

	/**
	 * Drop every table associated to Google Cloud Platform Functions
	 */
	public static void dropGoogle() {
		dropTable(GOOGLE);
	}

	/**
	 * Drop every table associated to Amazon Web Services Functions
	 */
	public static void dropAmazon() {
		dropTable(AMAZON);
	}

	/**
	 * Drop every table associated to Open Whisk Functions
	 */
	public static void dropOpenWhisk() {
		dropTable(OPENWHISK);
	}

	/**
	 * Generic drop table function
	 * @param provider select which provider is needed to delete corresponding tables
	 */
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
				case OPENWHISK:
					statement.executeUpdate(DROP_OPENWHISK_FUNCTIONS);
					break;
				default:
					System.err.println("Provider not supported! Could not perform DB drop");
			}

			statement.close();
			MySQLConnect.closeConnection(connection);
		} catch (SQLException e) {
			System.err.println("Could not drop table(s): " + e.getMessage());
		}
	}

	/**
	 * Persists a new Google Cloud Functions function to database
	 * @param functionName name of the function
	 * @param url url for function execution
	 * @param region function deployment region
	 */
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

	/**
	 * Persists a new Amazon Lambda and Api Gateway function to database
	 * @param functionName name of the function
	 * @param url url for function execution
	 * @param apiId id of the api associated to the function
	 * @param region function deployment region
	 */
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

	/**
	 * Persists a new Open Whisk function to database
	 * @param functionName name of the function
	 * @param url url for function execution
	 */
	public static void persistOpenWhisk(String functionName, String url) {
		try {
			Connection connection = MySQLConnect.connectDatabase();
			if (connection == null) {
				System.err.println("Could not connect to database, please check your connection");
				return;
			}
			initTables(connection, OPENWHISK);

			PreparedStatement preparedStatement = connection.prepareStatement(INSERT_OPENWHISK_FUNCTION);
			preparedStatement.setString(1, functionName);
			preparedStatement.setString(2, url);
			preparedStatement.execute();
			preparedStatement.close();
			MySQLConnect.closeConnection(connection);
		} catch (SQLException e) {
			System.err.println("Could not perform insertion: " + e.getMessage());
		}
	}

	/**
	 * List every Google Cloud Functions function
	 * @return list of functions (CloudEntityData)
	 */
	public static List<CloudEntityData> getGoogles() {
		try {
			Connection connection = MySQLConnect.connectDatabase();
			if (connection == null) {
				System.err.println("Could not connect to database, please check your connection");
				return null;
			}
			initTables(connection, GOOGLE);

			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(SELECT_GOOGLE_FUNCTIONS_INFO);

			List<CloudEntityData> result = new ArrayList<>();

			while (resultSet.next()) {
				result.add(new CloudEntityData(resultSet.getString("function_name"),
						resultSet.getString("region")));
			}

			statement.close();
			resultSet.close();
			MySQLConnect.closeConnection(connection);
			return result;
		} catch (SQLException e) {
			System.err.println("Could not perform select: " + e.getMessage());
			return null;
		}
	}

	/**
	 * List every Amazon Lambda and API Gateway function
	 * @return list of functions (CloudEntityData)
	 */
	public static List<CloudEntityData> getAmazons() {
		try {
			Connection connection = MySQLConnect.connectDatabase();
			if (connection == null) {
				System.err.println("Could not connect to database, please check your connection");
				return null;
			}
			initTables(connection, AMAZON);

			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(SELECT_AMAZON_FUNCTIONS_INFO);

			List<CloudEntityData> result = new ArrayList<>();

			while (resultSet.next()) {
				result.add(new CloudEntityData(resultSet.getString("function_name"),
						resultSet.getString("region"), resultSet.getString("api_id")));
			}

			statement.close();
			resultSet.close();
			MySQLConnect.closeConnection(connection);
			return result;
		} catch (SQLException e) {
			System.err.println("Could not perform select: " + e.getMessage());
			return null;
		}
	}

	/**
	 * List every Open Whisk function
	 * @return list of functions (CloudEntityData)
	 */
	public static List<CloudEntityData> getOpenWhisks() {
		try {
			Connection connection = MySQLConnect.connectDatabase();
			if (connection == null) {
				System.err.println("Could not connect to database, please check your connection");
				return null;
			}
			initTables(connection, OPENWHISK);

			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(SELECT_OPENWHISK_FUNCTIONS_INFO);

			List<CloudEntityData> result = new ArrayList<>();

			while (resultSet.next()) {
				result.add(new CloudEntityData(resultSet.getString("function_name")));
			}

			statement.close();
			resultSet.close();
			MySQLConnect.closeConnection(connection);
			return result;
		} catch (SQLException e) {
			System.err.println("Could not perform select: " + e.getMessage());
			return null;
		}
	}

	/**
	 * List every function url, there can be one or more URL per function basing on different provider implementation
	 * of the same function
	 * @return list of function urls (FunctionalityURL)
	 */
	public static List<FunctionalityURL> getUrls() {
		try {
			Connection connection = MySQLConnect.connectDatabase();
			if (connection == null) {
				System.err.println("Could not connect to database, please check your connection");
				return null;
			}
			initTables(connection, "*");

			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(SELECT_GOOGLE_FUNCTIONS_URL);

			HashMap<String, FunctionalityURL> dynamicResult = new HashMap<>();

			String name;
			String url;
			FunctionalityURL functionalityURL;

			while (resultSet.next()) {
				name = resultSet.getString("function_name");
				url = resultSet.getString("url");
				if (dynamicResult.containsKey(name)) {
					dynamicResult.get(name).setGoogleUrl(url);
				} else {
					functionalityURL = new FunctionalityURL(name);
					functionalityURL.setGoogleUrl(url);
					dynamicResult.put(name, functionalityURL);
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
					functionalityURL = new FunctionalityURL(name);
					functionalityURL.setAmazonUrl(url);
					dynamicResult.put(name, functionalityURL);
				}
			}

			statement.close();
			resultSet.close();

			statement = connection.createStatement();
			resultSet = statement.executeQuery(SELECT_OPENWHISK_FUNCTIONS_URL);

			while (resultSet.next()) {
				name = resultSet.getString("function_name");
				url = resultSet.getString("url");
				if (dynamicResult.containsKey(name)) {
					dynamicResult.get(name).setOpenWhiskUrl(url);
				} else {
					functionalityURL = new FunctionalityURL(name);
					functionalityURL.setOpenWhiskUrl(url);
					dynamicResult.put(name, functionalityURL);
				}
			}

			statement.close();
			resultSet.close();

			MySQLConnect.closeConnection(connection);

			return new ArrayList<>(dynamicResult.values());

		} catch (SQLException e) {
			System.err.println("Could not perform select: " + e.getMessage());
			return null;
		}
	}

}
