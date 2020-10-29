package cmd.functionality_commands;

import cmd.CommandUtility;
import utility.PropertiesManager;

/**
 * Utility for AWS CLI command build
 */
public class AmazonCommandUtility extends CommandUtility {

	/**
	 * Public constant variables: Languages
	 */
	public static final String PYTHON_3_7_RUNTIME = "python3.7";
	public static final String GO_1_X_RUNTIME = "go1.x";
	public static final String JAVA_11_RUNTIME = "java11";
	public static final String NODE_10_X_RUNTIME = "nodejs10.x";

	/**
	 * Public constant variables: Zones
	 */
	public static final String NORTH_VIRGINIA = "us-east-1";
	public static final String OHIO = "us-east-2";

	/**
	 * Public constant variables: S3 ACLs
	 */
	public static final String S3_ACL_PUBLIC = "public-read-write";
	public static final String S3_ACL_PUBLIC_READ = "public-read";
	public static final String S3_ACL_AUTH_READ= "authenticated-read";
	public static final String S3_ACL_PRIVATE = "private";


	/**
	 * Docker utils
	 */
	private static final String AWS_CLI = "amazon/aws-cli:2.0.60";
	private static final String PREAMBLE = "docker" + SEP + "run" + SEP + "--rm" + SEP + "-i" + SEP +
			"-v" + SEP + PropertiesManager.getInstance().getProperty(PropertiesManager.AWS_AUTH_CONFIG) +
			":" + "/root/.aws";
	private static final String CLOSURE = "--cli-connect-timeout" + SEP + "0";

	/**
	 * Auth utils
	 */
	private static final String EXECUTE_API = "execute-api";

	/**
	 * Lambda commands
	 */
	private static final String LAMBDA = "lambda";
	private static final String LAMBDA_NEW_FUNC = LAMBDA + SEP + "create-function";
	private static final String LAMBDA_LIST_FUNC = LAMBDA + SEP + "list-functions";
	private static final String LAMBDA_ADD_PERM = LAMBDA + SEP + "add-permission";
	private static final String LAMBDA_DEL = LAMBDA + SEP + "delete-function";

	/**
	 * Api Gateway commands
	 */
	private static final String GATEWAY = "apigateway";
	private static final String GATEWAY_CREATE_API = GATEWAY + SEP + "create-rest-api";
	private static final String GATEWAY_GET_API = GATEWAY + SEP + "get-rest-apis";
	private static final String GATEWAY_GET_RESOURCES = GATEWAY + SEP + "get-resources";
	private static final String GATEWAY_CREATE_RESOURCE = GATEWAY + SEP + "create-resource";
	private static final String GATEWAY_PUT_METHOD = GATEWAY + SEP + "put-method";
	private static final String GATEWAY_PUT_INTEGRATION = GATEWAY + SEP + "put-integration";
	private static final String GATEWAY_CREATE_DEPLOYMENT = GATEWAY + SEP + "create-deployment";
	private static final String GATEWAY_DEL = GATEWAY + SEP + "delete-rest-api";

	/**
	 * Step Functions commands
	 */
	private static final String STEP_FUNCTIONS = "stepfunctions";
	private static final String STEP_FUNCTIONS_CREATE = STEP_FUNCTIONS + SEP + "create-state-machine";
	private static final String STEP_FUNCTIONS_DROP = STEP_FUNCTIONS + SEP + "delete-state-machine";

	/**
	 * DynamoDB commands
	 */
	private static final String DYNAMO_DB = "dynamodb";
	private static final String DYNAMO_DB_CREATE_TABLE = DYNAMO_DB + SEP + "create-table";
	private static final String DYNAMO_DB_DELETE_TABLE = DYNAMO_DB + SEP + "delete-table";

	/**
	 * S3 commands
	 */
	private static final String S3 = "s3";
	private static final String S3_CREATE_BUCKET = S3 + "api" + SEP + "create-bucket";
	private static final String S3_DELETE_BUCKET = S3 + SEP + "rb";


	/**
	 * AWS CLI Docker image getter
	 * @return CLI Docker image name
	 */
	public static String getCli() {
		return AWS_CLI;
	}

