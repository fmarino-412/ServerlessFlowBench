import cmd.benchmark_commands.BenchmarkCommandExecutor;
import cmd.functionality_commands.AmazonCommandUtility;
import cmd.functionality_commands.CompositionCommandExecutor;
import cmd.functionality_commands.FunctionCommandExecutor;
import cmd.functionality_commands.GoogleCommandUtility;

@SuppressWarnings("DuplicatedCode")
public class MainTest {

	@SuppressWarnings("ConstantConditions")
	public static void main(String[] args) {

		int i = 5;

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
				customFunction();
				break;
			case 5:
				cleanupFunctions();
				cleanupCompositions();
				customFunction();
				//cleanupCompositions();
				//deployCompositions();
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

		/* Python on Google Cloud Platform */

		FunctionCommandExecutor.deployOnGoogleCloudFunction("latency-test",
				GoogleCommandUtility.PYTHON_3_7_RUNTIME,
				"gc_functions_handler",
				30,
				128,
				GoogleCommandUtility.IOWA,
				"/Users/francescomarino/IdeaProjects/serverless_composition_performance_" +
						"project/serverless_functions/gcloud/python/basic_test_composition/latency_test");

		FunctionCommandExecutor.deployOnGoogleCloudFunction("cpu-test",
				GoogleCommandUtility.PYTHON_3_7_RUNTIME,
				"gc_functions_handler",
				30,
				128,
				GoogleCommandUtility.IOWA,
				"/Users/francescomarino/IdeaProjects/serverless_composition_performance_" +
						"project/serverless_functions/gcloud/python/basic_test_composition/cpu_test");

		FunctionCommandExecutor.deployOnGoogleCloudFunction("memory-test",
				GoogleCommandUtility.PYTHON_3_7_RUNTIME,
				"gc_functions_handler",
				30,
				128,
				GoogleCommandUtility.IOWA,
				"/Users/francescomarino/IdeaProjects/serverless_composition_performance_" +
						"project/serverless_functions/gcloud/python/memory_test");



		/* Python on Amazon Web Services */

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



		/* Java on Google Cloud Platform */

		FunctionCommandExecutor.deployOnGoogleCloudFunction("latency-test",
				GoogleCommandUtility.JAVA_11_RUNTIME,
				"latency_test.Handler",
				30,
				128,
				GoogleCommandUtility.IOWA,
				"/Users/francescomarino/IdeaProjects/serverless_composition_performance_" +
						"project/serverless_functions/gcloud/java/basic_test_composition/latency_test");

		FunctionCommandExecutor.deployOnGoogleCloudFunction("cpu-test",
				GoogleCommandUtility.JAVA_11_RUNTIME,
				"cpu_test.Handler",
				30,
				128,
				GoogleCommandUtility.IOWA,
				"/Users/francescomarino/IdeaProjects/serverless_composition_performance_" +
						"project/serverless_functions/gcloud/java/basic_test_composition/cpu_test");

		FunctionCommandExecutor.deployOnGoogleCloudFunction("memory-test",
				GoogleCommandUtility.JAVA_11_RUNTIME,
				"memory_test.Handler",
				30,
				128,
				GoogleCommandUtility.IOWA,
				"/Users/francescomarino/IdeaProjects/serverless_composition_performance_" +
						"project/serverless_functions/gcloud/java/memory_test");



		/* Java on Amazon Web Services */

		FunctionCommandExecutor.deployOnAmazonRESTFunction("latency-test",
				AmazonCommandUtility.JAVA_11_RUNTIME,
				"latency_test.Handler",
				30,
				128,
				AmazonCommandUtility.OHIO,
				"/Users/francescomarino/IdeaProjects/serverless_composition_performance_project" +
						"/serverless_functions/aws/java/latency_test/target",
				"latency_test_java_aws-1.0.jar");

		FunctionCommandExecutor.deployOnAmazonRESTFunction("cpu-test",
				AmazonCommandUtility.JAVA_11_RUNTIME,
				"cpu_test.Handler",
				30,
				128,
				AmazonCommandUtility.OHIO,
				"/Users/francescomarino/IdeaProjects/serverless_composition_performance_project" +
						"/serverless_functions/aws/java/cpu_test/target",
				"cpu_test_java_aws-1.0.jar");

		FunctionCommandExecutor.deployOnAmazonRESTFunction("memory-test",
				AmazonCommandUtility.JAVA_11_RUNTIME,
				"memory_test.Handler",
				30,
				128,
				AmazonCommandUtility.OHIO,
				"/Users/francescomarino/IdeaProjects/serverless_composition_performance_project" +
						"/serverless_functions/aws/java/memory_test/target",
				"memory_test_java_aws-1.0.jar");



		/* Node.js on Google Cloud Platform */



		/* Node.js on Amazon Web Services */

		FunctionCommandExecutor.deployOnAmazonRESTFunction("latency-test",
				AmazonCommandUtility.NODE_10_X_RUNTIME,
				"index.lambda_handler",
				30,
				128,
				AmazonCommandUtility.OHIO,
				"/Users/francescomarino/IdeaProjects/serverless_composition_performance_project" +
						"/serverless_functions/aws/node/latency_test",
				"latency_test.zip");

		FunctionCommandExecutor.deployOnAmazonRESTFunction("cpu-test",
				AmazonCommandUtility.NODE_10_X_RUNTIME,
				"index.lambda_handler",
				30,
				128,
				AmazonCommandUtility.OHIO,
				"/Users/francescomarino/IdeaProjects/serverless_composition_performance_project" +
						"/serverless_functions/aws/node/cpu_test",
				"cpu_test.zip");
	}

