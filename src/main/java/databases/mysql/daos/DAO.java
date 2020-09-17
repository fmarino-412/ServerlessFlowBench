package databases.mysql.daos;

import com.sun.istack.internal.NotNull;
import utility.PropertiesManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Generic Data Access Object for serverless related information
 */
public abstract class DAO {

	/**
	 * Providers
	 */
	protected static final String GOOGLE = "GoogleCloud";
	protected static final String AMAZON = "AmazonWebServices";

	/**
	 * Create Database query
	 */
	private static final String CREATE_DATABASE = "CREATE DATABASE IF NOT EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB);


	/**
	 * Creates a new database
	 * @param connection connection to database
	 * @throws SQLException query definition and execution related problems
	 */
	protected static void initDatabase(@NotNull Connection connection) throws SQLException {
		Statement statement = connection.createStatement();
		statement.executeUpdate(CREATE_DATABASE);
		statement.close();
	}

}
