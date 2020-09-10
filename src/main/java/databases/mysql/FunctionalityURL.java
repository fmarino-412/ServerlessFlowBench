package databases.mysql;

public class FunctionalityURL {
	private final String name;
	private String googleUrl;
	private String amazonUrl;

	public FunctionalityURL(String name) {
		this.name = name;
		this.googleUrl = null;
		this.amazonUrl = null;
	}

	public String getName() {
		return name;
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
