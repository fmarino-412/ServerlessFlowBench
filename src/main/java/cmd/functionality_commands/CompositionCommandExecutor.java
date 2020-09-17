package cmd.functionality_commands;

import cmd.CommandExecutor;
import cmd.CommandUtility;
import cmd.StreamGobbler;
import cmd.functionality_commands.output_parsing.ReplyCollector;
import cmd.functionality_commands.security.GoogleAuthClient;
import databases.mysql.FunctionalityData;
import databases.mysql.daos.CompositionRepositoryDAO;
import utility.PropertiesManager;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("DuplicatedCode")
public class CompositionCommandExecutor extends CommandExecutor {

	private static final String PLACEHOLDER = "__PLACEHOLDER__";
	private static final String  HANDLER_NAME = "h-a-n-d-l-e-r";

	private static void deployGoogleCompositionHandler() throws IOException, InterruptedException {
		if (CompositionRepositoryDAO.existsGoogleHandler(null)) {
			return;
		}
		FunctionCommandExecutor.deployGoogleCloudHandlerFunction(HANDLER_NAME,
				GoogleCommandUtility.PYTHON_3_7_RUNTIME,
				"gc_functions_handler",
				300,
				128,
				GoogleCommandUtility.IOWA,
				PropertiesManager.getInstance().getProperty(PropertiesManager.GOOGLE_HANDLER_PATH));
	}

	private static void deployAmazonCompositionHandler() throws IOException, InterruptedException {
		if (CompositionRepositoryDAO.existsAmazonHandler(null)) {
			return;
		}
		FunctionCommandExecutor.deployAmazonRESTHandlerFunction(HANDLER_NAME,
				AmazonCommandUtility.PYTHON_3_7_RUNTIME,
				"orchestration_handler.lambda_handler",
				300,
				128,
				AmazonCommandUtility.OHIO,
				PropertiesManager.getInstance().getProperty(PropertiesManager.AWS_HANDLER_PATH),
				"orchestration_handler.zip");

	}

