package cmd.functionality_commands.output_parsing;

/**
 * Collector for Google CLI deployment urls
 */
public class UrlFinder {

	// string containing url
	private String result;

	public UrlFinder() {
		result = "";
	}

	/**
	 * Searches for the URL inside the passed string
	 * @param string output line to perform search on
	 */
	public void findGoogleCloudFunctionsUrl(String string) {
		if (string.contains("url: ")) {
			// exclude un-needed information and blank spaces
			String url = string.substring(string.indexOf("url: "));
			// remove preamble
			result = url.replace("url: ", "");
		}
	}

	/**
	 * Get final result
	 * @return string containing the url
	 */
	public String getResult() {
		return result;
	}
}