	/**
	 * Builds AWS CLI command for Lambda function deployment
	 * @param functionName name of function to deploy
	 * @param runtime runtime of function to deploy
	 * @param entryPoint entry point location of function to deploy
	 * @param timeout timeout in seconds of function to deploy
	 * @param memory memory amount in megabytes of function to deploy
	 * @param region region of deployment for function to deploy
	 * @param zipFolder path of the folder containing the zip package with the function
	 * @param zipName name of the zip package with the function
	 * @return command as string
	 */
	public static String buildLambdaFunctionDeployCommand(String functionName, String runtime, String entryPoint,
														  Integer timeout, Integer memory, String region,
														  String zipFolder, String zipName) {

		return 	// command beginning
				PREAMBLE + SEP +
						// volume attachment
						"-v" + SEP + zipFolder + ":" + FUNCTIONALITIES_DIR + SEP +
						// select docker image to use
						AWS_CLI + SEP +
						// operation define
						LAMBDA_NEW_FUNC + SEP +
						// parameters setting
						"--function-name" + SEP + functionName + SEP +
						"--runtime" + SEP + runtime + SEP +
						"--memory-size" + SEP + memory + SEP +
						"--role" + SEP +
						PropertiesManager.getInstance().getProperty(PropertiesManager.AWS_LAMBDA_EXEC_ROLE)
						+ SEP + "--handler" + SEP + entryPoint + SEP +
						"--timeout" + SEP + timeout + SEP +
						"--publish" + SEP +
						"--region" + SEP + region + SEP +
						"--zip-file" + SEP + "fileb://" + FUNCTIONALITIES_DIR + "/" + zipName + SEP +
						CLOSURE;

	}

	/**
	 * Builds AWS CLI command to get Lambda function ARN
	 * @param functionName name of the function
	 * @param region function region of deployment
	 * @return command as string
	 */
	public static String buildLambdaArnGetterCommand(String functionName, String region) {

		return	// command beginning
				PREAMBLE + SEP +
						// select docker image to use
						AWS_CLI + SEP +
						// operation define
						LAMBDA_LIST_FUNC + SEP +
						"--query" + SEP + "\"Functions[?FunctionName=='" + functionName + "'].FunctionArn\"" + SEP +
						"--region" + SEP + region + SEP +
						"--output" + SEP + "text" + SEP +
						CLOSURE;
	}

	/**
	 * Builds AWS CLI command for API Gateway new API creation
	 * @param apiName name of the new API
	 * @param description description of the new API
	 * @param region new API region of deployment
	 * @return command as string
	 */
	public static String buildGatewayApiCreationCommand(String apiName, String description, String region) {

		return 	// command beginning
				PREAMBLE + SEP +
						// select docker image to use
						AWS_CLI + SEP +
						// operation define
						GATEWAY_CREATE_API + SEP +
						"--name" + SEP + "\"" + apiName + "\"" + SEP +
						"--description" + SEP + "\"" + description + "\"" + SEP +
						"--region" + SEP + region + SEP +
						CLOSURE;

	}

	/**
	 * Builds AWS CLI command to get Gateway API id
	 * @param apiName name of the API corresponding to the needed id
	 * @param region API region of deployment
	 * @return command as string
	 */
	public static String buildGatewayApiIdGetterCommand(String apiName, String region) {

		return	// command beginning
				PREAMBLE + SEP +
						// select docker image to use
						AWS_CLI + SEP +
						// operation define
						GATEWAY_GET_API + SEP +
						"--query" + SEP + "\"items[?name=='"+ apiName + "'].id\"" + SEP +
						"--region" + SEP + region + SEP +
						"--output" + SEP + "text" + SEP +
						CLOSURE;
	}

