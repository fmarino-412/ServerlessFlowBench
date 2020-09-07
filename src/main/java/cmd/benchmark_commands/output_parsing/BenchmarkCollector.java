package cmd.benchmark_commands.output_parsing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BenchmarkCollector {

	private final BenchmarkStats result;

	private final static String avgDevRegex = "(#\\[Mean\\s+=\\s+)([0-9]+.[0-9]+)" +
			"(,\\s+StdDeviation\\s+=\\s+)([0-9]+.[0-9]+)(])";
	private final static String maxRegex = "(#\\[Max\\s+=\\s+)([0-9]+.[0-9]+)(,\\s+Total count\\s+=\\s+[0-9]+])";
	private final static String requestsRegex = "(Requests/sec:\\s+)([0-9]+\\.*[0-9]*)";
	private final static String transferRegex = "(Transfer/sec:\\s+)([0-9]+\\.*[0-9]*)([a-zA-Z]+)";


	public BenchmarkCollector() {
		this.result = new BenchmarkStats();
	}

	public void parseAndCollect(String line) {

		Pattern pattern;
		Matcher matcher;

		if (line.contains("#[Mean")) {

			pattern = Pattern.compile(avgDevRegex);
			matcher = pattern.matcher(line);
			if (matcher.find()) {
				this.result.setAvgLatency(Double.valueOf(matcher.group(2)));
				this.result.setStdDevLatency(Double.valueOf(matcher.group(4)));
			}

		} else if (line.contains("#[Max")) {

			pattern = Pattern.compile(maxRegex);
			matcher = pattern.matcher(line);
			if (matcher.find()) {
				this.result.setMaxLatency(Double.valueOf(matcher.group(2)));
			}

		} else if (line.contains("Requests/sec")) {

			pattern = Pattern.compile(requestsRegex);
			matcher = pattern.matcher(line);
			if (matcher.find()) {
				this.result.setRequestsThroughput(Double.valueOf(matcher.group(2)));
			}

		} else if (line.contains("Transfer/sec")) {

			pattern = Pattern.compile(transferRegex);
			matcher = pattern.matcher(line);
			if (matcher.find()) {
				double value = Double.parseDouble(matcher.group(2));
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

	public BenchmarkStats getResult() {
		// get final result
		return result;
	}
}
