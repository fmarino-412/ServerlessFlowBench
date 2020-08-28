package cmd;

public class GoogleCommandUtility extends CommandUtility{

	/* PARAMETERS */
	public static final String PYTHON_3_7_RUNTIME = "python37";
	public static final String NORTH_VIRGINIA = "us-east4";
	/*	*/

	private static final String GOOGLE_CLI = "google/cloud-sdk";
	private static final String GOOGLE_CONFIG_BIND = "--volumes-from" + SEP +
							PropertiesManager.getInstance().getProperty(PropertiesManager.GOOGLE_CONTAINER);

	/* Google Cloud Functions deploy */
	private static final String DEPLOY_FUNCTION_CMD = "gcloud" + SEP + "functions" + SEP + "deploy";

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

}
