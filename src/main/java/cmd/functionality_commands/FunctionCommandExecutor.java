package cmd.functionality_commands;

import cmd.CommandExecutor;
import cmd.StreamGobbler;
import cmd.functionality_commands.output_parsing.UrlFinder;
import cmd.functionality_commands.output_parsing.ReplyCollector;
import databases.mysql.daos.CompositionRepositoryDAO;
import databases.mysql.daos.FunctionsRepositoryDAO;
import databases.mysql.FunctionalityData;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("DuplicatedCode")
public class FunctionCommandExecutor extends CommandExecutor {

	public static void deployGoogleCloudHandlerFunction(String functionName, String runtime, String entryPoint,
												   Integer timeout, Integer memory_mb, String region,
												   String directoryAbsolutePath)
			throws IOException, InterruptedException {
		deployOnGoogleCloudFunctions(functionName, runtime, entryPoint, timeout, memory_mb, region,
				directoryAbsolutePath, 0);
	}

	public static void deployOnGoogleCloudFunction(String functionName, String runtime, String entryPoint,
													Integer timeout, Integer memory_mb, String region,
													String directoryAbsolutePath)
			throws IOException, InterruptedException {
		deployOnGoogleCloudFunctions(functionName, runtime, entryPoint, timeout, memory_mb, region,
				directoryAbsolutePath, 1);
	}

	protected static String deployOnGoogleCloudCompositionFunction(String functionName, String runtime, String entryPoint,
																   Integer timeout, Integer memory_mb, String region,
																   String directoryAbsolutePath)
			throws IOException, InterruptedException {
		return deployOnGoogleCloudFunctions(functionName, runtime, entryPoint, timeout, memory_mb, region,
				directoryAbsolutePath, 2);
	}

