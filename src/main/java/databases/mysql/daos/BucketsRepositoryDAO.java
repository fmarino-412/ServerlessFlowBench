package databases.mysql.daos;

import databases.mysql.CloudEntityData;
import databases.mysql.MySQLConnect;
import utility.PropertiesManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for cloud buckets related information
 */
@SuppressWarnings({"DuplicatedCode", "SqlResolve"})
public class BucketsRepositoryDAO extends DAO {

	/**
	 * Queries
	 */
	private static final String CREATE_GOOGLE_BUCKETS_TABLE = "CREATE TABLE IF NOT EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".google_cloud_buckets (" +
			"bucket_name varchar(100) NOT NULL, " +
			"PRIMARY KEY (bucket_name)" +
			")";

	private static final String CREATE_AMAZON_BUCKETS_TABLE = "CREATE TABLE IF NOT EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".amazon_cloud_buckets (" +
			"bucket_name varchar(100) NOT NULL, " +
			"region varchar(15) NOT NULL, " +
			"PRIMARY KEY (bucket_name)" +
			")";

	private static final String INSERT_GOOGLE_BUCKET = "INSERT INTO " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".google_cloud_buckets " +
			"(bucket_name) " + "VALUES (?) " +
			"ON DUPLICATE KEY UPDATE bucket_name=VALUES(bucket_name)";

	private static final String INSERT_AMAZON_BUCKET = "INSERT INTO " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".amazon_cloud_buckets " +
			"(bucket_name, region) " + "VALUES (?, ?) " +
			"ON DUPLICATE KEY UPDATE bucket_name=VALUES(bucket_name), region=VALUES(region)";

	private static final String SELECT_GOOGLE_BUCKETS = "SELECT bucket_name FROM " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".google_cloud_buckets";

	private static final String SELECT_AMAZON_BUCKETS = "SELECT bucket_name, region FROM " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".amazon_cloud_buckets";

	private static final String DROP_GOOGLE_BUCKETS = "DROP TABLE IF EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".google_cloud_buckets";

	private static final String DROP_AMAZON_BUCKETS = "DROP TABLE IF EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB) + ".amazon_cloud_buckets";


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
					statement.executeUpdate(CREATE_GOOGLE_BUCKETS_TABLE);
					break;
				case AMAZON:
					statement.executeUpdate(CREATE_AMAZON_BUCKETS_TABLE);
					break;
				default:
					statement.executeUpdate(CREATE_AMAZON_BUCKETS_TABLE);
					statement.executeUpdate(CREATE_GOOGLE_BUCKETS_TABLE);
					break;
			}
			statement.close();
		} else {
			System.err.println("Could not initialize database");
		}
	}

	/**
	 * Drop every table associated to Google Cloud Platform Buckets
	 */
	public static void dropGoogle() {
		dropTable(GOOGLE);
	}

	/**
	 * Drop every table associated to Amazon Web Services Buckets
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
					statement.executeUpdate(DROP_GOOGLE_BUCKETS);
					break;
				case AMAZON:
					statement.executeUpdate(DROP_AMAZON_BUCKETS);
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
	 * Persists a new Google Cloud Storage bucket to database
	 * @param bucketName name of the bucket
	 */
	public static void persistGoogle(String bucketName) {
		try {
			Connection connection = MySQLConnect.connectDatabase();
			if (connection == null) {
				System.err.println("Could not connect to database, please check your connection");
				return;
			}
			initTables(connection, GOOGLE);

			PreparedStatement preparedStatement = connection.prepareStatement(INSERT_GOOGLE_BUCKET);
			preparedStatement.setString(1, bucketName);
			preparedStatement.execute();
			preparedStatement.close();
			MySQLConnect.closeConnection(connection);
		} catch (SQLException e) {
			System.err.println("Could not perform insertion: " + e.getMessage());
		}
	}

	/**
	 * Persists a new Amazon S3 bucket to database
	 * @param bucketName name of the bucket
	 * @param region bucket region
	 */
	public static void persistAmazon(String bucketName, String region) {
		try {
			Connection connection = MySQLConnect.connectDatabase();
			if (connection == null) {
				System.err.println("Could not connect to database, please check your connection");
				return;
			}
			initTables(connection, AMAZON);

			PreparedStatement preparedStatement = connection.prepareStatement(INSERT_AMAZON_BUCKET);
			preparedStatement.setString(1, bucketName);
			preparedStatement.setString(2, region);
			preparedStatement.execute();
			preparedStatement.close();
			MySQLConnect.closeConnection(connection);
		} catch (SQLException e) {
			System.err.println("Could not perform insertion: " + e.getMessage());
		}
	}

	/**
	 * List every Google Cloud Storage bucket
	 * @return list of buckets (CloudEntityData)
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
			ResultSet resultSet = statement.executeQuery(SELECT_GOOGLE_BUCKETS);

			List<CloudEntityData> result = new ArrayList<>();

			while (resultSet.next()) {
				result.add(new CloudEntityData(resultSet.getString("bucket_name")));
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
	 * List every Amazon S3 bucket
	 * @return list of buckets (CloudEntityData)
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
			ResultSet resultSet = statement.executeQuery(SELECT_AMAZON_BUCKETS);

			List<CloudEntityData> result = new ArrayList<>();

			while (resultSet.next()) {
				result.add(new CloudEntityData(resultSet.getString("bucket_name"),
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
