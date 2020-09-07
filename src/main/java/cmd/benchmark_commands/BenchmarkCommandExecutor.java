package cmd.benchmark_commands;

import cmd.CommandExecutor;
import cmd.StreamGobbler;
import cmd.benchmark_commands.output_parsing.BenchmarkCollector;
import cmd.benchmark_commands.output_parsing.BenchmarkStats;
import databases.influx.InfluxClient;
import databases.mysql.FunctionURL;
import databases.mysql.FunctionsRepositoryDAO;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BenchmarkCommandExecutor extends CommandExecutor {

	private static final int COLD_START_SLEEP_INTERVAL_MS = 30 * 60 * 1000;

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

			int exitCode = process.waitFor();
			assert exitCode == 0;

			process.destroy();
			executorService.shutdown();

			return collector.getResult();

		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
			return null;
		}
	}

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

	// Cold start benchmark
	public static void performColdStartBenchmark(int iterations) {
		System.out.println("\n" + "\u001B[33m" +
				"Starting cold start benchmarks...\nFrom this moment on please make sure no one else is invoking " +
				"your functions.\n" + "Estimated time: approximately " +
				(((COLD_START_SLEEP_INTERVAL_MS/1000)*iterations)/60)/60 + " hours" + "\u001B[0m" + "\n");

		ArrayList<FunctionURL> functions = FunctionsRepositoryDAO.getUrls();
		if (functions == null) {
			System.err.println("Could not perform benchmarks");
			return;
		}

		ArrayList<Thread> threads = new ArrayList<>();
		ColdTestRunner runner;
		Thread t;

		for (FunctionURL url : functions) {
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

	// Load test
	public static void performLoadTest(Integer concurrency, Integer threadNum, Integer seconds,
									   Integer requestsPerSecond) {

		System.out.println("\n" + "\u001B[33m" +
				"Starting load benchmarks..." +
				"\u001B[0m" + "\n");

		ArrayList<FunctionURL> functions = FunctionsRepositoryDAO.getUrls();
		if (functions == null) {
			System.err.println("Could not perform benchmarks");
			return;
		}
		ArrayList<Thread> threads = new ArrayList<>();
		LoadTestRunner runner;
		Thread t;

		for (FunctionURL url : functions) {
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

	private static class LoadTestRunner implements Runnable {

		private final FunctionURL function;
		private final Integer concurrency;
		private final Integer threads;
		private final Integer seconds;
		private final Integer requestsPerSecond;


		public LoadTestRunner(FunctionURL function, Integer concurrency, Integer threads, Integer seconds,
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
				if (InfluxClient.insertLoadPoints(function.getFunctionName(), "google", google,
						System.currentTimeMillis())) {
					System.out.println("\u001B[32m" + "Persisted google benchmark for: " + function.getFunctionName() +
							"\u001B[0m");
				}
			}
			if (amazon != null) {
				System.out.println("avg latency amazon = " + amazon.getAvgLatency());
				if (InfluxClient.insertLoadPoints(function.getFunctionName(), "amazon", amazon,
						System.currentTimeMillis())) {
					System.out.println("\u001B[32m" + "Persisted amazon benchmark for: " + function.getFunctionName() +
							"\u001B[0m");
				}
			}

		}
	}

	private static class ColdTestRunner implements Runnable {

		private final FunctionURL function;
		private final Integer iterations;
		private final Integer sleepMs;

		public ColdTestRunner(FunctionURL function, Integer iterations, Integer sleepMs) {
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
						if (InfluxClient.insertColdPoint(function.getFunctionName(), "google", googleLatency,
								System.currentTimeMillis())) {
							System.out.println("\u001B[32m" + "Persisted google cold start benchmark for: " +
									function.getFunctionName() + "\u001B[0m");
						}
					}
					if (amazonLatency != -1) {
						// influx persist
						if (InfluxClient.insertColdPoint(function.getFunctionName(), "amazon", amazonLatency,
								System.currentTimeMillis())) {
							System.out.println("\u001B[32m" + "Persisted amazon cold start benchmark for: " +
									function.getFunctionName() + "\u001B[0m");
						}
					}
				} catch (InterruptedException ignored) {
					return;
				}
			}

		}
	}
}
