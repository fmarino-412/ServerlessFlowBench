import cmd.benchmark_commands.BenchmarkCommandExecutor;
import cmd.benchmark_commands.BenchmarkCommandUtility;
import cmd.function_commands.AmazonCommandUtility;
import cmd.function_commands.FunctionCommandExecutor;
import cmd.function_commands.GoogleCommandUtility;

import java.io.IOException;

public class MainTest {

	@SuppressWarnings("ConstantConditions")
	public static void main(String[] args) {
		int i = 2;

		switch (i) {
			case 0:
				deployFunctions();
				break;
			case 1:
				cleanupFunctions();
				break;
			case 2:
				benchmarkPerform();
				break;
		}
	}

	private static void cleanupFunctions() {
		FunctionCommandExecutor.cleanupGoogleCloudPlatform();
		FunctionCommandExecutor.cleanupAmazonWebServices();
	}

	private static void deployFunctions() {
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
	}

	private static void benchmarkPerform() {
		BenchmarkCommandExecutor.performBenchmark("https://us-east4-containers-254815.cloudfunctions.net/latency-test", 100, 2, 40, 200);
	}
}
