import cmd.benchmark_commands.BenchmarkCommandExecutor;
import cmd.functionality_commands.*;
import javax.annotation.Nullable;

/**
 * Application entry point class. This class garantees the use of the serverless benchmarking tool.
 */
@SuppressWarnings({"DuplicatedCode", "SpellCheckingInspection"})
public class ServerlessFlowBenchMain {

	/**
	 * Benchmark parameters, can be changed, choose a reasonable workload for your system.
	 */
	// time to cause request timeout in cold start HTTP invocation
	private static final int REQUEST_TIMEOUT_MILLISECONDS = Integer.MAX_VALUE;
	// number of thread to generate in order to perform load test
	private static final int THREAD_NUMBER = 2;
	// load test duration
	private static final int BENCHMARK_DURATION_SECONDS = 60;
	// number of request per second in load teste
	private static final int REQUESTS_PER_SECOND = 500;
	// number of concurrent opened connections
	private static final int CONCURRENCY = 50;
	// time to deallocate resources and perform a cold start
	private static final int SLEEP_INTERVAL_MILLISECONDS = 5 * 60 * 60 * 1000; // 5 hours
	// number of latency measurement to ignore in warm start detection
	private static final int IGNORED_COLD_START_VALUES = 5;
	// number of latency measurement to find to evaluate warm start latency
	private static final int WARM_START_AVG_WIDTH = 15;
	// number of iterations
	private static final int ITERATIONS = 10;
	// maximum concurrency level
	private static final int MAX_TOTAL_CONCURRENCY = 1;

	/**
	 * Execution parameters, can be changed
	 */
	// execute deployment on Google Cloud Platform
	private static final boolean GOOGLE_DEPLOY = true;
	// execute deployment on Amazon Web Services
	private static final boolean AMAZON_DEPLOY = true;
	// execute deployment on OpenWhisk
	private static final boolean OPENWHISK_DEPLOY = true;
	// execute Python functionalities deployment
	private static final boolean PYTHON = true;
	// execute Java functionalities deployment
	private static final boolean JAVA = true;
	// execute Node.js functionalities deployment
	private static final boolean NODE = true;

	/**
	 * Select operation to perform:
	 * 0 -> deploy functions and compositions
	 * 1 -> perform benchmarks
	 * 2 -> perform a complete cleanup
	 * 3 -> 0 + 1 + 2
	 * 4 -> deploy info gathering functionalities
	 */
	private static final int OPERATION_SELECTION = 3;


	/**
	 * Application entry point method, executes functionality expressed in OPERATION_SELECTION
	 * @param args default argument structure, none is needed
	 */
	@SuppressWarnings("ConstantConditions")
	public static void main(@Nullable String[] args) {

		switch (OPERATION_SELECTION) {
			case 0:
				deploy();
				break;
			case 1:
				benchmarkPerform();
				break;
			case 2:
				cleanup();
				break;
			case 3:
				deploy();
				benchmarkPerform();
				cleanup();
				break;
			case 4:
				deployInfoFunctions();
				break;
			default:
				System.err.println("Please provide a valid OPERATION_SELECTION value.");
		}
	}

	/**
	 * Performs a complete serverless environments cleanup
	 */
	private static void cleanup() {
		cleanupFunctions();
		cleanupCompositions();
		cleanupTables();
		cleanupBuckets();
	}

	/**
	 * Deletes serverless functions
	 */
	private static void cleanupFunctions() {
		System.out.println("\u001B[35m" + "\n\nRemoving benchmark functions...\n" + "\u001B[0m");
		FunctionCommandExecutor.cleanupGoogleCloudFunctions();
		FunctionCommandExecutor.cleanupAmazonRESTFunctions();
		FunctionCommandExecutor.cleanupOpenWhiskFunctions();
	}

	/**
	 * Deletes serverless compositions
	 */
	private static void cleanupCompositions() {
		System.out.println("\u001B[35m" + "\n\nRemoving benchmark compositions...\n" + "\u001B[0m");
		CompositionCommandExecutor.cleanupGoogleComposition();
		CompositionCommandExecutor.cleanupAmazonComposition();
		CompositionCommandExecutor.cleanupOpenWhiskComposition();
	}

	/**
	 * Deletes cloud tables
	 */
	private static void cleanupTables() {
		System.out.println("\u001B[35m" + "\n\nRemoving cloud tables...\n" + "\u001B[0m");
		TablesCommandExecutor.cleanupGoogleCloudTables();
		TablesCommandExecutor.cleanupAmazonCloudTables();
	}

