package cmd.functionality_commands;

import cmd.CommandUtility;
import cmd.StreamGobbler;
import utility.PropertiesManager;

/**
 * Utility for Google Cloud CLI command build
 */
public class GoogleCommandUtility extends CommandUtility {

	/**
	 * Public constant variables
	 */
	public static final String PYTHON_3_7_RUNTIME = "python37";
	public static final String GO_1_RUNTIME = "go111";
	public static final String JAVA_11_RUNTIME = "java11";
	public static final String NODE_10_RUNTIME = "nodejs10";

	public static final String NORTH_VIRGINIA = "us-east4";
	public static final String IOWA = "us-central1";

	public static final String HARD_DISK_STORAGE = "HDD";
	public static final String SOLID_STATE_STORAGE = "SSD";

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
	 * Google Cloud BigTable commands
	 */
	private static final String CLOUD_BIG_TABLE = "cbt";
	private static final String CBT_CREATE_INSTANCE = CLOUD_BIG_TABLE + SEP + "createinstance";
	private static final String CREATE_TABLE = CLOUD_BIG_TABLE + SEP + "createtable";
	private static final String CREATE_FAMILY = CLOUD_BIG_TABLE + SEP + "createfamily";
	private static final String CBT_DELETE_INSTANCE = CLOUD_BIG_TABLE + SEP + "deleteinstance";


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
	 * Builds Google Cloud CLI command to remove a workflow from Google Cloud Platform Workflows [BETA]
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
	 * Builds Google Cloud CLI command to create a new BigTable instance
	 * @param name name of the instance
	 * @param id id to assign to the instance
	 * @param clusterId instance cluster id
	 * @param region region of the instance cluster
	 * @param clusterNodes number of nodes in the instance cluster
	 * @param storageType type of storage: HDD or SSD
	 * @return command as string
	 */
	public static String buildGoogleCloudBigTableCreateInstanceCommand(String name, String id, String clusterId, String region, int clusterNodes, String storageType) {
		return 	// command beginning
				"docker run --rm -i" + SEP +
						// project config binding
						GOOGLE_CONFIG_BIND + SEP +
						// select docker image to use
						GOOGLE_CLI + SEP +
						// CLI command to deploy a new function
						CBT_CREATE_INSTANCE + SEP +
						// instance options
						id + SEP + name + SEP +
						clusterId + SEP + region + "-a" + SEP +
						clusterNodes + SEP + storageType;
	}

	/**
	 * Builds Google Cloud CLI command to delete a BigTable instance
	 * @param id id of the instance to delete
	 * @return command as string
	 */
	public static String buildGoogleCloudBigTableDropInstanceCommand(String id) {
		return 	// command beginning
				"docker run --rm -i" + SEP +
						// project config binding
						GOOGLE_CONFIG_BIND + SEP +
						// select docker image to use
						GOOGLE_CLI + SEP +
						// CLI command to deploy a new function
						CBT_DELETE_INSTANCE + SEP +
						// instance id
						id;
	}

	/**
	 * Builds Google Cloud CLI command to create a new table in Big Table
	 * @param instanceId id of the Big Table instance
	 * @param tableName name of the table
	 * @return command as string
	 */
	public static String buildGoogleCloudBigTableCreateTableCommand(String instanceId, String tableName) {
		return 	// command beginning
				"docker run --rm -i" + SEP +
						// project config binding
						GOOGLE_CONFIG_BIND + SEP +
						// select docker image to use
						GOOGLE_CLI + SEP +
						// CLI command to deploy a new function
						CLOUD_BIG_TABLE + SEP +
						"-instance=" + instanceId + SEP +
						CREATE_TABLE + SEP +
						// table name
						tableName;
	}

	/**
	 * Builds Google Cloud CLI command to create a new family in a table in Big Table
	 * @param instanceId id of the Big Table instance
	 * @param tableName name of the table
	 * @param familyName name of the new family
	 * @return command as string
	 */
	public static String buildGoogleCloudBigTableCreateFamilyCommand(String instanceId, String tableName,
																	 String familyName) {
		return 	// command beginning
				"docker run --rm -i" + SEP +
						// project config binding
						GOOGLE_CONFIG_BIND + SEP +
						// select docker image to use
						GOOGLE_CLI + SEP +
						// CLI command to deploy a new function
						CLOUD_BIG_TABLE + SEP +
						"-instance=" + instanceId + SEP +
						CREATE_FAMILY + SEP +
						// table and family
						tableName + SEP + familyName;
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
				case PYTHON_3_7_RUNTIME:
					return functionalityName + PYTHON_ID;
				case JAVA_11_RUNTIME:
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