	private static String deployOnGoogleCloudFunctions(String functionName, String runtime, String entryPoint,
													Integer timeout, Integer memory_mb, String region,
													String directoryAbsolutePath, Integer functionality)
			throws IOException, InterruptedException {

		assert functionality == 0 || functionality == 1 || functionality == 2;

		if (functionality != 2) {
			System.out.println("\n" + "\u001B[33m" +
					"Deploying \"" + functionName + "\" to Google Cloud Platform..." +
					"\u001B[0m" + "\n");
		}

		// build command
		String cmd = GoogleCommandUtility.buildGoogleCloudFunctionsDeployCommand(functionName, runtime, entryPoint,
				timeout, memory_mb, region, directoryAbsolutePath);

		// start process execution
		Process process = buildCommand(cmd).start();

		// create, execute and submit output gobblers
		UrlFinder urlFinder = new UrlFinder();
		StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(),
				urlFinder::findGoogleCloudFunctionsUrl);
		// google deploying progresses are on the error stream
		StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), System.out::println);

		ExecutorService executorServiceOut = Executors.newSingleThreadExecutor();
		ExecutorService executorServiceErr = Executors.newSingleThreadExecutor();

		executorServiceOut.submit(outputGobbler);
		executorServiceErr.submit(errorGobbler);

		// wait for completion and free environment
		if (process.waitFor() != 0) {
			System.err.println("Could not deploy function '" + functionName + "'");
			process.destroy();
			executorServiceOut.shutdown();
			executorServiceErr.shutdown();
			return "";
		}

		String url = urlFinder.getResult();
		if (functionality != 2) {
			System.out.println("\u001B[32m" + "Deployed function to: " + url + "\u001B[0m");
		}

		process.destroy();
		executorServiceOut.shutdown();
		executorServiceErr.shutdown();

		switch (functionality) {
			case 0:
				// handler
				CompositionRepositoryDAO.persistGoogleHandler(functionName, url, region);
				break;
			case 1:
				// function to persist
				FunctionsRepositoryDAO.persistGoogle(functionName, url, region);
				break;
			default:
				break;
		}

		return url;
	}

	protected static void deployAmazonRESTHandlerFunction(String functionName, String runtime, String entryPoint,
														  Integer timeout, Integer memory, String region,
														  String zipFolderAbsolutePath, String zipFileName)
			throws IOException, InterruptedException {

		deployOnAmazonRESTFunctions(functionName, runtime, entryPoint, timeout, memory, region, zipFolderAbsolutePath,
				zipFileName, true);
	}

	public static void deployOnAmazonRESTFunction(String functionName, String runtime, String entryPoint,
												  Integer timeout, Integer memory, String region,
												  String zipFolderAbsolutePath, String zipFileName)
			throws IOException, InterruptedException {

		deployOnAmazonRESTFunctions(functionName, runtime, entryPoint, timeout, memory, region, zipFolderAbsolutePath,
				zipFileName, false);

	}

	protected static String deployOnAmazonLambdaFunctions(String functionName, String runtime, String entryPoint,
														Integer timeout, Integer memory, String region,
														String zipFolderAbsolutePath, String zipFileName)
			throws IOException, InterruptedException {
		Process process;
		StreamGobbler outputGobbler;
		StreamGobbler errorGobbler;
		ExecutorService executorServiceOut = Executors.newSingleThreadExecutor();
		ExecutorService executorServiceErr = Executors.newSingleThreadExecutor();

		// deploy function
		String cmdDeploy = AmazonCommandUtility.buildLambdaFunctionDeployCommand(functionName, runtime, entryPoint,
				timeout, memory, region, zipFolderAbsolutePath, zipFileName);
		process = buildCommand(cmdDeploy).start();
		errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
		executorServiceErr.submit(errorGobbler);
		if (process.waitFor() != 0) {
			System.err.println("Could not deploy " + functionName + "on AWS Lambda");
			executorServiceOut.shutdown();
			executorServiceErr.shutdown();
			process.destroy();
			return "";
		}
		process.destroy();
		System.out.println(functionName + " deploy on AWS Lambda completed");

		// get lambda arn
		String cmdArnGetter = AmazonCommandUtility.buildLambdaArnGetterCommand(functionName, region);
		process = buildCommand(cmdArnGetter).start();
		ReplyCollector lambdaArnReplyCollector = new ReplyCollector();
		outputGobbler = new StreamGobbler(process.getInputStream(), lambdaArnReplyCollector::collectResult);
		errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
		executorServiceOut.submit(outputGobbler);
		executorServiceErr.submit(errorGobbler);
		if (process.waitFor() != 0) {
			System.err.println("Could not get AWS Lambda arn for " + functionName);
			executorServiceOut.shutdown();
			executorServiceErr.shutdown();
			process.destroy();
			return "";
		}
		String lambdaARN = lambdaArnReplyCollector.getResult();
		process.destroy();
		System.out.println("Get AWS Lambda arn completed for " + functionName);

		executorServiceOut.shutdown();
		executorServiceErr.shutdown();

		return lambdaARN;
	}

	private static void deployOnAmazonRESTFunctions(String functionName, String runtime, String entryPoint,
												  Integer timeout, Integer memory, String region,
												  String zipFolderAbsolutePath, String zipFileName, boolean handler)
			throws IOException, InterruptedException {

		System.out.println("\n" + "\u001B[33m" +
				"Deploying \"" + functionName + "\" to Amazon Web Services..." +
				"\u001B[0m" + "\n");

		String lambdaARN = deployOnAmazonLambdaFunctions(functionName, runtime, entryPoint, timeout, memory, region,
				zipFolderAbsolutePath, zipFileName);
		if (lambdaARN.equals("")) {
			return;
		}

		Process process;
		StreamGobbler outputGobbler;
		StreamGobbler errorGobbler;
		ExecutorService executorServiceOut = Executors.newSingleThreadExecutor();
		ExecutorService executorServiceErr = Executors.newSingleThreadExecutor();

		// create api
		String cmdApiCreation = AmazonCommandUtility.buildGatewayApiCreationCommand(functionName,
				functionName + " function API", region);
		process = buildCommand(cmdApiCreation).start();
		errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
		executorServiceErr.submit(errorGobbler);
		if (process.waitFor() != 0) {
			System.err.println("Could not create api on API Gateway for " + functionName);
			executorServiceOut.shutdown();
			executorServiceErr.shutdown();
			process.destroy();
			return;
		}
		process.destroy();
		System.out.println("Create api on API Gateway completed for " + functionName);

		// get api id
		String cmdApiIdGetter = AmazonCommandUtility.buildGatewayApiIdGetterCommand(functionName, region);
		process = buildCommand(cmdApiIdGetter).start();
		ReplyCollector apiIdReplyCollector = new ReplyCollector();
		outputGobbler = new StreamGobbler(process.getInputStream(),
				apiIdReplyCollector::collectResult);
		errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
		executorServiceOut.submit(outputGobbler);
		executorServiceErr.submit(errorGobbler);
		if (process.waitFor() != 0) {
			System.err.println("Could not get api id for " + functionName);
			executorServiceOut.shutdown();
			executorServiceErr.shutdown();
			process.destroy();
			return;
		}
		String apiId = apiIdReplyCollector.getResult();
		if (apiId.contains("\t")) {
			System.err.println("Too many APIs with the same name ('" + functionName +
					"'), could not continue execution");
			executorServiceOut.shutdown();
			executorServiceErr.shutdown();
			process.destroy();
			return;
		}
		process.destroy();
		System.out.println("Get api id completed for " + functionName);

		// get api parent id
		String cmdApiParentIdGetter = AmazonCommandUtility.buildGatewayApiParentIdGetterCommand(apiId, region);
		process = buildCommand(cmdApiParentIdGetter).start();
		ReplyCollector apiParentIdReplyCollector = new ReplyCollector();
		outputGobbler = new StreamGobbler(process.getInputStream(), apiParentIdReplyCollector::collectResult);
		errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
		executorServiceOut.submit(outputGobbler);
		executorServiceErr.submit(errorGobbler);
		if (process.waitFor() != 0) {
			System.err.println("Could not get api parent id for " + functionName);
			executorServiceOut.shutdown();
			executorServiceErr.shutdown();
			process.destroy();
			return;
		}
		String apiParentId = apiParentIdReplyCollector.getResult();
		process.destroy();
		System.out.println("Get api parent id completed for " + functionName);

		// create resource on api
		String cmdResourceApiCreation = AmazonCommandUtility.buildGatewayResourceApiCreationCommand(functionName, apiId,
				apiParentId, region);
		process = buildCommand(cmdResourceApiCreation).start();
		errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
		executorServiceErr.submit(errorGobbler);
		if (process.waitFor() != 0) {
			System.err.println("Could not create resource on api for " + functionName);
			executorServiceOut.shutdown();
			executorServiceErr.shutdown();
			process.destroy();
			return;
		}
		process.destroy();
		System.out.println("Create resource on api completed for " + functionName);

		// get api resource id
		String cmdResourceApiIdGetter = AmazonCommandUtility.buildGatewayResourceApiIdGetterCommand(functionName, apiId,
				region);
		process = buildCommand(cmdResourceApiIdGetter).start();
		ReplyCollector apiResourceIdReplyCollector = new ReplyCollector();
		outputGobbler = new StreamGobbler(process.getInputStream(), apiResourceIdReplyCollector::collectResult);
		errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
		executorServiceOut.submit(outputGobbler);
		executorServiceErr.submit(errorGobbler);
		if (process.waitFor() != 0) {
			System.err.println("Could not get api resource id for " + functionName);
			executorServiceOut.shutdown();
			executorServiceErr.shutdown();
			process.destroy();
			return;
		}
		String apiResourceId = apiResourceIdReplyCollector.getResult();
		process.destroy();
		System.out.println("Get api resource id completed for " + functionName);

		// create api method
		String cmdApiMethodCreation = AmazonCommandUtility.buildGatewayApiMethodOnResourceCreationCommand(apiId,
				apiResourceId, region);
		process = buildCommand(cmdApiMethodCreation).start();
		errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
		executorServiceErr.submit(errorGobbler);
		if (process.waitFor() != 0) {
			System.err.println("Could not create api method for " + functionName);
			executorServiceOut.shutdown();
			executorServiceErr.shutdown();
			process.destroy();
			return;
		}
		process.destroy();
		System.out.println("Create api method completed for " + functionName);

		// link api method and lambda function
		String cmdApiLinkage = AmazonCommandUtility.buildGatewayLambdaLinkageCommand(apiId, apiResourceId, lambdaARN,
				region);
		process = buildCommand(cmdApiLinkage).start();
		errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
		executorServiceErr.submit(errorGobbler);
		if (process.waitFor() != 0) {
			System.err.println("Could not link api method and lambda function '" + functionName + "'");
			executorServiceOut.shutdown();
			executorServiceErr.shutdown();
			process.destroy();
			return;
		}
		process.destroy();
		System.out.println("Link api method and lambda function '" + functionName + "' completed");

		// deploy api
		String cmdApiDeploy = AmazonCommandUtility.buildGatewayDeploymentCreationCommand(apiId, "benchmark",
				region);
		process = buildCommand(cmdApiDeploy).start();
		errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
		executorServiceErr.submit(errorGobbler);
		if (process.waitFor() != 0) {
			System.err.println("Could not deploy api for " + functionName);
			executorServiceOut.shutdown();
			executorServiceErr.shutdown();
			process.destroy();
			return;
		}
		process.destroy();
		System.out.println("Deploy api completed for " + functionName);

		// grant gateway permission for lambda function execution
		String cmdApiLambdaAuth = AmazonCommandUtility.buildGatewayLambdaAuthCommand(functionName, apiId, lambdaARN,
				region);
		process = buildCommand(cmdApiLambdaAuth).start();
		errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
		executorServiceErr.submit(errorGobbler);
		if (process.waitFor() != 0) {
			System.err.println("Could not authorize api gateway for '" + functionName + "' execution");
			executorServiceOut.shutdown();
			executorServiceErr.shutdown();
			process.destroy();
			return;
		}
		process.destroy();
		System.out.println("Authorize api gateway for '" + functionName + "' execution completed");

		String url = "https://" + apiId + ".execute-api." + region + ".amazonaws.com/benchmark/" + functionName;
		System.out.println("\u001B[32m" + "Deployed function to: " + url + "\u001B[0m");

		executorServiceOut.shutdown();
		executorServiceErr.shutdown();
		process.destroy();

		if (handler) {
			CompositionRepositoryDAO.persistAmazonHandler(functionName, url, apiId, region);
		} else {
			FunctionsRepositoryDAO.persistAmazon(functionName, url, apiId, region);
		}
	}

	protected static void removeGoogleFunction(String functionName, String region) {
		String cmd = GoogleCommandUtility.buildGoogleCloudFunctionsRemoveCommand(functionName, region);
		ExecutorService executorServiceOut = Executors.newSingleThreadExecutor();
		ExecutorService executorServiceErr = Executors.newSingleThreadExecutor();

		try {
			Process process = buildCommand(cmd).start();
			StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
			StreamGobbler errorGobbler = new StreamGobbler(process.getInputStream(), System.err::println);

			executorServiceOut.submit(outputGobbler);
			executorServiceErr.submit(errorGobbler);

			if (process.waitFor() != 0) {
				System.err.println("Could not delete google function '" + functionName + "'");
			} else {
				System.out.println("'" + functionName + "' function removed!");
			}
			process.destroy();
		} catch (InterruptedException | IOException e) {
			System.err.println("Could not delete google function '" + functionName + "': " + e.getMessage());
		} finally {
			executorServiceOut.shutdown();
			executorServiceErr.shutdown();
		}
	}

	public static void cleanupGoogleCloudFunctions() {

		System.out.println("\n" + "\u001B[33m" +
				"Cleaning up Google functions environment..." +
				"\u001B[0m" + "\n");

		List<FunctionalityData> toRemove = FunctionsRepositoryDAO.getGoogles();
		if (toRemove == null) {
			return;
		}

		for (FunctionalityData elem : toRemove) {
			removeGoogleFunction(elem.getFunctionalityName(), elem.getRegion());
		}

		System.out.println("\u001B[32m" + "\nGoogle cleanup completed!\n" + "\u001B[0m");

		FunctionsRepositoryDAO.dropGoogle();
	}

	protected static void removeLambdaFunction(String functionName, String region) {
		String cmd = AmazonCommandUtility.buildLambdaDropCommand(functionName, region);
		ExecutorService executorServiceOut = Executors.newSingleThreadExecutor();
		ExecutorService executorServiceErr = Executors.newSingleThreadExecutor();

		try {
			Process process = buildCommand(cmd).start();
			StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
			StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);

			executorServiceOut.submit(outputGobbler);
			executorServiceErr.submit(errorGobbler);

			if (process.waitFor() != 0) {
				System.err.println("Could not delete lambda function '" + functionName + "'");
			} else {
				System.out.println("'" + functionName + "' function removed!");
			}
			process.destroy();
		} catch (InterruptedException | IOException e) {
			System.err.println("Could not delete lambda function '" + functionName + "': " +
					e.getMessage());
		} finally {
			executorServiceOut.shutdown();
			executorServiceErr.shutdown();
		}
	}

	protected static void removeGatewayApi(String functionName, String apiId, String region) {
		String cmd = AmazonCommandUtility.buildGatewayDropCommand(apiId, region);
		ExecutorService executorServiceOut = Executors.newSingleThreadExecutor();
		ExecutorService executorServiceErr = Executors.newSingleThreadExecutor();

		try {
			Process process = buildCommand(cmd).start();
			StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
			StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);

			executorServiceOut.submit(outputGobbler);
			executorServiceErr.submit(errorGobbler);

			if (process.waitFor() != 0) {
				System.err.println("Could not delete gateway api '" + functionName + "'");
			} else {
				System.out.println("'" + functionName + "' api removed!");
			}
			process.destroy();
		} catch (InterruptedException | IOException e) {
			System.err.println("Could not delete gateway api '" + functionName + "': " + e.getMessage());
		} finally {
			executorServiceOut.shutdown();
			executorServiceErr.shutdown();
		}
	}

	public static void cleanupAmazonRESTFunctions() {

		System.out.println("\n" + "\u001B[33m" +
				"Cleaning up Amazon functions environment..." +
				"\u001B[0m" + "\n");

		List<FunctionalityData> toRemove = FunctionsRepositoryDAO.getAmazons();
		if (toRemove == null) {
			return;
		}

		for (FunctionalityData elem : toRemove) {
			removeLambdaFunction(elem.getFunctionalityName(), elem.getRegion());
			removeGatewayApi(elem.getFunctionalityName(), elem.getId(), elem.getRegion());
			waitFor("Cleanup", 30);
		}

		System.out.println("\u001B[32m" + "\nAmazon cleanup completed!\n" + "\u001B[0m");

		FunctionsRepositoryDAO.dropAmazon();
	}
}