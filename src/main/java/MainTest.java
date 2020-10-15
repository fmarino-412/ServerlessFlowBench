import cmd.CommandExecutor;
import cmd.benchmark_commands.BenchmarkCommandExecutor;
import cmd.functionality_commands.*;

@SuppressWarnings("DuplicatedCode")
public class MainTest {

	private static final int OPERATION_SELECTION = 6;

	@SuppressWarnings("ConstantConditions")
	public static void main(String[] args) {

		switch (OPERATION_SELECTION) {
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
				cleanupTables();
				break;
			case 3:
				deployFunctions();
				deployCompositions();
				benchmarkPerform();
				cleanupCompositions();
				cleanupFunctions();
				cleanupTables();
				break;
			case 4:
				deployFunctions();
				deployCompositions();
				cleanupCompositions();
				cleanupFunctions();
				cleanupTables();
				customFunction();
				break;
			case 5:
				deployInfoFunctions();
				break;
			case 6:
				cleanupFunctions();
				cleanupCompositions();
				cleanupTables();
				customFunction();
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

	private static void cleanupTables() {
		System.out.println("\u001B[35m" + "\n\nRemoving cloud tables...\n" + "\u001B[0m");
		TablesCommandExecutor.cleanupAmazonCloudTables();
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
						"project/serverless_functions/aws/python/basic_test_composition/latency_test",
				"latency_test.zip");

		FunctionCommandExecutor.deployOnAmazonRESTFunction("cpu-test",
				AmazonCommandUtility.PYTHON_3_7_RUNTIME,
				"cpu_test.lambda_handler",
				30,
				128,
				AmazonCommandUtility.OHIO,
				"/Users/francescomarino/IdeaProjects/serverless_composition_performance_" +
						"project/serverless_functions/aws/python/basic_test_composition/cpu_test",
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

		FunctionCommandExecutor.deployOnGoogleCloudFunction("latency-test",
				GoogleCommandUtility.NODE_10_RUNTIME,
				"gcFunctionsHandler",
				30,
				128,
				GoogleCommandUtility.IOWA,
				"/Users/francescomarino/IdeaProjects/serverless_composition_performance_" +
						"project/serverless_functions/gcloud/node/basic_test_composition/latency_test");

		FunctionCommandExecutor.deployOnGoogleCloudFunction("cpu-test",
				GoogleCommandUtility.NODE_10_RUNTIME,
				"gcFunctionsHandler",
				30,
				128,
				GoogleCommandUtility.IOWA,
				"/Users/francescomarino/IdeaProjects/serverless_composition_performance_" +
						"project/serverless_functions/gcloud/node/basic_test_composition/cpu_test");

		FunctionCommandExecutor.deployOnGoogleCloudFunction("memory-test",
				GoogleCommandUtility.NODE_10_RUNTIME,
				"gcFunctionsHandler",
				30,
				128,
				GoogleCommandUtility.IOWA,
				"/Users/francescomarino/IdeaProjects/serverless_composition_performance_" +
						"project/serverless_functions/gcloud/node/memory_test");



		/* Node.js on Amazon Web Services */

		FunctionCommandExecutor.deployOnAmazonRESTFunction("latency-test",
				AmazonCommandUtility.NODE_10_X_RUNTIME,
				"index.lambdaHandler",
				30,
				128,
				AmazonCommandUtility.OHIO,
				"/Users/francescomarino/IdeaProjects/serverless_composition_performance_project" +
						"/serverless_functions/aws/node/basic_test_composition/latency_test",
				"latency_test.zip");

		FunctionCommandExecutor.deployOnAmazonRESTFunction("cpu-test",
				AmazonCommandUtility.NODE_10_X_RUNTIME,
				"index.lambdaHandler",
				30,
				128,
				AmazonCommandUtility.OHIO,
				"/Users/francescomarino/IdeaProjects/serverless_composition_performance_project" +
						"/serverless_functions/aws/node/basic_test_composition/cpu_test",
				"cpu_test.zip");

		FunctionCommandExecutor.deployOnAmazonRESTFunction("memory-test",
				AmazonCommandUtility.NODE_10_X_RUNTIME,
				"index.lambdaHandler",
				30,
				128,
				AmazonCommandUtility.OHIO,
				"/Users/francescomarino/IdeaProjects/serverless_composition_performance_project" +
						"/serverless_functions/aws/node/memory_test",
				"memory_test.zip");
	}

	private static void deployCompositions() {
		System.out.println("\u001B[35m" + "\n\nDeploying benchmark compositions...\n" + "\u001B[0m");

		/* Cloud tables */

		TablesCommandExecutor.createAmazonTable("ranking_translator",
				"/Users/francescomarino/IdeaProjects/serverless_composition_performance_project/" +
						"serverless_functions/aws/dynamo_tables",
				"ranking_translator.json",
				AmazonCommandUtility.OHIO);



		/* Python on Google Cloud Platform */

		{
			String[] functionNames = {"image-recognition", "anger-detection"};
			String[] entryPoints = {"gc_functions_handler", "gc_functions_handler"};
			Integer[] timeouts = {30, 30};
			Integer[] memories = {1024, 1024};
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
			Integer[] memories = {1024, 1024};
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

		{
			String[] functionNames = {"loop-controller", "language-detection", "sentence-translation", "word-ranking"};
			String[] entryPoints = {"loop_controller.lambda_handler", "language_detection.lambda_handler",
					"sentence_translation.lambda_handler", "word_ranking.lambda_handler"};
			Integer[] timeouts = {30, 30, 30, 30};
			Integer[] memories = {512, 1024, 1024, 1024};
			String[] regions = {AmazonCommandUtility.OHIO, AmazonCommandUtility.OHIO, AmazonCommandUtility.OHIO,
					AmazonCommandUtility.OHIO};
			String[] zipFileNames = {"loop_controller.zip", "language_detection.zip", "sentence_translation.zip",
					"word_ranking.zip"};

			CompositionCommandExecutor.deployOnAmazonComposition("ranking-translator",
					"/Users/francescomarino/IdeaProjects/serverless_composition_performance_" +
							"project/serverless_functions/aws/python/ranking_translator",
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
			Integer[] memories = {1024, 1024};
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
			Integer[] memories = {1024, 1024};
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

		{
			String[] functionNames = {"loop-controller", "language-detection", "sentence-translation", "word-ranking"};
			String[] entryPoints = {"loop_controller.Handler", "language_detection.Handler",
					"sentence_translation.Handler", "word_ranking.Handler"};
			Integer[] timeouts = {30, 30, 30, 30};
			Integer[] memories = {512, 1024, 1024, 1024};
			String[] regions = {AmazonCommandUtility.OHIO, AmazonCommandUtility.OHIO, AmazonCommandUtility.OHIO,
					AmazonCommandUtility.OHIO};
			String[] zipFileNames = {"loop_controller_java_aws-1.0.jar", "language_detection_java_aws-1.0.jar",
					"sentence_translation_java_aws-1.0.jar", "word_ranking_java_aws-1.0.jar"};

			CompositionCommandExecutor.deployOnAmazonComposition("ranking-translator",
					"/Users/francescomarino/IdeaProjects/serverless_composition_performance_" +
							"project/serverless_functions/aws/java/ranking_translator",
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



		/* Node.js on Google Cloud Platform */

		{
			String[] functionNames = {"image-recognition", "anger-detection"};
			String[] entryPoints = {"gcFunctionsHandler", "gcFunctionsHandler"};
			Integer[] timeouts = {30, 30};
			Integer[] memories = {1024, 1024};
			String[] regions = {GoogleCommandUtility.IOWA, GoogleCommandUtility.IOWA};
			String[] functionDirs = {"image_recognition", "anger_detection"};

			CompositionCommandExecutor.deployOnGoogleComposition("face-detection",
					"/Users/francescomarino/IdeaProjects/serverless_composition_performance_" +
							"project/serverless_functions/gcloud/node/face_recognition",
					GoogleCommandUtility.IOWA,
					"step.yaml",
					functionNames,
					GoogleCommandUtility.NODE_10_RUNTIME,
					entryPoints,
					timeouts,
					memories,
					regions,
					functionDirs);
		}

		{
			String[] functionNames = {"latency-test-workflow", "cpu-test-workflow"};
			String[] entryPoints = {"gcFunctionsHandler", "gcFunctionsHandler"};
			Integer[] timeouts = {30, 30};
			Integer[] memories = {128, 128};
			String[] regions = {GoogleCommandUtility.IOWA, GoogleCommandUtility.IOWA};
			String[] functionDirs = {"latency_test", "cpu_test"};

			CompositionCommandExecutor.deployOnGoogleComposition("basic-composition",
					"/Users/francescomarino/IdeaProjects/serverless_composition_performance_" +
							"project/serverless_functions/gcloud/node/basic_test_composition",
					GoogleCommandUtility.IOWA,
					"step.yaml",
					functionNames,
					GoogleCommandUtility.NODE_10_RUNTIME,
					entryPoints,
					timeouts,
					memories,
					regions,
					functionDirs);
		}



		/* Node.js on Amazon Web Services */

		{
			String[] functionNames = {"image-recognition", "anger-detection"};
			String[] entryPoints = {"index.lambdaHandler", "index.lambdaHandler"};
			Integer[] timeouts = {30, 30};
			Integer[] memories = {1024, 1024};
			String[] regions = {AmazonCommandUtility.OHIO, AmazonCommandUtility.OHIO};
			String[] zipFileNames = {"image_recognition.zip", "anger_detection.zip"};

			CompositionCommandExecutor.deployOnAmazonComposition("face-detection",
					"/Users/francescomarino/IdeaProjects/serverless_composition_performance" +
							"_project/serverless_functions/aws/node/face_recognition",
					AmazonCommandUtility.OHIO,
					"step.json",
					functionNames,
					AmazonCommandUtility.NODE_10_X_RUNTIME,
					entryPoints,
					timeouts,
					memories,
					regions,
					zipFileNames);
		}

		{
			String[] functionNames = {"latency-test-workflow", "cpu-test-workflow"};
			String[] entryPoints = {"index.lambdaHandler", "index.lambdaHandler"};
			Integer[] timeouts = {30, 30};
			Integer[] memories = {128, 128};
			String[] regions = {AmazonCommandUtility.OHIO, AmazonCommandUtility.OHIO};
			String[] zipFileNames = {"latency_test.zip", "cpu_test.zip"};

			CompositionCommandExecutor.deployOnAmazonComposition("basic-composition",
					"/Users/francescomarino/IdeaProjects/serverless_composition_performance" +
							"_project/serverless_functions/aws/node/basic_test_composition",
					AmazonCommandUtility.OHIO,
					"step.json",
					functionNames,
					AmazonCommandUtility.NODE_10_X_RUNTIME,
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

	private static void deployInfoFunctions() {

		FunctionCommandExecutor.deployOnGoogleCloudFunction("info-getter",
				GoogleCommandUtility.PYTHON_3_7_RUNTIME,
				"gc_functions_handler",
				30,
				128,
				GoogleCommandUtility.IOWA,
				"/Users/francescomarino/IdeaProjects/serverless_composition_performance_" +
						"project/serverless_functions/gcloud/python/info_getter");

		FunctionCommandExecutor.deployOnAmazonRESTFunction("info-getter",
				AmazonCommandUtility.PYTHON_3_7_RUNTIME,
				"info_getter.lambda_handler",
				30,
				128,
				AmazonCommandUtility.OHIO,
				"/Users/francescomarino/IdeaProjects/serverless_composition_performance_" +
						"project/serverless_functions/aws/python/info_getter",
				"info_getter.zip");

	}

	@Deprecated
	private static void customFunction() {

		TablesCommandExecutor.createAmazonTable("ranking_translator",
				"/Users/francescomarino/IdeaProjects/serverless_composition_performance_project/" +
						"serverless_functions/aws/dynamo_tables",
				"ranking_translator.json",
				AmazonCommandUtility.OHIO);


		FunctionCommandExecutor.deployOnAmazonRESTFunction("language-detection",
				AmazonCommandUtility.NODE_10_X_RUNTIME,
				"index.lambdaHandler",
				30,
				1024,
				AmazonCommandUtility.OHIO,
				"/Users/francescomarino/IdeaProjects/serverless_composition_performance_project" +
						"/serverless_functions/aws/node/ranking_translator",
				"language_detection.zip");

		FunctionCommandExecutor.deployOnAmazonRESTFunction("loop-controller",
				AmazonCommandUtility.NODE_10_X_RUNTIME,
				"index.lambdaHandler",
				30,
				512,
				AmazonCommandUtility.OHIO,
				"/Users/francescomarino/IdeaProjects/serverless_composition_performance_project" +
						"/serverless_functions/aws/node/ranking_translator",
				"loop_controller.zip");

		FunctionCommandExecutor.deployOnAmazonRESTFunction("sentence-translation",
				AmazonCommandUtility.NODE_10_X_RUNTIME,
				"index.lambdaHandler",
				30,
				1024,
				AmazonCommandUtility.OHIO,
				"/Users/francescomarino/IdeaProjects/serverless_composition_performance_project" +
						"/serverless_functions/aws/node/ranking_translator",
				"sentence_translation.zip");

	}
}