	/**
	 * Deletes cloud storage buckets
	 */
	private static void cleanupBuckets() {
		System.out.println("\u001B[35m" + "\n\nRemoving cloud buckets...\n" + "\u001B[0m");
		BucketsCommandExecutor.cleanupGoogleCloudBuckets();
		BucketsCommandExecutor.cleanupAmazonCloudBuckets();
	}

	/**
	 * Performs deployment of serverless functions and compositions
	 */
	private static void deploy() {
		deployFunctions();
		deployCompositions();
	}

	/**
	 * Deploys serverless functions
	 */
	private static void deployFunctions() {
		System.out.println("\u001B[35m" + "\n\nDeploying benchmark functions...\n" + "\u001B[0m");

		/* Python on Google Cloud Platform */

		if (GOOGLE_DEPLOY && PYTHON) {

			FunctionCommandExecutor.deployOnGoogleCloudFunction("latency-test",
					GoogleCommandUtility.PYTHON_3_7_RUNTIME,
					"gc_functions_handler",
					30,
					128,
					GoogleCommandUtility.IOWA,
					"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
							"/serverless_functions/gcloud/python/basic_test_composition/latency_test");

			FunctionCommandExecutor.deployOnGoogleCloudFunction("cpu-test",
					GoogleCommandUtility.PYTHON_3_7_RUNTIME,
					"gc_functions_handler",
					30,
					128,
					GoogleCommandUtility.IOWA,
					"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
							"/serverless_functions/gcloud/python/basic_test_composition/cpu_test");

			FunctionCommandExecutor.deployOnGoogleCloudFunction("memory-test",
					GoogleCommandUtility.PYTHON_3_7_RUNTIME,
					"gc_functions_handler",
					30,
					128,
					GoogleCommandUtility.IOWA,
					"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
							"/serverless_functions/gcloud/python/memory_test");

		}



		/* Python on Amazon Web Services */

		if (AMAZON_DEPLOY && PYTHON) {

			FunctionCommandExecutor.deployOnAmazonRESTFunction("latency-test",
					AmazonCommandUtility.PYTHON_3_7_RUNTIME,
					"latency_test.lambda_handler",
					30,
					128,
					AmazonCommandUtility.OHIO,
					"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
							"/serverless_functions/aws/python/basic_test_composition/latency_test",
					"latency_test.zip");

			FunctionCommandExecutor.deployOnAmazonRESTFunction("cpu-test",
					AmazonCommandUtility.PYTHON_3_7_RUNTIME,
					"cpu_test.lambda_handler",
					30,
					128,
					AmazonCommandUtility.OHIO,
					"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
							"/serverless_functions/aws/python/basic_test_composition/cpu_test",
					"cpu_test.zip");

			FunctionCommandExecutor.deployOnAmazonRESTFunction("memory-test",
					AmazonCommandUtility.PYTHON_3_7_RUNTIME,
					"memory_test.lambda_handler",
					30,
					128,
					AmazonCommandUtility.OHIO,
					"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
							"/serverless_functions/aws/python/memory_test",
					"memory_test.zip");

		}



		/* Python on OpenWhisk */

		if (OPENWHISK_DEPLOY && PYTHON) {

			FunctionCommandExecutor.deployOnOpenWhisk("latency-test",
					OpenWhiskCommandUtility.PYTHON_3_RUNTIME,
					"ow_handler",
					30,
					128,
					"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
							"/serverless_functions/openwhisk/python/basic_test_composition/latency_test",
					"latency_test.zip");

			FunctionCommandExecutor.deployOnOpenWhisk("cpu-test",
					OpenWhiskCommandUtility.PYTHON_3_RUNTIME,
					"ow_handler",
					30,
					128,
					"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
							"/serverless_functions/openwhisk/python/basic_test_composition/cpu_test",
					"cpu_test.zip");

			FunctionCommandExecutor.deployOnOpenWhisk("memory-test",
					OpenWhiskCommandUtility.PYTHON_3_RUNTIME,
					"ow_handler",
					30,
					128,
					"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
							"/serverless_functions/openwhisk/python/memory_test",
					"memory_test.zip");
		}



		/* Java on Google Cloud Platform */

		if (GOOGLE_DEPLOY && JAVA) {

			FunctionCommandExecutor.deployOnGoogleCloudFunction("latency-test",
					GoogleCommandUtility.JAVA_11_RUNTIME,
					"latency_test.Handler",
					30,
					128,
					GoogleCommandUtility.IOWA,
					"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
							"/serverless_functions/gcloud/java/basic_test_composition/latency_test");

			FunctionCommandExecutor.deployOnGoogleCloudFunction("cpu-test",
					GoogleCommandUtility.JAVA_11_RUNTIME,
					"cpu_test.Handler",
					30,
					128,
					GoogleCommandUtility.IOWA,
					"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
							"/serverless_functions/gcloud/java/basic_test_composition/cpu_test");

			FunctionCommandExecutor.deployOnGoogleCloudFunction("memory-test",
					GoogleCommandUtility.JAVA_11_RUNTIME,
					"memory_test.Handler",
					30,
					128,
					GoogleCommandUtility.IOWA,
					"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
							"/serverless_functions/gcloud/java/memory_test");

		}



		/* Java on Amazon Web Services */

		if (AMAZON_DEPLOY && JAVA) {

			FunctionCommandExecutor.deployOnAmazonRESTFunction("latency-test",
					AmazonCommandUtility.JAVA_11_RUNTIME,
					"latency_test.Handler",
					30,
					128,
					AmazonCommandUtility.OHIO,
					"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
							"/serverless_functions/aws/java/latency_test/target",
					"latency_test_java_aws-1.0.jar");

			FunctionCommandExecutor.deployOnAmazonRESTFunction("cpu-test",
					AmazonCommandUtility.JAVA_11_RUNTIME,
					"cpu_test.Handler",
					30,
					128,
					AmazonCommandUtility.OHIO,
					"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
							"/serverless_functions/aws/java/cpu_test/target",
					"cpu_test_java_aws-1.0.jar");

			FunctionCommandExecutor.deployOnAmazonRESTFunction("memory-test",
					AmazonCommandUtility.JAVA_11_RUNTIME,
					"memory_test.Handler",
					30,
					128,
					AmazonCommandUtility.OHIO,
					"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
							"/serverless_functions/aws/java/memory_test/target",
					"memory_test_java_aws-1.0.jar");

		}



		/* Java on OpenWhisk */

		if (OPENWHISK_DEPLOY && JAVA) {

			FunctionCommandExecutor.deployOnOpenWhisk("latency-test",
					OpenWhiskCommandUtility.JAVA_8_RUNTIME,
					"latency_test.Handler",
					30,
					128,
					"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
							"/serverless_functions/openwhisk/java/basic_test_composition/latency_test/target",
					"latency_test_java_ow-1.0.jar");

			FunctionCommandExecutor.deployOnOpenWhisk("cpu-test",
					OpenWhiskCommandUtility.JAVA_8_RUNTIME,
					"cpu_test.Handler",
					30,
					128,
					"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
							"/serverless_functions/openwhisk/java/basic_test_composition/cpu_test/target",
					"cpu_test_java_ow-1.0.jar");

			FunctionCommandExecutor.deployOnOpenWhisk("memory-test",
					OpenWhiskCommandUtility.JAVA_8_RUNTIME,
					"memory_test.Handler",
					30,
					128,
					"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
							"/serverless_functions/openwhisk/java/memory_test/target",
					"memory_test_java_ow-1.0.jar");
		}



		/* Node.js on Google Cloud Platform */

		if (GOOGLE_DEPLOY && NODE) {

			FunctionCommandExecutor.deployOnGoogleCloudFunction("latency-test",
					GoogleCommandUtility.NODE_10_RUNTIME,
					"gcFunctionsHandler",
					30,
					128,
					GoogleCommandUtility.IOWA,
					"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
							"/serverless_functions/gcloud/node/basic_test_composition/latency_test");

			FunctionCommandExecutor.deployOnGoogleCloudFunction("cpu-test",
					GoogleCommandUtility.NODE_10_RUNTIME,
					"gcFunctionsHandler",
					30,
					128,
					GoogleCommandUtility.IOWA,
					"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
							"/serverless_functions/gcloud/node/basic_test_composition/cpu_test");

			FunctionCommandExecutor.deployOnGoogleCloudFunction("memory-test",
					GoogleCommandUtility.NODE_10_RUNTIME,
					"gcFunctionsHandler",
					30,
					128,
					GoogleCommandUtility.IOWA,
					"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
							"/serverless_functions/gcloud/node/memory_test");

		}



		/* Node.js on Amazon Web Services */

		if (AMAZON_DEPLOY && NODE) {

			FunctionCommandExecutor.deployOnAmazonRESTFunction("latency-test",
					AmazonCommandUtility.NODE_10_X_RUNTIME,
					"index.lambdaHandler",
					30,
					128,
					AmazonCommandUtility.OHIO,
					"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
							"/serverless_functions/aws/node/basic_test_composition/latency_test",
					"latency_test.zip");

			FunctionCommandExecutor.deployOnAmazonRESTFunction("cpu-test",
					AmazonCommandUtility.NODE_10_X_RUNTIME,
					"index.lambdaHandler",
					30,
					128,
					AmazonCommandUtility.OHIO,
					"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
							"/serverless_functions/aws/node/basic_test_composition/cpu_test",
					"cpu_test.zip");

			FunctionCommandExecutor.deployOnAmazonRESTFunction("memory-test",
					AmazonCommandUtility.NODE_10_X_RUNTIME,
					"index.lambdaHandler",
					30,
					128,
					AmazonCommandUtility.OHIO,
					"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
							"/serverless_functions/aws/node/memory_test",
					"memory_test.zip");

		}



		/* Node.js on OpenWhisk */

		if (OPENWHISK_DEPLOY && NODE) {

			FunctionCommandExecutor.deployOnOpenWhisk("latency-test",
					OpenWhiskCommandUtility.NODE_10_RUNTIME,
					"index.owHandler",
					30,
					128,
					"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
							"/serverless_functions/openwhisk/node/basic_test_composition/latency_test",
					"latency_test.zip");

			FunctionCommandExecutor.deployOnOpenWhisk("cpu-test",
					OpenWhiskCommandUtility.NODE_10_RUNTIME,
					"index.owHandler",
					30,
					128,
					"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
							"/serverless_functions/openwhisk/node/basic_test_composition/cpu_test",
					"cpu_test.zip");

			FunctionCommandExecutor.deployOnOpenWhisk("memory-test",
					OpenWhiskCommandUtility.NODE_10_RUNTIME,
					"index.owHandler",
					30,
					128,
					"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
							"/serverless_functions/openwhisk/node/memory_test",
					"memory_test.zip");
		}
	}

