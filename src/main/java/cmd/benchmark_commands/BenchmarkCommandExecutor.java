package cmd.benchmark_commands;

import cmd.CommandExecutor;
import cmd.StreamGobbler;
import cmd.benchmark_commands.output_parsing.BenchmarkCollector;
import cmd.benchmark_commands.output_parsing.BenchmarkStats;
import database.FunctionURL;
import database.FunctionsRepositoryDAO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BenchmarkCommandExecutor extends CommandExecutor {

	private static BenchmarkStats performBenchmark(String url, Integer concurrency, Integer threads, Integer seconds,
										Integer requestsPerSecond) {

		try {
			String cmd = BenchmarkCommandUtility.buildBenchmarkCommand(url, concurrency, threads, seconds,
					requestsPerSecond);

			Process process = buildCommand(cmd).start();

			StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), a ->
					BenchmarkCollector.getInstance().parseAndCollect(a));
			ExecutorService executorService = Executors.newSingleThreadExecutor();
			executorService.submit(outputGobbler);

			int exitCode = process.waitFor();
			assert exitCode == 0;

			process.destroy();
			executorService.shutdown();

			return BenchmarkCollector.getInstance().getResult();

		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void performBenchmarks(Integer concurrency, Integer threads, Integer seconds,
									   Integer requestsPerSecond) {

		System.out.println("\n" + "\u001B[33m" +
				"Starting benchmarks..." +
				"\u001B[0m" + "\n");

		ArrayList<FunctionURL> functions = FunctionsRepositoryDAO.getUrls();
		if (functions == null) {
			System.err.println("Could not perform benchmarks");
			return;
		}
		BenchmarkStats google;
		BenchmarkStats amazon;
		for (FunctionURL url : functions) {

			System.out.println("\n" + url.getFunctionName());

			if (url.getGoogleUrl() == null) {
				google = null;
			} else {
				google = performBenchmark(url.getGoogleUrl(), concurrency, threads, seconds, requestsPerSecond);
			}

			if (url.getAmazonUrl() == null) {
				amazon = null;
			} else {
				amazon = performBenchmark(url.getAmazonUrl(), concurrency, threads, seconds, requestsPerSecond);
			}

			System.out.println("---------------------------------------");
			if (google != null) {
				System.out.println("avg latency google = " + google.getAvgLatency());
			}
			if (amazon != null) {
				System.out.println("avg latency amazon = " + amazon.getAvgLatency());
			}
		}

		// TODO: InfluxDB?
	}
}