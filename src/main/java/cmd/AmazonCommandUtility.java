package cmd;

public class AmazonCommandUtility extends CommandUtility {

	/* PARAMETERS */
	public static final String PYTHON_3_7_RUNTIME = "python3.7";
	public static final String NORTH_VIRGINIA = "us-east-1";
	/*	*/

	private static final String AWS_CLI = "amazon/aws-cli";
	private static final String PREAMBLE = "docker" + SEP + "run" + SEP + "--rm" + SEP + "-i" + SEP +
			"-v" + SEP + PropertiesManager.getInstance().getProperty(PropertiesManager.AWS_AUTH_CONFIG) +
			":" + "/root/.aws";
	private static final String EXECUTE_API = "execute-api";
	private static final String LAMBDA = "lambda";
	private static final String LAMBDA_NEW_FUNC = LAMBDA + SEP + "create-function";
	private static final String LAMBDA_LIST_FUNC = LAMBDA + SEP + "list-functions";
	private static final String LAMBDA_ADD_PERM = LAMBDA + SEP + "add-permission";
	private static final String GATEWAY = "apigateway";
	private static final String GATEWAY_CREATE_API = GATEWAY + SEP + "create-rest-api";
	private static final String GATEWAY_GET_API = GATEWAY + SEP + "get-rest-apis";
	private static final String GATEWAY_GET_RESOURCES = GATEWAY + SEP + "get-resources";
	private static final String GATEWAY_CREATE_RESOURCE = GATEWAY + SEP + "create-resource";
	private static final String GATEWAY_PUT_METHOD = GATEWAY + SEP + "put-method";
	private static final String GATEWAY_PUT_INTEGRATION = GATEWAY + SEP + "put-integration";
	private static final String GATEWAY_CREATE_DEPLOYMENT = GATEWAY + SEP + "create-deployment";

	public static String buildLambdaFunctionDeployCommand(String functionName, String runtime, String entryPoint,
														  Integer timeout, Integer memory, String region, String zipFolder, String zipName) {
		return 	// command beginning
				PREAMBLE + SEP +
						// volume attachment
						"-v" + SEP + zipFolder + ":" + FUNCTIONS_DIR + SEP +
						// select docker image to use
						AWS_CLI + SEP +
						// operation define
						LAMBDA_NEW_FUNC + SEP +
						// parameters setting
						"--function-name" + SEP + functionName + SEP +
						"--runtime" + SEP + runtime + SEP +
						"--memory-size" + SEP + memory + SEP +
						"--role" + SEP + PropertiesManager.getInstance().getProperty(PropertiesManager.AWS_EXEC_ROLE)
						+ SEP + "--handler" + SEP + entryPoint + SEP +
						"--timeout" + SEP + timeout + SEP +
						"--publish" + SEP +
						"--region" + SEP + region + SEP +
						"--zip-file" + SEP + "fileb://" + FUNCTIONS_DIR + "/" + zipName;

	}

	public static String buildLambdaArnGetterCommand(String functionName, String region) {

		return	// command beginning
				PREAMBLE + SEP +
						// select docker image to use
						AWS_CLI + SEP +
						// operation define
						LAMBDA_LIST_FUNC + SEP +
						"--query" + SEP + "\"Functions[?FunctionName=='" + functionName + "'].FunctionArn\"" + SEP +
						"--region" + SEP + region + SEP +
						"--output" + SEP + "text";
	}

	public static String buildGatewayApiCreationCommand(String apiName, String description, String region) {

		return 	// command beginning
				PREAMBLE + SEP +
						// select docker image to use
						AWS_CLI + SEP +
						// operation define
						GATEWAY_CREATE_API + SEP +
						"--name" + SEP + "\"" + apiName + "\"" + SEP +
						"--description" + SEP + "\"" + description + "\"" + SEP +
						"--region" + SEP + region;

	}

	public static String buildGatewayApiIdGetterCommand(String apiName, String region) {
		return	// command beginning
				PREAMBLE + SEP +
						// select docker image to use
						AWS_CLI + SEP +
						// operation define
						GATEWAY_GET_API + SEP +
						"--query" + SEP + "\"items[?name=='"+ apiName + "'].id\"" + SEP +
						"--region" + SEP + region + SEP +
						"--output" + SEP + "text";
	}

