package databases.mysql;

import cmd.functionality_commands.security.GoogleAuthClient;

/**
 * Collection of functionality URLs (one per provider)
 */
public class FunctionalityURL {
	/**
	 * Information
	 */
	private final String name;
	private String googleUrl;
	private String amazonUrl;

	/**
	 * Tells whether the google url needs an authentication token
	 */
	private final boolean needsGoogleAuth;

	/**
	 * Basic constructor
	 * @param name functionality name
	 */
	public FunctionalityURL(String name) {
		this.name = name;
		this.googleUrl = null;
		this.amazonUrl = null;
		needsGoogleAuth = false;
	}

	/**
	 * Basic constructor with authentication info
	 * @param name functionality name
	 * @param needsGoogleAuth true if google url needs an authentication token
	 */
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
