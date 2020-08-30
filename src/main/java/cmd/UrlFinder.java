package cmd;

public class UrlFinder {

	private static UrlFinder singletonInstance = null;
	private String result;

	private UrlFinder() {
		result = "";
	}

	public static UrlFinder getInstance() {
		if (singletonInstance == null) {
			singletonInstance = new UrlFinder();
		}
		return singletonInstance;
	}

	/**
	 * Searches for the URL of the new function deployed on Google Cloud Platform
	 * @param string output line to perform search on
	 */
	public void findGoogleCloudFunctionsUrl(String string) {
		if (string.contains("url: ")) {
			String url = string.substring(string.indexOf("url: "));
			result = url.replace("url: ", "");
		}
	}

	public String getResult() {
		// get final result and reset finder
		singletonInstance = null;
		return result;
	}
}
