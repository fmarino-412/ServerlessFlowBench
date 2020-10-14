package cmd;

/**
 * Exception raised in case Docker daemon execution problem occurs
 */
public class DockerException extends Exception {

	/**
	 * Default constructor
	 * @param message exception message
	 */
	public DockerException(String message) {
		super(message);
	}
}
