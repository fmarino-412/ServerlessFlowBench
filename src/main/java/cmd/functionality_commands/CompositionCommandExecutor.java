package cmd.functionality_commands;

import cmd.CommandExecutor;
import cmd.StreamGobbler;
import cmd.functionality_commands.output_parsing.ReplyCollector;
import databases.mysql.daos.CompositionRepositoryDAO;
import utility.PropertiesManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompositionCommandExecutor extends CommandExecutor {

	private static final String HANDLER_NAME = "__handler__";
	private static final String HANDLER_RUNTIME = AmazonCommandUtility.PYTHON_3_7_RUNTIME;
	private static final String HANDLER_ENTRY_POINT = "orchestration_handler.lambda_handler";
	private static final String HANDLER_REGION = AmazonCommandUtility.NORTH_VIRGINIA;
	private static final String HANDLER_DIR_PATH = PropertiesManager.getInstance().
			getProperty(PropertiesManager.AWS_HANDLER_PATH);
	private static final String HANDLER_ZIP_FILE = "orchestration_handler.zip";
	private static final Integer HANDLER_TIMEOUT = 300;
	private static final Integer HANDLER_MEMORY = 128;

	private static final String PLACEHOLDER = "__PLACEHOLDER__";


	private static void deployAmazonCompositionHandler() throws IOException, InterruptedException {
		if (CompositionRepositoryDAO.existsAmazonHandler(null)) {
			return;
		}
		FunctionCommandExecutor.deployAmazonRESTHandlerFunction(HANDLER_NAME, HANDLER_RUNTIME, HANDLER_ENTRY_POINT,
				HANDLER_TIMEOUT, HANDLER_MEMORY, HANDLER_REGION, HANDLER_DIR_PATH, HANDLER_ZIP_FILE);

	}

	public static void deployOnAmazonComposition(String machineName, String contentFolderAbsolutePath,
												 String machineRegion, String jsonFileName, String[] functionNames,
												 String[] runtimes, String[] entryPoints, Integer[] timeouts,
												 Integer[] memory_mbs, String[] regions, String[] zipFileNames)
			throws IOException, InterruptedException {

		assert functionNames.length == runtimes.length;
		assert functionNames.length == entryPoints.length;
		assert functionNames.length == timeouts.length;
		assert functionNames.length == memory_mbs.length;
		assert functionNames.length == regions.length;
		assert functionNames.length == zipFileNames.length;

		deployAmazonCompositionHandler();

		System.out.println("\n" + "\u001B[33m" +
				"Deploying \"" + machineName + "\" to Amazon Web Services..." +
				"\u001B[0m" + "\n");

		String json = "";

		try {
			json = new String(Files.readAllBytes(Paths.get(contentFolderAbsolutePath + "/" + jsonFileName)));
		} catch (IOException e) {
			System.err.println("Could not load JSON file: " + e.getMessage());
		}

		// declare and initialize variables
		String cmd;
		Process process;
		StreamGobbler outputGobbler;
		StreamGobbler errorGobbler;
		ExecutorService executorServiceOut = Executors.newSingleThreadExecutor();
		ExecutorService executorServiceErr = Executors.newSingleThreadExecutor();

		ReplyCollector arnCollector;
		ArrayList<String> functionArns = new ArrayList<>();

		// publish functions
		for (int i = 0; i < functionNames.length; i++) {
			// deploy function
			cmd = AmazonCommandUtility.buildLambdaFunctionDeployCommand(functionNames[i], runtimes[i], entryPoints[i],
					timeouts[i], memory_mbs[i], regions[i], contentFolderAbsolutePath, zipFileNames[i]);
			process = buildCommand(cmd).start();
			errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
			executorServiceErr.submit(errorGobbler);
			if (process.waitFor() != 0) {
				System.err.println("Could not deploy " + functionNames[i] + " on AWS Lambda");
				executorServiceOut.shutdown();
				executorServiceErr.shutdown();
				process.destroy();
				return;
			}
			process.destroy();
			System.out.println("Function " + functionNames[i] + " deployed!");

			// get lambda arn
			cmd = AmazonCommandUtility.buildLambdaArnGetterCommand(functionNames[i], regions[i]);
			process = buildCommand(cmd).start();
			arnCollector = new ReplyCollector();
			outputGobbler = new StreamGobbler(process.getInputStream(), arnCollector::collectResult);
			errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
			executorServiceOut.submit(outputGobbler);
			executorServiceErr.submit(errorGobbler);
			if (process.waitFor() != 0) {
				System.err.println("Could not get ARN of " + functionNames[i] + " from AWS Lambda");
				executorServiceOut.shutdown();
				executorServiceErr.shutdown();
				process.destroy();
				return;
			}
			functionArns.add(arnCollector.getResult());
			process.destroy();
		}

		// json file: replacing placeholders
		json = json.replaceAll("\n", " ").replaceAll(" {2}", " ");
		for (int i = 0; i < functionNames.length; i++) {
			json = json.replaceFirst(PLACEHOLDER, functionArns.get(i) + ":\\$LATEST");
		}
		json = "'" + json + "'";

		Pattern pattern;
		Matcher matcher;
		String arnRegex = "(\"stateMachineArn\":\\s+\")(.*)(\",)";

		cmd = AmazonCommandUtility.buildStepFunctionCreationCommand(machineName, machineRegion, json);
		process = buildCommand(cmd).start();
		ReplyCollector machineArnCollector = new ReplyCollector();
		outputGobbler = new StreamGobbler(process.getInputStream(), machineArnCollector::collectResult);
		errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
		executorServiceOut.submit(outputGobbler);
		executorServiceErr.submit(errorGobbler);
		if (process.waitFor() != 0) {
			System.err.println("Could not deploy state machine on Step Functions");
			executorServiceOut.shutdown();
			executorServiceErr.shutdown();
			process.destroy();
			return;
		}
		String machineArn = machineArnCollector.getResult();
		pattern = Pattern.compile(arnRegex);
		matcher = pattern.matcher(machineArn);
		if (matcher.find()) {
			machineArn = matcher.group(2);
		} else {
			System.err.println("Could not deploy state machine on Step Functions");
			executorServiceOut.shutdown();
			executorServiceErr.shutdown();
			process.destroy();
			return;
		}

		process.destroy();
		executorServiceOut.shutdown();
		executorServiceErr.shutdown();

		CompositionRepositoryDAO.persistAmazon(machineName, machineArn, functionNames, regions);

		System.out.println("Machine " + machineName + " deployed!");
	}
}
