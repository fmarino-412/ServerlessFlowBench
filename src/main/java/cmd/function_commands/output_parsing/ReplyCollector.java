package cmd.function_commands.output_parsing;

public class ReplyCollector {

	private String result;

	public ReplyCollector() {
		this.result = "";
	}

	public void collectResult(String result) {
		this.result = this.result + result;
	}

	public String getResult() {
		// get final result
		return result.replace("\n", "");
	}

}
