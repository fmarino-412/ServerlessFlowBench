package cmd.functionality_commands.output_parsing;

public class UrlFinder {

	private String result;

	public UrlFinder() {
		result = "";
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
		// get final result
		return result;
	}
}