	private static void deployCompositions() {
		System.out.println("\u001B[35m" + "\n\nDeploying benchmark compositions...\n" + "\u001B[0m");

		/* Python on Google Cloud Platform */

		{
			String[] functionNames = {"image-recognition", "anger-detection"};
			String[] entryPoints = {"gc_functions_handler", "gc_functions_handler"};
			Integer[] timeouts = {30, 30};
			Integer[] memories = {512, 512};
			String[] regions = {GoogleCommandUtility.IOWA, GoogleCommandUtility.IOWA};
			String[] functionDirs = {"image_recognition", "anger_detection"};

			CompositionCommandExecutor.deployOnGoogleComposition("face-detection",
					"/Users/francescomarino/IdeaProjects/serverless_composition_performance_" +
							"project/serverless_functions/gcloud/python/face_recognition",
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
			String[] functionNames = {"latency-test-workflow", "cpu-test-workflow"};
			String[] entryPoints = {"gc_functions_handler", "gc_functions_handler"};
			Integer[] timeouts = {30, 30};
			Integer[] memories = {128, 128};
			String[] regions = {GoogleCommandUtility.IOWA, GoogleCommandUtility.IOWA};
			String[] functionDirs = {"latency_test", "cpu_test"};

			CompositionCommandExecutor.deployOnGoogleComposition("basic-composition",
					"/Users/francescomarino/IdeaProjects/serverless_composition_performance_" +
							"project/serverless_functions/gcloud/python/basic_test_composition",
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



		/* Python on Amazon Web Services */

		{
			String[] functionNames = {"image-recognition", "anger-detection"};
			String[] entryPoints = {"image_recognition.lambda_handler", "anger_detection.lambda_handler"};
			Integer[] timeouts = {30, 30};
			Integer[] memories = {512, 512};
			String[] regions = {AmazonCommandUtility.OHIO, AmazonCommandUtility.OHIO};
			String[] zipFileNames = {"image_recognition.zip", "anger_detection.zip"};

			CompositionCommandExecutor.deployOnAmazonComposition("face-detection",
					"/Users/francescomarino/IdeaProjects/serverless_composition_performance" +
							"_project/serverless_functions/aws/python/face_recognition",
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

		{
			String[] functionNames = {"latency-test-workflow", "cpu-test-workflow"};
			String[] entryPoints = {"latency_test.lambda_handler", "cpu_test.lambda_handler"};
			Integer[] timeouts = {30, 30};
			Integer[] memories = {128, 128};
			String[] regions = {AmazonCommandUtility.OHIO, AmazonCommandUtility.OHIO};
			String[] zipFileNames = {"latency_test.zip", "cpu_test.zip"};

			CompositionCommandExecutor.deployOnAmazonComposition("basic-composition",
					"/Users/francescomarino/IdeaProjects/serverless_composition_performance" +
							"_project/serverless_functions/aws/python/basic_test_composition",
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



		/* Java on Google Cloud Platform */

		{
			String[] functionNames = {"image-recognition", "anger-detection"};
			String[] entryPoints = {"image_recognition.Handler", "anger_detection.Handler"};
			Integer[] timeouts = {30, 30};
			Integer[] memories = {512, 512};
			String[] regions = {GoogleCommandUtility.IOWA, GoogleCommandUtility.IOWA};
			String[] functionDirs = {"image_recognition", "anger_detection"};

			CompositionCommandExecutor.deployOnGoogleComposition("face-detection",
					"/Users/francescomarino/IdeaProjects/serverless_composition_performance_" +
							"project/serverless_functions/gcloud/java/face_recognition",
					GoogleCommandUtility.IOWA,
					"step.yaml",
					functionNames,
					GoogleCommandUtility.JAVA_11_RUNTIME,
					entryPoints,
					timeouts,
					memories,
					regions,
					functionDirs);
		}

		{
			String[] functionNames = {"latency-test-workflow", "cpu-test-workflow"};
			String[] entryPoints = {"latency_test.Handler", "cpu_test.Handler"};
			Integer[] timeouts = {30, 30};
			Integer[] memories = {128, 128};
			String[] regions = {GoogleCommandUtility.IOWA, GoogleCommandUtility.IOWA};
			String[] functionDirs = {"latency_test", "cpu_test"};

			CompositionCommandExecutor.deployOnGoogleComposition("basic-composition",
					"/Users/francescomarino/IdeaProjects/serverless_composition_performance_" +
							"project/serverless_functions/gcloud/java/basic_test_composition",
					GoogleCommandUtility.IOWA,
					"step.yaml",
					functionNames,
					GoogleCommandUtility.JAVA_11_RUNTIME,
					entryPoints,
					timeouts,
					memories,
					regions,
					functionDirs);
		}



		/* Java on Amazon Web Services */

		{
			String[] functionNames = {"image-recognition", "anger-detection"};
			String[] entryPoints = {"image_recognition.Handler", "anger_detection.Handler"};
			Integer[] timeouts = {30, 30};
			Integer[] memories = {512, 512};
			String[] regions = {AmazonCommandUtility.OHIO, AmazonCommandUtility.OHIO};
			String[] zipFileNames = {"image_recognition_java_aws-1.0.jar", "anger_detection_java_aws-1.0.jar"};

			CompositionCommandExecutor.deployOnAmazonComposition("face-detection",
					"/Users/francescomarino/IdeaProjects/serverless_composition_performance" +
							"_project/serverless_functions/aws/java/face_recognition",
					AmazonCommandUtility.OHIO,
					"step.json",
					functionNames,
					AmazonCommandUtility.JAVA_11_RUNTIME,
					entryPoints,
					timeouts,
					memories,
					regions,
					zipFileNames);
		}

		{
			String[] functionNames = {"latency-test-workflow", "cpu-test-workflow"};
			String[] entryPoints = {"latency_test.Handler", "cpu_test.Handler"};
			Integer[] timeouts = {30, 30};
			Integer[] memories = {128, 128};
			String[] regions = {AmazonCommandUtility.OHIO, AmazonCommandUtility.OHIO};
			String[] zipFileNames = {"latency_test_java_aws-1.0.jar", "cpu_test_java_aws-1.0.jar"};

			CompositionCommandExecutor.deployOnAmazonComposition("basic-composition",
					"/Users/francescomarino/IdeaProjects/serverless_composition_performance" +
							"_project/serverless_functions/aws/java/basic_test_composition",
					AmazonCommandUtility.OHIO,
					"step.json",
					functionNames,
					AmazonCommandUtility.JAVA_11_RUNTIME,
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


	@Deprecated
	private static void customFunction() {

		FunctionCommandExecutor.deployOnAmazonRESTFunction("cpu-test",
				AmazonCommandUtility.NODE_10_X_RUNTIME,
				"index.lambda_handler",
				30,
				128,
				AmazonCommandUtility.OHIO,
				"/Users/francescomarino/IdeaProjects/serverless_composition_performance_project" +
						"/serverless_functions/aws/node/cpu_test",
				"cpu_test.zip");

	}
}
