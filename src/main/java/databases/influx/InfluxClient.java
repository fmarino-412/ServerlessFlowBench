package databases.influx;

import cmd.benchmark_commands.output_parsing.BenchmarkStats;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Pong;
import utility.PropertiesManager;

import java.util.concurrent.TimeUnit;

public class InfluxClient {

	private static final String DB_URL = "http://" +
			PropertiesManager.getInstance().getProperty(PropertiesManager.INFLUX_IP) + ":" +
			PropertiesManager.getInstance().getProperty(PropertiesManager.INFLUX_PORT);
	private static final String DB_USR = PropertiesManager.getInstance().getProperty(PropertiesManager.INFLUX_USR);
	private static final String DB_PASS = PropertiesManager.getInstance().getProperty(PropertiesManager.INFLUX_PASS);
	private static final String DB_NAME = PropertiesManager.getInstance().getProperty(PropertiesManager.INFLUX_DB);


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

	private static void closeConnection(InfluxDB connection) {

		connection.close();

	}

	public static void initDatabase(InfluxDB connection) {
		if (!connection.databaseExists(DB_NAME)) {
			connection.createDatabase(DB_NAME);
			// maintains time series data for specified time interval
			connection.createRetentionPolicy("defaultPolicy", DB_NAME, "365d", 1, true);
		}
	}

	public static boolean insertPoints(String functionName, String provider, BenchmarkStats stats, long millis) {

		// insert multiple points at a time
		BatchPoints batch = BatchPoints
				.database(DB_NAME)
				.retentionPolicy("defaultPolicy")
				.build();

		Point avg_latency = Point.measurement("avg_latency_" + functionName)
				.time(millis, TimeUnit.MILLISECONDS)
				.addField("provider", provider)
				.addField("value", stats.getAvgLatency())
				.build();
		batch.point(avg_latency);

		Point std_latency_dev = Point.measurement("std_latency_dev_" + functionName)
				.time(millis, TimeUnit.MILLISECONDS)
				.addField("provider", provider)
				.addField("value", stats.getStdDevLatency())
				.build();
		batch.point(std_latency_dev);

		Point max_latency = Point.measurement("max_latency_" + functionName)
				.time(millis, TimeUnit.MILLISECONDS)
				.addField("provider", provider)
				.addField("value", stats.getMaxLatency())
				.build();
		batch.point(max_latency);

		Point requests_throughput = Point.measurement("requests_throughput_" + functionName)
				.time(millis, TimeUnit.MILLISECONDS)
				.addField("provider", provider)
				.addField("value", stats.getRequestsThroughput())
				.build();
		batch.point(requests_throughput);

		Point transfer_throughput = Point.measurement("transfer_throughput_" + functionName)
				.time(millis, TimeUnit.MILLISECONDS)
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
}
