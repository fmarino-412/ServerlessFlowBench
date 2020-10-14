package cmd;

import cmd.functionality_commands.output_parsing.ReplyCollector;
import utility.PropertiesManager;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Utility for Docker related executions
 */
public class DockerExecutor extends CommandExecutor {

	private static final String COMPOSE_DIR = PropertiesManager.getInstance().getProperty(
			PropertiesManager.DOCKER_COMPOSE_DIR);

	/**
	 * New docker-compose containers report this string
	 */
	private static final String NEW_COMPOSITION_SUBSTRING = "Starting";

	/**
	 * Checks if Docker daemon is running
	 * @throws DockerException if Docker daemon is not running
	 */
	private static void checkDockerRunning() throws DockerException {

		// build command
		String cmd = "docker info";

		try {
			Process process = buildCommand(cmd).start();

			if (process.waitFor() != 0) {
				process.destroy();
				throw new DockerException("Docker daemon not in execution");
			}

			process.destroy();

		} catch (InterruptedException | IOException e) {
			throw new DockerException("Docker daemon not checked: " + e.getMessage());
		}
	}

	/**
	 * Deploys Docker compose
	 * @throws DockerException if error occurs
	 */
	private static void deployComposition() throws DockerException {

		// build command
		String cmd = "docker-compose -f " + COMPOSE_DIR + "/docker-compose.yml up -d";

		// start executor
		ExecutorService executorServiceErr = Executors.newSingleThreadExecutor();

		try {
			Process process = buildCommand(cmd).start();

			// create, execute and submit output gobblers
			ReplyCollector collector = new ReplyCollector();
			StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), collector::collectResult);

			executorServiceErr.submit(errorGobbler);

			if (process.waitFor() != 0) {
				process.destroy();
				throw new DockerException("Could not deploy docker compose environment");
			}

			if (collector.getResult().contains(NEW_COMPOSITION_SUBSTRING)) {
				// need to wait for compose environment coming up
				waitFor("Deploying environment", 15);
			}

			process.destroy();

		} catch (InterruptedException | IOException e) {
			throw new DockerException("Docker compose environment not checked: " + e.getMessage());
		} finally {
			executorServiceErr.shutdown();
		}
	}

	/**
	 * Checks Docker daemon running and deploys composition
	 * @throws DockerException if error occurs
	 */
	public static void checkDocker() throws DockerException {
		checkDockerRunning();
		deployComposition();
	}
}
