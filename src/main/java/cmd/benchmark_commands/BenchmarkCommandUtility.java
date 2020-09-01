package cmd.benchmark_commands;

import cmd.CommandUtility;

public class BenchmarkCommandUtility extends CommandUtility {

	private static final String WRK2_IMG = "bschitter/alpine-with-wrk2:0.1";
	private static final String PREAMBLE = "docker" + SEP + "run" + SEP + "--rm" + SEP + WRK2_IMG;

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
