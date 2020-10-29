package cmd.functionality_commands;

import cmd.CommandUtility;
import utility.PropertiesManager;

/**
 * Utility for Google Cloud CLI command build
 */
public class GoogleCommandUtility extends CommandUtility {

	/**
	 * Public constant variables: Languages
	 */
	public static final String PYTHON_3_7_RUNTIME = "python37";
	public static final String GO_1_RUNTIME = "go111";
	public static final String JAVA_11_RUNTIME = "java11";
	public static final String NODE_10_RUNTIME = "nodejs10";

	/**
	 * Public constant variables: Zones
	 */
	public static final String NORTH_VIRGINIA = "us-east4";
	public static final String IOWA = "us-central1";

	/**
	 * Public constant variables: Big Table memorization mediums
	 */
	public static final String BT_HARD_DISK_STORAGE = "HDD";
	public static final String BT_SOLID_STATE_STORAGE = "SSD";


	/**
	 * Docker utils
	 */
	private static final String PREAMBLE = "docker" + SEP + "run" + SEP + "--rm" + SEP + "-i";
	private static final String GOOGLE_CLI = "google/cloud-sdk:316.0.0";
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
	private static final String CREATE_TABLE = "createtable";
	private static final String CREATE_FAMILY = "createfamily";
	private static final String CBT_DELETE_INSTANCE = CLOUD_BIG_TABLE + SEP + "deleteinstance";

	/**
	 * Google Cloud Storage Commands
	 */
	private static final String CLOUD_STORAGE_UTILS = "gsutil";
	private static final String CLOUD_STORAGE_CREATE_BUCKET = CLOUD_STORAGE_UTILS + SEP + "mb";
	private static final String CLOUD_STORAGE_DELETE_BUCKET = CLOUD_STORAGE_UTILS + SEP + "-m" + SEP + "rm";


	/**
	 * Google CLI Docker image getter
	 * @return CLI Docker image name
	 */
	public static String getCli() {
		return GOOGLE_CLI;
	}

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
				PREAMBLE + SEP +
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
				PREAMBLE + SEP +
						// volume attachment
						"-v" + SEP + workflowDirPath + ":" + FUNCTIONALITIES_DIR + SEP +
						// project config binding
						GOOGLE_CONFIG_BIND + SEP +
						// select docker image to use
						GOOGLE_CLI + SEP +
						// CLI command to deploy a new workflow
						DEPLOY_WORKFLOW_CMD + SEP +
						// function name
						workflowName + SEP +
						"--location=" + region + SEP +
						"--source=" + FUNCTIONALITIES_DIR + getPathSep() + fileName;
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
	public static String buildGoogleCloudBigTableCreateInstanceCommand(String name, String id, String clusterId,
																	   String region, int clusterNodes,
																	   String storageType) {

		return 	// command beginning
				PREAMBLE + SEP +
						// project config binding
						GOOGLE_CONFIG_BIND + SEP +
						// select docker image to use
						GOOGLE_CLI + SEP +
						// CLI command to deploy a new instance
						CBT_CREATE_INSTANCE + SEP +
						// instance options
						id + SEP + name + SEP +
						clusterId + SEP + region + "-a" + SEP +
						clusterNodes + SEP + storageType;
	}

	/**
	 * Builds Google Cloud CLI command to create a new table in Big Table
	 * @param instanceId id of the Big Table instance
	 * @param tableName name of the table
	 * @return command as string
	 */
	public static String buildGoogleCloudBigTableCreateTableCommand(String instanceId, String tableName) {

		return 	// command beginning
				PREAMBLE + SEP +
						// project config binding
						GOOGLE_CONFIG_BIND + SEP +
						// select docker image to use
						GOOGLE_CLI + SEP +
						// CLI command to deploy a new table
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
				PREAMBLE + SEP +
						// project config binding
						GOOGLE_CONFIG_BIND + SEP +
						// select docker image to use
						GOOGLE_CLI + SEP +
						// CLI command to add a new column family
						CLOUD_BIG_TABLE + SEP +
						"-instance=" + instanceId + SEP +
						CREATE_FAMILY + SEP +
						// table and family
						tableName + SEP + familyName;
	}

	/**
	 * Builds Google Cloud CLI command for Cloud Storage bucket creation
	 * @param bucketName name of the new bucket
	 * @param region region of the new bucket
	 * @return command as string
	 */
	public static String buildGoogleCloudStorageBucketCreationCommand(String bucketName, String region) {

		return 	// command beginning
				PREAMBLE + SEP +
						// project config binding
						GOOGLE_CONFIG_BIND + SEP +
						// select docker image to use
						GOOGLE_CLI + SEP +
						// CLI command to create a new bucket
						CLOUD_STORAGE_CREATE_BUCKET + SEP +
						"-l" + SEP + region + SEP +
						// bucket name
						"gs://" + bucketName;
	}

	/**
	 * Builds Google Cloud CLI command for Functions function deletion
	 * @param functionName name of the function to remove
	 * @param region function to remove region of deployment
	 * @return command as string
	 */
	public static String buildGoogleCloudFunctionsRemoveCommand(String functionName, String region) {

		return 	// command beginning
				PREAMBLE + SEP +
						// project config binding
						GOOGLE_CONFIG_BIND + SEP +
						// select docker image to use
						GOOGLE_CLI + SEP +
						// CLI command to remove a function
						REMOVE_FUNCTION_CMD + SEP +
						// function name
						functionName + SEP +
						"--region=" + region + SEP +
						"--quiet";
	}

	/**
	 * Builds Google Cloud CLI command to remove a workflow from Google Cloud Platform Workflows [BETA]
	 * @param workflowName name of the workflow to delete
	 * @param region workflow to delete region of deployment
	 * @return command as string
	 */
	public static String buildGoogleCloudWorkflowsRemoveCommand(String workflowName, String region) {

		return 	// command beginning
				PREAMBLE + SEP +
						// project config binding
						GOOGLE_CONFIG_BIND + SEP +
						// select docker image to use
						GOOGLE_CLI + SEP +
						// CLI command to remove a workflow
						REMOVE_WORKFLOW_CMD + SEP +
						// function name
						workflowName + SEP +
						"--location=" + region + SEP +
						"--quiet";
	}

	/**
	 * Builds Google Cloud CLI command to delete a BigTable instance
	 * @param id id of the instance to delete
	 * @return command as string
	 */
	public static String buildGoogleCloudBigTableDropInstanceCommand(String id) {

		return 	// command beginning
				PREAMBLE + SEP +
						// project config binding
						GOOGLE_CONFIG_BIND + SEP +
						// select docker image to use
						GOOGLE_CLI + SEP +
						// CLI command to deploy a delete an instance
						CBT_DELETE_INSTANCE + SEP +
						// instance id
						id;
	}

	/**
	 * Builds Google Cloud CLI command for Cloud Storage bucket deletion
	 * @param bucketName name of the bucket to delete
	 * @return command as string
	 */
	public static String buildGoogleCloudStorageBucketDropCommand(String bucketName) {

		return 	// command beginning
				PREAMBLE + SEP +
						// project config binding
						GOOGLE_CONFIG_BIND + SEP +
						// select docker image to use
						GOOGLE_CLI + SEP +
						// CLI command to delete a bucket
						CLOUD_STORAGE_DELETE_BUCKET + SEP +
						"-r" + SEP +
						// bucket name
						"gs://" + bucketName;
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
