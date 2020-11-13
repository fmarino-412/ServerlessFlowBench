package cmd.functionality_commands;

import cmd.CommandExecutor;
import cmd.CommandUtility;
import cmd.StreamGobbler;
import cmd.docker_daemon_utility.DockerException;
import cmd.docker_daemon_utility.DockerExecutor;
import cmd.functionality_commands.output_parsing.ReplyCollector;
import cmd.functionality_commands.output_parsing.UrlFinder;
import cmd.functionality_commands.security.GoogleAuthClient;
import databases.mysql.CloudEntityData;
import databases.mysql.daos.CompositionsRepositoryDAO;
import utility.PropertiesManager;

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

/**
 * Utility for CLI serverless function composition related command execution
 */
@SuppressWarnings("DuplicatedCode")
public class CompositionCommandExecutor extends CommandExecutor {

	/**
	 * Placeholder string as must appear inside yaml and json composition definition files
	 */
	private static final String PLACEHOLDER = "__PLACEHOLDER__";
	/**
	 * Standard handler serverless function name
	 */
	private static final String HANDLER_NAME = "h-a-n-d-l-e-r";


	/**
	 * Deploys a serverless composition handler on Google Cloud Functions
	 */
	private static void deployGoogleCompositionHandler() {

		if (CompositionsRepositoryDAO.existsGoogleHandler(null)) {
			return;
		}
		// timeout and memory set to avoid the handler being a bottleneck
		FunctionCommandExecutor.deployGoogleCloudHandlerFunction(HANDLER_NAME,
				GoogleCommandUtility.PYTHON_3_7_RUNTIME,
				"gc_functions_handler",
				300,
				512,
				GoogleCommandUtility.IOWA,
				PropertiesManager.getInstance().getProperty(PropertiesManager.GOOGLE_HANDLER_PATH));
	}

	/**
	 * Deploys a serverless composition handler on AWS
	 */
	private static void deployAmazonCompositionHandler() {

		if (CompositionsRepositoryDAO.existsAmazonHandler(null)) {
			return;
		}
		// timeout and memory set to avoid the handler being a bottleneck
		FunctionCommandExecutor.deployAmazonRESTHandlerFunction(HANDLER_NAME,
				AmazonCommandUtility.PYTHON_3_7_RUNTIME,
				"orchestration_handler.lambda_handler",
				300,
				512,
				AmazonCommandUtility.OHIO,
				PropertiesManager.getInstance().getProperty(PropertiesManager.AWS_HANDLER_PATH),
				"orchestration_handler.zip");

	}

	/**
	 * Deploys a workflow to Google Cloud Platform Workflows [BETA], needed serverless functions
	 * and performs DB persistence
	 * @param workflowName name of the workflow to deploy
	 * @param contentFolderAbsolutePath folder containing workflow functions and definition
	 * @param workflowRegion region for workflow deployment
	 *                          (only GoogleCommandUtility.IOWA supported by Google at the moment)
	 * @param yamlFileName yaml containing workflow definition file name
	 * @param functionNames list of workflow function names (consistent ordering)
	 * @param runtime function runtimes (same for every function!)
	 * @param entryPoints list of workflow function entry points (consistent ordering)
	 * @param timeouts list of workflow function timeouts is seconds (consistent ordering)
	 * @param memory list of workflow function memory amount in megabytes (consistent ordering)
	 * @param regions list of workflow function regions of deployment (consistent ordering)
	 * @param functionDirPaths list of workflow function directories containing implementation (consistent ordering)
	 */
	public static void deployOnGoogleComposition(String workflowName, String contentFolderAbsolutePath,
												 String workflowRegion, String yamlFileName, String[] functionNames,
												 String runtime, String[] entryPoints, Integer[] timeouts,
												 Integer[] memory, String[] regions, String[] functionDirPaths) {

		assert functionNames.length == entryPoints.length;
		assert functionNames.length == timeouts.length;
		assert functionNames.length == memory.length;
		assert functionNames.length == regions.length;
		assert functionNames.length == functionDirPaths.length;

		try {
			workflowName = GoogleCommandUtility.applyRuntimeId(workflowName, runtime);
			DockerExecutor.checkDocker();
		} catch (IllegalNameException | DockerException e) {
			System.err.println("Could not deploy workflow '" + workflowName + "': " + e.getMessage());
			return;
		}

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
			try {
				functionNames[i] = GoogleCommandUtility.applyRuntimeId(functionNames[i], runtime);
			} catch (IllegalNameException e) {
				System.err.println("Could not deploy function '" + functionNames[i] + "' to Google Cloud Functions: " +
						e.getMessage());
				deleteFile(tempYaml);
				return;
			}
			functionUrls.add(FunctionCommandExecutor.deployOnGoogleCloudCompositionFunction(functionNames[i],
					runtime,
					entryPoints[i],
					timeouts[i],
					memory[i],
					regions[i],
					contentFolderAbsolutePath + CommandUtility.getPathSep() + functionDirPaths[i]));
			if (!functionUrls.get(functionUrls.size() - 1).equals("")) {
				System.out.println("'" + functionNames[i] + "' deploy on Google Cloud Platform completed");
			}
		}
		if (functionUrls.contains("")) {
			System.err.println("Error deploying functions!");
			deleteFile(tempYaml);
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
			deleteFile(tempYaml);
			return;
		}

