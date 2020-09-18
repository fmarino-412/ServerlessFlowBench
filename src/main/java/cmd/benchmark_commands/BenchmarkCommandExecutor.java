package cmd.benchmark_commands;

import cmd.CommandExecutor;
import cmd.StreamGobbler;
import cmd.benchmark_commands.output_parsing.BenchmarkCollector;
import cmd.benchmark_commands.output_parsing.BenchmarkStats;
import databases.influx.InfluxClient;
import databases.mysql.FunctionalityURL;
import databases.mysql.daos.CompositionRepositoryDAO;
import databases.mysql.daos.FunctionsRepositoryDAO;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BenchmarkCommandExecutor extends CommandExecutor {

	private static final int COLD_START_SLEEP_INTERVAL_MS = 30 * 60 * 1000;


	/**
	 * Perform a load benchmark through wrk2
	 * @param url url to test
	 * @param concurrency number of concurrent requests
	 * @param threads number of threads
	 * @param seconds test duration
	 * @param requestsPerSecond number of requests per second
	 * @return benchmark result as BenchmarkStats
	 */
	private static BenchmarkStats performBenchmark(String url, Integer concurrency, Integer threads, Integer seconds,
										Integer requestsPerSecond) {

		try {
			BenchmarkCollector collector = new BenchmarkCollector();
			String cmd = BenchmarkCommandUtility.buildBenchmarkCommand(url, concurrency, threads, seconds,
					requestsPerSecond);

			Process process = buildCommand(cmd).start();

			StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), collector::parseAndCollect);
			ExecutorService executorService = Executors.newSingleThreadExecutor();
			executorService.submit(outputGobbler);

			if (process.waitFor() != 0) {
				System.err.println("Could not perform benchmark!");
				return null;
			}

			process.destroy();
			executorService.shutdown();

			return collector.getResult();

		} catch (InterruptedException | IOException e) {
			System.err.println("Could not perform benchmark: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Measure a single http request latency (necessary for cold start tests)
	 * @param targetUrl url to test
	 * @return latency in milliseconds
	 */
	private static long measureHttpLatency(String targetUrl) {

		HttpURLConnection connection = null;
		InputStream inputStream;
		BufferedReader reader;

		try {

			long startTime = System.currentTimeMillis();

			// create connection
			URL url = new URL(targetUrl);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setUseCaches(false);
			connection.setDoOutput(true);

			inputStream = connection.getInputStream();
			reader = new BufferedReader(new InputStreamReader(inputStream));
			//noinspection StatementWithEmptyBody
			while (reader.readLine() != null) {}
			long latency = System.currentTimeMillis() - startTime;
			inputStream.close();
			reader.close();
			return latency;


		} catch (IOException e) {
			System.err.println("Could not perform HTTP request: " + e.getMessage());
			return -1;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	/**
	 * Collects url of both serverless functions and compositions
	 * @return list of FunctionalityURL for both serverless functions and compositions
	 */
	private static List<FunctionalityURL> extractUrls() {
		List<FunctionalityURL> total = new ArrayList<>();

		List<FunctionalityURL> functions = FunctionsRepositoryDAO.getUrls();
		if (functions == null) {
			System.err.println("Could not perform benchmarks on functions");
		} else {
			total.addAll(functions);
		}
		List<FunctionalityURL> machines = CompositionRepositoryDAO.getUrls();
		if (machines == null) {
			System.err.println("Could not perform benchmarks on state machines");
		} else {
			total.addAll(machines);
		}

		return total;
	}

	/**
	 * Perform cold start benchmarks
	 * @param iterations number of test
	 */
	@Deprecated
	public static void performColdStartBenchmark(int iterations) {
		System.out.println("\n" + "\u001B[33m" +
				"Starting cold start benchmarks...\nFrom this moment on please make sure no one else is invoking " +
				"your functions.\n" + "Estimated time: approximately " +
				(((COLD_START_SLEEP_INTERVAL_MS/1000)*iterations)/60)/60 + " hours" + "\u001B[0m" + "\n");

		List<FunctionalityURL> total = extractUrls();
		if (total.isEmpty()) {
			System.err.println("Could not perform benchmarks");
			return;
		}

		ArrayList<Thread> threads = new ArrayList<>();
		ColdTestRunner runner;
		Thread t;

		for (FunctionalityURL url : total) {
			runner = new ColdTestRunner(url, iterations, COLD_START_SLEEP_INTERVAL_MS);
			t = new Thread(runner);
			threads.add(t);
			t.start();
		}
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException ignored) {}
		}

		System.out.println("\u001B[32m" + "Cold start benchmark completed!" + "\u001B[0m");
	}

	/**
	 * Performs load benchmark
	 * @param concurrency number of concurrent requests
	 * @param threadNum number of threads
	 * @param seconds test duration
	 * @param requestsPerSecond number of requests per second
	 */
	@Deprecated
	public static void performLoadTest(Integer concurrency, Integer threadNum, Integer seconds,
									   Integer requestsPerSecond) {

		System.out.println("\n" + "\u001B[33m" +
				"Starting load benchmarks..." +
				"\u001B[0m" + "\n");

		List<FunctionalityURL> total = extractUrls();
		if (total.isEmpty()) {
			System.err.println("Could not perform benchmarks");
			return;
		}

		ArrayList<Thread> threads = new ArrayList<>();
		LoadTestRunner runner;
		Thread t;

		for (FunctionalityURL url : total) {
			runner = new LoadTestRunner(url, concurrency, threadNum, seconds, requestsPerSecond);
			t = new Thread(runner);
			threads.add(t);
			t.start();
		}
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException ignored) {}
		}

		System.out.println("\u001B[32m" + "Load benchmark completed!" + "\u001B[0m");
	}

	@Deprecated
	private static class LoadTestRunner implements Runnable {

		private final FunctionalityURL function;
		private final Integer concurrency;
		private final Integer threads;
		private final Integer seconds;
		private final Integer requestsPerSecond;


		public LoadTestRunner(FunctionalityURL function, Integer concurrency, Integer threads, Integer seconds,
							  Integer requestsPerSecond) {
			this.function = function;
			this.concurrency = concurrency;
			this.threads = threads;
			this.seconds = seconds;
			this.requestsPerSecond = requestsPerSecond;
		}

		@Override
		public void run() {

			BenchmarkStats google;
			BenchmarkStats amazon;

			if (function.getGoogleUrl() == null) {
				google = null;
			} else {
				google = performBenchmark(function.getGoogleUrl(), concurrency, threads, seconds, requestsPerSecond);
			}

			if (function.getAmazonUrl() == null) {
				amazon = null;
			} else {
				amazon = performBenchmark(function.getAmazonUrl(), concurrency, threads, seconds, requestsPerSecond);
			}

			if (google != null) {
				System.out.println("avg latency google = " + google.getAvgLatency());
				if (InfluxClient.insertLoadPoints(function.getName(), "google", google,
						System.currentTimeMillis())) {
					System.out.println("\u001B[32m" + "Persisted google benchmark for: " + function.getName() +
							"\u001B[0m");
				}
			}
			if (amazon != null) {
				System.out.println("avg latency amazon = " + amazon.getAvgLatency());
				if (InfluxClient.insertLoadPoints(function.getName(), "amazon", amazon,
						System.currentTimeMillis())) {
					System.out.println("\u001B[32m" + "Persisted amazon benchmark for: " + function.getName() +
							"\u001B[0m");
				}
			}

		}
	}

	@Deprecated
	private static class ColdTestRunner implements Runnable {

		private final FunctionalityURL function;
		private final Integer iterations;
		private final Integer sleepMs;

		public ColdTestRunner(FunctionalityURL function, Integer iterations, Integer sleepMs) {
			this.function = function;
			this.iterations = iterations;
			this.sleepMs = sleepMs;
		}

		@Override
		public void run() {

			long googleLatency;
			long amazonLatency;

			for (int i = 0; i < iterations; i++) {
				try {
					// time to let provider deallocate resources for function execution
					Thread.sleep(sleepMs);
					googleLatency = measureHttpLatency(function.getGoogleUrl());
					amazonLatency = measureHttpLatency(function.getAmazonUrl());
					if (googleLatency != -1) {
						// influx persist
						if (InfluxClient.insertColdPoint(function.getName(), "google", googleLatency,
								System.currentTimeMillis())) {
							System.out.println("\u001B[32m" + "Persisted google cold start benchmark for: " +
									function.getName() + "\u001B[0m");
						}
					}
					if (amazonLatency != -1) {
						// influx persist
						if (InfluxClient.insertColdPoint(function.getName(), "amazon", amazonLatency,
								System.currentTimeMillis())) {
							System.out.println("\u001B[32m" + "Persisted amazon cold start benchmark for: " +
									function.getName() + "\u001B[0m");
						}
					}
				} catch (InterruptedException ignored) {
					return;
				}
			}

		}
	}
}
