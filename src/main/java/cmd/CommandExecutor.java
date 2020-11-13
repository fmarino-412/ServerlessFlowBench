package cmd;

import cmd.functionality_commands.GoogleCommandUtility;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Utility for CLI related command execution
 */
public abstract class CommandExecutor {

	/**
	 * Builds a command differently basing on Docker on Windows OS or Docker on UNIX systems
	 * @param cmd string containing the command to execute
	 * @return ProcessBuilder ready to start
	 */
	protected static ProcessBuilder buildCommand(String cmd) {
		ProcessBuilder builder = new ProcessBuilder();

		// define command
		if (GoogleCommandUtility.isWindows()) {
			builder.command("cmd.exe", "/c", cmd);
		} else {
			builder.command("sh", "-c", cmd);
		}

		return builder;
	}

	/**
	 * Prints a wait progress bar on console
	 * @param message message to show
	 * @param seconds time to wait in seconds
	 */
	public static void waitFor(String message, Integer seconds) {
		ProgressBar progressBar = new ProgressBar(message, 100, 1000, System.out,
				ProgressBarStyle.ASCII);
		progressBar.setExtraMessage("Wait...");
		progressBar.start();
		for (int i = 0; i < seconds; i++) {
			progressBar.stepBy((100/seconds));
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ignored) {
			}
		}
		progressBar.stepTo(100);
		progressBar.setExtraMessage("Completed!");
		progressBar.stop();
	}

	/**
	 * Executes a command ignoring return values
	 * @param command command to execute
	 * @return true if execution completed, false if error occurs
	 * @throws IOException process start related problems
	 * @throws InterruptedException process execution related problems
	 */
	protected static boolean commandSilentExecution(String command) throws IOException, InterruptedException {

		// start execution
		Process process = buildCommand(command).start();

		// wait for completion
		if (process.waitFor() != 0) {
			process.destroy();
			return false;
		}

		process.destroy();
		return true;
	}

	/**
	 * Wrapper: deletes a file and, in case of error, prints a warning
	 * @param filePath path of the file to delete
	 */
	protected static void deleteFile(Path filePath) {
		deleteFile(filePath, false);
	}

	/**
	 * Deletes a file: used for temporary file deletion
	 * @param filePath path of the file to delete
	 * @param silent true if warning can be printed, false elsewhere
	 */
	protected static void deleteFile(Path filePath, boolean silent) {

		File file = filePath.toFile();

		if (!file.delete() && !silent) {
			System.err.println("WARNING:\tCould not delete temporary files, check: " + filePath.toString());
		}
	}
}
