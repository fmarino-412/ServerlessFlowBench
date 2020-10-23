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
	 * Creates a new table to Google CLoud Big Table
	 * @param tableName name of the new table
	 * @param region region of deployment
	 * @param nodes number of nodes to store the table
	 * @param storageType type of storage: HDD or SSD
	 * @param columnFamilies column families of the table
	 */
	public static void createGoogleTable(String tableName, String region, int nodes, String storageType,
										 String... columnFamilies) {

		try {
			DockerExecutor.checkDocker();
		} catch (DockerException e) {
			System.err.println("Could not create table '" + tableName + "' on Google: " + e.getMessage());
			return;
		}

		System.out.println("\n" + "\u001B[33m" +
				"Creating table \"" + tableName + "\" to Google..." +
				"\u001B[0m" + "\n");

		ExecutorService executorServiceErr = Executors.newSingleThreadExecutor();
		String cmd;

		try {
			Process process;
			StreamGobbler errorGobbler;

			// generate id
			String instanceId = tableName.toLowerCase().replace("_", "-") + "-" +
					System.currentTimeMillis();
			instanceId = instanceId.substring(0, Math.min(instanceId.length(), 30));
			String clusterId = "cl-" + instanceId;
			clusterId = clusterId.substring(0, Math.min(clusterId.length(), 30));

			// create instance
			System.out.println("Creating instance for '" + tableName + "'");
			cmd = GoogleCommandUtility.buildGoogleCloudBigTableCreateInstanceCommand(tableName,
					instanceId,
					clusterId,
					region,
					nodes,
					storageType);
			process = buildCommand(cmd).start();
			errorGobbler = new StreamGobbler(process.getErrorStream(), System.out::println);
			executorServiceErr.submit(errorGobbler);
			if (process.waitFor() != 0) {
				System.err.println("Could not create instance for table '" + tableName + "' on Google");
				process.destroy();
				return;
			}
			process.destroy();
			TablesRepositoryDAO.persistGoogle(instanceId, tableName);

			// create table
			System.out.println("Creating '" + tableName + "'");
			cmd = GoogleCommandUtility.buildGoogleCloudBigTableCreateTableCommand(instanceId, tableName);
			process = buildCommand(cmd).start();
			errorGobbler = new StreamGobbler(process.getErrorStream(), System.out::println);
			executorServiceErr.submit(errorGobbler);
			if (process.waitFor() != 0) {
				System.err.println("Could not create table '" + tableName + "' on Google");
				process.destroy();
				return;
			}
			process.destroy();

			// create column families
			for (String family : columnFamilies) {
				System.out.println("Creating family '" + family + "' in '" + tableName + "'");
				cmd = GoogleCommandUtility.buildGoogleCloudBigTableCreateFamilyCommand(instanceId, tableName, family);
				process = buildCommand(cmd).start();
				errorGobbler = new StreamGobbler(process.getErrorStream(), System.out::println);
				executorServiceErr.submit(errorGobbler);
				if (process.waitFor() != 0) {
					System.err.println("Could not create family '" + family + " in '" + tableName + "' on Google");
					process.destroy();
					return;
				}
				process.destroy();

			}

			System.out.println("'" + tableName + "' created on Google");
		} catch (InterruptedException | IOException e) {
			System.out.println("'" + tableName + "' creation on Google failed: " + e.getMessage());
		} finally {
			executorServiceErr.shutdown();
		}

	}

	/**
	 * Creates a new table to Amazon Dynamo DB
	 * @param tableName name of the new table
	 * @param directoryAbsolutePath path of the table definition file directory
	 * @param definitionFileName table definition file name
	 * @param region region for table creation
	 */
	public static void createAmazonTable(String tableName, String directoryAbsolutePath, String definitionFileName,
										 String region) {

		try {
			DockerExecutor.checkDocker();
		} catch (DockerException e) {
			System.err.println("Could not create table '" + tableName + "' on Amazon: " + e.getMessage());
			return;
		}

		System.out.println("\n" + "\u001B[33m" +
				"Creating table \"" + tableName + "\" to Amazon Web Services..." +
				"\u001B[0m" + "\n");

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
	 * Removes an instance storing a table in Google Cloud Big Table
	 * @param instanceId id of the instance
	 * @param tableName name of the table stored
	 * @throws IOException exception related to process execution
	 * @throws InterruptedException exception related to Thread management
	 */
	private static void removeGoogleInstance(String instanceId, String tableName)
			throws IOException, InterruptedException {

		String cmd = GoogleCommandUtility.buildGoogleCloudBigTableDropInstanceCommand(instanceId);
		ExecutorService executorServiceErr = Executors.newSingleThreadExecutor();

		Process process = buildCommand(cmd).start();
		StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), System.out::println);

		executorServiceErr.submit(errorGobbler);
		if (process.waitFor() != 0) {
			System.err.println("Could not delete instance '" + tableName + "' from Google");
		} else {
			System.out.println("'" + tableName + "' instance removed from Google!");
		}
		process.destroy();
		executorServiceErr.shutdown();
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
	 * Removes every instance and table from Google Cloud Big Table
	 */
	public static void cleanupGoogleCloudTables() {

		try {
			DockerExecutor.checkDocker();
		} catch (DockerException e) {
			System.err.println("Could not cleanup Google tables environment: " + e.getMessage());
			return;
		}

		System.out.println("\n" + "\u001B[33m" +
				"Cleaning up Google tables environment..." +
				"\u001B[0m" + "\n");

		List<CloudEntityData> toRemove = TablesRepositoryDAO.getGoogles();
		if (toRemove == null) {
			return;
		}

		for (CloudEntityData elem : toRemove) {
			try {
				removeGoogleInstance(elem.getId(), elem.getEntityName());
			} catch (InterruptedException | IOException e) {
				System.err.println("Could not delete Google table '" + elem.getEntityName() + "': " +
						e.getMessage());
			}
		}

		TablesRepositoryDAO.dropGoogle();

		if (!toRemove.isEmpty()) {
			// cluster deallocate
			waitFor("Cleanup", 60);
		}
		System.out.println("\u001B[32m" + "\nGoogle cleanup completed!\n" + "\u001B[0m");
	}

	/**
	 * Removes every table from Amazon Dynamo DB
	 */
	public static void cleanupAmazonCloudTables() {

		try {
			DockerExecutor.checkDocker();
		} catch (DockerException e) {
			System.err.println("Could not cleanup Amazon tables environment: " + e.getMessage());
			return;
		}

		System.out.println("\n" + "\u001B[33m" +
				"Cleaning up Amazon tables environment..." +
				"\u001B[0m" + "\n");

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
