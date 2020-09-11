package cmd;

import cmd.functionality_commands.GoogleCommandUtility;

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
}
