/**
 * 
 */
package main.logic;

/**
 * @author Bevin Seetoh Jia Jin
 *
 */
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Stack;

import main.data.Command;
import main.data.Task;
import main.parser.CommandParser;
import main.storage.Storage;

public class Controller {
	
	private final int FLOATING_TASKS_INDEX = 0;
	private final int DATED_TASKS_INDEX = 1;
	
	CommandParser parser = null;
	Storage storage = null;
	Stack<Command> history = new Stack<Command>();
	
	ArrayList<Task> floatingTasks = new ArrayList<Task>();
	ArrayList<Task> datedTasks = new ArrayList<Task>();
	
	Command currentCommand = null;
	
	public Controller() {
		parser = new CommandParser();
		storage = Storage.getStorage();
		
		ArrayList<ArrayList<Task>> tasksFromStorage = storage.readTasks();;
		floatingTasks = tasksFromStorage.get(FLOATING_TASKS_INDEX);
		datedTasks = tasksFromStorage.get(DATED_TASKS_INDEX);
	}
	
	/*
	 * If delete command, use readCommand(<user command>, <TAB TO DELETE>)
	 * If add command, use readCommand(null, <user command>)
	 * <TAB TO DELETE> takes in "floating","dated"
	 */
	public String readCommand(String userCommand, String tab) {
		Command command = parser.parse(userCommand);
		
		if (!tab.equals(null)) {
			//delete command
			command.setTab(tab);
			return "Delete detected";
		} else {
			//add command
			return command.getTask().toString();
		}
				
		
	}
	
	public void executeCommand() {
		execute(currentCommand);
		//add command to history stack
	}
	
	private void execute(Command command) {
		switch (command.getCommandType().toLowerCase()) {
			case "add":
				addTask(command.getTab(),command.getTask());
				break;
			case "delete":
				deleteTask(command.getTab(),command.getIndexes());
				break;
			default:
				break;
		}
	}
	
	public ArrayList<Task> getAllTasks() {
		ArrayList<Task> allTasks = new ArrayList<Task>();
		allTasks.addAll(floatingTasks);
		allTasks.addAll(datedTasks);
		return allTasks;
	}
	
	public ArrayList<Task> getFloatingTasks() {
		return floatingTasks;
	}
	
	public ArrayList<Task> getTodayTasks() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");
		String today = dateFormat.format(new Date());
		
		ArrayList<Task> result = new ArrayList<Task>();
		
		for (Task task : getAllTasks()) {
			if (task.getEndDate() != null) {
				String endDate = dateFormat.format(task.getEndDate());
				if (today.equals(endDate)) {
					result.add(task);
				}
			}
		}
		return result;
	}
	
	public ArrayList<Task> getNextSevenDays() {
		Date today = new Date();
		Date eighthDay = getEigthDay();
		
		ArrayList<Task> result = new ArrayList<Task>();
		
		for (Task task : getAllTasks()) {
			if (task.getEndDate() != null) {
				if (task.getEndDate().compareTo(today) > 0) {
					if (task.getEndDate().compareTo(eighthDay) < 0) {
						result.add(task);
					}
				}
			}
		}
		return result;
	}

	private Date getEigthDay() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DATE, 8);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}
	
	public Storage getStorage() {
		return storage;
	}
	
	public void addTask(String tab, Task task) {
		switch (tab.toLowerCase()) {
			case "floating":
				floatingTasks.add(task);
				break;
			case "dated":
				datedTasks.add(task);
				break;
			default:
				break;
		}
		saveTasks();
	}
	
	public void deleteTask(String tab, ArrayList<Integer> indexes) {
		ArrayList<Integer> indexesToDelete = decreaseIndex(indexes);
		ArrayList<Task> listToDelete = null;
		
		switch (tab.toLowerCase()) {
			case "floating":
				listToDelete = floatingTasks;
				break;
			case "dated":
				listToDelete = datedTasks;
				break;
			default:
				break;
		}
		
		while (indexesToDelete.size() > 0) {
			int indexToDelete = indexesToDelete.remove(0);
			listToDelete.remove(indexToDelete);
			indexesToDelete = decreaseIndex(indexesToDelete);
		}
		saveTasks();
	}
	
	//decrements every integer in the array by 1
	private ArrayList<Integer> decreaseIndex(ArrayList<Integer> indexes) {
		for (int i = 0; i < indexes.size(); i++) {
			indexes.set(i, indexes.get(i) - 1);
		}
		return indexes;
	}
	
	public void editTask(String tab, int oldTaskIndex, Task newTask) {
		oldTaskIndex--;
		
		switch (tab.toLowerCase()) {
			case "floating":
				editList(floatingTasks, oldTaskIndex, newTask);
				break;
			case "dated":
				editList(datedTasks, oldTaskIndex, newTask);
				break;
			default:
				break;
		}
		saveTasks();
	}

	private void editList(ArrayList<Task> list, int oldTaskIndex, Task newTask) {
		list.remove(oldTaskIndex);
		list.add(oldTaskIndex, newTask);
	}

	private void saveTasks() {
		try {
			ArrayList<ArrayList<Task>> allTasks = new ArrayList<ArrayList<Task>>();
			allTasks.add(floatingTasks);
			allTasks.add(datedTasks);
			storage.writeTasks(allTasks);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
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
