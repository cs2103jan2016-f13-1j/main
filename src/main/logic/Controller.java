/**
 * Summary of public methods that can be called:
 * 
 * Controller();
 * parseCommand(String userCommand, String tab);
 * editTask(String tab, int oldTaskIndex);
 * executeCommand();
 * 
 * getFloatingTasks();
 * getDatedTasks();
 * getTodayTasks();
 * getNextSevenDays();
 * getFileDirectory();
 * getFileName();
 * 
 * setFileDirectory(String fileDirectory);
 * setFileName(String fileName);
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
	
	public static final String DATE_FORMAT_DDMMYY = "ddMMyyyy";
	
	public static final String NO_TAB = "none";
	public static final String FLOATING_TAB = "floating";
	public static final String DATED_TAB = "dated";
	public static final String TODAY_TAB = "today";
	public static final String NEXT_SEVEN_DAYS_TAB = "nextSevenDays";
	
	private static final String COMMAND_TYPE_ADD = "add";
	private static final String COMMAND_TYPE_DELETE = "delete";
	
	private static final int FLOATING_TASKS_INDEX = 0;
	private static final int DATED_TASKS_INDEX = 1;
	
	CommandParser parser = null;
	Storage storage = null;
	
	Stack<Command> history = new Stack<Command>();
	ArrayList<Task> floatingTasks = new ArrayList<Task>();
	ArrayList<Task> datedTasks = new ArrayList<Task>();
	
	Command command = null;
	
	/**
	 * Initializes a newly created {@code Controller} object.
	 */
	public Controller() {
		parser = new CommandParser();
		storage = Storage.getStorage();
		
		ArrayList<ArrayList<Task>> tasksFromStorage = storage.readTasks();;
		floatingTasks = tasksFromStorage.get(FLOATING_TASKS_INDEX);
		datedTasks = tasksFromStorage.get(DATED_TASKS_INDEX);
	}
	
	private void addTask(String tab, Task task) {
		switch (tab.toLowerCase()) {
			case FLOATING_TAB:
				floatingTasks.add(task);
				break;
			case DATED_TAB:
				datedTasks.add(task);
				break;
			default:
				break;
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
	
	private ArrayList<Task> deleteFromList(ArrayList<Task> listToDelete, ArrayList<Integer> indexesToDelete) {
		while (!indexesToDelete.isEmpty()) {
			int indexToDelete = indexesToDelete.remove(0);
			listToDelete.remove(indexToDelete);
			indexesToDelete = decreaseIndex(indexesToDelete);
		}
		return listToDelete;
	}
	
	private void deleteTask(String tab, ArrayList<Integer> indexes) {
		ArrayList<Task> temp = null;	
		
		switch (tab) {
			case FLOATING_TAB:
				deleteFromList(floatingTasks, indexes);
				break;
			case DATED_TAB:
				deleteFromList(datedTasks, indexes);
				break;
			case TODAY_TAB:
				ArrayList<Task> newTodayTasks = deleteFromList(getTodayTasks(), indexes);
				temp = new ArrayList<Task>();
				temp.addAll(newTodayTasks);
				temp.addAll(getNextSevenDays());
				datedTasks = new ArrayList<Task>();
				for (Task task : temp) {
					datedTasks.add(task);
				}
			case NEXT_SEVEN_DAYS_TAB:
				ArrayList<Task> newNextSevenDaysTasks = deleteFromList(getNextSevenDays(), indexes);
				temp = new ArrayList<Task>();
				temp.addAll(getTodayTasks());
				temp.addAll(newNextSevenDaysTasks);
				datedTasks = new ArrayList<Task>();
				for (Task task : temp) {
					datedTasks.add(task);
				}
			default:
				break;
		}
		saveTasks();
	}
	
	private void addToList(String tab, int oldTaskIndex) {
		ArrayList<Task> temp = null;
		
		switch (tab) {
			case FLOATING_TAB:
				floatingTasks.add(oldTaskIndex,command.getTask());
				break;
			case DATED_TAB:
				datedTasks.add(oldTaskIndex,command.getTask());
				break;
			case TODAY_TAB:
				ArrayList<Task> newTodayTasks = getTodayTasks();
				newTodayTasks.add(oldTaskIndex,command.getTask());
				temp = new ArrayList<Task>();
				temp.addAll(newTodayTasks);
				temp.addAll(getNextSevenDays());
				datedTasks = temp;
			case NEXT_SEVEN_DAYS_TAB:
				ArrayList<Task> newNextSevenDaysTasks = getNextSevenDays();
				newNextSevenDaysTasks.add(oldTaskIndex,command.getTask());
				temp = new ArrayList<Task>();
				datedTasks.addAll(getTodayTasks());
				datedTasks.addAll(newNextSevenDaysTasks);
				datedTasks = temp;
			default:
				break;
		}
	}
	
	/**
	 * Edits the task in the respective {@code tab} at position {@code oldTaskIndex}
	 * @param 	tab
	 * 			the tab where the task is
	 * @param 	oldTaskIndex
	 * 			the index of the task
	 */
	public void editTask(String tab, int oldTaskIndex) {
		ArrayList<Integer> indexToDelete = new ArrayList<Integer>();
		indexToDelete.add(oldTaskIndex);
		deleteTask(tab, indexToDelete);
		addToList(tab, oldTaskIndex);
		saveTasks();
	}
	
	private void execute(Command command) {
		switch (command.getCommandType().toLowerCase()) {
			case COMMAND_TYPE_ADD:
				addTask(command.getTab(),command.getTask());
				break;
			case COMMAND_TYPE_DELETE:
				deleteTask(command.getTab(),command.getIndexes());
				break;
			default:
				break;
		}
	}

	/**
	 * Executes the last command stored when parseCommand was called,
	 * and stores it in a {@code Command} history stack
	 */
	public void executeCommand() {
		execute(command);
		//add command to history stack
	}

	/**
	 * Combines all floating and dated tasks
	 * 
	 * @return  the combined list of tasks
	 */
	public ArrayList<Task> getAllTasks() {
		ArrayList<Task> allTasks = new ArrayList<Task>();
		allTasks.addAll(floatingTasks);
		allTasks.addAll(datedTasks);
		return allTasks;
	}
	
	private Date getEigthDay() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DATE,8);
		calendar.set(Calendar.HOUR_OF_DAY,0);
		calendar.set(Calendar.MINUTE,0);
		calendar.set(Calendar.SECOND,0);
		calendar.set(Calendar.MILLISECOND,0);
		return calendar.getTime();
	}
	
	public String getFileDirectory() {
		return storage.getFileDirectory();
	}
	
	public String getFileName() {
		return storage.getFileName();
	}

	/**
	 * @return the list of floating tasks
	 */
	public ArrayList<Task> getFloatingTasks() {
		return floatingTasks;
	}
	
	/**
	 * Creates a new list of tasks that are within the next seven days
	 * of the current system date
	 * 
	 * @return the list of tasks due in the next seven days
	 */
	public ArrayList<Task> getNextSevenDays() {
		Date tomorrow = getTomorrow();
		Date eighthDay = getEigthDay();
		
		ArrayList<Task> result = new ArrayList<Task>();
		
		for (Task task : getAllTasks()) {
			if (task.getEndDate() != null) {
				if (task.getEndDate().compareTo(tomorrow) >= 0) {
					if (task.getEndDate().compareTo(eighthDay) < 0) {
						result.add(task);
					}
				}
			}
		}
		return result;
	}

	/**
	 * Creates a new list of tasks that have the same date as the 
	 * current system date
	 * 
	 * @return the list of tasks due today
	 */
	public ArrayList<Task> getTodayTasks() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_DDMMYY);
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
	
	/**
	 * Use to retrieve every task which has the date field
	 * @return the list of tasks with dates
	 */
	public ArrayList<Task> getDatedTasks() {
		return datedTasks;
	}

	private Date getTomorrow() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DATE,1);
		calendar.set(Calendar.HOUR_OF_DAY,0);
		calendar.set(Calendar.MINUTE,0);
		calendar.set(Calendar.SECOND,0);
		calendar.set(Calendar.MILLISECOND,0);
		return calendar.getTime();
	}
	
	/*
	 * For edit mode, use this command to get feedback while user is typing
	 * On hit enter, use "editTask" command
	 * 
	 * Examples of use: 
	 * add - parseCommand("cook dinner", Controller.NO_TAB);
	 * add - parseCommand("cook dinner #home", Controller.NO_TAB);
	 * delete - parseCommand("delete 5,6-7", Controller.FLOATING_TAB);
	 * 
	 * Returns feedback to be displayed to user
	 */
	/**
	 * Evaluates the given command and provide feedback
	 * 
	 * @param   userCommand 
	 * 			the command to be evaluated
	 * 
	 * @param   tab 
	 * 			the tab affected by the command
	 * 
	 * @return	feedback resulting from the evaluation of the command
	 */
	public String parseCommand(String userCommand, String tab) {
		command = parser.parse(userCommand);
		String feedback = null;
		
		switch (tab) {
			case NO_TAB:
				feedback = command.getTask().toString();
				break;
			case FLOATING_TAB:
				feedback = "delete from all";
				command.setTab(FLOATING_TAB);
				break;
			case DATED_TAB:
				feedback = "delete from all";
				command.setTab(DATED_TAB);
				break;
			case TODAY_TAB:
				feedback = "delete from today";
				command.setTab(TODAY_TAB);
				break;
			case NEXT_SEVEN_DAYS_TAB:
				feedback = "delete from next seven days";
				command.setTab(NEXT_SEVEN_DAYS_TAB);
				break;
		}
		
		return feedback;
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
	
	public void setFileDirectory(String fileDirectory) {
		storage.setFileDirectory(fileDirectory);
	}
	
	public void setFileName(String fileName) {
		storage.setFileName(fileName);
	}
}