	/**
	 * Deploys serverless compositions
	 */
	private static void deployCompositions() {
		System.out.println("\u001B[35m" + "\n\nDeploying benchmark compositions...\n" + "\u001B[0m");

		/* Cloud buckets */

		if (GOOGLE_DEPLOY) {

			BucketsCommandExecutor.createGoogleBucket("benchmarking-project-translator-logging-bucket",
					GoogleCommandUtility.IOWA);

		}

		if (AMAZON_DEPLOY) {

			BucketsCommandExecutor.createAmazonBucket("benchmarking-project-translator-logging-bucket",
					AmazonCommandUtility.S3_ACL_PRIVATE, AmazonCommandUtility.OHIO);

		}



		/* Python on Google Cloud Platform */

		if (GOOGLE_DEPLOY && PYTHON) {

			{
				String[] functionNames = {"image-recognition", "anger-detection"};
				String[] entryPoints = {"gc_functions_handler", "gc_functions_handler"};
				Integer[] timeouts = {30, 30};
				Integer[] memories = {1024, 1024};
				String[] regions = {GoogleCommandUtility.IOWA, GoogleCommandUtility.IOWA};
				String[] functionDirs = {"image_recognition", "anger_detection"};

				CompositionCommandExecutor.deployOnGoogleComposition("face-detection",
						"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
								"/serverless_functions/gcloud/python/face_recognition",
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
						"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
								"/serverless_functions/gcloud/python/basic_test_composition",
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
				String[] functionNames = {"loop-controller", "language-detection", "sentence-translation",
						"translation-logger"};
				String[] entryPoints = {"gc_functions_handler", "gc_functions_handler", "gc_functions_handler",
						"gc_functions_handler"};
				Integer[] timeouts = {30, 30, 30, 30};
				Integer[] memories = {512, 1024, 1024, 1024};
				String[] regions = {GoogleCommandUtility.IOWA, GoogleCommandUtility.IOWA, GoogleCommandUtility.IOWA,
						GoogleCommandUtility.IOWA};
				String[] functionDirs = {"loop_controller", "language_detection", "sentence_translation",
						"translation_logger"};

				CompositionCommandExecutor.deployOnGoogleComposition("cycle-translator",
						"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
								"/serverless_functions/gcloud/python/cycle_translator",
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

		}



		/* Python on Amazon Web Services */

		if (AMAZON_DEPLOY && PYTHON) {

			{
				String[] functionNames = {"image-recognition", "anger-detection"};
				String[] entryPoints = {"image_recognition.lambda_handler", "anger_detection.lambda_handler"};
				Integer[] timeouts = {30, 30};
				Integer[] memories = {1024, 1024};
				String[] regions = {AmazonCommandUtility.OHIO, AmazonCommandUtility.OHIO};
				String[] zipFileNames = {"image_recognition.zip", "anger_detection.zip"};

				CompositionCommandExecutor.deployOnAmazonComposition("face-detection",
						"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
								"/serverless_functions/aws/python/face_recognition",
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
						"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
								"/serverless_functions/aws/python/basic_test_composition",
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
				String[] functionNames = {"loop-controller", "language-detection", "sentence-translation",
						"translation-logger"};
				String[] entryPoints = {"loop_controller.lambda_handler", "language_detection.lambda_handler",
						"sentence_translation.lambda_handler", "translation_logger.lambda_handler"};
				Integer[] timeouts = {30, 30, 30, 30};
				Integer[] memories = {512, 1024, 1024, 1024};
				String[] regions = {AmazonCommandUtility.OHIO, AmazonCommandUtility.OHIO, AmazonCommandUtility.OHIO,
						AmazonCommandUtility.OHIO};
				String[] zipFileNames = {"loop_controller.zip", "language_detection.zip", "sentence_translation.zip",
						"translation_logger.zip"};

				CompositionCommandExecutor.deployOnAmazonComposition("cycle-translator",
						"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
								"/serverless_functions/aws/python/cycle_translator",
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



		/* Python on OpenWhisk */

		if (OPENWHISK_DEPLOY && PYTHON) {

			{
				String[] functionNames = {"image-recognition", "anger-detection"};
				String[] entryPoints = {"ow_handler", "ow_handler"};
				Integer[] timeouts = {30, 30};
				// 1024 mb not available
				Integer[] memories = {500, 500};
				String[] zipFileNames = {"image_recognition.zip", "anger_detection.zip"};

				CompositionCommandExecutor.deployOnOpenWhiskComposition("face-detection",
						"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
								"/serverless_functions/openwhisk/python/face_recognition",
						"step.js",
						functionNames,
						OpenWhiskCommandUtility.PYTHON_3_RUNTIME,
						entryPoints,
						timeouts,
						memories,
						zipFileNames);
			}

			{
				String[] functionNames = {"latency-test-workflow", "cpu-test-workflow"};
				String[] entryPoints = {"ow_handler", "ow_handler"};
				Integer[] timeouts = {30, 30};
				Integer[] memories = {128, 128};
				String[] zipFileNames = {"latency_test.zip", "cpu_test.zip"};

				CompositionCommandExecutor.deployOnOpenWhiskComposition("basic-composition",
						"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
								"/serverless_functions/openwhisk/python/basic_test_composition",
						"step.js",
						functionNames,
						OpenWhiskCommandUtility.PYTHON_3_RUNTIME,
						entryPoints,
						timeouts,
						memories,
						zipFileNames);
			}

		}



		/* Java on Google Cloud Platform */

		if (GOOGLE_DEPLOY && JAVA) {

			{
				String[] functionNames = {"image-recognition", "anger-detection"};
				String[] entryPoints = {"image_recognition.Handler", "anger_detection.Handler"};
				Integer[] timeouts = {30, 30};
				Integer[] memories = {1024, 1024};
				String[] regions = {GoogleCommandUtility.IOWA, GoogleCommandUtility.IOWA};
				String[] functionDirs = {"image_recognition", "anger_detection"};

				CompositionCommandExecutor.deployOnGoogleComposition("face-detection",
						"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
								"/serverless_functions/gcloud/java/face_recognition",
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
						"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
								"/serverless_functions/gcloud/java/basic_test_composition",
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
				String[] functionNames = {"loop-controller", "language-detection", "sentence-translation",
						"translation-logger"};
				String[] entryPoints = {"loop_controller.Handler", "language_detection.Handler",
						"sentence_translation.Handler", "translation_logger.Handler"};
				Integer[] timeouts = {30, 30, 30, 30};
				Integer[] memories = {512, 1024, 1024, 1024};
				String[] regions = {GoogleCommandUtility.IOWA, GoogleCommandUtility.IOWA, GoogleCommandUtility.IOWA,
						GoogleCommandUtility.IOWA};
				String[] functionDirs = {"loop_controller", "language_detection", "sentence_translation",
						"translation_logger"};

				CompositionCommandExecutor.deployOnGoogleComposition("cycle-translator",
						"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
								"/serverless_functions/gcloud/java/cycle_translator",
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

		}



		/* Java on Amazon Web Services */

		if (AMAZON_DEPLOY && JAVA) {

			{
				String[] functionNames = {"image-recognition", "anger-detection"};
				String[] entryPoints = {"image_recognition.Handler", "anger_detection.Handler"};
				Integer[] timeouts = {30, 30};
				Integer[] memories = {1024, 1024};
				String[] regions = {AmazonCommandUtility.OHIO, AmazonCommandUtility.OHIO};
				String[] zipFileNames = {"image_recognition_java_aws-1.0.jar", "anger_detection_java_aws-1.0.jar"};

				CompositionCommandExecutor.deployOnAmazonComposition("face-detection",
						"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
								"/serverless_functions/aws/java/face_recognition",
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
						"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
								"/serverless_functions/aws/java/basic_test_composition",
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
				String[] functionNames = {"loop-controller", "language-detection", "sentence-translation",
						"translation-logger"};
				String[] entryPoints = {"loop_controller.Handler", "language_detection.Handler",
						"sentence_translation.Handler", "translation_logger.Handler"};
				Integer[] timeouts = {30, 30, 30, 30};
				Integer[] memories = {512, 1024, 1024, 1024};
				String[] regions = {AmazonCommandUtility.OHIO, AmazonCommandUtility.OHIO, AmazonCommandUtility.OHIO,
						AmazonCommandUtility.OHIO};
				String[] zipFileNames = {"loop_controller_java_aws-1.0.jar", "language_detection_java_aws-1.0.jar",
						"sentence_translation_java_aws-1.0.jar", "translation_logger_java_aws-1.0.jar"};

				CompositionCommandExecutor.deployOnAmazonComposition("cycle-translator",
						"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
								"/serverless_functions/aws/java/cycle_translator",
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



		/* Java on OpenWhisk */

		if (OPENWHISK_DEPLOY && JAVA) {

			{
				String[] functionNames = {"image-recognition", "anger-detection"};
				String[] entryPoints = {"image_recognition.Handler", "anger_detection.Handler"};
				Integer[] timeouts = {30, 30};
				// 1024 mb not available
				Integer[] memories = {500, 500};
				String[] zipFileNames = {"image_recognition_java_ow-1.0.jar", "anger_detection_java_ow-1.0.jar"};

				CompositionCommandExecutor.deployOnOpenWhiskComposition("face-detection",
						"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
								"/serverless_functions/openwhisk/java/face_recognition",
						"step.js",
						functionNames,
						OpenWhiskCommandUtility.JAVA_8_RUNTIME,
						entryPoints,
						timeouts,
						memories,
						zipFileNames);
			}

			{
				String[] functionNames = {"latency-test-workflow", "cpu-test-workflow"};
				String[] entryPoints = {"latency_test.Handler", "cpu_test.Handler"};
				Integer[] timeouts = {30, 30};
				Integer[] memories = {128, 128};
				String[] zipFileNames = {"latency_test_java_ow-1.0.jar", "cpu_test_java_ow-1.0.jar"};

				CompositionCommandExecutor.deployOnOpenWhiskComposition("basic-composition",
						"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
								"/serverless_functions/openwhisk/java/basic_test_composition",
						"step.js",
						functionNames,
						OpenWhiskCommandUtility.JAVA_8_RUNTIME,
						entryPoints,
						timeouts,
						memories,
						zipFileNames);
			}

		}



		/* Node.js on Google Cloud Platform */

		if (GOOGLE_DEPLOY && NODE) {

			{
				String[] functionNames = {"image-recognition", "anger-detection"};
				String[] entryPoints = {"gcFunctionsHandler", "gcFunctionsHandler"};
				Integer[] timeouts = {30, 30};
				Integer[] memories = {1024, 1024};
				String[] regions = {GoogleCommandUtility.IOWA, GoogleCommandUtility.IOWA};
				String[] functionDirs = {"image_recognition", "anger_detection"};

				CompositionCommandExecutor.deployOnGoogleComposition("face-detection",
						"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
								"/serverless_functions/gcloud/node/face_recognition",
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
						"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
								"/serverless_functions/gcloud/node/basic_test_composition",
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
				String[] functionNames = {"loop-controller", "language-detection", "sentence-translation",
						"translation-logger"};
				String[] entryPoints = {"gcFunctionsHandler", "gcFunctionsHandler", "gcFunctionsHandler",
						"gcFunctionsHandler"};
				Integer[] timeouts = {30, 30, 30, 30};
				Integer[] memories = {512, 1024, 1024, 1024};
				String[] regions = {GoogleCommandUtility.IOWA, GoogleCommandUtility.IOWA, GoogleCommandUtility.IOWA,
						GoogleCommandUtility.IOWA};
				String[] functionDirs = {"loop_controller", "language_detection", "sentence_translation",
						"translation_logger"};

				CompositionCommandExecutor.deployOnGoogleComposition("cycle-translator",
						"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
								"/serverless_functions/gcloud/node/cycle_translator",
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

		}



		/* Node.js on Amazon Web Services */

		if (AMAZON_DEPLOY && NODE) {

			{
				String[] functionNames = {"image-recognition", "anger-detection"};
				String[] entryPoints = {"index.lambdaHandler", "index.lambdaHandler"};
				Integer[] timeouts = {30, 30};
				Integer[] memories = {1024, 1024};
				String[] regions = {AmazonCommandUtility.OHIO, AmazonCommandUtility.OHIO};
				String[] zipFileNames = {"image_recognition.zip", "anger_detection.zip"};

				CompositionCommandExecutor.deployOnAmazonComposition("face-detection",
						"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
								"/serverless_functions/aws/node/face_recognition",
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
						"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
								"/serverless_functions/aws/node/basic_test_composition",
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
				String[] functionNames = {"loop-controller", "language-detection", "sentence-translation",
						"translation-logger"};
				String[] entryPoints = {"index.lambdaHandler", "index.lambdaHandler", "index.lambdaHandler",
						"index.lambdaHandler"};
				Integer[] timeouts = {30, 30, 30, 30};
				Integer[] memories = {512, 1024, 1024, 1024};
				String[] regions = {AmazonCommandUtility.OHIO, AmazonCommandUtility.OHIO, AmazonCommandUtility.OHIO,
						AmazonCommandUtility.OHIO};
				String[] zipFileNames = {"loop_controller.zip", "language_detection.zip", "sentence_translation.zip",
						"translation_logger.zip"};

				CompositionCommandExecutor.deployOnAmazonComposition("cycle-translator",
						"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
								"/serverless_functions/aws/node/cycle_translator",
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



		/* Node.js on OpenWhisk */

		if (OPENWHISK_DEPLOY && NODE) {

			{
				String[] functionNames = {"image-recognition", "anger-detection"};
				String[] entryPoints = {"index.owHandler", "index.owHandler"};
				Integer[] timeouts = {30, 30};
				// 1024 mb not available
				Integer[] memories = {500, 500};
				String[] zipFileNames = {"image_recognition.zip", "anger_detection.zip"};

				CompositionCommandExecutor.deployOnOpenWhiskComposition("face-detection",
						"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
								"/serverless_functions/openwhisk/node/face_recognition",
						"step.js",
						functionNames,
						OpenWhiskCommandUtility.NODE_10_RUNTIME,
						entryPoints,
						timeouts,
						memories,
						zipFileNames);
			}

			{
				String[] functionNames = {"latency-test-workflow", "cpu-test-workflow"};
				String[] entryPoints = {"index.owHandler", "index.owHandler"};
				Integer[] timeouts = {30, 30};
				Integer[] memories = {128, 128};
				String[] zipFileNames = {"latency_test.zip", "cpu_test.zip"};

				CompositionCommandExecutor.deployOnOpenWhiskComposition("basic-composition",
						"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
								"/serverless_functions/openwhisk/node/basic_test_composition",
						"step.js",
						functionNames,
						OpenWhiskCommandUtility.NODE_10_RUNTIME,
						entryPoints,
						timeouts,
						memories,
						zipFileNames);
			}

		}

	}

	/**
	 * Performs benchmarks on deployed resources
	 */
	private static void benchmarkPerform() {

		BenchmarkCommandExecutor benchmarker = new BenchmarkCommandExecutor(MAX_TOTAL_CONCURRENCY);
		benchmarker.performBenchmarks(
				CONCURRENCY,
				THREAD_NUMBER,
				BENCHMARK_DURATION_SECONDS,
				REQUESTS_PER_SECOND,
				SLEEP_INTERVAL_MILLISECONDS,
				REQUEST_TIMEOUT_MILLISECONDS,
				ITERATIONS,
				IGNORED_COLD_START_VALUES,
				WARM_START_AVG_WIDTH);
	}

	/**
	 * Deploys serverless functions that can collect memory and CPU information
	 */
	private static void deployInfoFunctions() {
		System.out.println("\u001B[35m" + "\n\nDeploying information collecting functions...\n" + "\u001B[0m");

		if (GOOGLE_DEPLOY) {

			FunctionCommandExecutor.deployOnGoogleCloudFunction("info-getter-128",
					GoogleCommandUtility.PYTHON_3_7_RUNTIME,
					"gc_functions_handler",
					30,
					128,
					GoogleCommandUtility.IOWA,
					"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
							"/serverless_functions/gcloud/python/info_getter");

			FunctionCommandExecutor.deployOnGoogleCloudFunction("info-getter-256",
					GoogleCommandUtility.PYTHON_3_7_RUNTIME,
					"gc_functions_handler",
					30,
					256,
					GoogleCommandUtility.IOWA,
					"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
							"/serverless_functions/gcloud/python/info_getter");

			FunctionCommandExecutor.deployOnGoogleCloudFunction("info-getter-512",
					GoogleCommandUtility.PYTHON_3_7_RUNTIME,
					"gc_functions_handler",
					30,
					512,
					GoogleCommandUtility.IOWA,
					"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
							"/serverless_functions/gcloud/python/info_getter");

			FunctionCommandExecutor.deployOnGoogleCloudFunction("info-getter-1024",
					GoogleCommandUtility.PYTHON_3_7_RUNTIME,
					"gc_functions_handler",
					30,
					1024,
					GoogleCommandUtility.IOWA,
					"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
							"/serverless_functions/gcloud/python/info_getter");

			FunctionCommandExecutor.deployOnGoogleCloudFunction("info-getter-2048",
					GoogleCommandUtility.PYTHON_3_7_RUNTIME,
					"gc_functions_handler",
					30,
					2048,
					GoogleCommandUtility.IOWA,
					"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
							"/serverless_functions/gcloud/python/info_getter");

		}

		if (AMAZON_DEPLOY) {

			FunctionCommandExecutor.deployOnAmazonRESTFunction("info-getter-128",
					AmazonCommandUtility.PYTHON_3_7_RUNTIME,
					"info_getter.lambda_handler",
					30,
					128,
					AmazonCommandUtility.OHIO,
					"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
							"/serverless_functions/aws/python/info_getter",
					"info_getter.zip");

			FunctionCommandExecutor.deployOnAmazonRESTFunction("info-getter-256",
					AmazonCommandUtility.PYTHON_3_7_RUNTIME,
					"info_getter.lambda_handler",
					30,
					256,
					AmazonCommandUtility.OHIO,
					"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
							"/serverless_functions/aws/python/info_getter",
					"info_getter.zip");

			FunctionCommandExecutor.deployOnAmazonRESTFunction("info-getter-512",
					AmazonCommandUtility.PYTHON_3_7_RUNTIME,
					"info_getter.lambda_handler",
					30,
					512,
					AmazonCommandUtility.OHIO,
					"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
							"/serverless_functions/aws/python/info_getter",
					"info_getter.zip");

			FunctionCommandExecutor.deployOnAmazonRESTFunction("info-getter-1024",
					AmazonCommandUtility.PYTHON_3_7_RUNTIME,
					"info_getter.lambda_handler",
					30,
					1024,
					AmazonCommandUtility.OHIO,
					"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
							"/serverless_functions/aws/python/info_getter",
					"info_getter.zip");

			FunctionCommandExecutor.deployOnAmazonRESTFunction("info-getter-2048",
					AmazonCommandUtility.PYTHON_3_7_RUNTIME,
					"info_getter.lambda_handler",
					30,
					2048,
					AmazonCommandUtility.OHIO,
					"/Users/francescomarino/IdeaProjects/ServerlessFlowBench" +
							"/serverless_functions/aws/python/info_getter",
					"info_getter.zip");

		}

	}
}