	public static void deployOnGoogleComposition(String workflowName, String contentFolderAbsolutePath,
												 String workflowRegion, String yamlFileName, String[] functionNames,
												 String[] runtimes, String[] entryPoints, Integer[] timeouts,
												 Integer[] memory_mbs, String[] regions, String[] functionDirPaths)
			throws IOException, InterruptedException {

		assert functionNames.length == runtimes.length;
		assert functionNames.length == entryPoints.length;
		assert functionNames.length == timeouts.length;
		assert functionNames.length == memory_mbs.length;
		assert functionNames.length == regions.length;
		assert functionNames.length == functionDirPaths.length;

		deployGoogleCompositionHandler();

		System.out.println("\n" + "\u001B[33m" +
				"Deploying \"" + workflowName + "\" to Google Cloud Platform..." +
				"\u001B[0m" + "\n");

		// create temporary file in which is possible to replace PLACEHOLDER without editing original file
		Path tempYaml;
		String yaml;
		try {
			tempYaml = Files.createTempFile("temp", ".yaml");
			yaml = new String(Files.readAllBytes(Paths.get(contentFolderAbsolutePath +
					CommandUtility.getPathSep() + yamlFileName)), StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.err.println("Could not read '" + contentFolderAbsolutePath +
					CommandUtility.getPathSep() + yamlFileName + "': " + e.getMessage());
			return;
		}

		ArrayList<String> functionUrls = new ArrayList<>();

		// publish functions
		for (int i = 0; i < functionNames.length; i++) {
			functionUrls.add(FunctionCommandExecutor.deployOnGoogleCloudCompositionFunction(functionNames[i],
					runtimes[i],
					entryPoints[i],
					timeouts[i],
					memory_mbs[i],
					regions[i],
					contentFolderAbsolutePath + CommandUtility.getPathSep() + functionDirPaths[i]));
			System.out.println("'" + functionNames[i] + "' deploy on Google Cloud Platform completed");
		}
		if (functionUrls.contains("")) {
			System.err.println("Error deploying functions!");
			return;
		}

		// yaml file: replacing placeholders
		for (int i = 0; i < functionNames.length; i++) {
			yaml = yaml.replaceFirst(PLACEHOLDER, functionUrls.get(i));
		}
		// write yaml to temp file
		try {
			PrintWriter writer = new PrintWriter(tempYaml.toString());
			writer.print(yaml);
			writer.flush();
		} catch (IOException e) {
			System.err.println("Could not parse '" + yamlFileName + "': " + e.getMessage());
			return;
		}

		// declare and initialize variables
		String cmd;
		Process process;
		StreamGobbler outputGobbler;
		ExecutorService executorServiceOut = Executors.newSingleThreadExecutor();

		cmd = GoogleCommandUtility.buildGoogleCloudWorkflowsDeployCommand(workflowName, workflowRegion,
				tempYaml.getParent().toString(), tempYaml.getFileName().toString());
		process = buildCommand(cmd).start();
		outputGobbler = new StreamGobbler(process.getErrorStream(), System.out::println);
		executorServiceOut.submit(outputGobbler);
		if (process.waitFor() != 0) {
			System.err.println("Could not deploy workflow '" + workflowName + "' on Google Cloud Platform");
			executorServiceOut.shutdown();
			process.destroy();
			return;
		}

		// delete temporary file
		File file = tempYaml.toFile();
		if (!file.delete()) {
			System.err.println("WARNING:\tCould not delete temporary files, check: " + tempYaml.toString());
		}

		String url = CompositionRepositoryDAO.getGoogleHandlerUrl(null);
		if (url == null) {
			System.err.println("WARNING: Handler not found! Workflow is not reachable");
		}
		url = url + "?workflow=" + workflowName + GoogleAuthClient.getInstance().getUrlToken();
		System.out.println("\u001B[32m" + "Deployed workflow '" + workflowName + "' to: " + url + "\u001B[0m");

		process.destroy();
		executorServiceOut.shutdown();

		CompositionRepositoryDAO.persistGoogle(workflowName, workflowRegion, functionNames, regions);
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
			json = new String(Files.readAllBytes(Paths.get(contentFolderAbsolutePath + CommandUtility.getPathSep()
					+ jsonFileName)));
		} catch (IOException e) {
			System.err.println("Could not load JSON file for '" + machineName + "': " + e.getMessage());
		}

		ArrayList<String> functionArns = new ArrayList<>();

		// publish functions
		for (int i = 0; i < functionNames.length; i++) {
			// deploy function
			functionArns.add(FunctionCommandExecutor.deployOnAmazonLambdaFunctions(functionNames[i],
					runtimes[i],
					entryPoints[i],
					timeouts[i],
					memory_mbs[i],
					regions[i],
					contentFolderAbsolutePath,
					zipFileNames[i]));
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

		// declare and initialize variables
		String cmd;
		Process process;
		StreamGobbler outputGobbler;
		StreamGobbler errorGobbler;
		ExecutorService executorServiceOut = Executors.newSingleThreadExecutor();
		ExecutorService executorServiceErr = Executors.newSingleThreadExecutor();

		cmd = AmazonCommandUtility.buildStepFunctionCreationCommand(machineName, machineRegion, json);
		process = buildCommand(cmd).start();
		ReplyCollector machineArnCollector = new ReplyCollector();
		outputGobbler = new StreamGobbler(process.getInputStream(), machineArnCollector::collectResult);
		errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
		executorServiceOut.submit(outputGobbler);
		executorServiceErr.submit(errorGobbler);
		if (process.waitFor() != 0) {
			System.err.println("Could not deploy state machine '" + machineName + "' on Step Functions");
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
			System.err.println("Could not deploy state machine '" + machineName + "' on Step Functions");
			executorServiceOut.shutdown();
			executorServiceErr.shutdown();
			process.destroy();
			return;
		}

		String url = CompositionRepositoryDAO.getAmazonHandlerUrl(null);
		if (url == null) {
			System.err.println("WARNING: Handler not found! Machine is not reachable");
		}
		url = url + "?arn=" + machineArn;
		System.out.println("\u001B[32m" + "Deployed machine '" + machineName + "' to: " + url + "\u001B[0m");

		process.destroy();
		executorServiceOut.shutdown();
		executorServiceErr.shutdown();

		CompositionRepositoryDAO.persistAmazon(machineName, machineArn, machineRegion, functionNames, regions);
	}

	private static void removeWorkflow(String workflowName, String region) {
		String cmd = GoogleCommandUtility.buildGoogleCloudWorkflowsRemoveCommand(workflowName, region);
		ExecutorService executorServiceErr = Executors.newSingleThreadExecutor();

		try {
			Process process = buildCommand(cmd).start();
			// google deploying progresses are on the error stream
			StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), System.out::println);

			executorServiceErr.submit(errorGobbler);

			if (process.waitFor() != 0) {
				System.err.println("Could not delete workflow '" + workflowName + "'");
			} else {
				System.out.println("'" + workflowName + "' workflow removed!");
			}
			process.destroy();
		} catch (InterruptedException | IOException e) {
			System.err.println("Could not delete workflow '" + workflowName + "': " + e.getMessage());
		} finally {
			executorServiceErr.shutdown();
		}
	}

	public static void cleanupGoogleComposition() {
		System.out.println("\n" + "\u001B[33m" +
				"Cleaning up Google composition environment..." +
				"\u001B[0m" + "\n");
		// remove handler
		FunctionalityData handler = CompositionRepositoryDAO.getGoogleHandlerInfo();
		if (handler != null) {
			FunctionCommandExecutor.removeGoogleFunction(handler.getFunctionalityName(), handler.getRegion());
		}
		// remove functions
		List<FunctionalityData> toRemove = CompositionRepositoryDAO.getGoogleFunctionInfos();
		if (toRemove != null) {
			for (FunctionalityData functionalityData : toRemove) {
				FunctionCommandExecutor.removeGoogleFunction(functionalityData.getFunctionalityName(),
						functionalityData.getRegion());
			}
		}
		// remove state machines
		toRemove = CompositionRepositoryDAO.getGoogleWorkflowInfos();
		if (toRemove != null) {
			for (FunctionalityData functionalityData : toRemove) {
				removeWorkflow(functionalityData.getFunctionalityName(), functionalityData.getRegion());
			}
		}
		System.out.println("\u001B[32m" + "\nGoogle cleanup completed!\n" + "\u001B[0m");

		CompositionRepositoryDAO.dropGoogle();
	}

	private static void removeCompositionMachine(String machineName, String machineArn, String machineRegion) {
		String cmd = AmazonCommandUtility.buildStepFunctionDropCommand(machineArn, machineRegion);
		ExecutorService executorServiceOut = Executors.newSingleThreadExecutor();
		ExecutorService executorServiceErr = Executors.newSingleThreadExecutor();

		try {
			Process process = buildCommand(cmd).start();
			StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
			StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);

			executorServiceOut.submit(outputGobbler);
			executorServiceErr.submit(errorGobbler);

			if (process.waitFor() != 0) {
				System.err.println("Could not delete state machine '" + machineName + "'");
			} else {
				System.out.println("'" + machineName + "' machine removed!");
			}
			process.destroy();
		} catch (InterruptedException | IOException e) {
			System.err.println("Could not delete state machine '" + machineName + "': " + e.getMessage());
		} finally {
			executorServiceOut.shutdown();
			executorServiceErr.shutdown();
		}
	}

	public static void cleanupAmazonComposition() {
		System.out.println("\n" + "\u001B[33m" +
				"Cleaning up Amazon composition environment..." +
				"\u001B[0m" + "\n");
		// remove handler
		FunctionalityData handler = CompositionRepositoryDAO.getAmazonHandlerInfo();
		if (handler != null) {
			FunctionCommandExecutor.removeLambdaFunction(handler.getFunctionalityName(), handler.getRegion());
			FunctionCommandExecutor.removeGatewayApi(handler.getFunctionalityName(), handler.getId(), handler.getRegion());
			waitFor("Cleanup", 30);
		}
		// remove functions
		List<FunctionalityData> toRemove = CompositionRepositoryDAO.getAmazonFunctionInfos();
		if (toRemove != null) {
			for (FunctionalityData functionalityData : toRemove) {
				FunctionCommandExecutor.removeLambdaFunction(functionalityData.getFunctionalityName(),
						functionalityData.getRegion());
			}
		}
		// remove state machines
		toRemove = CompositionRepositoryDAO.getAmazonMachineInfos();
		if (toRemove != null) {
			for (FunctionalityData functionalityData : toRemove) {
				removeCompositionMachine(functionalityData.getFunctionalityName(),
						functionalityData.getId(),
						functionalityData.getRegion());
			}
		}
		System.out.println("\u001B[32m" + "\nAmazon cleanup completed!\n" + "\u001B[0m");

		CompositionRepositoryDAO.dropAmazon();
	}
}