	public static String buildGatewayApiParentIdGetterCommand(String apiId, String region) {
		return	// command beginning
				PREAMBLE + SEP +
						// select docker image to use
						AWS_CLI + SEP +
						// operation define
						GATEWAY_GET_RESOURCES + SEP +
						"--rest-api-id" + SEP + apiId + SEP +
						"--query" + SEP + "\"items[?path=='/'].id\"" + SEP +
						"--region" + SEP + region + SEP +
						"--output" + SEP + "text";
	}

	public static String buildGatewayResourceApiCreationCommand(String functionName, String apiId, String parentId,
																String region) {
		return 	// command beginning
				PREAMBLE + SEP +
						// select docker image to use
						AWS_CLI + SEP +
						// operation define
						GATEWAY_CREATE_RESOURCE + SEP +
						"--rest-api-id" + SEP + apiId + SEP +
						"--parent-id" + SEP + parentId + SEP +
						"--path-part" + SEP + functionName + SEP +
						"--region" + SEP + region;
	}

	public static String buildGatewayResourceApiIdGetterCommand(String functionName, String apiId, String region) {

		return 	// command beginning
				PREAMBLE + SEP +
						// select docker image to use
						AWS_CLI + SEP +
						// operation define
						GATEWAY_GET_RESOURCES + SEP +
						"--rest-api-id" + SEP + apiId + SEP +
						"--query" + SEP + "\"items[?path=='/" + functionName + "'].id\"" + SEP +
						"--region" + SEP + region + SEP +
						"--output" + SEP + "text";
	}

	public static String buildGatewayApiMethodOnResourceCreationCommand(String apiId, String resourceId,
																		String region) {

		return 	// command beginning
				PREAMBLE + SEP +
						// select docker image to use
						AWS_CLI + SEP +
						// operation define
						GATEWAY_PUT_METHOD + SEP +
						"--rest-api-id" + SEP + apiId + SEP +
						"--resource-id" + SEP + resourceId + SEP +
						"--http-method" + SEP + "ANY" + SEP +
						"--authorization-type" + SEP + "NONE" + SEP +
						"--region" + SEP + region;
	}

	public static String buildGatewayLambdaLinkageCommand(String apiId, String resourceId, String lambdaARN, String region) {

		return 	// command beginning
				PREAMBLE + SEP +
						// select docker image to use
						AWS_CLI + SEP +
						// operation define
						GATEWAY_PUT_INTEGRATION + SEP +
						"--rest-api-id" + SEP + apiId + SEP +
						"--resource-id" + SEP + resourceId + SEP +
						"--http-method" + SEP + "ANY" + SEP +
						"--type" + SEP + "AWS_PROXY" + SEP +
						"--integration-http-method" + SEP + "POST" + SEP +
						"--uri" + SEP + "arn:aws:apigateway:" + region + ":" + LAMBDA +
							":path/2015-03-31/functions/" + lambdaARN + "/invocations" + SEP +
						"--region" + SEP + region;
	}

	public static String buildGatewayDeploymentCreationCommand(String apiId, String stageName, String region) {

		return 	// command beginning
				PREAMBLE + SEP +
						// select docker image to use
						AWS_CLI + SEP +
						// operation define
						GATEWAY_CREATE_DEPLOYMENT + SEP +
						"--rest-api-id" + SEP + apiId + SEP +
						"--stage-name" + SEP + stageName + SEP +
						"--region" + SEP + region;
	}

	public static String buildGatewayLambdaAuthCommand(String functionName, String apiId, String lambdaARN,
													   String region) {
		String apiARN = lambdaARN.replace(LAMBDA, EXECUTE_API);
		apiARN = apiARN.replace("function:" + functionName, apiId);

		return 	// command beginning
				PREAMBLE + SEP +
						// select docker image to use
						AWS_CLI + SEP +
						// operation define
						LAMBDA_ADD_PERM + SEP +
						"--function-name" + SEP + functionName + SEP +
						"--statement-id" + SEP + functionName + SEP +
						"--action" + SEP + LAMBDA + ":" + "InvokeFunction" + SEP +
						"--principal" + SEP + "apigateway.amazonaws.com" + SEP +
						"--source-arn" + SEP + "\"" + apiARN + "/*/*/" + functionName + "\"" + SEP +
						"--region" + SEP + region;
	}

}
