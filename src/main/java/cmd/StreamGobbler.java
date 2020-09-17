package cmd;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

/**
 * Class that allows calling a Consumer function when new data appears in a Stream
 */
public class StreamGobbler implements Runnable {

	// stream to consume
	private final InputStream inputStream;
	// consumer function to invoke
	private final Consumer<String> consumer;

	/**
	 * Default constructor
	 * @param inputStream stream to consume
	 * @param consumer function to invoke (must take just a string as argument)
	 */
	public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
		this.inputStream = inputStream;
		this.consumer = consumer;
	}

	@Override
	public void run() {
		// calls the consumer function for every line in the stream
		new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumer);
	}
}
