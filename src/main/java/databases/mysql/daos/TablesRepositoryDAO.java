package databases.mysql.daos;

import databases.mysql.CloudEntityData;
import databases.mysql.MySQLConnect;
import utility.PropertiesManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for cloud NoSQL tables related information
 */
@SuppressWarnings({"DuplicatedCode", "SqlResolve"})
public class TablesRepositoryDAO extends DAO {

	/**
	 * Queries
	 */
	private static final String CREATE_GOOGLE_TABLES_TABLE = "CREATE TABLE IF NOT EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".google_cloud_tables (" +
			"instance_id varchar(100) NOT NULL, " +
			"PRIMARY KEY (instance_id)" +
			")";

	private static final String CREATE_AMAZON_TABLES_TABLE = "CREATE TABLE IF NOT EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".amazon_cloud_tables (" +
			"table_name varchar(50) NOT NULL, " +
			"region varchar(15) NOT NULL, " +
			"PRIMARY KEY (table_name)" +
			")";

	private static final String INSERT_GOOGLE_TABLE = "INSERT INTO " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".google_cloud_tables " +
			"(instance_id) " + "VALUES (?) " +
			"ON DUPLICATE KEY UPDATE instance_id=VALUES(instance_id)";

	private static final String INSERT_AMAZON_TABLE = "INSERT INTO " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".amazon_cloud_tables " +
			"(table_name, region) " + "VALUES (?, ?) " +
			"ON DUPLICATE KEY UPDATE table_name=VALUES(table_name), region=VALUES(region)";

	private static final String SELECT_GOOGLE_TABLES = "SELECT instance_id FROM " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".google_cloud_tables";

	private static final String SELECT_AMAZON_TABLES = "SELECT table_name, region FROM " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".amazon_cloud_tables";

	private static final String DROP_GOOGLE_TABLES = "DROP TABLE IF EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".google_cloud_tables";

	private static final String DROP_AMAZON_TABLES = "DROP TABLE IF EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".amazon_cloud_tables";

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
					statement.executeUpdate(CREATE_GOOGLE_TABLES_TABLE);
					break;
				case AMAZON:
					statement.executeUpdate(CREATE_AMAZON_TABLES_TABLE);
					break;
				default:
					statement.executeUpdate(CREATE_AMAZON_TABLES_TABLE);
					statement.executeUpdate(CREATE_GOOGLE_TABLES_TABLE);
					break;
			}
			statement.close();
		} else {
			System.err.println("Could not initialize database");
		}
	}

	/**
	 * Drop every table associated to Google Cloud Platform
	 */
	public static void dropGoogle() {
		dropTable(GOOGLE);
	}

	/**
	 * Drop every table associated to Amazon Web Services
	 */
	public static void dropAmazon() {
		dropTable(AMAZON);
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
					statement.executeUpdate(DROP_GOOGLE_TABLES);
					break;
				case AMAZON:
					statement.executeUpdate(DROP_AMAZON_TABLES);
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
	 * Persists a new Google Big Table table to database
	 * @param instanceId Big Table instance id
	 */
	public static void persistGoogle(String instanceId) {
		try {
			Connection connection = MySQLConnect.connectDatabase();
			if (connection == null) {
				System.err.println("Could not connect to database, please check your connection");
				return;
			}
			initTables(connection, GOOGLE);

			PreparedStatement preparedStatement = connection.prepareStatement(INSERT_GOOGLE_TABLE);
			preparedStatement.setString(1, instanceId);
			preparedStatement.execute();
			preparedStatement.close();
			MySQLConnect.closeConnection(connection);
		} catch (SQLException e) {
			System.err.println("Could not perform insertion: " + e.getMessage());
		}
	}

	/**
	 * Persists a new Amazon Dynamo DB table to database
	 * @param tableName name of the table
	 * @param region table deployment region
	 */
	public static void persistAmazon(String tableName, String region) {
		try {
			Connection connection = MySQLConnect.connectDatabase();
			if (connection == null) {
				System.err.println("Could not connect to database, please check your connection");
				return;
			}
			initTables(connection, AMAZON);

			PreparedStatement preparedStatement = connection.prepareStatement(INSERT_AMAZON_TABLE);
			preparedStatement.setString(1, tableName);
			preparedStatement.setString(2, region);
			preparedStatement.execute();
			preparedStatement.close();
			MySQLConnect.closeConnection(connection);
		} catch (SQLException e) {
			System.err.println("Could not perform insertion: " + e.getMessage());
		}
	}

	/**
	 * List every Google Bug Table table
	 * @return list of tables (CloudEntityData)
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
			ResultSet resultSet = statement.executeQuery(SELECT_GOOGLE_TABLES);

			List<CloudEntityData> result = new ArrayList<>();

			while (resultSet.next()) {
				result.add(new CloudEntityData(resultSet.getString("instance_id")));
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
	 * List every Amazon Dynamo DB table
	 * @return list of tables (CloudEntityData)
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
			ResultSet resultSet = statement.executeQuery(SELECT_AMAZON_TABLES);

			List<CloudEntityData> result = new ArrayList<>();

			while (resultSet.next()) {
				result.add(new CloudEntityData(resultSet.getString("table_name"),
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
}
