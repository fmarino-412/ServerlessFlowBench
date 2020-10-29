package cmd.docker_daemon_utility;

import cmd.CommandExecutor;
import cmd.StreamGobbler;
import cmd.benchmark_commands.BenchmarkCommandUtility;
import cmd.functionality_commands.AmazonCommandUtility;
import cmd.functionality_commands.GoogleCommandUtility;
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
	private static final Integer DOCKER_MISSING_IMAGE = 17;
	private static final Integer DOCKER_NEEDS_GOOGLE_INITIALIZATION = 18;

	/**
	 * Docker composition directory
	 */
	private static final String COMPOSE_DIR = PropertiesManager.getInstance().getProperty(
			PropertiesManager.DOCKER_COMPOSE_DIR);

	/**
	 * Docker needed containers
	 */
	private static final String PULL = "docker pull ";
	private static final String AWS_CLI = AmazonCommandUtility.getCli();
	private static final String GOOGLE_CLI = GoogleCommandUtility.getCli();
	private static final String MYSQL = "mysql:8.0.17";
	private static final String INFLUX = "influxdb:1.8.2";
	private static final String GRAFANA = "grafana/grafana:6.5.0";
	private static final String WRK2 = BenchmarkCommandUtility.WRK2_IMG;

	/**
	 * Google cloud CLI configuration
	 */
	private static final String GOOGLE_CONFIG_CONTAINER = PropertiesManager.getInstance().getProperty(
			PropertiesManager.GOOGLE_CONTAINER);
	private static final String GOOGLE_CONFIG_COMMAND = "docker run -it --name " + GOOGLE_CONFIG_CONTAINER +
			" " + GOOGLE_CLI + " gcloud init";

	/**
	 * New docker-compose start containers report this string
	 */
	private static final String START_COMPOSITION_SUBSTRING = "Starting";
	private static final String NEW_COMPOSITION_SUBSTRING = "Creating";

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
	 * Checks if a specific Docker image exists locally
	 * @param image image to check
	 * @return true if image needs to be downloaded, false elsewhere
	 * @throws InterruptedException process execution related problems
	 * @throws IOException process start related problems
	 */
	private static boolean needsDockerImage(String image) throws Exception {

		ExecutorService executorServiceOut = Executors.newSingleThreadExecutor();

		try {
			// build command
			String cmd = "docker images -q " + image;

			// start process execution
			ReplyCollector collector = new ReplyCollector();
			Process process = buildCommand(cmd).start();
			StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), collector::collectResult);
			executorServiceOut.submit(outputGobbler);

			if (process.waitFor() != 0) {
				process.destroy();
				throw new Exception("Process exited with error");
			}
			process.destroy();

			// if empty image is not present locally
			return collector.getResult().equals("");
		} finally {
			executorServiceOut.shutdown();
		}
	}

	/**
	 * Checks if Docker images are missing locally
	 * @throws DockerException if images cannot be checked
	 */
	@SuppressWarnings("DuplicatedCode")
	private static void checkDockerImages() throws DockerException {

		try {

			boolean google = needsDockerImage(GOOGLE_CLI);
			boolean amazon = needsDockerImage(AWS_CLI);
			boolean mySql = needsDockerImage(MYSQL);
			boolean influx = needsDockerImage(INFLUX);
			boolean grafana = needsDockerImage(GRAFANA);
			boolean wrk2 = needsDockerImage(WRK2);

			if (google || amazon || mySql || influx || grafana || wrk2) {

				System.err.println("Docker images are missing!\n" +
						"Please execute the following command(s) in your shell:");

				if (google) {
					System.err.println("\u001B[34m" + PULL + GOOGLE_CLI + "\u001B[0m");
				}
				if (amazon) {
					System.err.println("\u001B[34m" + PULL + AWS_CLI + "\u001B[0m");
				}
				if (mySql) {
					System.err.println("\u001B[34m" + PULL + MYSQL + "\u001B[0m");
				}
				if (influx) {
					System.err.println("\u001B[34m" + PULL + INFLUX + "\u001B[0m");
				}
				if (grafana) {
					System.err.println("\u001B[34m" + PULL + GRAFANA + "\u001B[0m");
				}
				if (wrk2) {
					System.err.println("\u001B[34m" + PULL + WRK2 + "\u001B[0m");
				}
				System.exit(DOCKER_MISSING_IMAGE);
			}

		} catch (Exception e) {
			throw new DockerException("Could not check Docker images presence: " + e.getMessage());
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

			process.destroy();
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

			String response = collector.getResult();
			if (response.contains(START_COMPOSITION_SUBSTRING) || response.contains(NEW_COMPOSITION_SUBSTRING)) {
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
		checkDockerImages();
		checkDockerConfig();
		deployComposition();
	}
}
