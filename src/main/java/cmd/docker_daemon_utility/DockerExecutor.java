package cmd.docker_daemon_utility;

import cmd.CommandExecutor;
import cmd.StreamGobbler;
import cmd.docker_daemon_utility.DockerException;
import cmd.functionality_commands.output_parsing.ReplyCollector;
import utility.PropertiesManager;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Utility for Docker related executions
 */
public class DockerExecutor extends CommandExecutor {

	/**
	 * Exit codes
	 */
	private static final Integer DOCKER_DAEMON_NOT_FOUND_EXIT_CODE = 16;
	private static final Integer DOCKER_NEEDS_GOOGLE_INITIALIZATION = 17;

	/**
	 * Docker composition directory
	 */
	private static final String COMPOSE_DIR = PropertiesManager.getInstance().getProperty(
			PropertiesManager.DOCKER_COMPOSE_DIR);

	/**
	 * Google cloud CLI configuration
	 */
	private static final String GOOGLE_CONFIG_CONTAINER = PropertiesManager.getInstance().getProperty(
			PropertiesManager.GOOGLE_CONTAINER);
	private static final String GOOGLE_CONFIG_COMMAND = "docker run -it --name " + GOOGLE_CONFIG_CONTAINER +
			" google/cloud-sdk gcloud init";

	/**
	 * New docker-compose containers report this string
	 */
	private static final String NEW_COMPOSITION_SUBSTRING = "Starting";

	/**
	 * Checks if Docker daemon is running
	 * @throws DockerException if Docker daemon cannot be checked
	 */
	private static void checkDockerRunning() throws DockerException {

		// build command
		String cmd = "docker info";

		try {
			Process process = buildCommand(cmd).start();

			if (process.waitFor() != 0) {
				process.destroy();
				System.err.println("Docker daemon not in execution");
				System.exit(DOCKER_DAEMON_NOT_FOUND_EXIT_CODE);
			}

			process.destroy();

		} catch (InterruptedException | IOException e) {
			throw new DockerException("Docker daemon not checked: " + e.getMessage());
		}
	}

	/**
	 * Checks if Google Cloud CLI container is correctly configured in Docker
	 * @throws DockerException if configuration cannot be checked
	 */
	private static void checkDockerConfig() throws DockerException {

		// build command
		String cmd = "docker ps -a";

		// start executor
		ExecutorService executorServiceOut = Executors.newSingleThreadExecutor();

		// create reply collector
		ReplyCollector listCollector = new ReplyCollector();

		try {
			// start process execution
			Process process = buildCommand(cmd).start();
			StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), listCollector::collectResult);
			executorServiceOut.submit(outputGobbler);

			if (process.waitFor() != 0) {
				process.destroy();
				throw new DockerException("Could not check Google CLI correct configuration");
			}

			// check container existence
			String list = listCollector.getResult();
			if (!list.contains(GOOGLE_CONFIG_CONTAINER)) {
				process.destroy();
				executorServiceOut.shutdown();
				System.err.println("Google CLI initial configuration is needed!\n" +
						"Please execute the following command in your shell:\n" +
						"\u001B[34m" + GOOGLE_CONFIG_COMMAND + "\u001B[0m");
				System.exit(DOCKER_NEEDS_GOOGLE_INITIALIZATION);
			}
		} catch (InterruptedException | IOException e) {
			throw new DockerException("Could not check Google CLI correct configuration: " + e.getMessage());
		} finally {
			executorServiceOut.shutdown();
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
				waitFor("Deploying Docker", 15);
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
		checkDockerConfig();
		deployComposition();
	}
}
