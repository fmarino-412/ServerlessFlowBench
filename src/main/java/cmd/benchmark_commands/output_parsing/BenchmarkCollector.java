package cmd.benchmark_commands.output_parsing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A collector and parser for mrk2's benchmark results
 */
public class BenchmarkCollector {

	private final BenchmarkStats result;

	// regex to identify latency average and standard deviation
	private final static String avgDevRegex = "(#\\[Mean\\s+=\\s+)([0-9]+.[0-9]+)" +
			"(,\\s+StdDeviation\\s+=\\s+)([0-9]+.[0-9]+)(])";
	// regex to identify latency maximum measurement
	private final static String maxRegex = "(#\\[Max\\s+=\\s+)([0-9]+.[0-9]+)(,\\s+Total count\\s+=\\s+[0-9]+])";
	// regex to identify throughput as requests per second
	private final static String requestsRegex = "(Requests/sec:\\s+)([0-9]+\\.*[0-9]*)";
	// regex to identify throughput as kilobytes per second
	private final static String transferRegex = "(Transfer/sec:\\s+)([0-9]+\\.*[0-9]*)([a-zA-Z]+)";


	/**
	 * Default constructor
	 */
	public BenchmarkCollector() {
		this.result = new BenchmarkStats();
	}

	/**
	 * Parses a result line and saves data
	 * @param line output to analyze
	 */
	public void parseAndCollect(String line) {

		Pattern pattern;
		Matcher matcher;

		if (line.contains("#[Mean")) {

			// search for average and standard deviation
			pattern = Pattern.compile(avgDevRegex);
			matcher = pattern.matcher(line);
			if (matcher.find()) {
				this.result.setAvgLatency(Double.valueOf(matcher.group(2)));
				this.result.setStdDevLatency(Double.valueOf(matcher.group(4)));
			}

		} else if (line.contains("#[Max")) {

			// search for max value
			pattern = Pattern.compile(maxRegex);
			matcher = pattern.matcher(line);
			if (matcher.find()) {
				this.result.setMaxLatency(Double.valueOf(matcher.group(2)));
			}

		} else if (line.contains("Requests/sec")) {

			// search for requests/sec throughput
			pattern = Pattern.compile(requestsRegex);
			matcher = pattern.matcher(line);
			if (matcher.find()) {
				this.result.setRequestsThroughput(Double.valueOf(matcher.group(2)));
			}

		} else if (line.contains("Transfer/sec")) {

			// search for kb/sec throughput
			pattern = Pattern.compile(transferRegex);
			matcher = pattern.matcher(line);
			if (matcher.find()) {
				double value = Double.parseDouble(matcher.group(2));

				// apply value conversion to store as kb/sec
				String unit = matcher.group(3);
				switch (unit) {
					case "B":
						this.result.setTransferThroughput(value / 1000);
						break;
					case "KB":
						this.result.setTransferThroughput(value);
						break;
					case "MB":
						this.result.setTransferThroughput(value * 1000);
						break;
					case "GB":
						this.result.setTransferThroughput(value * 1000 * 1000);
						break;
				}
			}

		}
	}

	/**
	 * Get final result
	 * @return BenchmarkStats containing every parsed and collected info
	 */
	public BenchmarkStats getResult() {
		return result;
	}
}
