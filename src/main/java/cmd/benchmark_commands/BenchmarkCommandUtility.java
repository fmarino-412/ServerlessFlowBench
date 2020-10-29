package cmd.benchmark_commands;

import cmd.CommandUtility;

/**
 * Utility for wrk2 benchmark command execution
 */
public class BenchmarkCommandUtility extends CommandUtility {

	/**
	 * Docker image and command preamble
	 */
	@SuppressWarnings("SpellCheckingInspection")
	public static final String WRK2_IMG = "bschitter/alpine-with-wrk2:0.1";
	private static final String PREAMBLE = "docker" + SEP + "run" + SEP + "--rm" + SEP + WRK2_IMG;


	/**
	 * Builds wrk2 benchmark execution command
	 * @param url url to perform benchmark on
	 * @param concurrency number of concurrent requests
	 * @param threads number of active threads
	 * @param seconds test duration
	 * @param requestsPerSecond number of requests per second
	 * @return command as string
	 */
	public static String buildBenchmarkCommand(String url, Integer concurrency, Integer threads, Integer seconds,
											   Integer requestsPerSecond) {
		return	// command beginning
				PREAMBLE + SEP +
						// operation define
						"-c" + concurrency + SEP +
						"-t" + threads + SEP +
						"-d" + seconds + "s" + SEP +
						"-R" + requestsPerSecond + SEP +
						"-L" + SEP +
						url;

	}
}
