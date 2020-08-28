import cmd.AmazonCommandUtility;
import cmd.CommandExecutor;
import cmd.GoogleCommandUtility;

import java.io.IOException;

public class MainTest {

	public static void main(String[] args) {

		/*
		try {
			CommandExecutor.deployOnGoogleCloudPlatform("latency-test",
					GoogleCommandUtility.PYTHON_3_7_RUNTIME,
					"gc_functions_handler",
					30,
					128,
					GoogleCommandUtility.NORTH_VIRGINIA,
					"/Users/francescomarino/IdeaProjects/serverless_composition_performance_project/serverless_functions/gcloud/latency_test");
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}

		try {
			CommandExecutor.deployOnGoogleCloudPlatform("cpu-test",
					GoogleCommandUtility.PYTHON_3_7_RUNTIME,
					"gc_functions_handler",
					30,
					128,
					GoogleCommandUtility.NORTH_VIRGINIA,
					"/Users/francescomarino/IdeaProjects/serverless_composition_performance_project/serverless_functions/gcloud/cpu_test");
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}

		try {
			CommandExecutor.deployOnGoogleCloudPlatform("memory-test",
					GoogleCommandUtility.PYTHON_3_7_RUNTIME,
					"gc_functions_handler",
					30,
					128,
					GoogleCommandUtility.NORTH_VIRGINIA,
					"/Users/francescomarino/IdeaProjects/serverless_composition_performance_project/serverless_functions/gcloud/memory_test");
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
*/
		try {
			CommandExecutor.deployOnAmazonWebServices("cpu-test",
					AmazonCommandUtility.PYTHON_3_7_RUNTIME,
					"cpu_test.lambda_handler",
					30,
					128,
					AmazonCommandUtility.NORTH_VIRGINIA,
					"/Users/francescomarino/IdeaProjects/serverless_composition_performance_project/serverless_functions/aws/cpu_test",
					"cpu_test.zip");
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
	}
}
