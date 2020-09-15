package cmd;

public abstract class CommandUtility {

	protected static final String SEP = " ";
	protected static final String FUNCTIONS_DIR = "/mnt/functionalities";
	public static boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().contains("windows");
	}
	public static String getPathSep() {
		return isWindows() ? "\\" : "/";
	}
}
