import cmd.benchmark_commands.BenchmarkCommandExecutor;
import cmd.function_commands.AmazonCommandUtility;
import cmd.function_commands.FunctionCommandExecutor;
import cmd.function_commands.GoogleCommandUtility;

import java.io.IOException;

public class MainTest {

	@SuppressWarnings("ConstantConditions")
	public static void main(String[] args) {

		int i = 5;

		switch (i) {
			case 0:
				deployFunctions();
				break;
			case 1:
				loadBenchmarkPerform();
				break;
			case 2:
				coldBenchmarkPerform();
				break;
			case 3:
				cleanupFunctions();
				break;
			case 4:
				deployFunctions();
				loadBenchmarkPerform();
				coldBenchmarkPerform();
				cleanupFunctions();
				break;
			case 5:
				cleanupFunctions();
				customFunction();
				break;
		}
	}

	private static void cleanupFunctions() {
		FunctionCommandExecutor.cleanupGoogleCloudPlatform();
		FunctionCommandExecutor.cleanupAmazonWebServices();
	}

	private static void deployFunctions() {
		System.out.println("\n\nDeploying benchmark functions...\n");

		try {
			FunctionCommandExecutor.deployOnGoogleCloudPlatform("latency-test",
					GoogleCommandUtility.PYTHON_3_7_RUNTIME,
					"gc_functions_handler",
					30,
					128,
					GoogleCommandUtility.NORTH_VIRGINIA,
					"/Users/francescomarino/IdeaProjects/serverless_composition_performance_" +
							"project/serverless_functions/gcloud/latency_test");
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}

		try {
			FunctionCommandExecutor.deployOnGoogleCloudPlatform("cpu-test",
					GoogleCommandUtility.PYTHON_3_7_RUNTIME,
					"gc_functions_handler",
					30,
					128,
					GoogleCommandUtility.NORTH_VIRGINIA,
					"/Users/francescomarino/IdeaProjects/serverless_composition_performance_" +
							"project/serverless_functions/gcloud/cpu_test");
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}

		try {
			FunctionCommandExecutor.deployOnGoogleCloudPlatform("memory-test",
					GoogleCommandUtility.PYTHON_3_7_RUNTIME,
					"gc_functions_handler",
					30,
					128,
					GoogleCommandUtility.NORTH_VIRGINIA,
					"/Users/francescomarino/IdeaProjects/serverless_composition_performance_" +
							"project/serverless_functions/gcloud/memory_test");
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}

		try {
			FunctionCommandExecutor.deployOnAmazonWebServices("latency-test",
					AmazonCommandUtility.PYTHON_3_7_RUNTIME,
					"latency_test.lambda_handler",
					30,
					128,
					AmazonCommandUtility.NORTH_VIRGINIA,
					"/Users/francescomarino/IdeaProjects/serverless_composition_performance_" +
							"project/serverless_functions/aws/latency_test",
					"latency_test.zip");
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}

		try {
			FunctionCommandExecutor.deployOnAmazonWebServices("cpu-test",
					AmazonCommandUtility.PYTHON_3_7_RUNTIME,
					"cpu_test.lambda_handler",
					30,
					128,
					AmazonCommandUtility.NORTH_VIRGINIA,
					"/Users/francescomarino/IdeaProjects/serverless_composition_performance_" +
							"project/serverless_functions/aws/cpu_test",
					"cpu_test.zip");
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}

		try {
			FunctionCommandExecutor.deployOnAmazonWebServices("memory-test",
					AmazonCommandUtility.PYTHON_3_7_RUNTIME,
					"memory_test.lambda_handler",
					30,
					128,
					AmazonCommandUtility.NORTH_VIRGINIA,
					"/Users/francescomarino/IdeaProjects/serverless_composition_performance_" +
							"project/serverless_functions/aws/memory_test",
					"memory_test.zip");
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}

		System.out.println("\n\nDeploying application functions...\n");

		try {
			FunctionCommandExecutor.deployOnGoogleCloudPlatform("image-recognition",
					GoogleCommandUtility.PYTHON_3_7_RUNTIME,
					"gc_functions_handler",
					30,
					128,
					GoogleCommandUtility.NORTH_VIRGINIA,
					"/Users/francescomarino/IdeaProjects/serverless_composition_performance_" +
							"project/serverless_functions/gcloud/image_recognition");
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}

		try {
			FunctionCommandExecutor.deployOnAmazonWebServices("image-recognition",
					AmazonCommandUtility.PYTHON_3_7_RUNTIME,
					"image_recognition.lambda_handler",
					30,
					128,
					AmazonCommandUtility.NORTH_VIRGINIA,
					"/Users/francescomarino/IdeaProjects/serverless_composition_performance_" +
							"project/serverless_functions/aws/image_recognition",
					"image_recognition.zip");
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
	}

	private static void loadBenchmarkPerform() {
		for (int i = 0; i < 10; i++) {
			try {
				BenchmarkCommandExecutor.performLoadTest(500, 100, 40,
						100000);
				Thread.sleep(60 * 1000); // 1 minute sleeping, not enough for resource deallocation
			} catch (InterruptedException ignored) {}
		}
	}

	private static void coldBenchmarkPerform() {
		BenchmarkCommandExecutor.performColdStartBenchmark(10);
	}

	@SuppressWarnings("DuplicatedCode")
	@Deprecated
	private static void customFunction() {

		try {
			FunctionCommandExecutor.deployOnGoogleCloudPlatform("image-recognition",
					GoogleCommandUtility.PYTHON_3_7_RUNTIME,
					"gc_functions_handler",
					30,
					128,
					GoogleCommandUtility.NORTH_VIRGINIA,
					"/Users/francescomarino/IdeaProjects/serverless_composition_performance_" +
							"project/serverless_functions/gcloud/image_recognition");
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}

		try {
			FunctionCommandExecutor.deployOnAmazonWebServices("image-recognition",
					AmazonCommandUtility.PYTHON_3_7_RUNTIME,
					"image_recognition.lambda_handler",
					30,
					128,
					AmazonCommandUtility.NORTH_VIRGINIA,
					"/Users/francescomarino/IdeaProjects/serverless_composition_performance_" +
							"project/serverless_functions/aws/image_recognition",
					"image_recognition.zip");
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
	}
}
