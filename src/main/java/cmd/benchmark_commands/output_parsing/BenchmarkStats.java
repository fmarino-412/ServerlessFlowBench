package cmd.benchmark_commands.output_parsing;

public class BenchmarkStats {
	private Double avgLatency = null;
	private Double stdDevLatency = null;
	private Double maxLatency = null;

	private Double requestsThroughput = null;

	// in kilobytes
	private Double transferThroughput = null;

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
