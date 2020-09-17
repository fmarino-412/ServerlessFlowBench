package cmd.benchmark_commands.output_parsing;

/**
 * A collection of mrk2's benchmark results
 */
public class BenchmarkStats {
	// latency average
	private Double avgLatency;
	// latency standard deviation
	private Double stdDevLatency;
	// latency maximum measurement
	private Double maxLatency;
	// throughput as requests per second
	private Double requestsThroughput;
	// throughput as kilobytes per second
	private Double transferThroughput;

	/**
	 * Default constructor
	 */
	public BenchmarkStats() {
		this.avgLatency = null;
		this.stdDevLatency = null;
		this.maxLatency = null;
		this.requestsThroughput = null;
		this.transferThroughput = null;
	}

	public Double getAvgLatency() {
		return avgLatency;
	}

	public void setAvgLatency(Double avgLatency) {
		this.avgLatency = avgLatency;
	}

	public Double getStdDevLatency() {
		return stdDevLatency;
	}

	public void setStdDevLatency(Double stdDevLatency) {
		this.stdDevLatency = stdDevLatency;
	}

	public Double getMaxLatency() {
		return maxLatency;
	}

	public void setMaxLatency(Double maxLatency) {
		this.maxLatency = maxLatency;
	}

	public Double getRequestsThroughput() {
		return requestsThroughput;
	}

	public void setRequestsThroughput(Double requestsThroughput) {
		this.requestsThroughput = requestsThroughput;
	}

	public Double getTransferThroughput() {
		return transferThroughput;
	}

	public void setTransferThroughput(Double transferThroughput) {
		this.transferThroughput = transferThroughput;
	}
}
