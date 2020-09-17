package cmd.functionality_commands.output_parsing;

/**
 * Collector for console command executions text
 */
public class ReplyCollector {

	// string containing execution text
	private String result;

	/**
	 * Default constructor
	 */
	public ReplyCollector() {
		this.result = "";
	}

	/**
	 * Append new slice of result to the current one
	 * @param result new text to append
	 */
	public void collectResult(String result) {
		this.result = this.result + result;
	}

	/**
	 * Get final result
	 * @return string containing execution text without newlines
	 */
	public String getResult() {
		return result.replace("\n", "");
	}

}
