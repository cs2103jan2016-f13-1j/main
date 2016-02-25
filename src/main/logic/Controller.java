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
	
	public enum Tab {
		NO_TAB, FLOATING_TAB, DATED_TAB, TODAY_TAB, NEXT_SEVEN_DAYS_TAB
	}
	public static final String FLOATING = "floating";
	public static final String DATED = "dated";
	public static final String TODAY = "today";
	public static final String NEXT_SEVEN_DAYS = "nextSevenDays";
	
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
	
	/**
	 * Adds the {@code Task} object to the tab
	 * 
	 * @param 	tab 
	 * 			the tab where the task should be added to
	 * @param 	task 
	 * 			the task to be added
	 */
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
	
	//decrements every integer in the array by 1
	private ArrayList<Integer> decreaseIndex(ArrayList<Integer> indexes) {
		for (int i = 0; i < indexes.size(); i++) {
			indexes.set(i, indexes.get(i) - 1);
		}
		return indexes;
	}
	
	private ArrayList<Task> deleteFromList(ArrayList<Task> listToDelete, ArrayList<Integer> indexesToDelete) {
		while (indexesToDelete.size() > 0) {
			int indexToDelete = indexesToDelete.remove(0);
			listToDelete.remove(indexToDelete);
			indexesToDelete = decreaseIndex(indexesToDelete);
		}
		return listToDelete;
	}
	
	/**
	 * Deletes tasks from tab based on given indexes
	 * @param 	tab 
	 * 			the tab to delete from
	 * @param 	indexes
	 * 			the indexes of the tasks to delete
	 */
	public void deleteTask(String tab, ArrayList<Integer> indexes) {
		ArrayList<Integer> indexesToDelete = decreaseIndex(indexes);
		ArrayList<Task> temp = null;	
		
		switch (tab) {
			case FLOATING:
				deleteFromList(floatingTasks, indexesToDelete);
				break;
			case DATED:
				deleteFromList(datedTasks, indexesToDelete);
				break;
			case TODAY:
				ArrayList<Task> newTodayTasks = deleteFromList(getTodayTasks(), indexesToDelete);
				temp = new ArrayList<Task>();
				temp.addAll(newTodayTasks);
				temp.addAll(getNextSevenDays());
				datedTasks = temp;
			case NEXT_SEVEN_DAYS:
				ArrayList<Task> newNextSevenDaysTasks = deleteFromList(getNextSevenDays(), indexesToDelete);
				temp = new ArrayList<Task>();
				datedTasks.addAll(getTodayTasks());
				datedTasks.addAll(newNextSevenDaysTasks);
				datedTasks = temp;
			default:
				break;
		}
		saveTasks();
	}
	
	private ArrayList<Task> editList(ArrayList<Task> list, int oldTaskIndex, Task newTask) {
		list.remove(oldTaskIndex);
		list.add(oldTaskIndex, newTask);
		return list;
	}
	
	public void editTask(String tab, int oldTaskIndex, Task newTask) {
		oldTaskIndex--;
		ArrayList<Task> temp = null;
		
		switch (tab) {
			case FLOATING:
				editList(floatingTasks,oldTaskIndex,newTask);
				break;
			case DATED:
				editList(datedTasks,oldTaskIndex,newTask);
				break;
			case TODAY:
				ArrayList<Task> newTodayTasks = editList(getTodayTasks(),oldTaskIndex,newTask);
				temp = new ArrayList<Task>();
				temp.addAll(newTodayTasks);
				temp.addAll(getNextSevenDays());
				datedTasks = temp;
			case NEXT_SEVEN_DAYS:
				ArrayList<Task> newNextSevenDaysTasks = editList(getNextSevenDays(),oldTaskIndex,newTask);
				temp = new ArrayList<Task>();
				datedTasks.addAll(getTodayTasks());
				datedTasks.addAll(newNextSevenDaysTasks);
				datedTasks = temp;
			default:
				break;
		}
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
	 * Executes the last evaluated command and stores it in a {@code Command} history stack
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
	
	
	public Storage getStorage() {
		return storage;
	}

	/**
	 * Creates a new list of tasks that have the same date as the 
	 * current system date
	 * 
	 * @return the list of tasks due today
	 */
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
	public String parseCommand(String userCommand, Tab tab) {
		command = parser.parse(userCommand);
		String feedback = null;
		
		switch (tab) {
			case NO_TAB:
				//add command
				//parser will assign the tag field
				feedback = command.getTask().toString();
				break;
			case FLOATING_TAB:
				feedback = "delete from floating";
				command.setTab(FLOATING);
				break;
			case DATED_TAB:
				feedback = "delete from dated tasks under all tab";
				command.setTab(DATED);
				break;
			case TODAY_TAB:
				feedback = "delete from today";
				command.setTab(TODAY);
				break;
			case NEXT_SEVEN_DAYS_TAB:
				feedback = "delete from next seven days";
				command.setTab(NEXT_SEVEN_DAYS);
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
