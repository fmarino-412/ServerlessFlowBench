package cmd;

public class UrlFinder {

	/**
	 * Searches for the URL of the new function deployed on Google Cloud Platform
	 * @param string output line to perform search on
	 */
	public static void findGoogleCloudFunctionsUrl(String string) {
		if (string.contains("url: ")) {
			String url = string.substring(string.indexOf("url: "));
			System.out.println(url.replace("url: ", ""));
		}
	}
}
