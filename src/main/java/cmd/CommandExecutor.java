package cmd;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandExecutor {

	public static void deployOnGoogleCloudPlatform(String functionName, String runtime, String entryPoint,
												   Integer timeout, Integer memory_mb, String region,
												   String directoryAbsolutePath)
			throws IOException, InterruptedException {

		// build command
		String cmd = GoogleCommandUtility.buildGoogleCloudFunctionsDeployCommand(functionName, runtime, entryPoint,
				timeout, memory_mb, region, directoryAbsolutePath);

		// start process execution
		Process process = buildCommand(cmd).start();

		// create, execute and submit output gobblers
		StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(),
				UrlFinder::findGoogleCloudFunctionsUrl);
		StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);

		ExecutorService executorServiceOut = Executors.newSingleThreadExecutor();
		ExecutorService executorServiceErr = Executors.newSingleThreadExecutor();

		executorServiceOut.submit(outputGobbler);
		executorServiceErr.submit(errorGobbler);

		// wait for completion and free environment
		int exitCode = process.waitFor();
		assert exitCode == 0;

		process.destroy();
		executorServiceOut.shutdown();
		executorServiceErr.shutdown();
	}

	public static void deployOnAmazonWebServices(String functionName, String runtime, String entryPoint,
												 Integer timeout, Integer memory, String region,
												 String zipFolderAbsolutePath, String zipFileName)
			throws IOException, InterruptedException {

		Process process;
		StreamGobbler outputGobbler;
		ExecutorService executorServiceOut = Executors.newSingleThreadExecutor();

		// deploy function
		String cmdDeploy = AmazonCommandUtility.buildLambdaFunctionDeployCommand(functionName, runtime, entryPoint,
				timeout, memory, region, zipFolderAbsolutePath, zipFileName);
		process = buildCommand(cmdDeploy).start();
		outputGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
		executorServiceOut.submit(outputGobbler);
		// TODO: DEBUG - la crea ma va in exit code != 0...
		if (process.waitFor() != 0) {
			System.err.println("Could not deploy function on AWS Lambda");
			executorServiceOut.shutdown();
			process.destroy();
			return;
		}
		process.destroy();

		// get lambda arn
		String cmdArnGetter = AmazonCommandUtility.buildLambdaArnGetterCommand(functionName, region);
		process = buildCommand(cmdArnGetter).start();
		outputGobbler = new StreamGobbler(process.getInputStream(),
				a -> ReplyCollector.getInstance().collectResult(a));
		executorServiceOut.submit(outputGobbler);
		if (process.waitFor() != 0) {
			System.err.println("Could not deploy function on AWS Lambda");
			executorServiceOut.shutdown();
			ReplyCollector.getInstance().getResult();
			process.destroy();
			return;
		}
		String lambdaARN = ReplyCollector.getInstance().getResult();
		process.destroy();

		// create api
		String cmdApiCreation = AmazonCommandUtility.buildGatewayApiCreationCommand(functionName,
				functionName + " function API", region);
		process = buildCommand(cmdApiCreation).start();
		outputGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
		executorServiceOut.submit(outputGobbler);
		if (process.waitFor() != 0) {
			System.err.println("Could not create api on API Gateway");
			executorServiceOut.shutdown();
			process.destroy();
			return;
		}
		process.destroy();

		// get api id
		String cmdApiIdGetter = AmazonCommandUtility.buildGatewayApiIdGetterCommand(functionName, region);
		process = buildCommand(cmdApiIdGetter).start();
		outputGobbler = new StreamGobbler(process.getInputStream(),
				a -> ReplyCollector.getInstance().collectResult(a));
		executorServiceOut.submit(outputGobbler);
		if (process.waitFor() != 0) {
			System.err.println("Could not get api id");
			executorServiceOut.shutdown();
			ReplyCollector.getInstance().getResult();
			process.destroy();
			return;
		}
		String apiId = ReplyCollector.getInstance().getResult();
		process.destroy();

		// get api parent id
		String cmdApiParentIdGetter = AmazonCommandUtility.buildGatewayApiParentIdGetterCommand(apiId, region);
		process = buildCommand(cmdApiParentIdGetter).start();
		outputGobbler = new StreamGobbler(process.getInputStream(),
				a -> ReplyCollector.getInstance().collectResult(a));
		executorServiceOut.submit(outputGobbler);
		if (process.waitFor() != 0) {
			System.err.println("Could not get api parent id");
			executorServiceOut.shutdown();
			ReplyCollector.getInstance().getResult();
			process.destroy();
			return;
		}
		String apiParentId = ReplyCollector.getInstance().getResult();
		process.destroy();

		// create resource on api
		String cmdResourceApiCreation = AmazonCommandUtility.buildGatewayResourceApiCreationCommand(functionName, apiId,
				apiParentId, region);
		process = buildCommand(cmdResourceApiCreation).start();
		outputGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
		executorServiceOut.submit(outputGobbler);
		if (process.waitFor() != 0) {
			System.err.println("Could not create resource on api");
			executorServiceOut.shutdown();
			process.destroy();
			return;
		}
		process.destroy();

		// get api resource id
		String cmdResourceApiIdGetter = AmazonCommandUtility.buildGatewayResourceApiIdGetterCommand(functionName, apiId,
				region);
		process = buildCommand(cmdResourceApiIdGetter).start();
		outputGobbler = new StreamGobbler(process.getInputStream(),
				a -> ReplyCollector.getInstance().collectResult(a));
		executorServiceOut.submit(outputGobbler);
		if (process.waitFor() != 0) {
			System.err.println("Could not get api resource id");
			executorServiceOut.shutdown();
			ReplyCollector.getInstance().getResult();
			process.destroy();
			return;
		}
		String apiResourceId = ReplyCollector.getInstance().getResult();
		process.destroy();

		// create api method
		String cmdApiMethodCreation = AmazonCommandUtility.buildGatewayApiMethodOnResourceCreationCommand(apiId,
				apiResourceId, region);
		process = buildCommand(cmdApiMethodCreation).start();
		outputGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
		executorServiceOut.submit(outputGobbler);
		if (process.waitFor() != 0) {
			System.err.println("Could not create api method");
			executorServiceOut.shutdown();
			process.destroy();
			return;
		}
		process.destroy();

		// link api method and lambda function
		String cmdApiLinkage = AmazonCommandUtility.buildGatewayLambdaLinkageCommand(apiId, apiResourceId, lambdaARN,
				region);
		process = buildCommand(cmdApiLinkage).start();
		outputGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
		executorServiceOut.submit(outputGobbler);
		if (process.waitFor() != 0) {
			System.err.println("Could not link api method and lambda function");
			executorServiceOut.shutdown();
			process.destroy();
			return;
		}
		process.destroy();

		// deploy api
		String cmdApiDeploy = AmazonCommandUtility.buildGatewayDeploymentCreationCommand(apiId, "benchmark",
				region);
		process = buildCommand(cmdApiDeploy).start();
		outputGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
		executorServiceOut.submit(outputGobbler);
		if (process.waitFor() != 0) {
			System.err.println("Could not deploy api");
			executorServiceOut.shutdown();
			process.destroy();
			return;
		}
		process.destroy();

		// grant gateway permission for lambda function execution
		String cmdApiLambdaAuth = AmazonCommandUtility.buildGatewayLambdaAuthCommand(functionName, apiId, lambdaARN,
				region);
		process = buildCommand(cmdApiLambdaAuth).start();
		outputGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
		executorServiceOut.submit(outputGobbler);
		if (process.waitFor() != 0) {
			System.err.println("Could not authorize api gateway for lambda execution");
			executorServiceOut.shutdown();
			process.destroy();
			return;
		}
		process.destroy();

		String url = "https://" + apiId + ".execute-api." + region + ".amazonaws.com/benchmark/" + functionName;
		System.out.println("Deployed function to: " + url);
	}

	public static ProcessBuilder buildCommand(String cmd) {
		ProcessBuilder builder = new ProcessBuilder();

		// define command
		if (GoogleCommandUtility.isWindows()) {
			// TODO: TEST
			builder.command("cmd.exe", "/c", cmd);
		} else {
			builder.command("sh", "-c", cmd);
		}

		return builder;
	}
}
