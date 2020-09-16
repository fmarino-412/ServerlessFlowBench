package databases.mysql;

import cmd.functionality_commands.security.GoogleAuthClient;

public class FunctionalityURL {
	private final String name;
	private String googleUrl;
	private String amazonUrl;
	private final boolean needsGoogleAuth;

	public FunctionalityURL(String name) {
		this.name = name;
		this.googleUrl = null;
		this.amazonUrl = null;
		needsGoogleAuth = false;
	}

	public FunctionalityURL(String name, boolean needsGoogleAuth) {
		this.name = name;
		this.googleUrl = null;
		this.amazonUrl = null;
		this.needsGoogleAuth = needsGoogleAuth;
	}

	public String getName() {
		return name;
	}

	public String getGoogleUrl() {
		if (needsGoogleAuth) {
			// returns always current token version
			return googleUrl + GoogleAuthClient.getInstance().getUrlToken();
		} else {
			return googleUrl;
		}
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
