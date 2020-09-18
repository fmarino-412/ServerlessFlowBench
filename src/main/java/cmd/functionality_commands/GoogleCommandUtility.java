package cmd.functionality_commands;

import cmd.CommandUtility;
import utility.PropertiesManager;

/**
 * Utility for Google Cloud CLI command build
 */
public class GoogleCommandUtility extends CommandUtility {

	/**
	 * Public constant variables
	 */
	public static final String PYTHON_3_7_RUNTIME = "python37";
	public static final String NORTH_VIRGINIA = "us-east4";
	public static final String IOWA = "us-central1";

	/**
	 * Docker utils
	 */
	private static final String GOOGLE_CLI = "google/cloud-sdk";
	private static final String GOOGLE_CONFIG_BIND = "--volumes-from" + SEP +
							PropertiesManager.getInstance().getProperty(PropertiesManager.GOOGLE_CONTAINER);

	/**
	 * Google Cloud Functions commands
	 */
	private static final String FUNCTIONS = "gcloud" + SEP + "functions";
	private static final String DEPLOY_FUNCTION_CMD =  FUNCTIONS + SEP + "deploy";
	private static final String REMOVE_FUNCTION_CMD =  FUNCTIONS + SEP + "delete";

	/**
	 * Google Cloud Workflows [BETA] commands
	 */
	private static final String WORKFLOWS = "gcloud" + SEP + "beta" + SEP + "workflows";
	private static final String DEPLOY_WORKFLOW_CMD = WORKFLOWS + SEP + "deploy";
	private static final String REMOVE_WORKFLOW_CMD = WORKFLOWS + SEP + "delete";


	/**
	 * Builds Google Cloud CLI command for Functions function deployment
	 * @param functionName name of function to deploy
	 * @param runtime runtime of function to deploy
	 * @param entryPoint entry point location of function to deploy
	 * @param timeout timeout in seconds of function to deploy
	 * @param memory memory amount in megabytes of function to deploy
	 * @param region region of deployment for function to deploy
	 * @param functionDirPath path of the folder containing function and requirements
	 * @return command as string
	 */
	public static String buildGoogleCloudFunctionsDeployCommand(String functionName, String runtime, String entryPoint,
																Integer timeout, Integer memory, String region,
																String functionDirPath) {

		return 	// command beginning
				"docker run --rm -i" + SEP +
						// volume attachment
						"-v" + SEP + functionDirPath + ":" + FUNCTIONALITIES_DIR + SEP +
						// project config binding
						GOOGLE_CONFIG_BIND + SEP +
						// select docker image to use
						GOOGLE_CLI + SEP +
						// CLI command to deploy a new function
						DEPLOY_FUNCTION_CMD + SEP +
						// function name
						functionName + SEP +
						// deployment options
						"--allow-unauthenticated" + SEP +
						"--memory=" + memory + "MB" + SEP +
						"--runtime=" + runtime + SEP +
						"--timeout=" + timeout + "s" + SEP +
						"--region=" + region + SEP +
						"--trigger-http" + SEP +
						"--stage-bucket=" +
							PropertiesManager.getInstance().getProperty(PropertiesManager.GOOGLE_STAGE_BUCKET) + SEP +
						"--source=" + FUNCTIONALITIES_DIR + SEP +
						"--entry-point=" + entryPoint;
	}

	/**
	 * Builds Google Cloud CLI command for Functions function deletion
	 * @param functionName name of the function to remove
	 * @param region function to remove region of deployment
	 * @return command as string
	 */
	public static String buildGoogleCloudFunctionsRemoveCommand(String functionName, String region) {

		return 	// command beginning
				"docker run --rm -i" + SEP +
						// project config binding
						GOOGLE_CONFIG_BIND + SEP +
						// select docker image to use
						GOOGLE_CLI + SEP +
						// CLI command to deploy a new function
						REMOVE_FUNCTION_CMD + SEP +
						// function name
						functionName + SEP +
						"--region=" + region + SEP +
						"--quiet";
	}

	/**
	 * Builds Google Cloud CLI command to deploy a new workflow to Google Cloud Platform Workflows [BETA]
	 * @param workflowName name of the new workflow
	 * @param region workflow region of deployment
	 * @param workflowDirPath path of the directory containing flow definition yaml
	 * @param fileName yaml workflow definition file name
	 * @return command as string
	 */
	public static String buildGoogleCloudWorkflowsDeployCommand(String workflowName, String region,
																String workflowDirPath, String fileName) {
		return 	// command beginning
				"docker run --rm -i" + SEP +
						// volume attachment
						"-v" + SEP + workflowDirPath + ":" + FUNCTIONALITIES_DIR + SEP +
						// project config binding
						GOOGLE_CONFIG_BIND + SEP +
						// select docker image to use
						GOOGLE_CLI + SEP +
						// CLI command to deploy a new function
						DEPLOY_WORKFLOW_CMD + SEP +
						// function name
						workflowName + SEP +
						"--location=" + region + SEP +
						"--source=" + FUNCTIONALITIES_DIR + getPathSep() + fileName;
	}

	/**
	 * Builds Google Cloud CLI command to remo a workflow from Google Cloud Platform Workflows [BETA]
	 * @param workflowName name of the workflow to delete
	 * @param region workflow to delete region of deployment
	 * @return command as string
	 */
	public static String buildGoogleCloudWorkflowsRemoveCommand(String workflowName, String region) {
		return 	// command beginning
				"docker run --rm -i" + SEP +
						// project config binding
						GOOGLE_CONFIG_BIND + SEP +
						// select docker image to use
						GOOGLE_CLI + SEP +
						// CLI command to deploy a new function
						REMOVE_WORKFLOW_CMD + SEP +
						// function name
						workflowName + SEP +
						"--location=" + region + SEP +
						"--quiet";
	}

	/**
	 * Translates function name and runtime to a string that will work as function identifier
	 * @param functionalityName name of the function to apply id
	 * @param runtime to translate
	 * @return function identifier
	 * @throws IllegalNameException if functionalityName is an illegal name
	 */
	public static String applyRuntimeId(String functionalityName, String runtime) throws IllegalNameException {
		if (needsRuntimeId(functionalityName)) {
			if (runtime.equals(PYTHON_3_7_RUNTIME)) {
				return functionalityName + PYTHON_ID;
			} else {
				return functionalityName + OTHERS_ID;
			}
		} else {
			return functionalityName;
		}
	}
}
