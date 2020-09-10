package databases.mysql.daos;

import com.sun.istack.internal.Nullable;
import databases.mysql.FunctionalityURL;
import databases.mysql.MySQLConnect;
import utility.PropertiesManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("DuplicatedCode")
public class CompositionRepositoryDAO extends DAO {

	private static final String CREATE_AMAZON_COMPOSITION_LAMBDA_HANDLER = "CREATE TABLE IF NOT EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".amazon_serverless_handler_function (" +
			"id enum('1') NOT NULL, " +
			"function_name varchar(50) NOT NULL, " +
			"url varchar(100) NOT NULL, " +
			"api_id varchar(50) NOT NULL, " +
			"region varchar(15) NOT NULL, " +
			"PRIMARY KEY (id)" +
			")";

	private static final String CREATE_AMAZON_COMPOSITION_TABLE_MAIN = "CREATE TABLE IF NOT EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".amazon_serverless_compositions_main (" +
			"machine_name varchar(50) NOT NULL, " +
			"machine_arn varchar(100) NOT NULL, " +
			"PRIMARY KEY (machine_name)" +
			")";

	private static final String CREATE_AMAZON_COMPOSITION_TABLE_FUNCTIONS = "CREATE TABLE IF NOT EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".amazon_serverless_compositions_functions (" +
			"function_name varchar(50) NOT NULL, " +
			"function_region varchar(15) NOT NULL, " +
			"state_machine_arn varchar(100) NOT NULL, " +
			"PRIMARY KEY (function_name) " +
			"FOREIGN KEY (state_machine_arn) REFERENCES " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".amazon_serverless_compositions_main(machine_arn)" +
			")";

	private static final String INSERT_AMAZON_HANDLER = "INSERT INTO " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".amazon_serverless_handler_function " +
			"(function_name, url, api_id, region) " + "VALUES (?, ?, ?, ?) " +
			"ON DUPLICATE KEY UPDATE function_name=VALUES(function_name), url=VALUES(url), " +
			"api_id=VALUES(api_id), region=VALUES(region)";

	private static final String INSERT_AMAZON_COMPOSITION_MAIN = "INSERT INTO " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".amazon_serverless_compositions_main " +
			"(machine_name, machine_arn) " + "VALUES (?, ?) " +
			"ON DUPLICATE KEY UPDATE machine_name=VALUES(machine_name), machine_arn=VALUES(machine_arn)";

	private static final String INSERT_AMAZON_COMPOSITION_FUNCTION = "INSERT INTO " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".amazon_serverless_compositions_functions " +
			"(function_name, function_region, state_machine_arn) " + "VALUES (?, ?, ?) " +
			"ON DUPLICATE KEY UPDATE function_name=VALUES(function_name), function_region=VALUES(function_region), " +
			"state_machine_arn=VALUES(state_machine_arn)";

	private static final String SELECT_HANDLER_INFO = "SELECT function_name, api_id, region " +
			"FROM " + PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".amazon_serverless_handler_function WHERE id=1";

	private static final String SELECT_HANDLER_URL = "SELECT url " +
			"FROM " + PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".amazon_serverless_handler_function WHERE id=1";

	private static final String SELECT_MACHINES_ARNS = "SELECT machine_name, machine_arn " +
			"FROM " + PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".amazon_serverless_compositions_main";

	private static final String DROP_LAMBDA_HANDLER = "DROP TABLE IF EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".amazon_serverless_handler_function";

	private static final String DROP_AMAZON_COMPOSITION_FUNCTIONS = "DROP TABLE IF EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".amazon_serverless_compositions_functions";

	private static final String DROP_AMAZON_COMPOSITION_MAIN = "DROP TABLE IF EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".amazon_serverless_compositions_main";

	// TODO: select for urls and deletions

	private static void initTables(Connection connection, String provider) throws SQLException {
		if (connection != null) {

			initDatabase(connection);

			Statement statement = connection.createStatement();

			switch (provider) {
				case GOOGLE:
					//statement.executeUpdate("");
					break;
				case AMAZON:
					statement.executeUpdate(CREATE_AMAZON_COMPOSITION_LAMBDA_HANDLER);
					statement.executeUpdate(CREATE_AMAZON_COMPOSITION_TABLE_MAIN);
					statement.executeUpdate(CREATE_AMAZON_COMPOSITION_TABLE_FUNCTIONS);
					break;
				default:
					//statement.executeUpdate("");
					statement.executeUpdate(CREATE_AMAZON_COMPOSITION_LAMBDA_HANDLER);
					statement.executeUpdate(CREATE_AMAZON_COMPOSITION_TABLE_MAIN);
					statement.executeUpdate(CREATE_AMAZON_COMPOSITION_TABLE_FUNCTIONS);
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
					//statement.executeUpdate(DROP_GOOGLE_FUNCTIONS);
					break;
				case AMAZON:
					statement.executeUpdate(DROP_AMAZON_COMPOSITION_FUNCTIONS);
					statement.executeUpdate(DROP_AMAZON_COMPOSITION_MAIN);
					statement.executeUpdate(DROP_LAMBDA_HANDLER);
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

	public static void persistAmazonHandler(String functionName, String url, String apiId, String region) {
		try {
			Connection connection = MySQLConnect.connectDatabase();
			if (connection == null) {
				System.err.println("Could not connect to database, please check your connection");
				return;
			}
			initTables(connection, AMAZON);

			PreparedStatement preparedStatement = connection.prepareStatement(INSERT_AMAZON_HANDLER);
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

	public static void persistAmazon(String machineName, String machineArn, String[] functionNames, String[] regions) {

		assert functionNames.length == regions.length;

		try {
			Connection connection = MySQLConnect.connectDatabase();
			if (connection == null) {
				System.err.println("Could not connect to database, please check your connection");
				return;
			}
			initTables(connection, AMAZON);

			PreparedStatement preparedStatement = connection.prepareStatement(INSERT_AMAZON_COMPOSITION_MAIN);
			preparedStatement.setString(1, machineName);
			preparedStatement.setString(2, machineArn);
			preparedStatement.execute();
			preparedStatement.close();

			preparedStatement = connection.prepareStatement(INSERT_AMAZON_COMPOSITION_FUNCTION);

			for (int i = 0; i < functionNames.length; i++) {
				preparedStatement.setString(1, functionNames[i]);
				preparedStatement.setString(2, regions[i]);
				preparedStatement.setString(3, machineArn);
				preparedStatement.addBatch();
			}

			preparedStatement.executeBatch();
			preparedStatement.close();

			MySQLConnect.closeConnection(connection);
		} catch (SQLException e) {
			System.err.println("Could not perform insertion: " + e.getMessage());
		}
	}

	public static boolean existsHandler(@Nullable Connection openedConnection) {
		try {
			Connection connection;
			if (openedConnection != null) {
				connection = openedConnection;
			} else {
				connection = MySQLConnect.connectDatabase();
			}

			if (connection == null) {
				System.err.println("Could not connect to database, please check your connection");
				return false;
			}
			initTables(connection, AMAZON);

			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(SELECT_HANDLER_INFO);

			boolean result = resultSet.next();

			statement.close();
			resultSet.close();

			if (openedConnection == null) {
				MySQLConnect.closeConnection(connection);
			}
			return result;
		} catch (SQLException e) {
			System.err.println("Could not perform select: " + e.getMessage());
			return false;
		}
	}

	public static ArrayList<FunctionalityURL> getUrls() {
		try {
			Connection connection = MySQLConnect.connectDatabase();
			if (connection == null) {
				System.err.println("Could not connect to database, please check your connection");
				return null;
			}
			initTables(connection, "*");

			if (!existsHandler(connection)) {
				System.err.println("Could not build Amazon urls: handler not found");
				return null;
			}

			HashMap<String, FunctionalityURL> dynamicResult = new HashMap<>();

			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(SELECT_HANDLER_URL);
			resultSet.next();
			String preamble = resultSet.getString("url") + "?arn=";
			resultSet.close();
			statement.close();

			statement = connection.createStatement();
			resultSet = statement.executeQuery(SELECT_MACHINES_ARNS);

			String name;
			String url;
			FunctionalityURL functionalityURL;

			while (resultSet.next()) {
				name = resultSet.getString("machine_name");
				url = preamble + resultSet.getString("machine_arn");

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

			// TODO: Google

			MySQLConnect.closeConnection(connection);

			return new ArrayList<>(dynamicResult.values());

		} catch (SQLException e) {
			System.err.println("Could not perform select: " + e.getMessage());
			return null;
		}
	}


}
