package cmd;

public class CommandUtility {

	/* PERSONALIZE */
	private static final String GOOGLE_CONFIG = "gcloud-cli";
	private static final String FUNCTIONS_DIR = "/mnt/functions";
	private static final String STAGE_BUCKET = "gcloudfunctions_stage_bucket";
	/**/
	//GENERAL
	private static final String SEP = " ";
	// GOOGLE
	private static final String GOOGLE_CLI = "google/cloud-sdk";
	private static final String GOOGLE_CONFIG_BIND = "--volumes-from" + SEP + GOOGLE_CONFIG;

	private static final String DEPLOY_FUNCTION_GLOBAL_OPT =
			"--allow-unauthenticated" + SEP +
					"--memory=128MB" + SEP +
					"--runtime=python37" + SEP +
					"--timeout=30s" + SEP +
					"--region=us-east4" + SEP +
					"--trigger-http" + SEP +
					"--stage-bucket=" + STAGE_BUCKET + SEP +
					"--source=" + FUNCTIONS_DIR;
	private static final String DEPLOY_FUNCTION_CMD = "gcloud" + SEP + "functions" + SEP + "deploy";
	private static final String DEPLOY_FUNCTION_ENTRY_POINT = "--entry-point=";

	// DOCKER
	private static final String PREAMBLE = "docker" + SEP + "run" + SEP + "--rm" + SEP + "-i"; //t";

	public static String buildGoogleCloudFunctionsDeployCommand(String name, String entryPoint,
																String functionDirPath) {
		return 	// command beginning
				PREAMBLE + SEP +
						// volume attachment
						"-v" + SEP + functionDirPath + ":" + FUNCTIONS_DIR + SEP +
						// project config binding
						GOOGLE_CONFIG_BIND + SEP +
						// select docker image to use
						GOOGLE_CLI + SEP +
						// CLI command to deploy a new function
						DEPLOY_FUNCTION_CMD + SEP +
						// function name
						name + SEP +
						// deployment options
						DEPLOY_FUNCTION_GLOBAL_OPT + SEP +
						// entry point
						DEPLOY_FUNCTION_ENTRY_POINT + entryPoint;
	}

	public static boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().contains("windows");
	}

}
