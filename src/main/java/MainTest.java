import cmd.CommandExecutor;
import cmd.benchmark_commands.BenchmarkCommandExecutor;
import cmd.functionality_commands.AmazonCommandUtility;
import cmd.functionality_commands.CompositionCommandExecutor;
import cmd.functionality_commands.FunctionCommandExecutor;
import cmd.functionality_commands.GoogleCommandUtility;
import cmd.functionality_commands.security.GoogleAuthClient;
import databases.mysql.daos.CompositionRepositoryDAO;

import java.io.IOException;

public class MainTest {

	@SuppressWarnings("ConstantConditions")
	public static void main(String[] args) {

		int i = 4;

		switch (i) {
			case 0:
				deployFunctions();
				deployCompositions();
				break;
			case 1:
				benchmarkPerform();
				break;
			case 2:
				cleanupFunctions();
				cleanupCompositions();
				break;
			case 3:
				deployFunctions();
				deployCompositions();
				benchmarkPerform();
				cleanupCompositions();
				cleanupFunctions();
				break;
			case 4:
				deployFunctions();
				deployCompositions();
				cleanupCompositions();
				cleanupFunctions();
				//customFunction();
				break;
		}
	}

	private static void cleanupFunctions() {
		System.out.println("\u001B[35m" + "\n\nRemoving benchmark functions...\n" + "\u001B[0m");
		FunctionCommandExecutor.cleanupGoogleCloudFunctions();
		FunctionCommandExecutor.cleanupAmazonRESTFunctions();
	}

	private static void cleanupCompositions() {
		System.out.println("\u001B[35m" + "\n\nRemoving benchmark compositions...\n" + "\u001B[0m");
		CompositionCommandExecutor.cleanupGoogleComposition();
		CompositionCommandExecutor.cleanupAmazonComposition();
	}

	private static void deployFunctions() {
		System.out.println("\u001B[35m" + "\n\nDeploying benchmark functions...\n" + "\u001B[0m");

		FunctionCommandExecutor.deployOnGoogleCloudFunction("latency-test",
				GoogleCommandUtility.PYTHON_3_7_RUNTIME,
				"gc_functions_handler",
				30,
				128,
				GoogleCommandUtility.IOWA,
				"/Users/francescomarino/IdeaProjects/serverless_composition_performance_" +
						"project/serverless_functions/gcloud/python/latency_test");

		FunctionCommandExecutor.deployOnGoogleCloudFunction("cpu-test",
				GoogleCommandUtility.PYTHON_3_7_RUNTIME,
				"gc_functions_handler",
				30,
				128,
				GoogleCommandUtility.IOWA,
				"/Users/francescomarino/IdeaProjects/serverless_composition_performance_" +
						"project/serverless_functions/gcloud/python/cpu_test");

		FunctionCommandExecutor.deployOnGoogleCloudFunction("memory-test",
				GoogleCommandUtility.PYTHON_3_7_RUNTIME,
				"gc_functions_handler",
				30,
				128,
				GoogleCommandUtility.IOWA,
				"/Users/francescomarino/IdeaProjects/serverless_composition_performance_" +
						"project/serverless_functions/gcloud/python/memory_test");

		FunctionCommandExecutor.deployOnAmazonRESTFunction("latency-test",
				AmazonCommandUtility.PYTHON_3_7_RUNTIME,
				"latency_test.lambda_handler",
				30,
				128,
				AmazonCommandUtility.OHIO,
				"/Users/francescomarino/IdeaProjects/serverless_composition_performance_" +
						"project/serverless_functions/aws/python/latency_test",
				"latency_test.zip");

		FunctionCommandExecutor.deployOnAmazonRESTFunction("cpu-test",
				AmazonCommandUtility.PYTHON_3_7_RUNTIME,
				"cpu_test.lambda_handler",
				30,
				128,
				AmazonCommandUtility.OHIO,
				"/Users/francescomarino/IdeaProjects/serverless_composition_performance_" +
						"project/serverless_functions/aws/python/cpu_test",
				"cpu_test.zip");

		FunctionCommandExecutor.deployOnAmazonRESTFunction("memory-test",
				AmazonCommandUtility.PYTHON_3_7_RUNTIME,
				"memory_test.lambda_handler",
				30,
				128,
				AmazonCommandUtility.OHIO,
				"/Users/francescomarino/IdeaProjects/serverless_composition_performance_" +
						"project/serverless_functions/aws/python/memory_test",
				"memory_test.zip");
	}

	private static void deployCompositions() {
		System.out.println("\u001B[35m" + "\n\nDeploying benchmark compositions...\n" + "\u001B[0m");

		{
			String[] functionNames = {"image-recognition"};
			String[] entryPoints = {"gc_functions_handler"};
			Integer[] timeouts = {30};
			Integer[] memories = {128};
			String[] regions = {GoogleCommandUtility.IOWA};
			String[] functionDirs = {"image_recognition"};

			CompositionCommandExecutor.deployOnGoogleComposition("image_detection",
					"/Users/francescomarino/IdeaProjects/serverless_composition_performance_" +
							"project/serverless_functions/gcloud/python/image_recognition",
					GoogleCommandUtility.IOWA,
					"step.yaml",
					functionNames,
					GoogleCommandUtility.PYTHON_3_7_RUNTIME,
					entryPoints,
					timeouts,
					memories,
					regions,
					functionDirs);
		}

		{
			String[] functionNames = {"image-recognition"};
			String[] entryPoints = {"image_recognition.lambda_handler"};
			Integer[] timeouts = {30};
			Integer[] memories = {128};
			String[] regions = {AmazonCommandUtility.OHIO};
			String[] zipFileNames = {"image_recognition.zip"};

			CompositionCommandExecutor.deployOnAmazonComposition("image_detection",
					"/Users/francescomarino/IdeaProjects/serverless_composition_performance" +
							"_project/serverless_functions/aws/python/image_recognition",
					AmazonCommandUtility.OHIO,
					"step.json",
					functionNames,
					AmazonCommandUtility.PYTHON_3_7_RUNTIME,
					entryPoints,
					timeouts,
					memories,
					regions,
					zipFileNames);
		}
	}

	private static void benchmarkPerform() {
		BenchmarkCommandExecutor.performBenchmarks(1000,
				100,
				60,
				100000,
				10);
	}


	@SuppressWarnings("DuplicatedCode")
	@Deprecated
	private static void customFunction() {
		System.out.println(GoogleAuthClient.getInstance().getUrlToken());
	}
}
