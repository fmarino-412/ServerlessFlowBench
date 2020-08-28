import cmd.CommandExecutor;

import java.io.IOException;

public class MainTest {

	public static void main(String[] args) {

		try {
			CommandExecutor.deployOnGoogleCloudPlatform("latency-test",
					"gc_functions_handler",
					"/Users/francescomarino/IdeaProjects/serverless_composition_performance_project/serverless_functions/gcloud/latency_test");
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}

		try {
			CommandExecutor.deployOnGoogleCloudPlatform("cpu-test",
					"gc_functions_handler",
					"/Users/francescomarino/IdeaProjects/serverless_composition_performance_project/serverless_functions/gcloud/cpu_test");
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
	}
}
