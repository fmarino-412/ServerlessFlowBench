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
				directoryAbsolutePath, true);
	}

	public static void deployOnGoogleCloudFunction(String functionName, String runtime, String entryPoint,
													Integer timeout, Integer memory_mb, String region,
													String directoryAbsolutePath)
			throws IOException, InterruptedException {
		deployOnGoogleCloudFunctions(functionName, runtime, entryPoint, timeout, memory_mb, region,
				directoryAbsolutePath, false);
	}

	private static void deployOnGoogleCloudFunctions(String functionName, String runtime, String entryPoint,
													Integer timeout, Integer memory_mb, String region,
													String directoryAbsolutePath, boolean handler)
			throws IOException, InterruptedException {

		System.out.println("\n" + "\u001B[33m" +
				"Deploying \"" + functionName + "\" to Google Cloud Platform..." +
				"\u001B[0m" + "\n");

		// build command
		String cmd = GoogleCommandUtility.buildGoogleCloudFunctionsDeployCommand(functionName, runtime, entryPoint,
				timeout, memory_mb, region, directoryAbsolutePath);

		// start process execution
		Process process = buildCommand(cmd).start();

		// create, execute and submit output gobblers
		UrlFinder urlFinder = new UrlFinder();
		StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(),
				urlFinder::findGoogleCloudFunctionsUrl);
		StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), System.out::println);

		ExecutorService executorServiceOut = Executors.newSingleThreadExecutor();
		ExecutorService executorServiceErr = Executors.newSingleThreadExecutor();

		executorServiceOut.submit(outputGobbler);
		executorServiceErr.submit(errorGobbler);

		// wait for completion and free environment
		int exitCode = process.waitFor();
		assert exitCode == 0;

		String url = urlFinder.getResult();
		System.out.println("\u001B[32m" + "Deployed function to: " + url + "\u001B[0m");

		process.destroy();
		executorServiceOut.shutdown();
		executorServiceErr.shutdown();

		if (handler) {
			CompositionRepositoryDAO.persistGoogleHandler(functionName, url, region);
		} else {
			FunctionsRepositoryDAO.persistGoogle(functionName, url, region);
		}
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

	private static void deployOnAmazonRESTFunctions(String functionName, String runtime, String entryPoint,
												  Integer timeout, Integer memory, String region,
												  String zipFolderAbsolutePath, String zipFileName, boolean handler)
			throws IOException, InterruptedException {

		Process process;
		StreamGobbler outputGobbler;
		StreamGobbler errorGobbler;
		ExecutorService executorServiceOut = Executors.newSingleThreadExecutor();
		ExecutorService executorServiceErr = Executors.newSingleThreadExecutor();
		int exitCode;

		System.out.println("\n" + "\u001B[33m" +
				"Deploying \"" + functionName + "\" to Amazon Web Services..." +
				"\u001B[0m" + "\n");

		// deploy function
		String cmdDeploy = AmazonCommandUtility.buildLambdaFunctionDeployCommand(functionName, runtime, entryPoint,
				timeout, memory, region, zipFolderAbsolutePath, zipFileName);
		process = buildCommand(cmdDeploy).start();
		errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
		executorServiceErr.submit(errorGobbler);
		exitCode = process.waitFor();
		if (exitCode != 0) {
			System.err.println("Could not deploy function on AWS Lambda");
			executorServiceOut.shutdown();
			executorServiceErr.shutdown();
			process.destroy();
			return;
		}
		process.destroy();
		System.out.println("Deploy function on AWS Lambda completed");

		// get lambda arn
		String cmdArnGetter = AmazonCommandUtility.buildLambdaArnGetterCommand(functionName, region);
		process = buildCommand(cmdArnGetter).start();
		ReplyCollector lambdaArnReplyCollector = new ReplyCollector();
		outputGobbler = new StreamGobbler(process.getInputStream(), lambdaArnReplyCollector::collectResult);
		errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
		executorServiceOut.submit(outputGobbler);
		executorServiceErr.submit(errorGobbler);
		exitCode = process.waitFor();
		if (exitCode != 0) {
			System.err.println("Could not get AWS Lambda arn");
			executorServiceOut.shutdown();
			executorServiceErr.shutdown();
			process.destroy();
			return;
		}
		String lambdaARN = lambdaArnReplyCollector.getResult();
		process.destroy();
		System.out.println("Get AWS Lambda arn completed");

		// create api
		String cmdApiCreation = AmazonCommandUtility.buildGatewayApiCreationCommand(functionName,
				functionName + " function API", region);
		process = buildCommand(cmdApiCreation).start();
		errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
		executorServiceErr.submit(errorGobbler);
		exitCode = process.waitFor();
		if (exitCode != 0) {
			System.err.println("Could not create api on API Gateway");
			executorServiceOut.shutdown();
			executorServiceErr.shutdown();
			process.destroy();
			return;
		}
		process.destroy();
		System.out.println("Create api on API Gateway completed");

		// get api id
		String cmdApiIdGetter = AmazonCommandUtility.buildGatewayApiIdGetterCommand(functionName, region);
		process = buildCommand(cmdApiIdGetter).start();
		ReplyCollector apiIdReplyCollector = new ReplyCollector();
		outputGobbler = new StreamGobbler(process.getInputStream(),
				apiIdReplyCollector::collectResult);
		errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
		executorServiceOut.submit(outputGobbler);
		executorServiceErr.submit(errorGobbler);
		exitCode = process.waitFor();
		if (exitCode != 0) {
			System.err.println("Could not get api id");
			executorServiceOut.shutdown();
			executorServiceErr.shutdown();
			process.destroy();
			return;
		}
		String apiId = apiIdReplyCollector.getResult();
		if (apiId.contains("\t")) {
			System.err.println("Too many APIs with the same name, could not continue execution");
			executorServiceOut.shutdown();
			executorServiceErr.shutdown();
			process.destroy();
			return;
		}
		process.destroy();
		System.out.println("Get api id completed");

		// get api parent id
		String cmdApiParentIdGetter = AmazonCommandUtility.buildGatewayApiParentIdGetterCommand(apiId, region);
		process = buildCommand(cmdApiParentIdGetter).start();
		ReplyCollector apiParentIdReplyCollector = new ReplyCollector();
		outputGobbler = new StreamGobbler(process.getInputStream(), apiParentIdReplyCollector::collectResult);
		errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
		executorServiceOut.submit(outputGobbler);
		executorServiceErr.submit(errorGobbler);
		exitCode = process.waitFor();
		if (exitCode != 0) {
			System.err.println("Could not get api parent id");
			executorServiceOut.shutdown();
			executorServiceErr.shutdown();
			process.destroy();
			return;
		}
		String apiParentId = apiParentIdReplyCollector.getResult();
		process.destroy();
		System.out.println("Get api parent id completed");

		// create resource on api
		String cmdResourceApiCreation = AmazonCommandUtility.buildGatewayResourceApiCreationCommand(functionName, apiId,
				apiParentId, region);
		process = buildCommand(cmdResourceApiCreation).start();
		errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
		executorServiceErr.submit(errorGobbler);
		exitCode = process.waitFor();
		if (exitCode != 0) {
			System.err.println("Could not create resource on api");
			executorServiceOut.shutdown();
			executorServiceErr.shutdown();
			process.destroy();
			return;
		}
		process.destroy();
		System.out.println("Create resource on api completed");

		// get api resource id
		String cmdResourceApiIdGetter = AmazonCommandUtility.buildGatewayResourceApiIdGetterCommand(functionName, apiId,
				region);
		process = buildCommand(cmdResourceApiIdGetter).start();
		ReplyCollector apiResourceIdReplyCollector = new ReplyCollector();
		outputGobbler = new StreamGobbler(process.getInputStream(), apiResourceIdReplyCollector::collectResult);
		errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
		executorServiceOut.submit(outputGobbler);
		executorServiceErr.submit(errorGobbler);
		exitCode = process.waitFor();
		if (exitCode != 0) {
			System.err.println("Could not get api resource id");
			executorServiceOut.shutdown();
			executorServiceErr.shutdown();
			process.destroy();
			return;
		}
		String apiResourceId = apiResourceIdReplyCollector.getResult();
		process.destroy();
		System.out.println("Get api resource id completed");

		// create api method
		String cmdApiMethodCreation = AmazonCommandUtility.buildGatewayApiMethodOnResourceCreationCommand(apiId,
				apiResourceId, region);
		process = buildCommand(cmdApiMethodCreation).start();
		errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
		executorServiceErr.submit(errorGobbler);
		exitCode = process.waitFor();
		if (exitCode != 0) {
			System.err.println("Could not create api method");
			executorServiceOut.shutdown();
			executorServiceErr.shutdown();
			process.destroy();
			return;
		}
		process.destroy();
		System.out.println("Create api method completed");

		// link api method and lambda function
		String cmdApiLinkage = AmazonCommandUtility.buildGatewayLambdaLinkageCommand(apiId, apiResourceId, lambdaARN,
				region);
		process = buildCommand(cmdApiLinkage).start();
		errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
		executorServiceErr.submit(errorGobbler);
		exitCode = process.waitFor();
		if (exitCode != 0) {
			System.err.println("Could not link api method and lambda function");
			executorServiceOut.shutdown();
			executorServiceErr.shutdown();
			process.destroy();
			return;
		}
		process.destroy();
		System.out.println("Link api method and lambda function completed");

		// deploy api
		String cmdApiDeploy = AmazonCommandUtility.buildGatewayDeploymentCreationCommand(apiId, "benchmark",
				region);
		process = buildCommand(cmdApiDeploy).start();
		errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
		executorServiceErr.submit(errorGobbler);
		exitCode = process.waitFor();
		if (exitCode != 0) {
			System.err.println("Could not deploy api");
			executorServiceOut.shutdown();
			executorServiceErr.shutdown();
			process.destroy();
			return;
		}
		process.destroy();
		System.out.println("Deploy api completed");

		// grant gateway permission for lambda function execution
		String cmdApiLambdaAuth = AmazonCommandUtility.buildGatewayLambdaAuthCommand(functionName, apiId, lambdaARN,
				region);
		process = buildCommand(cmdApiLambdaAuth).start();
		errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
		executorServiceErr.submit(errorGobbler);
		exitCode = process.waitFor();
		if (exitCode != 0) {
			System.err.println("Could not authorize api gateway for lambda execution");
			executorServiceOut.shutdown();
			executorServiceErr.shutdown();
			process.destroy();
			return;
		}
		process.destroy();
		System.out.println("Authorize api gateway for lambda execution completed");

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

	public static void cleanupGoogleCloudFunctions() {

		System.out.println("\n" + "\u001B[33m" +
				"Cleaning up Google functions environment..." +
				"\u001B[0m" + "\n");

		List<FunctionalityData> toRemove = FunctionsRepositoryDAO.getGoogles();
		if (toRemove == null) {
			return;
		}
		String cmd;
		Process process;
		StreamGobbler outGobbler;
		StreamGobbler errGobbler;

		ExecutorService output = Executors.newSingleThreadExecutor();
		ExecutorService error = Executors.newSingleThreadExecutor();

		for (FunctionalityData elem : toRemove) {
			cmd = GoogleCommandUtility.buildGoogleCloudFunctionsRemoveCommand(elem.getFunctionName(), elem.getRegion());
			try {
				process = buildCommand(cmd).start();
				outGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
				errGobbler = new StreamGobbler(process.getInputStream(), System.err::println);

				output.submit(outGobbler);
				error.submit(errGobbler);

				if (process.waitFor() != 0) {
					System.err.println("Could not delete google function '" + elem.getFunctionName() + "'");
				} else {
					System.out.println("'" + elem.getFunctionName() + "' function removed!");
				}
				process.destroy();
			} catch (InterruptedException | IOException e) {
				System.err.println("Could not delete google function '" + elem.getFunctionName() + "': " +
						e.getMessage());
			}
		}

		output.shutdown();
		error.shutdown();

		System.out.println("\u001B[32m" + "\nGoogle cleanup completed!\n" + "\u001B[0m");

		FunctionsRepositoryDAO.dropGoogle();
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
			removeLambdaFunction(elem.getFunctionName(), elem.getRegion());
			removeGatewayApi(elem.getFunctionName(), elem.getId(), elem.getRegion());
			try {
				Thread.sleep(30000);
			} catch (InterruptedException ignored) {
			}
		}

		System.out.println("\u001B[32m" + "\nAmazon cleanup completed!\n" + "\u001B[0m");

		FunctionsRepositoryDAO.dropAmazon();
	}

	protected static void removeLambdaFunction(String functionName, String region) {
		String cmd = AmazonCommandUtility.buildLambdaDropCommand(functionName, region);
		ExecutorService executorServiceOut = Executors.newSingleThreadExecutor();
		ExecutorService executorServiceErr = Executors.newSingleThreadExecutor();

		try {
			Process process = buildCommand(cmd).start();
			StreamGobbler outGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
			StreamGobbler errGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);

			executorServiceOut.submit(outGobbler);
			executorServiceErr.submit(errGobbler);

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
			StreamGobbler outGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
			StreamGobbler errGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);

			executorServiceOut.submit(outGobbler);
			executorServiceErr.submit(errGobbler);

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
}