package cmd.benchmark_commands;

import cmd.CommandExecutor;
import cmd.StreamGobbler;
import cmd.benchmark_commands.output_parsing.BenchmarkCollector;
import cmd.benchmark_commands.output_parsing.BenchmarkStats;
import databases.influx.InfluxClient;
import databases.mysql.FunctionURL;
import databases.mysql.FunctionsRepositoryDAO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BenchmarkCommandExecutor extends CommandExecutor {

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

	// TODO: ADD COLD START BENCHMARK WITH IF CONDITION ON TIME

	// Load test
	public static void performBenchmarks(Integer concurrency, Integer threadNum, Integer seconds,
									   Integer requestsPerSecond) {

		System.out.println("\n" + "\u001B[33m" +
				"Starting benchmarks..." +
				"\u001B[0m" + "\n");

		ArrayList<FunctionURL> functions = FunctionsRepositoryDAO.getUrls();
		if (functions == null) {
			System.err.println("Could not perform benchmarks");
			return;
		}
		ArrayList<Thread> threads = new ArrayList<>();
		BenchmarkRunner runner;
		Thread t;

		for (FunctionURL url : functions) {
			runner = new BenchmarkRunner(url, concurrency, threadNum, seconds, requestsPerSecond);
			t = new Thread(runner);
			threads.add(t);
			t.start();
		}
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException ignored) {}
		}

		System.out.println("\u001B[32m" + "Benchmark completed!" + "\u001B[0m");
	}

	private static class BenchmarkRunner implements Runnable {

		private final FunctionURL function;
		private final Integer concurrency;
		private final Integer threads;
		private final Integer seconds;
		private final Integer requestsPerSecond;


		public BenchmarkRunner(FunctionURL function, Integer concurrency, Integer threads, Integer seconds,
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

			long time = System.currentTimeMillis();
			if (google != null) {
				System.out.println("avg latency google = " + google.getAvgLatency());
				if (InfluxClient.insertPoints(function.getFunctionName(), "google", google, time)) {
					System.out.println("\u001B[32m" + "Persisted google benchmark for: " + function.getFunctionName() +
							"\u001B[0m");
				}
			}
			if (amazon != null) {
				System.out.println("avg latency amazon = " + amazon.getAvgLatency());
				if (InfluxClient.insertPoints(function.getFunctionName(), "amazon", amazon, time + 1)) {
					System.out.println("\u001B[32m" + "Persisted amazon benchmark for: " + function.getFunctionName() +
							"\u001B[0m");
				}
			}

		}
	}
}
