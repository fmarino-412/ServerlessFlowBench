package cmd.functionality_commands;

/**
 * Exception raised in case of malformed function, workflow or state machine name
 */
public class IllegalNameException extends Exception {

	/**
	 * Default constructor
	 * @param message exception message
	 */
	public IllegalNameException(String message) {
		super(message);
	}
}
