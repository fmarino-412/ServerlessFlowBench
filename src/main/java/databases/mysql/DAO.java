package databases.mysql;

import com.sun.istack.internal.NotNull;
import utility.PropertiesManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class DAO {

	protected static final String GOOGLE = "GoogleCloud";
	protected static final String AMAZON = "AmazonWebServices";

	private static final String CREATE_DATABASE = "CREATE DATABASE IF NOT EXISTS " +
			PropertiesManager.getInstance().getProperty(PropertiesManager.MYSQL_DB);

	protected static void initDatabase(@NotNull Connection connection) throws SQLException {
		Statement statement = connection.createStatement();
		statement.executeUpdate(CREATE_DATABASE);
		statement.close();
	}

}
