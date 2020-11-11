package cmd.functionality_commands;

import cmd.CommandUtility;
import utility.PropertiesManager;

/**
 * Utility for OpenWhisk CLI command build
 */
@SuppressWarnings({"unused", "RedundantSuppression"})
public class OpenWhiskCommandUtility extends CommandUtility {

	/**
	 * Public constant variables: Languages
	 */
	public static final String PYTHON_3_RUNTIME = "python@3";
	public static final String GO_1_RUNTIME = "go:1.11";
	public static final String JAVA_8_RUNTIME = "java8";
	public static final String NODE_10_RUNTIME = "nodejs@10";

	/**
	 * Docker utils
	 */
	private static final String PREAMBLE = "docker" + SEP + "run" + SEP + "--rm" + SEP + "-i";
	private static final String OPENWHISK_CLI = "openwhisk/ow-utils:63a5498";
	private static final String GOOGLE_CONFIG_BIND = "-v" + SEP +
			PropertiesManager.getInstance().getProperty(PropertiesManager.OPENWHISK_AUTH_CONFIG) + ":" + "/root";



	/**
	 * OpenWhisk CLI Docker image getter
	 * @return CLI Docker image name
	 */
	public static String getCli() {
		return OPENWHISK_CLI;
	}
}
