package cmd;

/**
 * Abstract class representing CLI command build common data ond operations
 */
public abstract class CommandUtility {

	// CLI separator character
	protected static final String SEP = " ";

	// Docker container local folder for external data
	protected static final String FUNCTIONALITIES_DIR = "/mnt/functionalities";

	/**
	 * Tells whether the host system is a Windows OS based or not
	 * @return true if Windows OS, false elsewhere
	 */
	public static boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().contains("windows");
	}

	/**
	 * Returns path separation character basing on the host OS
	 * @return correct path separation character
	 */
	public static String getPathSep() {
		return isWindows() ? "\\" : "/";
	}
}
