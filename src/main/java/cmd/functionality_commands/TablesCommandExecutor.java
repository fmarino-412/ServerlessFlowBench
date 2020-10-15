package cmd.functionality_commands;

import cmd.CommandExecutor;
import cmd.StreamGobbler;
import cmd.docker_daemon_utility.DockerException;
import cmd.docker_daemon_utility.DockerExecutor;
import databases.mysql.CloudEntityData;
import databases.mysql.daos.TablesRepositoryDAO;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Utility for CLI cloud NoSQL storage related command execution
 */
public class TablesCommandExecutor extends CommandExecutor {

	/**
	 * Creates a new table to Amazon Dynamo DB
	 * @param tableName name of the new table
	 * @param directoryAbsolutePath path of the table definition file directory
	 * @param definitionFileName table definition file name
	 * @param region region for table creation
	 */
	public static void createAmazonTable(String tableName, String directoryAbsolutePath, String definitionFileName, String region) {

		System.out.println("\n" + "\u001B[33m" +
				"Creating table \"" + tableName + "\" to Amazon Web Services..." +
				"\u001B[0m" + "\n");

		try {
			DockerExecutor.checkDocker();
		} catch (DockerException e) {
			System.err.println("Could not create table '" + tableName + "' on Amazon: " + e.getMessage());
			return;
		}

		ExecutorService executorServiceErr = Executors.newSingleThreadExecutor();

		try {
			Process process;
			StreamGobbler errorGobbler;

			String cmd = AmazonCommandUtility.buildDynamoTableCreationCommand(tableName, directoryAbsolutePath,
					definitionFileName, region);

			process = buildCommand(cmd).start();
			errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
			executorServiceErr.submit(errorGobbler);
			if (process.waitFor() != 0) {
				System.err.println("Could not create table '" + tableName + "' on Amazon");
				process.destroy();
				return;
			}
			process.destroy();
			TablesRepositoryDAO.persistAmazon(tableName, region);
			waitFor("Table creation", 10);
			System.out.println("'" + tableName + "' created on Amazon");
		} catch (InterruptedException | IOException e) {
			System.out.println("'" + tableName + "' creation on Amazon failed: " + e.getMessage());
		} finally {
			executorServiceErr.shutdown();
		}
	}

	/**
	 * Removes a table from Amazon Dynamo DB
	 * @param tableName name of the table to remove
	 * @param region region of the table to remove
	 * @throws IOException exception related to process execution
	 * @throws InterruptedException exception related to Thread management
	 */
	private static void removeAmazonTable(String tableName, String region)
			throws IOException, InterruptedException {

		String cmd = AmazonCommandUtility.buildDynamoTableDropCommand(tableName, region);
		ExecutorService executorServiceErr = Executors.newSingleThreadExecutor();

		Process process = buildCommand(cmd).start();
		StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);

		executorServiceErr.submit(errorGobbler);

		if (process.waitFor() != 0) {
			System.err.println("Could not delete table '" + tableName + "' from Amazon");
		} else {
			System.out.println("'" + tableName + "' table removed from Amazon!");
		}
		process.destroy();
		executorServiceErr.shutdown();
	}

	/**
	 * Removes every table from Amazon Dynamo DB
	 */
	public static void cleanupAmazonCloudTables() {

		System.out.println("\n" + "\u001B[33m" +
				"Cleaning up Amazon tables environment..." +
				"\u001B[0m" + "\n");

		try {
			DockerExecutor.checkDocker();
		} catch (DockerException e) {
			System.err.println("Could not cleanup Amazon tables environment: " + e.getMessage());
			return;
		}

		List<CloudEntityData> toRemove = TablesRepositoryDAO.getAmazons();
		if (toRemove == null) {
			return;
		}

		for (CloudEntityData elem : toRemove) {
			try {
				removeAmazonTable(elem.getEntityName(), elem.getRegion());
			} catch (InterruptedException | IOException e) {
				System.err.println("Could not delete Amazon table '" + elem.getEntityName() + "': " +
						e.getMessage());
			}
		}

		TablesRepositoryDAO.dropAmazon();

		if (!toRemove.isEmpty()) {
			waitFor("Cleanup", 5);
		}
		System.out.println("\u001B[32m" + "\nAmazon cleanup completed!\n" + "\u001B[0m");
	}

}
