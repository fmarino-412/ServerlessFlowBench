package databases.influx;

import cmd.CommandUtility;
import cmd.benchmark_commands.output_parsing.BenchmarkStats;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Pong;
import utility.PropertiesManager;

import java.util.concurrent.TimeUnit;

/**
 * An influxDB client offering functionalities to export benchmark result to the time series database
 */
@SuppressWarnings("DuplicatedCode")
public class InfluxClient {

	/**
	 * Database connection properties
	 */
	private static final String DB_URL = "http://" +
			PropertiesManager.getInstance().getProperty(PropertiesManager.INFLUX_IP) + ":" +
			PropertiesManager.getInstance().getProperty(PropertiesManager.INFLUX_PORT);
	private static final String DB_USR = PropertiesManager.getInstance().getProperty(PropertiesManager.INFLUX_USR);
	private static final String DB_PASS = PropertiesManager.getInstance().getProperty(PropertiesManager.INFLUX_PASS);
	private static final String DB_NAME = PropertiesManager.getInstance().getProperty(PropertiesManager.INFLUX_DB);


	/**
	 * Connects to time series database
	 * @return instance of InfluxDB connection
	 */
	private static InfluxDB getConnection() {

		InfluxDB connection = InfluxDBFactory.connect(DB_URL, DB_USR, DB_PASS);
		connection.setLogLevel(InfluxDB.LogLevel.NONE);
		Pong response = connection.ping();
		if (response.getVersion().equalsIgnoreCase("unknown")) {
			// error pinging server
			System.err.println("Error pinging influx db");
			return null;
		}

		return connection;
	}

	/**
	 * Closes an InfluxDB connection
	 * @param connection InfluxDB connection to close
	 */
	private static void closeConnection(InfluxDB connection) {

		connection.close();

	}

	/**
	 * Initializes database
	 * @param connection InfluxDB connection
	 */
	public static void initDatabase(InfluxDB connection) {
		if (!connection.databaseExists(DB_NAME)) {
			connection.createDatabase(DB_NAME);
			// maintains time series data for specified time interval
			connection.createRetentionPolicy("defaultPolicy", DB_NAME, "365d", 1, true);
		}
	}

	/**
	 * Inserts a Cold Start Benchmark result
	 * @param functionalityName name of the functionality tested
	 * @param provider provider associated to the result
	 * @param latency latency result in milliseconds
	 * @param millis measurement timestamp
	 * @return true if insertion has been completed, false elsewhere
	 */
	public static boolean insertColdPoint(String functionalityName, String provider, long latency, long millis) {

		if (functionalityName == null || provider == null) {
			return false;
		}

		String[] parts = splitNameEnv(functionalityName);
		String runtime = parts[1];
		String name = parts[0];

		Point cold_start_latency =  Point.measurement("cold_start_latency_" + name)
				.time(millis, TimeUnit.MILLISECONDS)
				.addField("runtime", runtime)
				.addField("provider", provider)
				.addField("value", latency)
				.build();

		InfluxDB connection = getConnection();
		if (connection == null) {
			return false;
		} else {
			initDatabase(connection);
			connection.setRetentionPolicy("defaultPolicy");
			connection.setDatabase(DB_NAME);
			connection.write(cold_start_latency);
			closeConnection(connection);
			return true;
		}
	}

	/**
	 * Inserts a Load Benchmark result
	 * @param functionalityName name of the functionality tested
	 * @param provider provider associated to the result
	 * @param stats benchmark result
	 * @param millis measurement timestamp
	 * @return true if insertion has been completed, false elsewhere
	 */
	public static boolean insertLoadPoints(String functionalityName, String provider, BenchmarkStats stats,
										   long millis) {

		if (functionalityName == null || provider == null || stats == null
				|| stats.getAvgLatency() == null || stats.getMaxLatency() == null || stats.getStdDevLatency() == null
				|| stats.getRequestsThroughput() == null || stats.getTransferThroughput() == null) {
			return false;
		}

		String[] parts = splitNameEnv(functionalityName);
		String runtime = parts[1];
		String name = parts[0];

		// insert multiple points at a time using a batch (every latency result is to be considered in milliseconds)
		BatchPoints batch = BatchPoints
				.database(DB_NAME)
				.retentionPolicy("defaultPolicy")
				.build();

		Point avg_latency = Point.measurement("avg_latency_" + name)
				.time(millis, TimeUnit.MILLISECONDS)
				.addField("runtime", runtime)
				.addField("provider", provider)
				.addField("value", stats.getAvgLatency())
				.build();
		batch.point(avg_latency);

		Point std_latency_dev = Point.measurement("std_latency_dev_" + name)
				.time(millis, TimeUnit.MILLISECONDS)
				.addField("runtime", runtime)
				.addField("provider", provider)
				.addField("value", stats.getStdDevLatency())
				.build();
		batch.point(std_latency_dev);

		Point max_latency = Point.measurement("max_latency_" + name)
				.time(millis, TimeUnit.MILLISECONDS)
				.addField("runtime", runtime)
				.addField("provider", provider)
				.addField("value", stats.getMaxLatency())
				.build();
		batch.point(max_latency);

		Point requests_throughput = Point.measurement("requests_throughput_" + name)
				.time(millis, TimeUnit.MILLISECONDS)
				.addField("runtime", runtime)
				.addField("provider", provider)
				.addField("value", stats.getRequestsThroughput())
				.build();
		batch.point(requests_throughput);

		Point transfer_throughput = Point.measurement("transfer_throughput_" + name)
				.time(millis, TimeUnit.MILLISECONDS)
				.addField("runtime", runtime)
				.addField("provider", provider)
				.addField("value", stats.getTransferThroughput())
				.build();
		batch.point(transfer_throughput);


		InfluxDB connection = getConnection();

		if (connection == null) {
			return false;
		} else {
			initDatabase(connection);
			connection.setRetentionPolicy("defaultPolicy");
			connection.setDatabase(DB_NAME);
			connection.write(batch);
			closeConnection(connection);
			return true;
		}

	}

	/**
	 * Extract function name and runtime info from the joined name
	 * @param completeName joined name
	 * @return String[], name in position 0, runtime in position 1
	 */
	private static String[] splitNameEnv(String completeName) {

		String separator = CommandUtility.getRuntimeSep();
		String[] parts = completeName.split(separator);

		// extract runtime info
		String runtime = parts[parts.length - 1];

		// extract function common name info
		StringBuilder name = new StringBuilder();
		for (int i = 0; i < parts.length - 1; i++) {
			name.append(parts[i]).append(separator);
		}
		// ignore last separation character
		name.setLength(name.length() - separator.length());

		return new String[]{name.toString(), runtime};
	}
}
