//@@author A0126297X

package main.parser.exceptions;

/**
 * This exception is thrown when a task's index cannot be parsed.
 * This means that it is in a wrong format.
 */
@SuppressWarnings("serial")
public class InvalidTaskIndexFormat extends Exception {
	public InvalidTaskIndexFormat(String message) {
		super (message);
	}
}