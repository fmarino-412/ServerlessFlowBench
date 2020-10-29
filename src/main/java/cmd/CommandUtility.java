package cmd;

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
	private static final String RUNTIME_SEP = "__";
	protected static final String PYTHON_ID = RUNTIME_SEP + "python";
	protected static final String JAVA_ID = RUNTIME_SEP + "java";
	protected static final String NODE_ID = RUNTIME_SEP + "node";
	protected static final String GO_ID = RUNTIME_SEP + "go";
	protected static final String OTHERS_ID = RUNTIME_SEP + "not-supported";

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
	 * Returns runtime separation string
	 * @return separation string
	 */
	public static String getRuntimeSep() {
		return RUNTIME_SEP;
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

		if ((!hasId && functionalityName.contains(RUNTIME_SEP)) ||
				countOccurrences(functionalityName, RUNTIME_SEP) > 1) {
			throw new IllegalNameException("Original name cannot contain '" + RUNTIME_SEP + "'");
		}

		return !hasId;
	}

	/**
	 * Counts occurrences of a sub-string inside of a string
	 * @param string string for occurrences count
	 * @param subString string to find
	 * @return number of occurrences (0 if not present)
	 */
	@SuppressWarnings("SameParameterValue")
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
