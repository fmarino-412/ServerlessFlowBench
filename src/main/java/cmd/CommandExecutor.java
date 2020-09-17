package cmd;

import cmd.functionality_commands.GoogleCommandUtility;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;

public abstract class CommandExecutor {

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

	public static void waitFor(String message, Integer seconds) {
		ProgressBar progressBar = new ProgressBar(message, 100, ProgressBarStyle.ASCII);
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
}
