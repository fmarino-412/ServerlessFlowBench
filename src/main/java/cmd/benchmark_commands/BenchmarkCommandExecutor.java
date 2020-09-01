package cmd.benchmark_commands;

import cmd.CommandExecutor;
import cmd.StreamGobbler;
import cmd.benchmark_commands.output_parsing.BenchmarkCollector;
import cmd.benchmark_commands.output_parsing.BenchmarkStats;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BenchmarkCommandExecutor extends CommandExecutor {

	public static void performBenchmark(String url, Integer concurrency, Integer threads, Integer seconds,
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

			BenchmarkStats stats = BenchmarkCollector.getInstance().getResult();
			// TODO: DECIDE HOW TO PROCEED

		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
	}
}