		ExecutorService executorServiceOut = Executors.newSingleThreadExecutor();

		try {
			// declare and initialize variables
			String cmd;
			Process process;
			StreamGobbler outputGobbler;

			cmd = GoogleCommandUtility.buildGoogleCloudWorkflowsDeployCommand(workflowName, workflowRegion,
					tempYaml.getParent().toString(), tempYaml.getFileName().toString());
			process = buildCommand(cmd).start();
			outputGobbler = new StreamGobbler(process.getErrorStream(), System.out::println);
			executorServiceOut.submit(outputGobbler);
			if (process.waitFor() != 0) {
				System.err.println("Could not deploy workflow '" + workflowName + "' on Google Cloud Platform");
				process.destroy();
				deleteFile(tempYaml);
				return;
			}

			// delete temporary file
			deleteFile(tempYaml);

			String url = CompositionsRepositoryDAO.getGoogleHandlerUrl(null);
			if (url == null) {
				System.err.println("WARNING: Handler not found! Workflow is not reachable");
			}
			url = url + "?workflow=" + workflowName + GoogleAuthClient.getInstance().getUrlToken();
			System.out.println("\u001B[32m" + "Deployed workflow '" + workflowName + "' to: " + url + "\u001B[0m");

			process.destroy();

			CompositionsRepositoryDAO.persistGoogle(workflowName, workflowRegion, functionNames, regions);
		} catch (InterruptedException | IOException e) {
			System.err.println("Could not deploy workflow '" + workflowName + "' on Google Cloud Platform: " +
					e.getMessage());
		} finally {
			executorServiceOut.shutdown();
		}
	}

	/**
	 * Deploys a state machine to AWS Step Functions, needed serverless functions and performs DB persistence
	 * @param machineName name of the state machine to deploy
	 * @param contentFolderAbsolutePath folder containing state machine functions and definition
	 * @param machineRegion region for state machine deployment
	 * @param jsonFileName json containing state machine definition file name
	 * @param functionNames list of state machine function names (consistent ordering)
	 * @param runtime function runtimes (same for every function!)
	 * @param entryPoints list of state machine function entry points (consistent ordering)
	 * @param timeouts list of state machine function timeouts is seconds (consistent ordering)
	 * @param memory list of state machine function memory amount in megabytes (consistent ordering)
	 * @param regions list of state machine function regions of deployment (consistent ordering)
	 * @param zipFileNames list of state machine function file names of zipped implementations
	 */
	public static void deployOnAmazonComposition(String machineName, String contentFolderAbsolutePath,
												 String machineRegion, String jsonFileName, String[] functionNames,
												 String runtime, String[] entryPoints, Integer[] timeouts,
												 Integer[] memory, String[] regions, String[] zipFileNames) {

		assert functionNames.length == entryPoints.length;
		assert functionNames.length == timeouts.length;
		assert functionNames.length == memory.length;
		assert functionNames.length == regions.length;
		assert functionNames.length == zipFileNames.length;

		try {
			machineName = AmazonCommandUtility.applyRuntimeId(machineName, runtime);
			DockerExecutor.checkDocker();
		} catch (IllegalNameException | DockerException e) {
			System.err.println("Could not deploy state machine '" + machineName + "': " + e.getMessage());
			return;
		}

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

		//noinspection SpellCheckingInspection
		ArrayList<String> functionArns = new ArrayList<>();

		// publish functions
		for (int i = 0; i < functionNames.length; i++) {
			// deploy function
			try {
				functionNames[i] = AmazonCommandUtility.applyRuntimeId(functionNames[i], runtime);
				functionArns.add(FunctionCommandExecutor.deployOnAmazonLambdaFunctions(functionNames[i],
						runtime,
						entryPoints[i],
						timeouts[i],
						memory[i],
						regions[i],
						contentFolderAbsolutePath,
						zipFileNames[i]));
			} catch (InterruptedException | IOException | IllegalNameException e) {
				System.err.println("Could not deploy '" + functionNames[i] + "' to AWS Lambda: " + e.getMessage());
				return;
			}
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

		ExecutorService executorServiceOut = Executors.newSingleThreadExecutor();
		ExecutorService executorServiceErr = Executors.newSingleThreadExecutor();

		try {
			// declare and initialize variables
			String cmd;
			Process process;
			StreamGobbler outputGobbler;
			StreamGobbler errorGobbler;

			cmd = AmazonCommandUtility.buildStepFunctionCreationCommand(machineName, machineRegion, json);
			process = buildCommand(cmd).start();
			ReplyCollector machineArnCollector = new ReplyCollector();
			outputGobbler = new StreamGobbler(process.getInputStream(), machineArnCollector::collectResult);
			errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
			executorServiceOut.submit(outputGobbler);
			executorServiceErr.submit(errorGobbler);
			if (process.waitFor() != 0) {
				System.err.println("Could not deploy state machine '" + machineName + "' on Step Functions");
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
				process.destroy();
				return;
			}

			String url = CompositionsRepositoryDAO.getAmazonHandlerUrl(null);
			if (url == null) {
				System.err.println("WARNING: Handler not found! Machine is not reachable");
			}
			url = url + "?arn=" + machineArn;
			System.out.println("\u001B[32m" + "Deployed machine '" + machineName + "' to: " + url + "\u001B[0m");

			process.destroy();

			CompositionsRepositoryDAO.persistAmazon(machineName, machineArn, machineRegion, functionNames, regions);
		} catch (InterruptedException | IOException e) {
			System.err.println("Could not deploy state machine '" + machineName + "' on Step Functions: " +
					e.getMessage());
		} finally {
			executorServiceOut.shutdown();
			executorServiceErr.shutdown();
		}
	}

	public static void deployOnOpenWhiskComposition(String compositionName, String contentFolderAbsolutePath,
													String javascriptFileName, String[] functionNames, String runtime,
													String[] entryPoints, Integer[] timeouts, Integer[] memory,
													String[] zipFileNames) {

		assert functionNames.length == entryPoints.length;
		assert functionNames.length == timeouts.length;
		assert functionNames.length == memory.length;
		assert functionNames.length == zipFileNames.length;

		try {
			compositionName = OpenWhiskCommandUtility.applyRuntimeId(compositionName, runtime);
			DockerExecutor.checkDocker();
		} catch (IllegalNameException | DockerException e) {
			System.err.println("Could not deploy composition '" + compositionName + "': " + e.getMessage());
			return;
		}

		System.out.println("\n" + "\u001B[33m" +
				"Deploying \"" + compositionName + "\" to OpenWhisk..." +
				"\u001B[0m" + "\n");

		// create temporary file in which is possible to replace PLACEHOLDER without editing original file
		Path tempJs;
		String js;
		try {
			tempJs = Files.createTempFile("temp", ".js");
			js = new String(Files.readAllBytes(Paths.get(contentFolderAbsolutePath +
					CommandUtility.getPathSep() + javascriptFileName)), StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.err.println("Could not read '" + contentFolderAbsolutePath +
					CommandUtility.getPathSep() + javascriptFileName + "': " + e.getMessage());
			return;
		}

		ArrayList<String> deployedFunctions = new ArrayList<>();

		// publish functions
		for (int i = 0; i < functionNames.length; i++) {
			try {
				functionNames[i] = OpenWhiskCommandUtility.applyRuntimeId(functionNames[i], runtime);
			} catch (IllegalNameException e) {
				System.err.println("Could not deploy function '" + functionNames[i] + "' to OpenWhisk: " +
						e.getMessage());
				deleteFile(tempJs);
				return;
			}
			deployedFunctions.add(FunctionCommandExecutor.deployOnOpenWhiskCompositions(functionNames[i],
					runtime,
					entryPoints[i],
					timeouts[i],
					memory[i],
					contentFolderAbsolutePath,
					zipFileNames[i]));
			if (!deployedFunctions.get(deployedFunctions.size() - 1).equals("")) {
				System.out.println("'" + functionNames[i] + "' deploy on OpenWhisk completed");
			}
		}
		if (deployedFunctions.contains("")) {
			System.err.println("Error deploying functions!");
			deleteFile(tempJs);
			return;
		}

		// javascript file: replacing placeholders
		for (String functionName : functionNames) {
			js = js.replaceFirst(PLACEHOLDER, functionName);
		}
		// write js to temp file
		try {
			PrintWriter writer = new PrintWriter(tempJs.toString());
			writer.print(js);
			writer.flush();
		} catch (IOException e) {
			System.err.println("Could not parse '" + javascriptFileName + "': " + e.getMessage());
			deleteFile(tempJs);
			return;
		}

		// create temporary json file to save composition description
		Path tempJson;
		try {
			tempJson = Files.createTempFile("temp", ".json");
		} catch (IOException e) {
			System.err.println("Could not parse '" + javascriptFileName + "': " + e.getMessage());
			deleteFile(tempJs);
			return;
		}

		ExecutorService executorServiceOut = Executors.newSingleThreadExecutor();
		ExecutorService executorServiceErr = Executors.newSingleThreadExecutor();

		try {
			// declare and initialize variables
			String cmd;
			Process process;
			StreamGobbler outputGobbler;
			StreamGobbler errorGobbler;

			cmd = OpenWhiskCommandUtility.buildCompositionFileCreationCommand(tempJs.toAbsolutePath().toString());
			process = buildCommand(cmd).start();
			ReplyCollector replyCollector = new ReplyCollector();
			outputGobbler = new StreamGobbler(process.getInputStream(), replyCollector::collectResult);
			errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
			executorServiceOut.submit(outputGobbler);
			executorServiceErr.submit(errorGobbler);
			if (process.waitFor() != 0) {
				System.err.println("Could not deploy composition '" + compositionName + "' on OpenWhisk");
				process.destroy();
				deleteFile(tempJs);
				deleteFile(tempJson);
				return;
			}

			// delete javascript temporary file
			deleteFile(tempJs);

			// write json to temp file
			try {
				PrintWriter writer = new PrintWriter(tempJson.toString());
				writer.print(replyCollector.getResult());
				writer.flush();
			} catch (IOException e) {
				System.err.println("Could not parse '" + javascriptFileName + "': " + e.getMessage());
				deleteFile(tempJson);
				return;
			}

			// deploy composition
			cmd = OpenWhiskCommandUtility.buildCompositionDeployCommand(compositionName,
					tempJson.toAbsolutePath().toString());
			process = buildCommand(cmd).start();
			errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
			executorServiceErr.submit(errorGobbler);
			if (process.waitFor() != 0) {
				System.err.println("Could not deploy composition '" + compositionName + "' on OpenWhisk");
				process.destroy();
				deleteFile(tempJson);
				return;
			}

			// delete json temporary file
			deleteFile(tempJson);

			// enable web
			cmd = OpenWhiskCommandUtility.buildCompositionWebEnableCommand(compositionName);
			process = buildCommand(cmd).start();
			errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
			executorServiceErr.submit(errorGobbler);
			if (process.waitFor() != 0) {
				System.err.println("Could not enable composition web reachability for '" + compositionName +
						"' on OpenWhisk");
				process.destroy();
				return;
			}

			// get composition url
			cmd = OpenWhiskCommandUtility.buildActionUrlGetterCommand(compositionName);
			process = buildCommand(cmd).start();
			UrlFinder urlFinder = new UrlFinder(Boolean.parseBoolean(PropertiesManager.getInstance()
					.getProperty(PropertiesManager.OPENWHISK_SSL_IGNORE)));
			outputGobbler = new StreamGobbler(process.getInputStream(), urlFinder::findOpenWhiskUrl);
			errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
			executorServiceOut.submit(outputGobbler);
			executorServiceErr.submit(errorGobbler);
			if (process.waitFor() != 0) {
				System.err.println("Could not obtain '" + compositionName + "' url on OpenWhisk");
				process.destroy();
				return;
			}

			String url = urlFinder.getResult();
			System.out.println("\u001B[32m" + "Deployed composition '" + compositionName + "' to: " + url +
					"\u001B[0m");

			process.destroy();

			// TODO: persist!
		} catch (InterruptedException | IOException e) {
			System.err.println("Could not deploy composition '" + compositionName + "' on OpenWhisk: " +
					e.getMessage());
			// silent because if fails has already been deleted
			deleteFile(tempJs, true);
			deleteFile(tempJson, true);
		} finally {
			executorServiceOut.shutdown();
			executorServiceErr.shutdown();
		}
	}

	/**
	 * Removes a workflow from Google Cloud Platform Workflows [BETA]
	 * @param workflowName name of the workflow to remove
	 * @param region region of workflow deployment
	 * @throws IOException exception related to process execution
	 * @throws InterruptedException exception related to Thread management
	 */
	private static void removeGoogleWorkflow(String workflowName, String region)
			throws IOException, InterruptedException {
		String cmd = GoogleCommandUtility.buildGoogleCloudWorkflowsRemoveCommand(workflowName, region);
		ExecutorService executorServiceErr = Executors.newSingleThreadExecutor();

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
		executorServiceErr.shutdown();
	}

	/**
	 * Removes every workflow and related functions (handler included) from Google Cloud Platform
	 */
	public static void cleanupGoogleComposition() {

		try {
			DockerExecutor.checkDocker();
		} catch (DockerException e) {
			System.err.println("Could not cleanup Google composition environment: " + e.getMessage());
			return;
		}

		System.out.println("\n" + "\u001B[33m" +
				"Cleaning up Google composition environment..." +
				"\u001B[0m" + "\n");

		// remove handler
		CloudEntityData handler = CompositionsRepositoryDAO.getGoogleHandlerInfo();
		if (handler != null) {
			try {
				FunctionCommandExecutor.removeGoogleFunction(handler.getEntityName(), handler.getRegion());
			} catch (InterruptedException | IOException e) {
				System.err.println("Could not remove handler from Google Cloud Functions: " + e.getMessage());
			}
		}
		// remove functions
		List<CloudEntityData> toRemove = CompositionsRepositoryDAO.getGoogleFunctionInfos();
		if (toRemove != null) {
			for (CloudEntityData functionalityData : toRemove) {
				try {
					FunctionCommandExecutor.removeGoogleFunction(functionalityData.getEntityName(),
							functionalityData.getRegion());
				} catch (InterruptedException | IOException e) {
					System.err.println("Could not delete '" + functionalityData.getEntityName() +
							"' from Google Cloud Functions: " + e.getMessage());
				}
			}
		}
		// remove state machines
		toRemove = CompositionsRepositoryDAO.getGoogleWorkflowInfos();
		if (toRemove != null) {
			for (CloudEntityData functionalityData : toRemove) {
				try {
					removeGoogleWorkflow(functionalityData.getEntityName(), functionalityData.getRegion());
				} catch (InterruptedException | IOException e) {
					System.err.println("Could not delete '" + functionalityData.getEntityName() +
							"' workflow from Google Cloud Platform Workflows [BETA]: " + e.getMessage());
				}
			}
		}
		System.out.println("\u001B[32m" + "\nGoogle cleanup completed!\n" + "\u001B[0m");

		CompositionsRepositoryDAO.dropGoogle();
	}

	/**
	 * Removes a state machine from AWS Step Functions
	 * @param machineName name of the state machine to remove
	 * @param machineArn ARN of the state machine to remove
	 * @param machineRegion region of state machine deployment
	 * @throws IOException exception related to process execution
	 * @throws InterruptedException exception related to Thread management
	 */
	private static void removeCompositionMachine(String machineName, String machineArn, String machineRegion)
			throws IOException, InterruptedException {

		String cmd = AmazonCommandUtility.buildStepFunctionDropCommand(machineArn, machineRegion);
		ExecutorService executorServiceOut = Executors.newSingleThreadExecutor();
		ExecutorService executorServiceErr = Executors.newSingleThreadExecutor();

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
		executorServiceOut.shutdown();
		executorServiceErr.shutdown();
	}

	/**
	 * Removes every workflow and related functions (handler included) from AWS
	 */
	public static void cleanupAmazonComposition() {

		try {
			DockerExecutor.checkDocker();
		} catch (DockerException e) {
			System.err.println("Could not cleanup Amazon composition environment: " + e.getMessage());
			return;
		}

		System.out.println("\n" + "\u001B[33m" +
				"Cleaning up Amazon composition environment..." +
				"\u001B[0m" + "\n");

		// remove handler
		CloudEntityData handler = CompositionsRepositoryDAO.getAmazonHandlerInfo();
		if (handler != null) {
			try {
				FunctionCommandExecutor.removeLambdaFunction(handler.getEntityName(), handler.getRegion());
			} catch (InterruptedException | IOException e) {
				System.err.println("Could not remove handler from AWS Lambda: " + e.getMessage());
			}
			try {
				FunctionCommandExecutor.removeGatewayApi(handler.getEntityName(), handler.getId(),
						handler.getRegion());
			} catch (InterruptedException | IOException e) {
				System.err.println("Could not remove handler from API Gateway: " + e.getMessage());
			}
			waitFor("Cleanup", 30);
		}
		// remove functions
		List<CloudEntityData> toRemove = CompositionsRepositoryDAO.getAmazonFunctionInfos();
		if (toRemove != null) {
			for (CloudEntityData functionalityData : toRemove) {
				try {
					FunctionCommandExecutor.removeLambdaFunction(functionalityData.getEntityName(),
							functionalityData.getRegion());
				} catch (InterruptedException | IOException e) {
					System.err.println("Could not delete '" + functionalityData.getEntityName() +
							"' from AWS Lambda: " + e.getMessage());
				}
			}
		}
		// remove state machines
		toRemove = CompositionsRepositoryDAO.getAmazonMachineInfos();
		if (toRemove != null) {
			for (CloudEntityData functionalityData : toRemove) {
				try {
					removeCompositionMachine(functionalityData.getEntityName(),
							functionalityData.getId(),
							functionalityData.getRegion());
				} catch (InterruptedException | IOException e) {
					System.err.println("Could not delete '" + functionalityData.getEntityName() +
							"' workflow from AWS Step Functions: " + e.getMessage());
				}
				waitFor("Cleanup", 30);
			}
		}
		System.out.println("\u001B[32m" + "\nAmazon cleanup completed!\n" + "\u001B[0m");

		CompositionsRepositoryDAO.dropAmazon();
	}
}
