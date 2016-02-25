/**
 * 
 */
package main.parser;

import java.util.Date;

import main.data.Command;
import main.data.Task;

/**
 * @author Joleen
 *
 */
public class CommandParser {
	public CommandParser() {
		
	}
	
	/*
	 * Logic controller will send you a String userCommand
	 * expected results
	 * add => (commandType, Task task)
	 * del => (commandType, Int index)
	 * edit => (commandType, Int oldTaskIndex, Task newEditedTask)
	 */
	public Command parse(String commandString) {
		Task task = new Task();
		task.setTitle("sweep the floor");
		task.setEndDate(new Date());
        return new Command("add","floating",task);
    }
}
