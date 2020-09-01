package database;

public class FunctionURL {
	private final String functionName;
	private String googleUrl;
	private String amazonUrl;

	public FunctionURL(String functionName) {
		this.functionName = functionName;
		this.googleUrl = null;
		this.amazonUrl = null;
	}

	public String getFunctionName() {
		return functionName;
	}

	public String getGoogleUrl() {
		return googleUrl;
	}

	public void setGoogleUrl(String googleUrl) {
		this.googleUrl = googleUrl;
	}

	public String getAmazonUrl() {
		return amazonUrl;
	}

	public void setAmazonUrl(String amazonUrl) {
		this.amazonUrl = amazonUrl;
	}
}
