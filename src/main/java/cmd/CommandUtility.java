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
				functionalityName.contains(OTHERS_ID);

		if (!hasId && functionalityName.contains("__")) {
			throw new IllegalNameException("Original name cannot contain '__'");
		}

		return !hasId;
	}
}
