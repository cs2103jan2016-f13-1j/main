/**
 * 
 */
package main.logic;

/**
 * @author Bevin Seetoh Jia Jin
 *
 */
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Stack;

import main.data.Command;
import main.data.Task;
import main.parser.CommandParser;
import main.storage.Storage;

public class Controller {
	
	CommandParser parser = null;
	Storage storage = null;
	ArrayList<Task> taskList = new ArrayList<Task>();
	Stack<Command> history = new Stack<Command>();
	
	public Controller() {
		parser = new CommandParser();
		storage = Storage.getStorage();
		taskList = storage.readTasks();
	}
	
	public String readCommand(String userCommand) {
		//Command command = parser.parseCommand(userCommand);
		//add command to history stack
		return "instant feedback, task's toString()";
	}
	
	public ArrayList<Task> getTasks() {
		return taskList;
	}
	
	public Storage getStorage() {
		return storage;
	}
	
	public void addTask(Task task) {
		taskList.add(task);
		saveTasks();
	}
	
	public void deleteTask(int index) {
		taskList.remove(index);
		saveTasks();
	}
	
	public void editTask(int oldTaskIndex, Task newTask) {
		taskList.remove(oldTaskIndex);
		taskList.add(oldTaskIndex, newTask);
		saveTasks();
	}

	private void saveTasks() {
		try {
			storage.writeTasks(taskList);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void printTasks() {
		for (Task task : taskList) {
			System.out.println(task);
		}
	}
	
	public String getFileDirectory() {
		return storage.getFileDirectory();
	}
	
	public void setFileDirectory(String fileDirectory) {
		storage.setFileDirectory(fileDirectory);
	}
	
	public String getFileName() {
		return storage.getFileName();
	}
	
	public void setFileName(String fileName) {
		storage.setFileName(fileName);
	}
}
