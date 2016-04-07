//@@author A0126297X

package main.parser.exceptions;
import main.data.Task;

/**
 * This exception is thrown when there is an invalid title in the user input.
 * An invalid title would be a input containing only date information.
 */
@SuppressWarnings("serial")
public class InvalidTitle extends Exception {
	Task task;
	
	public InvalidTitle(String message, Task task) {
		super (message);
		this.task = task;
	}
	
	public Task getTask() {
		return task;
	}
}