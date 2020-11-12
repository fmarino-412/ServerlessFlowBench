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
	public static final String PYTHON_3_RUNTIME = "python:3";
	public static final String GO_1_RUNTIME = "go:1.11";
	public static final String JAVA_8_RUNTIME = "java:8";
	public static final String NODE_10_RUNTIME = "nodejs:10";

	/**
	 * Docker utils
	 */
	private static final String PREAMBLE = "docker" + SEP + "run" + SEP + "--rm" + SEP + "-i";
	private static final String OPENWHISK_CLI = "openwhisk/ow-utils:63a5498";
	@SuppressWarnings("SpellCheckingInspection")
	private static final String OPENWHISK_AUTH_CLOSURE = "--apihost" + SEP +
			PropertiesManager.getInstance().getProperty(PropertiesManager.OPENWHISK_HOST) + SEP + "--auth" + SEP +
			PropertiesManager.getInstance().getProperty(PropertiesManager.OPENWHISK_AUTH);

	/**
	 * Wsk command preamble
	 */
	private static final String WSK = "wsk" + SEP + "-i";

	/**
	 * Wsk actions
	 */
	private static final String ACTION = WSK + SEP + "action";
	private static final String CREATE_ACTION = ACTION + SEP + "create";
	private static final String GET_ACTION = ACTION + SEP + "get";
	private static final String DELETE_ACTION = ACTION + SEP + "delete";



	/**
	 * OpenWhisk CLI Docker image getter
	 * @return CLI Docker image name
	 */
	public static String getCli() {
		return OPENWHISK_CLI;
	}

	/**
	 * Builds OpenWhisk CLI command for new action creation
	 * @param actionName name of the action
	 * @param runtime runtime of the new function
	 * @param timeout timeout in seconds of function to deploy
	 * @param memory memory amount in megabytes of function to deploy
	 * @param zipFolder path of the folder containing the zip package with the function
	 * @param zipName name of the zip package with the function
	 * @return command as string
	 */
	public static String buildActionDeployCommand(String actionName, String runtime, String entryPoint, Integer timeout,
												  Integer memory, String zipFolder, String zipName) {

		return 	// command beginning
				PREAMBLE + SEP +
						// volume attachment
						"-v" + SEP + zipFolder + ":" + FUNCTIONALITIES_DIR + SEP +
						// select docker image to use
						OPENWHISK_CLI + SEP +
						// operation define
						CREATE_ACTION + SEP +
						// parameters setting
						actionName + SEP +
						"--web" + SEP + "true" + SEP +
						"--kind" + SEP + runtime + SEP +
						"--main" + SEP + entryPoint + SEP +
						"--memory" + SEP + memory + SEP +
						"--timeout" + SEP + timeout*1000 + SEP +
						FUNCTIONALITIES_DIR + "/" + zipName + SEP +
						// configuration binding
						OPENWHISK_AUTH_CLOSURE;
	}

	/**
	 * Builds OpenWhisk CLI command for action URL retrieval
	 * @param actionName name of the action associated to the desired url
	 * @return command as string
	 */
	public static String buildActionUrlGetterCommand(String actionName) {

		return 	// command beginning
				PREAMBLE + SEP +
						// select docker image to use
						OPENWHISK_CLI + SEP +
						// operation define
						GET_ACTION + SEP +
						// parameters setting
						actionName + SEP +
						"--url" + SEP +
						// configuration binding
						OPENWHISK_AUTH_CLOSURE;
	}

	/**
	 * Builds OpenWhisk CLI command for action deletion
	 * @param actionName name of the action to delete
	 * @return command as string
	 */
	public static String buildActionDeletionCommand(String actionName) {

		return 	// command beginning
				PREAMBLE + SEP +
						// select docker image to use
						OPENWHISK_CLI + SEP +
						// operation define
						DELETE_ACTION + SEP +
						// parameters setting
						actionName + SEP +
						// configuration binding
						OPENWHISK_AUTH_CLOSURE;
	}

	/**
	 * Translates function name and runtime to a string that will work as function identifier
	 * @param functionalityName name of the function to apply id
	 * @param runtime to translate
	 * @return function identifier
	 * @throws IllegalNameException if functionalityName is an illegal name
	 */
	@SuppressWarnings("DuplicatedCode")
	public static String applyRuntimeId(String functionalityName, String runtime) throws IllegalNameException {

		if (needsRuntimeId(functionalityName)) {
			switch (runtime) {
				case PYTHON_3_RUNTIME:
					return functionalityName + PYTHON_ID;
				case JAVA_8_RUNTIME:
					return functionalityName + JAVA_ID;
				case NODE_10_RUNTIME:
					return functionalityName + NODE_ID;
				case GO_1_RUNTIME:
					return functionalityName + GO_1_RUNTIME;
				default:
					return functionalityName + OTHERS_ID;
			}
		} else {
			return functionalityName;
		}
	}
}
