package cmd;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandExecutor {

	public static void deployOnGoogleCloudPlatform(String functionalityName, String functionalityEntryPoint,
												   String absolutePath) throws IOException, InterruptedException {

		ProcessBuilder builder = new ProcessBuilder();

		// build command
		String cmd = CommandUtility.buildGoogleCloudFunctionsDeployCommand(functionalityName,
				functionalityEntryPoint, absolutePath);

		if (CommandUtility.isWindows()) {
			// TODO: TEST
			builder.command("cmd.exe", "/c", cmd);
		} else {
			builder.command("sh", "-c", cmd);
		}

		// start process execution
		Process process = builder.start();

		// create, execute and submit output gobblers
		StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(),
				UrlFinder::findGoogleCloudFunctionsUrl);
		StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);

		ExecutorService executorServiceOut = Executors.newSingleThreadExecutor();
		ExecutorService executorServiceErr = Executors.newSingleThreadExecutor();

		executorServiceOut.submit(outputGobbler);
		executorServiceErr.submit(errorGobbler);

		// wait for completion and free environment
		int exitCode = process.waitFor();
		assert exitCode == 0;

		process.destroy();
		executorServiceOut.shutdown();
		executorServiceErr.shutdown();
	}
}
