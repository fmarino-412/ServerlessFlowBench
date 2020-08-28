package cmd;

public class ReplyCollector {

	private static ReplyCollector singletonInstance = null;
	private String result;

	private ReplyCollector() {
		result = "";
	}

	public static ReplyCollector getInstance() {
		if (singletonInstance == null) {
			singletonInstance = new ReplyCollector();
		}
		return singletonInstance;
	}

	public void collectResult(String result) {
		this.result = this.result + result;
	}

	public String getResult() {
		// get final result and reset collector
		singletonInstance = null;
		return result.replace("\n", "");
	}


}
