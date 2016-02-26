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
import main.data.TaskBean;
import main.parser.CommandParser;
import main.storage.StorageStub;

public class ControllerStub {
	
	CommandParser parser = null;
	StorageStub storage = null;
	ArrayList<TaskBean> taskList = new ArrayList<TaskBean>();
	Stack<Command> history = new Stack<Command>();
	
	public ControllerStub() {
		parser = new CommandParser();
		storage = StorageStub.getStorage();
		taskList = storage.readTasks();
	}
	
	public String readCommand(String userCommand) {
		//Command command = parser.parseCommand(userCommand);
		//add command to history stack
		return "instant feedback, task's toString()";
	}
	
	public ArrayList<TaskBean> getTasks() {
		return taskList;
	}
	
	public StorageStub getStorage() {
		return storage;
	}
	
	public void addTask(TaskBean task) {
		taskList.add(task);
		saveTasks();
	}
	
	public void deleteTask(int index) {
		taskList.remove(index);
		saveTasks();
	}
	
	public void editTask(int oldTaskIndex, TaskBean newTask) {
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
		for (TaskBean task : taskList) {
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
