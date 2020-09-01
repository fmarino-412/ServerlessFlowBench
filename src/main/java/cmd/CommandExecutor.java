package cmd;

import cmd.function_commands.GoogleCommandUtility;

public abstract class CommandExecutor {

	protected static ProcessBuilder buildCommand(String cmd) {
		ProcessBuilder builder = new ProcessBuilder();

		// define command
		if (GoogleCommandUtility.isWindows()) {
			// TODO: TEST
			builder.command("cmd.exe", "/c", cmd);
		} else {
			builder.command("sh", "-c", cmd);
		}

		return builder;
	}
}
