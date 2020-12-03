package databases.mysql.daos;

import com.sun.istack.internal.Nullable;
import databases.mysql.CloudEntityData;
import databases.mysql.DAO;
import databases.mysql.FunctionalityURL;
import databases.mysql.MySQLConnect;
import utility.PropertiesManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Data Access Object for serverless function composition related information
 */
@SuppressWarnings({"DuplicatedCode", "SqlResolve", "RedundantSuppression"})
public class CompositionsRepositoryDAO extends DAO {

	/**
	 * Queries
	 */
	private static final String CREATE_GOOGLE_COMPOSITION_CLOUD_FUNCTIONS_HANDLER = "CREATE TABLE IF NOT EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".google_serverless_handler_function (" +
			"id enum('1') NOT NULL, " +
			"function_name varchar(50) NOT NULL, " +
			"url varchar(100) NOT NULL, " +
			"region varchar(15) NOT NULL, " +
			"PRIMARY KEY (id)" +
			")";

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

	private static final String CREATE_GOOGLE_COMPOSITION_TABLE_MAIN = "CREATE TABLE IF NOT EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".google_serverless_compositions_main (" +
			"workflow_name varchar(50) NOT NULL, " +
			"workflow_region varchar(15) NOT NULL, " +
			"PRIMARY KEY (workflow_name)" +
			")";

	private static final String CREATE_AMAZON_COMPOSITION_TABLE_MAIN = "CREATE TABLE IF NOT EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".amazon_serverless_compositions_main (" +
			"machine_name varchar(50) NOT NULL, " +
			"machine_arn varchar(100) NOT NULL UNIQUE, " +
			"machine_region varchar(15) NOT NULL, " +
			"PRIMARY KEY (machine_name)" +
			")";

	private static final String CREATE_OPENWHISK_COMPOSITION_TABLE_MAIN = "CREATE TABLE IF NOT EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".openwhisk_serverless_compositions_main (" +
			"composition_name varchar(50) NOT NULL, " +
			"url varchar(100) NOT NULL, " +
			"PRIMARY KEY (composition_name)" +
			")";

	private static final String CREATE_GOOGLE_COMPOSITION_TABLE_FUNCTIONS = "CREATE TABLE IF NOT EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".google_serverless_compositions_functions (" +
			"function_name varchar(50) NOT NULL, " +
			"function_region varchar(15) NOT NULL, " +
			"workflow varchar(50) NOT NULL, " +
			"PRIMARY KEY (function_name), " +
			"CONSTRAINT workflow_name_fk FOREIGN KEY (workflow) REFERENCES " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".google_serverless_compositions_main(workflow_name)" +
			")";

	private static final String CREATE_AMAZON_COMPOSITION_TABLE_FUNCTIONS = "CREATE TABLE IF NOT EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".amazon_serverless_compositions_functions (" +
			"function_name varchar(50) NOT NULL, " +
			"function_region varchar(15) NOT NULL, " +
			"state_machine_arn varchar(100) NOT NULL, " +
			"PRIMARY KEY (function_name), " +
			"CONSTRAINT state_machine_arn_fk FOREIGN KEY (state_machine_arn) REFERENCES " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".amazon_serverless_compositions_main(machine_arn)" +
			")";

	private static final String CREATE_OPENWHISK_COMPOSITION_TABLE_FUNCTIONS = "CREATE TABLE IF NOT EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".openwhisk_serverless_compositions_functions (" +
			"function_name varchar(50) NOT NULL, " +
			"composition varchar(50) NOT NULL, " +
			"PRIMARY KEY (function_name), " +
			"CONSTRAINT composition_name_fk FOREIGN KEY (composition) REFERENCES " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".openwhisk_serverless_compositions_main(composition_name)" +
			")";

	private static final String INSERT_GOOGLE_HANDLER = "INSERT INTO " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".google_serverless_handler_function " +
			"(function_name, url, region) " + "VALUES (?, ?, ?) " +
			"ON DUPLICATE KEY UPDATE function_name=VALUES(function_name), url=VALUES(url), region=VALUES(region)";

	private static final String INSERT_AMAZON_HANDLER = "INSERT INTO " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".amazon_serverless_handler_function " +
			"(function_name, url, api_id, region) " + "VALUES (?, ?, ?, ?) " +
			"ON DUPLICATE KEY UPDATE function_name=VALUES(function_name), url=VALUES(url), " +
			"api_id=VALUES(api_id), region=VALUES(region)";

	private static final String INSERT_GOOGLE_COMPOSITION_MAIN = "INSERT INTO " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".google_serverless_compositions_main " +
			"(workflow_name, workflow_region) " + "VALUES (?, ?) " +
			"ON DUPLICATE KEY UPDATE workflow_name=VALUES(workflow_name), workflow_region=VALUES(workflow_region)";

	private static final String INSERT_AMAZON_COMPOSITION_MAIN = "INSERT INTO " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".amazon_serverless_compositions_main " +
			"(machine_name, machine_arn, machine_region) " + "VALUES (?, ?, ?) " +
			"ON DUPLICATE KEY UPDATE machine_name=VALUES(machine_name), machine_arn=VALUES(machine_arn), " +
			"machine_region=VALUES(machine_region)";

	private static final String INSERT_OPENWHISK_COMPOSITION_MAIN = "INSERT INTO " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".openwhisk_serverless_compositions_main " +
			"(composition_name, url) " + "VALUES (?, ?) " +
			"ON DUPLICATE KEY UPDATE composition_name=VALUES(composition_name), url=VALUES(url)";

	private static final String INSERT_GOOGLE_COMPOSITION_FUNCTION = "INSERT INTO " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".google_serverless_compositions_functions " +
			"(function_name, function_region, workflow) " + "VALUES (?, ?, ?) " +
			"ON DUPLICATE KEY UPDATE function_name=VALUES(function_name), function_region=VALUES(function_region), " +
			"workflow=VALUES(workflow)";

	private static final String INSERT_AMAZON_COMPOSITION_FUNCTION = "INSERT INTO " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".amazon_serverless_compositions_functions " +
			"(function_name, function_region, state_machine_arn) " + "VALUES (?, ?, ?) " +
			"ON DUPLICATE KEY UPDATE function_name=VALUES(function_name), function_region=VALUES(function_region), " +
			"state_machine_arn=VALUES(state_machine_arn)";

	private static final String INSERT_OPENWHISK_COMPOSITION_FUNCTION = "INSERT INTO " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".openwhisk_serverless_compositions_functions " +
			"(function_name, composition) " + "VALUES (?, ?) " +
			"ON DUPLICATE KEY UPDATE function_name=VALUES(function_name), composition=VALUES(composition)";

	private static final String SELECT_GOOGLE_HANDLER_INFO = "SELECT function_name, region " +
			"FROM " + PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".google_serverless_handler_function WHERE id=1";

	private static final String SELECT_AMAZON_HANDLER_INFO = "SELECT function_name, api_id, region " +
			"FROM " + PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".amazon_serverless_handler_function WHERE id=1";

	private static final String SELECT_GOOGLE_FUNCTION_INFOS = "SELECT function_name, function_region " +
			"FROM " + PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".google_serverless_compositions_functions";

	private static final String SELECT_AMAZON_FUNCTION_INFOS = "SELECT function_name, function_region " +
			"FROM " + PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".amazon_serverless_compositions_functions";

	private static final String SELECT_OPENWHISK_FUNCTION_INFOS = "SELECT function_name " +
			"FROM " + PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".openwhisk_serverless_compositions_functions";

	private static final String SELECT_GOOGLE_WORKFLOW_INFOS = "SELECT workflow_name, workflow_region " +
			"FROM " + PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".google_serverless_compositions_main";

	private static final String SELECT_AMAZON_MACHINE_INFOS = "SELECT machine_name, machine_arn, machine_region " +
			"FROM " + PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".amazon_serverless_compositions_main";

	private static final String SELECT_OPENWHISK_COMPOSITION_INFOS = "SELECT composition_name, url " +
			"FROM " + PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".openwhisk_serverless_compositions_main";

	private static final String SELECT_GOOGLE_HANDLER_URL = "SELECT url " +
			"FROM " + PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".google_serverless_handler_function WHERE id=1";

	private static final String SELECT_AMAZON_HANDLER_URL = "SELECT url " +
			"FROM " + PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".amazon_serverless_handler_function WHERE id=1";

	private static final String SELECT_GOOGLE_WORKFLOWS_NAMES = "SELECT workflow_name " +
			"FROM " + PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".google_serverless_compositions_main";

	@SuppressWarnings("SpellCheckingInspection")
	private static final String SELECT_AMAZON_MACHINES_ARNS = "SELECT machine_name, machine_arn " +
			"FROM " + PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".amazon_serverless_compositions_main";

	private static final String SELECT_OPENWHISK_COMPOSITION_NAMES = "SELECT composition_name " +
			"FROM " + PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".openwhisk_serverless_compositions_main";

	private static final String DROP_CLOUD_FUNCTIONS_HANDLER = "DROP TABLE IF EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".google_serverless_handler_function";

	private static final String DROP_LAMBDA_HANDLER = "DROP TABLE IF EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".amazon_serverless_handler_function";

	private static final String DROP_GOOGLE_COMPOSITION_FUNCTIONS = "DROP TABLE IF EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".google_serverless_compositions_functions";

	private static final String DROP_AMAZON_COMPOSITION_FUNCTIONS = "DROP TABLE IF EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".amazon_serverless_compositions_functions";

	private static final String DROP_OPENWHISK_COMPOSITION_FUNCTIONS = "DROP TABLE IF EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".openwhisk_serverless_compositions_functions";

	private static final String DROP_GOOGLE_COMPOSITION_MAIN = "DROP TABLE IF EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".google_serverless_compositions_main";

	private static final String DROP_AMAZON_COMPOSITION_MAIN = "DROP TABLE IF EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".amazon_serverless_compositions_main";

	private static final String DROP_OPENWHISK_COMPOSITION_MAIN = "DROP TABLE IF EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) +
			".openwhisk_serverless_compositions_main";


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
					statement.executeUpdate(CREATE_GOOGLE_COMPOSITION_CLOUD_FUNCTIONS_HANDLER);
					statement.executeUpdate(CREATE_GOOGLE_COMPOSITION_TABLE_MAIN);
					statement.executeUpdate(CREATE_GOOGLE_COMPOSITION_TABLE_FUNCTIONS);
					break;
				case AMAZON:
					statement.executeUpdate(CREATE_AMAZON_COMPOSITION_LAMBDA_HANDLER);
					statement.executeUpdate(CREATE_AMAZON_COMPOSITION_TABLE_MAIN);
					statement.executeUpdate(CREATE_AMAZON_COMPOSITION_TABLE_FUNCTIONS);
					break;
				case OPENWHISK:
					statement.executeUpdate(CREATE_OPENWHISK_COMPOSITION_TABLE_MAIN);
					statement.executeUpdate(CREATE_OPENWHISK_COMPOSITION_TABLE_FUNCTIONS);
				default:
					statement.executeUpdate(CREATE_GOOGLE_COMPOSITION_CLOUD_FUNCTIONS_HANDLER);
					statement.executeUpdate(CREATE_GOOGLE_COMPOSITION_TABLE_MAIN);
					statement.executeUpdate(CREATE_GOOGLE_COMPOSITION_TABLE_FUNCTIONS);
					statement.executeUpdate(CREATE_AMAZON_COMPOSITION_LAMBDA_HANDLER);
					statement.executeUpdate(CREATE_AMAZON_COMPOSITION_TABLE_MAIN);
					statement.executeUpdate(CREATE_AMAZON_COMPOSITION_TABLE_FUNCTIONS);
					statement.executeUpdate(CREATE_OPENWHISK_COMPOSITION_TABLE_MAIN);
					statement.executeUpdate(CREATE_OPENWHISK_COMPOSITION_TABLE_FUNCTIONS);
					break;
			}
			statement.close();
		} else {
			System.err.println("Could not initialize database");
		}
	}

	/**
	 * Drop every table associated to Google Cloud Platform Function Compositions
	 */
	public static void dropGoogle() {
		dropTable(GOOGLE);
	}

	/**
	 * Drop every table associated to Amazon Web Services Function Compositions
	 */
	public static void dropAmazon() {
		dropTable(AMAZON);
	}

	/**
	 * Drop every table associated to OpenWhisk Compositions
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
					statement.executeUpdate(DROP_GOOGLE_COMPOSITION_FUNCTIONS);
					statement.executeUpdate(DROP_GOOGLE_COMPOSITION_MAIN);
					statement.executeUpdate(DROP_CLOUD_FUNCTIONS_HANDLER);
					break;
				case AMAZON:
					statement.executeUpdate(DROP_AMAZON_COMPOSITION_FUNCTIONS);
					statement.executeUpdate(DROP_AMAZON_COMPOSITION_MAIN);
					statement.executeUpdate(DROP_LAMBDA_HANDLER);
					break;
				case OPENWHISK:
					statement.executeUpdate(DROP_OPENWHISK_COMPOSITION_FUNCTIONS);
					statement.executeUpdate(DROP_OPENWHISK_COMPOSITION_MAIN);
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
	 * Persists a new Google Cloud Functions Handler (just one for DB)
	 * @param functionName name of the handler
	 * @param url url of the handler
	 * @param region region of deployment for the handler
	 */
	public static void persistGoogleHandler(String functionName, String url, String region) {
		try {
			Connection connection = MySQLConnect.connectDatabase();
			if (connection == null) {
				System.err.println("Could not connect to database, please check your connection");
				return;
			}
			initTables(connection, GOOGLE);

			PreparedStatement preparedStatement = connection.prepareStatement(INSERT_GOOGLE_HANDLER);
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
	 * Persists a new Amazon Lambda and API Gateway Handler (just one for DB)
	 * @param functionName name of the handler
	 * @param url url of the handler
	 * @param apiId id of the api associated to the handler
	 * @param region region of deployment for the handler
	 */
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

	/**
	 * Persists a new Google Cloud Platform workflow and associated functions
	 * @param workflowName name of the workflow
	 * @param workflowRegion workflow region of deployment
	 * @param functionNames names of functions (consistent ordering)
	 * @param functionRegions regions of function deployments (consistent ordering)
	 */
	public static void persistGoogle(String workflowName, String workflowRegion, String[] functionNames,
									 String[] functionRegions) {

		assert functionNames.length == functionRegions.length;

		try {
			Connection connection = MySQLConnect.connectDatabase();
			if (connection == null) {
				System.err.println("Could not connect to database, please check your connection");
				return;
			}
			initTables(connection, GOOGLE);

			PreparedStatement preparedStatement = connection.prepareStatement(INSERT_GOOGLE_COMPOSITION_MAIN);
			preparedStatement.setString(1, workflowName);
			preparedStatement.setString(2, workflowRegion);
			preparedStatement.execute();
			preparedStatement.close();

			preparedStatement = connection.prepareStatement(INSERT_GOOGLE_COMPOSITION_FUNCTION);

			for (int i = 0; i < functionNames.length; i++) {
				preparedStatement.setString(1, functionNames[i]);
				preparedStatement.setString(2, functionRegions[i]);
				preparedStatement.setString(3, workflowName);
				preparedStatement.addBatch();
			}

			preparedStatement.executeBatch();
			preparedStatement.close();

			MySQLConnect.closeConnection(connection);
		} catch (SQLException e) {
			System.err.println("Could not perform insertion: " + e.getMessage());
		}
	}

	/**
	 * Persists a new Amazon Web Services state machine and associated functions
	 * @param machineName name of the state machine
	 * @param machineArn ARN of the state machine
	 * @param machineRegion state machine region of deployment
	 * @param functionNames names of functions (consistent ordering)
	 * @param functionRegions regions of function deployments (consistent ordering)
	 */
	public static void persistAmazon(String machineName, String machineArn, String machineRegion,
									 String[] functionNames, String[] functionRegions) {

		assert functionNames.length == functionRegions.length;

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
			preparedStatement.setString(3, machineRegion);
			preparedStatement.execute();
			preparedStatement.close();

			preparedStatement = connection.prepareStatement(INSERT_AMAZON_COMPOSITION_FUNCTION);

			for (int i = 0; i < functionNames.length; i++) {
				preparedStatement.setString(1, functionNames[i]);
				preparedStatement.setString(2, functionRegions[i]);
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

	/**
	 * Persists a new OpenWhisk composition and associated functions
	 * @param compositionName name of the composition
	 * @param url url of the composition
	 * @param functionNames names of functions
	 */
	public static void persistOpenWhisk(String compositionName, String url, String[] functionNames) {

		try {
			Connection connection = MySQLConnect.connectDatabase();
			if (connection == null) {
				System.err.println("Could not connect to database, please check your connection");
				return;
			}
			initTables(connection, OPENWHISK);

			PreparedStatement preparedStatement = connection.prepareStatement(INSERT_OPENWHISK_COMPOSITION_MAIN);
			preparedStatement.setString(1, compositionName);
			preparedStatement.setString(2, url);
			preparedStatement.execute();
			preparedStatement.close();

			preparedStatement = connection.prepareStatement(INSERT_OPENWHISK_COMPOSITION_FUNCTION);

			for (String functionName : functionNames) {
				preparedStatement.setString(1, functionName);
				preparedStatement.setString(2, compositionName);
				preparedStatement.addBatch();
			}

			preparedStatement.executeBatch();
			preparedStatement.close();

			MySQLConnect.closeConnection(connection);
		} catch (SQLException e) {
			System.err.println("Could not perform insertion: " + e.getMessage());
		}
	}

	/**
	 * Checks whether an handler for Google Workflows [BETA] already exists
	 * @param openedConnection opened connection to database (or null)
	 * @return true if the handler exists, false elsewhere
	 */
	public static boolean existsGoogleHandler(@Nullable Connection openedConnection) {
		try {
			Connection connection;
			if (openedConnection != null && !openedConnection.isClosed()) {
				connection = openedConnection;
			} else {
				connection = MySQLConnect.connectDatabase();
			}

			if (connection == null) {
				System.err.println("Could not connect to database, please check your connection");
				return false;
			}
			initTables(connection, GOOGLE);

			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(SELECT_GOOGLE_HANDLER_INFO);

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

	/**
	 * Checks whether an handler for Amazon Step Functions already exists
	 * @param openedConnection opened connection to database (or null)
	 * @return true if the handler exists, false elsewhere
	 */
	public static boolean existsAmazonHandler(@Nullable Connection openedConnection) {
		try {
			Connection connection;
			if (openedConnection != null && !openedConnection.isClosed()) {
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
			ResultSet resultSet = statement.executeQuery(SELECT_AMAZON_HANDLER_INFO);

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

	/**
	 * Getter for Google Workflows [BETA] handler
	 * @return CloudEntityData containing handler info
	 */
	public static CloudEntityData getGoogleHandlerInfo() {
		try {
			Connection connection = MySQLConnect.connectDatabase();
			if (connection == null) {
				System.err.println("Could not connect to database, please check your connection");
				return null;
			}
			initTables(connection, GOOGLE);

			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(SELECT_GOOGLE_HANDLER_INFO);

			// function name and region
			CloudEntityData result = null;
			if (resultSet.next()) {
				result = new CloudEntityData(resultSet.getString("function_name"),
						resultSet.getString("region"));
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
	 * Getter for Amazon Step Functions handler
	 * @return CloudEntityData containing handler info
	 */
	public static CloudEntityData getAmazonHandlerInfo() {
		try {
			Connection connection = MySQLConnect.connectDatabase();
			if (connection == null) {
				System.err.println("Could not connect to database, please check your connection");
				return null;
			}
			initTables(connection, AMAZON);

			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(SELECT_AMAZON_HANDLER_INFO);

			// function name, region and api id
			CloudEntityData result = null;
			if (resultSet.next()) {
				result = new CloudEntityData(resultSet.getString("function_name"),
						resultSet.getString("region"), resultSet.getString("api_id"));
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
	 * Getter for every workflow associated Google Cloud Functions function
	 * @return list of CloudEntityData with function information
	 */
	public static List<CloudEntityData> getGoogleFunctionInfos() {
		try {
			Connection connection = MySQLConnect.connectDatabase();
			if (connection == null) {
				System.err.println("Could not connect to database, please check your connection");
				return null;
			}
			initTables(connection, GOOGLE);

			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(SELECT_GOOGLE_FUNCTION_INFOS);

			List<CloudEntityData> result = new ArrayList<>();

			// function_name, function_region
			while (resultSet.next()) {
				result.add(new CloudEntityData(resultSet.getString("function_name"),
						resultSet.getString("function_region"), null));
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
	 * Getter for every state machine associated Amazon Lambda function
	 * @return list of CloudEntityData with function information
	 */
	public static List<CloudEntityData> getAmazonFunctionInfos() {
		try {
			Connection connection = MySQLConnect.connectDatabase();
			if (connection == null) {
				System.err.println("Could not connect to database, please check your connection");
				return null;
			}
			initTables(connection, AMAZON);

			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(SELECT_AMAZON_FUNCTION_INFOS);

			List<CloudEntityData> result = new ArrayList<>();

			// function_name, function_region
			while (resultSet.next()) {
				result.add(new CloudEntityData(resultSet.getString("function_name"),
						resultSet.getString("function_region"), null));
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
	 * Getter for every composition associated OpenWhisk function
	 * @return list of CloudEntityData with function information
	 */
	public static List<CloudEntityData> getOpenWhiskFunctionInfos() {
		try {
			Connection connection = MySQLConnect.connectDatabase();
			if (connection == null) {
				System.err.println("Could not connect to database, please check your connection");
				return null;
			}
			initTables(connection, OPENWHISK);

			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(SELECT_OPENWHISK_FUNCTION_INFOS);

			List<CloudEntityData> result = new ArrayList<>();

			// function_name, function_region
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
	 * Getter for every Google Cloud Platform Workflows [BETA] workflow
	 * @return list of CloudEntityData with workflow information
	 */
	public static List<CloudEntityData> getGoogleWorkflowInfos() {
		try {
			Connection connection = MySQLConnect.connectDatabase();
			if (connection == null) {
				System.err.println("Could not connect to database, please check your connection");
				return null;
			}
			initTables(connection, GOOGLE);

			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(SELECT_GOOGLE_WORKFLOW_INFOS);

			List<CloudEntityData> result = new ArrayList<>();

			while (resultSet.next()) {
				result.add(new CloudEntityData(resultSet.getString("workflow_name"),
						resultSet.getString("workflow_region")));
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
	 * Getter for every AWS Step Functions state machine
	 * @return list of CloudEntityData with state machine information
	 */
	public static List<CloudEntityData> getAmazonMachineInfos() {
		try {
			Connection connection = MySQLConnect.connectDatabase();
			if (connection == null) {
				System.err.println("Could not connect to database, please check your connection");
				return null;
			}
			initTables(connection, AMAZON);

			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(SELECT_AMAZON_MACHINE_INFOS);

			List<CloudEntityData> result = new ArrayList<>();

			while (resultSet.next()) {
				result.add(new CloudEntityData(resultSet.getString("machine_name"),
						resultSet.getString("machine_region"),
						resultSet.getString("machine_arn")));
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
	 * Getter for every OpenWhisk composition
	 * @return list of CloudEntityData with composition information
	 */
	public static List<CloudEntityData> getOpenWhiskCompositionInfos() {
		try {
			Connection connection = MySQLConnect.connectDatabase();
			if (connection == null) {
				System.err.println("Could not connect to database, please check your connection");
				return null;
			}
			initTables(connection, OPENWHISK);

			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(SELECT_OPENWHISK_COMPOSITION_NAMES);

			List<CloudEntityData> result = new ArrayList<>();

			while (resultSet.next()) {
				result.add(new CloudEntityData(resultSet.getString("composition_name")));
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
	 * Getter for Google Workflows [BETA] handler url
	 * @param openedConnection opened connection to database (or null)
	 * @return url as string
	 */
	public static String getGoogleHandlerUrl(@Nullable Connection openedConnection) {
		try {
			Connection connection;
			if (openedConnection != null && !openedConnection.isClosed()) {
				connection = openedConnection;
			} else {
				connection = MySQLConnect.connectDatabase();
			}

			if (connection == null) {
				System.err.println("Could not connect to database, please check your connection");
				return null;
			}
			initTables(connection, GOOGLE);

			if (!existsGoogleHandler(connection)) {
				System.err.println("Google handler not found");
				return null;
			}

			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(SELECT_GOOGLE_HANDLER_URL);
			resultSet.next();

			String result = resultSet.getString("url");

			resultSet.close();
			statement.close();

			if (openedConnection == null) {
				MySQLConnect.closeConnection(connection);
			}
			return result;
		} catch (SQLException e) {
			System.err.println("Could not perform select: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Getter for Amazon Step Functions handler url
	 * @param openedConnection opened connection to database (or null)
	 * @return url as string
	 */
	public static String getAmazonHandlerUrl(@Nullable Connection openedConnection) {
		try {
			Connection connection;
			if (openedConnection != null && !openedConnection.isClosed()) {
				connection = openedConnection;
			} else {
				connection = MySQLConnect.connectDatabase();
			}

			if (connection == null) {
				System.err.println("Could not connect to database, please check your connection");
				return null;
			}
			initTables(connection, AMAZON);

			if (!existsAmazonHandler(connection)) {
				System.err.println("Amazon handler not found");
				return null;
			}

			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(SELECT_AMAZON_HANDLER_URL);
			resultSet.next();

			String result = resultSet.getString("url");

			resultSet.close();
			statement.close();

			if (openedConnection == null) {
				MySQLConnect.closeConnection(connection);
			}
			return result;
		} catch (SQLException e) {
			System.err.println("Could not perform select: " + e.getMessage());
			return null;
		}
	}

	/**
	 * List every composition url, there can be one or more URL per composition basing on different provider
	 * implementation of the same one
	 * @return list of composition urls (FunctionalityURL)
	 */
	public static List<FunctionalityURL> getUrls() {
		try {
			Connection connection = MySQLConnect.connectDatabase();
			if (connection == null) {
				System.err.println("Could not connect to database, please check your connection");
				return null;
			}
			initTables(connection, "*");

			HashMap<String, FunctionalityURL> dynamicResult = new HashMap<>();
			String name;
			String url;
			FunctionalityURL functionalityURL;

			String googlePreamble = getGoogleHandlerUrl(connection);
			if (googlePreamble == null) {
				System.err.println("Could not build urls, Google handler not found!");
				return null;
			}
			googlePreamble = googlePreamble + "?workflow=";

			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(SELECT_GOOGLE_WORKFLOWS_NAMES);

			while (resultSet.next()) {
				name = resultSet.getString("workflow_name");
				url = googlePreamble + name;

				if (dynamicResult.containsKey(name)) {
					dynamicResult.get(name).setGoogleUrl(url);
				} else {
					functionalityURL = new FunctionalityURL(name, true);
					functionalityURL.setGoogleUrl(url);
					dynamicResult.put(name, functionalityURL);
				}
			}

			statement.close();
			resultSet.close();

			String amazonPreamble = getAmazonHandlerUrl(connection);
			if (amazonPreamble == null) {
				System.err.println("Could not build urls, Amazon handler not found!");
				return null;
			}
			amazonPreamble = amazonPreamble + "?arn=";

			statement = connection.createStatement();
			resultSet = statement.executeQuery(SELECT_AMAZON_MACHINES_ARNS);

			while (resultSet.next()) {
				name = resultSet.getString("machine_name");
				url = amazonPreamble + resultSet.getString("machine_arn");

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
			resultSet = statement.executeQuery(SELECT_OPENWHISK_COMPOSITION_INFOS);

			while (resultSet.next()) {
				name = resultSet.getString("composition_name");
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
