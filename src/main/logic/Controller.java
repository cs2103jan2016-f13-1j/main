/**
 * Summary of public methods that can be called:
 * 
 * Controller();
 * parseCommand(String userCommand, String tab);
 * editTask(String tab, int index);
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
	private static final String COMMAND_TYPE_EDIT = "edit";
	private static final String COMMAND_TYPE_DELETE = "delete";
	
	private static final int FLOATING_TASKS_INDEX = 0;
	private static final int DATED_TASKS_INDEX = 1;
	
	CommandParser parser = null;
	Storage storage = null;
	
	Stack<Command> undoHistory = new Stack<Command>();
	Stack<Command> redoHistory = new Stack<Command>();
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
	
	private ArrayList<Task> deleteMultipleFromList(ArrayList<Task> listToDelete, ArrayList<Integer> indexesToDelete) {
		int j = 0;
		for (int i = 0; i < indexesToDelete.size(); i++) {
		    int indexToDelete = indexesToDelete.get(i-j);
		    Task removedTask = listToDelete.remove(indexToDelete);
		    command.getPreviousTasks().add(removedTask);
		    j++;
		}
		return listToDelete;
	}
	
	private void deleteTask(String tab, ArrayList<Integer> indexes) {
		ArrayList<Task> temp = null;	
		
		switch (tab) {
			case FLOATING_TAB:
				deleteMultipleFromList(floatingTasks, indexes);
				break;
			case DATED_TAB:
				deleteMultipleFromList(datedTasks, indexes);
				break;
			case TODAY_TAB:
				ArrayList<Task> newTodayTasks = deleteMultipleFromList(getTodayTasks(), indexes);
				temp = new ArrayList<Task>();
				temp.addAll(newTodayTasks);
				temp.addAll(getNextSevenDays());
				datedTasks = new ArrayList<Task>();
				for (Task task : temp) {
					datedTasks.add(task);
				}
			case NEXT_SEVEN_DAYS_TAB:
				ArrayList<Task> newNextSevenDaysTasks = deleteMultipleFromList(getNextSevenDays(), indexes);
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
	
	private void addToList(String tab, int index, Task task) {
		ArrayList<Task> temp = null;
		
		switch (tab.toLowerCase()) {
			case FLOATING_TAB:
				floatingTasks.add(index,task);
				break;
			case DATED_TAB:
				datedTasks.add(index,task);
				break;
			case TODAY_TAB:
				ArrayList<Task> newTodayTasks = getTodayTasks();
				newTodayTasks.add(index,task);
				temp = new ArrayList<Task>();
				temp.addAll(newTodayTasks);
				temp.addAll(getNextSevenDays());
				datedTasks = new ArrayList<Task>();
                for (Task t : temp) {
                    datedTasks.add(t);
                }
			case NEXT_SEVEN_DAYS_TAB:
				ArrayList<Task> newNextSevenDaysTasks = getNextSevenDays();
				newNextSevenDaysTasks.add(index,task);
				temp = new ArrayList<Task>();
                temp.addAll(getTodayTasks());
                temp.addAll(newNextSevenDaysTasks);
                datedTasks = new ArrayList<Task>();
                for (Task t : temp) {
                    datedTasks.add(t);
                }
			default:
				break;
		}
	}
	
	/**
	 * Edits the task in the respective {@code tab} at position {@code index}
	 * @param 	tab
	 * 			the tab where the task is
	 * @param 	index
	 * 			the index of the task
	 */
	public void editTask(String tab, int index) {
		command.setCommandType(COMMAND_TYPE_EDIT);
		command.getPreviousTasks().add(getTaskAtIndex(tab,index));
		command.getIndexes().add(index);
		
		deleteTask(tab,command.getIndexes());
		addToList(tab,index,command.getTask());
		saveTasks();
		undoHistory.push(command);
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
		undoHistory.push(command);
	}
	
	public void undo() {
		Command undoCommand = undoHistory.pop();
		redoHistory.push(undoCommand);
		String tab = undoCommand.getTab();
		
		ArrayList<Integer> indexes = null;
		
		switch (undoCommand.getCommandType().toLowerCase()) {
			case COMMAND_TYPE_ADD:
			    ArrayList<Integer> indexToDelete = new ArrayList<Integer>();
			    indexToDelete.add(getLastIndexOf(tab));
			    deleteTask(tab,indexToDelete);
				break;
			case COMMAND_TYPE_EDIT:
                //delete task and add previous task at index
			    Task previousTask = undoCommand.getPreviousTasks().get(0);
			    indexes = undoCommand.getIndexes();
			    deleteTask(tab,indexes);
			    addToList(tab,indexes.get(0),previousTask);
                break;
			case COMMAND_TYPE_DELETE:
			    ArrayList<Task> previousTasks = undoCommand.getPreviousTasks();
			    indexes = undoCommand.getIndexes();
			    for (int i = 0; i < previousTasks.size(); i++) {
			        addToList(tab, indexes.get(i), previousTasks.get(i));
			    }
				break;
			default:
				break;
		}
		saveTasks();
	}
	
	public void redo() {
	    Command redoCommand = redoHistory.pop();
        undoHistory.push(redoCommand);
        String tab = redoCommand.getTab();

        switch (redoCommand.getCommandType().toLowerCase()) {
            case COMMAND_TYPE_ADD:
                command = redoCommand;
                executeCommand();
                break;
            case COMMAND_TYPE_EDIT:
                deleteTask(tab,command.getIndexes());
                addToList(tab,command.getIndexes().get(0),command.getTask());
                saveTasks();
                break;
            case COMMAND_TYPE_DELETE:
                command = redoCommand;
                executeCommand();
                break;
            default:
                break;
        }
	}
	
	private int getLastIndexOf(String tab) {
	    int index = 0;
	    
	    switch (tab.toLowerCase()) {
    	    case FLOATING_TAB:
                index = floatingTasks.size();
                break;
            case DATED_TAB:
                index = datedTasks.size();
                break;
            case TODAY_TAB:
                index = getTodayTasks().size();
            case NEXT_SEVEN_DAYS_TAB:
                index = getNextSevenDays().size();
            default:
                break;
	    }
	    index--;
	    return index;
	}
	
   private Task getTaskAtIndex(String tab, int index) {
        Task task = null;
        
        switch (tab.toLowerCase()) {
            case FLOATING_TAB:
                task = floatingTasks.get(index);
                break;
            case DATED_TAB:
                task = datedTasks.get(index);
                break;
            case TODAY_TAB:
                task = getTodayTasks().get(index);
            case NEXT_SEVEN_DAYS_TAB:
                task = getNextSevenDays().get(index);
            default:
                break;
        }
        return task;
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
	
	/**
	 * Evaluates the given command and provide feedback
	 * 
	 * Examples of use: 
     * add - parseCommand("cook dinner", Controller.NO_TAB);
     * add - parseCommand("cook dinner #home", Controller.NO_TAB);
     * delete - parseCommand("delete 5,6-7", Controller.FLOATING_TAB);
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