	/**
	 * Builds AWS CLI command to get Gateway API parent id
	 * @param apiId id of the API corresponding to the needed parent id
	 * @param region API region of deployment
	 * @return command as string
	 */
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
						"--output" + SEP + "text" + SEP +
						CLOSURE;
	}

	/**
	 * Builds AWS CLI command to create a new Gateway API resource
	 * @param functionName name of the Lambda function representing the resource
	 * @param apiId API id
	 * @param parentId API parent id
	 * @param region API region of deployment
	 * @return command as string
	 */
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
						"--region" + SEP + region + SEP +
						CLOSURE;
	}

	/**
	 * Builds AWS CLI command to get a Gateway API resource id
	 * @param functionName name of the Lambda function representing the resource
	 * @param apiId API id
	 * @param region API region of deployment
	 * @return command as string
	 */
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
						"--output" + SEP + "text" + SEP +
						CLOSURE;
	}

	/**
	 * Builds AWS CLI command to add a new method to a Gateway API
	 * @param apiId API id
	 * @param resourceId resource id
	 * @param region API region of deployment
	 * @return command as string
	 */
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
						"--region" + SEP + region + SEP +
						CLOSURE;
	}

	/**
	 * Builds AWS CLI command to associate a Gateway API method to resource
	 * @param apiId API id
	 * @param resourceId resource id
	 * @param lambdaARN ARN of the lambda function to execute
	 * @param region API region of deployment
	 * @return command as string
	 */
	public static String buildGatewayLambdaLinkageCommand(String apiId, String resourceId, String lambdaARN,
														  String region) {

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
						"--region" + SEP + region + SEP +
						CLOSURE;
	}

	/**
	 * Builds AWS CLI command to deploy a Gateway API
	 * @param apiId API id
	 * @param stageName name of the deployment stage
	 * @param region API region of deployment
	 * @return command as string
	 */
	public static String buildGatewayDeploymentCreationCommand(String apiId, String stageName, String region) {

		return 	// command beginning
				PREAMBLE + SEP +
						// select docker image to use
						AWS_CLI + SEP +
						// operation define
						GATEWAY_CREATE_DEPLOYMENT + SEP +
						"--rest-api-id" + SEP + apiId + SEP +
						"--stage-name" + SEP + stageName + SEP +
						"--region" + SEP + region + SEP +
						CLOSURE;
	}

	/**
	 * Builds AWS CLI command to authenticate Lambda function execution
	 * @param functionName name of the function
	 * @param apiId API id
	 * @param lambdaARN ARN of the lambda function
	 * @param region Lambda region of deployment
	 * @return command as string
	 */
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
						"--region" + SEP + region + SEP +
						CLOSURE;
	}

	/**
	 * Builds AWS CLI command to create a new state machine on Step Functions
	 * @param machineName name of the new state machine
	 * @param region machine region of deployment
	 * @param definitionJson string in json format containing machine definition
	 * @return command as string
	 */
	public static String buildStepFunctionCreationCommand(String machineName, String region, String definitionJson) {

		return	// command beginning
				PREAMBLE + SEP +
						// select docker image to use
						AWS_CLI + SEP +
						// operation define
						STEP_FUNCTIONS_CREATE + SEP +
						"--name" + SEP + machineName + SEP +
						"--role-arn" + SEP +
						PropertiesManager.getInstance().getProperty(PropertiesManager.AWS_STEP_FUNCTIONS_EXEC_ROLE) +
						SEP + "--type" + SEP + "STANDARD" + SEP +
						"--region" + SEP + region + SEP +
						"--definition" + SEP + definitionJson + SEP +
						CLOSURE;
	}

	/**
	 * Builds AWS CLI command to create a Dynamo DB table
	 * @param jsonFolder absolute path of the folder containing json table definition
	 * @param jsonName json definition file name
	 * @param region table region of creation
	 * @return command as string
	 */
	public static String buildDynamoTableCreationCommand(String tableName, String jsonFolder, String jsonName, String region) {

		return 	// command beginning
				PREAMBLE + SEP +
						// volume attachment
						"-v" + SEP + jsonFolder + ":" + FUNCTIONALITIES_DIR + SEP +
						// select docker image to use
						AWS_CLI + SEP +
						// operation define
						DYNAMO_DB_CREATE_TABLE + SEP +
						// parameters setting
						"--region" + SEP + region + SEP +
						"--table-name" + SEP + tableName + SEP +
						"--cli-input-json" + SEP + "fileb://" + FUNCTIONALITIES_DIR + "/" + jsonName + SEP +
						CLOSURE;
	}

	/**
	 * Builds AWS CLI command to create a new S3 bucket
	 * @param bucketName name of the new bucket
	 * @param acl access control list of the new bucket: use static options
	 * @param region region of the new bucket
	 * @return command as string
	 */
	public static String buildS3BucketCreationCommand(String bucketName, String acl, String region) {

		return 	// command beginning
				PREAMBLE + SEP +
						// select docker image to use
						AWS_CLI + SEP +
						// operation define
						S3_CREATE_BUCKET + SEP +
						// parameters setting
						"--bucket" + SEP + bucketName + SEP +
						"--acl" + SEP + acl + SEP +
						"--region" + SEP + region + SEP +
						"--create-bucket-configuration" + SEP + "LocationConstraint=" + region + SEP +
						CLOSURE;
	}

	/**
	 * Builds AWS CLI command to delete a Lambda function
	 * @param functionName name of function to delete
	 * @param region deployment region of function to delete
	 * @return command as string
	 */
	public static String buildLambdaDropCommand(String functionName, String region) {

		return	// command beginning
				PREAMBLE + SEP +
						// select docker image to use
						AWS_CLI + SEP +
						// operation define
						LAMBDA_DEL + SEP +
						"--function-name" + SEP + functionName + SEP +
						"--region" + SEP + region + SEP +
						CLOSURE;
	}

	/**
	 * Builds AWS CLI command to delete an API from API Gateway
	 * @param apiId id of the API to delete
	 * @param region deployment region of the API to delete
	 * @return command as string
	 */
	public static String buildGatewayDropCommand(String apiId, String region) {

		return	// command beginning
				PREAMBLE + SEP +
						// select docker image to use
						AWS_CLI + SEP +
						// operation define
						GATEWAY_DEL + SEP +
						"--rest-api-id" + SEP + apiId + SEP +
						"--region" + SEP + region + SEP +
						CLOSURE;
	}

	/**
	 * Builds AWS CLI command to delete a Step Functions state machine
	 * @param machineArn state machine ARN
	 * @param region state machine region of deployment
	 * @return command as string
	 */
	public static String buildStepFunctionDropCommand(String machineArn, String region) {

		return	// command beginning
				PREAMBLE + SEP +
						// select docker image to use
						AWS_CLI + SEP +
						// operation define
						STEP_FUNCTIONS_DROP + SEP +
						"--region" + SEP + region + SEP +
						"--state-machine-arn" + SEP + machineArn + SEP +
						CLOSURE;
	}

	/**
	 * Builds AWS CLI command to delete a Dynamo DB table
	 * @param tableName name of the table to delete
	 * @param region region where the table to delete has been created
	 * @return command as string
	 */
	public static String buildDynamoTableDropCommand(String tableName, String region) {

		return 	// command beginning
				PREAMBLE + SEP +
						// select docker image to use
						AWS_CLI + SEP +
						// operation define
						DYNAMO_DB_DELETE_TABLE + SEP +
						// parameters setting
						"--region" + SEP + region + SEP +
						"--table-name" + SEP + tableName + SEP +
						CLOSURE;
	}

	/**
	 * Builds AWS CLI command to delete a S3 bucket
	 * @param bucketName name of the bucket to delete
	 * @return command as string
	 */
	public static String buildS3BucketDropCommand(String bucketName, String region) {

		return 	// command beginning
				PREAMBLE + SEP +
						// select docker image to use
						AWS_CLI + SEP +
						// operation define
						S3_DELETE_BUCKET + SEP +
						// parameters setting
						"s3://" + bucketName + SEP +
						"--region" + SEP + region + SEP +
						"--force" + SEP +
						CLOSURE;
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
				case NODE_10_X_RUNTIME:
					return functionalityName + NODE_ID;
				case GO_1_X_RUNTIME:
					return functionalityName + GO_ID;
				default:
					return functionalityName + OTHERS_ID;
			}
		} else {
			return functionalityName;
		}
	}
}
