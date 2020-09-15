package cmd.functionality_commands;

import cmd.CommandUtility;
import utility.PropertiesManager;

public class GoogleCommandUtility extends CommandUtility {

	/* PARAMETERS */
	public static final String PYTHON_3_7_RUNTIME = "python37";
	public static final String NORTH_VIRGINIA = "us-east4";
	public static final String IOWA = "us-central1";
	/*	*/

	private static final String GOOGLE_CLI = "google/cloud-sdk";
	private static final String GOOGLE_CONFIG_BIND = "--volumes-from" + SEP +
							PropertiesManager.getInstance().getProperty(PropertiesManager.GOOGLE_CONTAINER);

	/* Google Cloud Functions deploy */
	private static final String FUNCTIONS = "gcloud" + SEP + "functions";
	private static final String DEPLOY_FUNCTION_CMD =  FUNCTIONS + SEP + "deploy";
	private static final String REMOVE_FUNCTION_CMD =  FUNCTIONS + SEP + "delete";

	/* Google Cloud Workflows deploy */
	private static final String WORKFLOWS = "gcloud" + SEP + "beta" + SEP + "workflows";
	private static final String DEPLOY_WORKFLOW_CMD = WORKFLOWS + SEP + "deploy";
	private static final String REMOVE_WORKFLOW_CMD = WORKFLOWS + SEP + "delete";


	public static String buildGoogleCloudFunctionsDeployCommand(String functionName, String runtime, String entryPoint,
																Integer timeout, Integer memory, String region,
																String functionDirPath) {

		return 	// command beginning
				"docker run --rm -i" + SEP +
						// volume attachment
						"-v" + SEP + functionDirPath + ":" + FUNCTIONS_DIR + SEP +
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
						"--source=" + FUNCTIONS_DIR + SEP +
						"--entry-point=" + entryPoint;
	}

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

	public static String buildGoogleCloudWorkflowsDeployCommand(String workflowName, String region,
																String workflowDirPath, String fileName) {
		return 	// command beginning
				"docker run --rm -i" + SEP +
						// volume attachment
						"-v" + SEP + workflowDirPath + ":" + FUNCTIONS_DIR + SEP +
						// project config binding
						GOOGLE_CONFIG_BIND + SEP +
						// select docker image to use
						GOOGLE_CLI + SEP +
						// CLI command to deploy a new function
						DEPLOY_WORKFLOW_CMD + SEP +
						// function name
						workflowName + SEP +
						"--location=" + region + SEP +
						"--source=" + FUNCTIONS_DIR + getPathSep() + fileName;
	}

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

}
