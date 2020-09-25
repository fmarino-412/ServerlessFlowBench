package cmd;

import cmd.functionality_commands.GoogleCommandUtility;
import cmd.functionality_commands.IllegalNameException;

/**
 * Abstract class representing CLI command build common data ond operations
 */
public abstract class CommandUtility {

	// CLI separator character
	protected static final String SEP = " ";

	// Docker container local folder for external data
	protected static final String FUNCTIONALITIES_DIR = "/mnt/functionalities";

	// runtime IDs
	protected static final String PYTHON_ID = "__python";
	protected static final String JAVA_ID = "__java";
	protected static final String NODE_ID = "__node";
	protected static final String GO_ID = "__go";
	protected static final String OTHERS_ID = "__not-supported";

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

	/**
	 * Tells whether a runtime identifier needs to be placed in function name
	 * This has been necessary due to impossibility to deploy function with same name but different runtime on
	 * serverless services!
	 * @param functionalityName name to verify id is in
	 * @return true if id is in name, false elsewhere
	 * @throws IllegalNameException if functionalityName is an illegal name
	 */
	protected static boolean needsRuntimeId(String functionalityName) throws IllegalNameException {
		boolean hasId = functionalityName.contains(PYTHON_ID) ||
				functionalityName.contains(JAVA_ID) ||
				functionalityName.contains(NODE_ID) ||
				functionalityName.contains(GO_ID) ||
				functionalityName.contains(OTHERS_ID);

		if ((!hasId && functionalityName.contains("__")) || countOccurrences(functionalityName, "__") > 1) {
			throw new IllegalNameException("Original name cannot contain '__'");
		}

		return !hasId;
	}

	/**
	 * Counts occurrences of a sub-string inside of a string
	 * @param string string for occurrences count
	 * @param subString string to find
	 * @return number of occurrences (0 if not present)
	 */
	private static int countOccurrences(String string, String subString) {
		int lastIndex = 0;
		int count = 0;

		while (lastIndex != -1) {
			lastIndex = string.indexOf(subString, lastIndex);

			if (lastIndex != -1) {
				count ++;
				lastIndex += subString.length();
			}
		}

		return count;
	}
}
