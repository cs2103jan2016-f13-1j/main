/**
 * 
 */
package main.parser;

import main.data.Command;

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
	public Command parseCommand(String userCommand) {
		return new Command();
	}
}
