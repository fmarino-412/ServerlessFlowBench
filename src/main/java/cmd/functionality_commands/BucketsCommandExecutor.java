package cmd.functionality_commands;

import cmd.CommandExecutor;
import cmd.StreamGobbler;
import cmd.docker_daemon_utility.DockerException;
import cmd.docker_daemon_utility.DockerExecutor;
import databases.mysql.CloudEntityData;
import databases.mysql.daos.BucketsRepositoryDAO;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Utility for CLI cloud buckets related command execution
 */
public class BucketsCommandExecutor extends CommandExecutor {

	/**
	 * Creates a new bucket to Amazon S3
	 * @param bucketName name of the new bucket
	 * @param acl access control list for the new bucket: use static options
	 * @param region region for table creation
	 */
	public static void createAmazonBucket(String bucketName, String acl, String region) {

		System.out.println("\n" + "\u001B[33m" +
				"Creating bucket \"" + bucketName + "\" to Amazon Web Services..." +
				"\u001B[0m" + "\n");

		try {
			DockerExecutor.checkDocker();
		} catch (DockerException e) {
			System.err.println("Could not create bucket '" + bucketName + "' on Amazon: " + e.getMessage());
			return;
		}

		ExecutorService executorServiceErr = Executors.newSingleThreadExecutor();

		try {
			Process process;
			StreamGobbler errorGobbler;

			String cmd = AmazonCommandUtility.buildS3BucketCreationCommand(bucketName, acl, region);

			process = buildCommand(cmd).start();
			errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);
			executorServiceErr.submit(errorGobbler);
			if (process.waitFor() != 0) {
				System.err.println("Could not create bucket '" + bucketName + "' on Amazon");
				process.destroy();
				return;
			}
			process.destroy();
			BucketsRepositoryDAO.persistAmazon(bucketName, region);

			System.out.println("'" + bucketName + "' created on Amazon");
		} catch (InterruptedException | IOException e) {
			System.out.println("'" + bucketName + "' creation on Amazon failed: " + e.getMessage());
		} finally {
			executorServiceErr.shutdown();
		}
	}

	/**
	 * Removes a bucket from S3
	 * @param bucketName name of the bucket to remove
	 * @throws IOException exception related to process execution
	 * @throws InterruptedException exception related to Thread management
	 */
	private static void removeAmazonBucket(String bucketName, String region) throws IOException, InterruptedException {

		String cmd = AmazonCommandUtility.buildS3BucketDropCommand(bucketName, region);
		ExecutorService executorServiceErr = Executors.newSingleThreadExecutor();

		Process process = buildCommand(cmd).start();
		StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), System.err::println);

		executorServiceErr.submit(errorGobbler);

		if (process.waitFor() != 0) {
			System.err.println("Could not delete bucket '" + bucketName + "' from Amazon");
		} else {
			System.out.println("'" + bucketName + "' bucket removed from Amazon!");
		}
		process.destroy();
		executorServiceErr.shutdown();
	}

	/**
	 * Removes every bucket from Amazon S3
	 */
	public static void cleanupAmazonCloudBuckets() {

		System.out.println("\n" + "\u001B[33m" +
				"Cleaning up Amazon buckets environment..." +
				"\u001B[0m" + "\n");

		try {
			DockerExecutor.checkDocker();
		} catch (DockerException e) {
			System.err.println("Could not cleanup Amazon buckets environment: " + e.getMessage());
			return;
		}

		List<CloudEntityData> toRemove = BucketsRepositoryDAO.getAmazons();
		if (toRemove == null) {
			return;
		}

		for (CloudEntityData elem : toRemove) {
			try {
				removeAmazonBucket(elem.getEntityName(), elem.getRegion());
			} catch (InterruptedException | IOException e) {
				System.err.println("Could not delete Amazon bucket '" + elem.getEntityName() + "': " +
						e.getMessage());
			}
		}

		BucketsRepositoryDAO.dropAmazon();

		System.out.println("\u001B[32m" + "\nAmazon cleanup completed!\n" + "\u001B[0m");
	}
}
