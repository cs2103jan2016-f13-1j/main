//@@author A0126297X

package main.parser.exceptions;
import main.data.Task;

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