/**
 * TO BE DONE: mark tasks as done, sorting
 * 
 * Summary of public methods that can be called:
 * 
 * Controller();
 * parseCommand(String userCommand, String tab);
 * editTask(String tab, int index);
 * executeCommand();
 * undo();
 * redo();
 * 
 * getFloatingTasks();
 * getDatedTasks();
 * getTodayTasks();
 * getNextSevenDays();
 * getFileLocation();
 * getUndoCount();
 * getRedoCount();
 * 
 * setFileLocation(String fileLocation);
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

public class Logic {
	
    private static Logic logic;
    
	public static final String DATE_FORMAT_DDMMYY = "ddMMyyyy";
	
	public static enum List {
	    FLOATING, DATED, TODAY, NEXT_SEVEN_DAYS
	}
	
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
    private Logic() {
        parser = new CommandParser();
        storage = Storage.getStorage();
         
        ArrayList<ArrayList<Task>> tasksFromStorage = storage.readTasks();;
        floatingTasks = tasksFromStorage.get(FLOATING_TASKS_INDEX);
        datedTasks = tasksFromStorage.get(DATED_TASKS_INDEX);
    }

    public static synchronized Logic getLogic() {
        if (logic == null) {
            logic = new Logic();
        }
        return logic;
    }

    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
	
	private void addTask(List type, Task task) {
		switch (type) {
			case FLOATING:
				floatingTasks.add(task);
				break;
			case DATED:
				datedTasks.add(task);
				break;
			default:
				break;
		}
	}
	
	private ArrayList<Task> deleteTasksFromList(ArrayList<Task> list, ArrayList<Integer> indexes) {
		int j = 0;
		ArrayList<Task> previousTasks = new ArrayList<Task>();
		for (int i = 0; i < indexes.size(); i++) {
		    int indexToDelete = indexes.get(i-j);
		    Task removedTask = list.remove(indexToDelete);
		    previousTasks.add(removedTask);
		    j++;
		}
		command.setPreviousTasks(previousTasks);
		return list;
	}
	
	private void deleteTask(List type, ArrayList<Integer> indexes) {
		switch (type) {
			case FLOATING:
				deleteTasksFromList(floatingTasks, indexes);
				break;
			case DATED:
				deleteTasksFromList(datedTasks, indexes);
				break;
			case TODAY:
                deleteTasksFromToday(indexes);
			case NEXT_SEVEN_DAYS:
            deleteTasksFromNextSevenDays(indexes);
			default:
				break;
		}
	}

    private void deleteTasksFromNextSevenDays(ArrayList<Integer> indexes) {
        ArrayList<Task> temp = new ArrayList<Task>();
        ArrayList<Task> newNextSevenDaysTasks = deleteTasksFromList(getNextSevenDays(), indexes);
        temp.addAll(getTodayTasks());
        temp.addAll(newNextSevenDaysTasks);
        datedTasks = new ArrayList<Task>();
        for (Task task : temp) {
        	datedTasks.add(task);
        }
    }

    private void deleteTasksFromToday(ArrayList<Integer> indexes) {
        ArrayList<Task> temp = new ArrayList<Task>();
        ArrayList<Task> newTodayTasks = deleteTasksFromList(getTodayTasks(), indexes);
        temp.addAll(newTodayTasks);
        temp.addAll(getNextSevenDays());
        datedTasks = new ArrayList<Task>();
        for (Task task : temp) {
        	datedTasks.add(task);
        }
    }
	
	private void addToList(List type, int index, Task task) {
		switch (type) {
			case FLOATING:
				floatingTasks.add(index,task);
				break;
			case DATED:
				datedTasks.add(index,task);
				break;
			case TODAY:
                addToToday(index, task);
			case NEXT_SEVEN_DAYS:
                addToNextSevenDays(index, task);
			default:
				break;
		}
	}

    private void addToNextSevenDays(int index, Task task) {
        ArrayList<Task> temp = new ArrayList<Task>();
        ArrayList<Task> newNextSevenDaysTasks = getNextSevenDays();
        newNextSevenDaysTasks.add(index,task);
        temp.addAll(getTodayTasks());
        temp.addAll(newNextSevenDaysTasks);
        datedTasks = new ArrayList<Task>();
        for (Task t : temp) {
            datedTasks.add(t);
        }
    }

    private void addToToday(int index, Task task) {
        ArrayList<Task> temp = new ArrayList<Task>();
        ArrayList<Task> newTodayTasks = getTodayTasks();
        newTodayTasks.add(index,task);
        temp.addAll(newTodayTasks);
        temp.addAll(getNextSevenDays());
        datedTasks = new ArrayList<Task>();
        for (Task t : temp) {
            datedTasks.add(t);
        }
    }
	
	/**
	 * Edits the task in the respective {@code tab} at position {@code index}
	 * @param 	tab
	 * 			the tab where the task is at
	 * @param 	index
	 * 			the index of the task
	 */
	public void editTask(List type, int index) {
	    command.setCommandType(Command.Type.EDIT);
	    command.setPreviousTasks(new ArrayList<Task>());
	    command.getPreviousTasks().add(getTaskAtIndex(type,index));
	    command.setIndexes(new ArrayList<Integer>());
	    command.getIndexes().add(index);
	    
		Task task = command.getTask();
		
		deleteTask(type,command.getIndexes());
		
		//if no change in tab, edit in position
		if (type.name().equals(command.getListType())) {
		    addToList(type,index,task);
		} else {
		    if (task.hasDate()) {
		        datedTasks.add(task);
		    } else {
		        floatingTasks.add(task);
		    }
		}
		command.setPreviousListType(type.name());
		saveTasks();
		addToHistory();
	}

    private void addToHistory() {
        undoHistory.push(command);
		redoHistory = new Stack<Command>();
    }

	/**
	 * Executes the last command stored when parseCommand was called,
	 * and stores it in a {@code Command} history stack.
	 * 
	 * Used for add and delete operations.
	 */
	public void executeCommand() {
	    switch (command.getCommandType()) {
            case ADD:
                addTask(Enum.valueOf(List.class, command.getListType()),command.getTask());
                break;
            case DELETE:
                deleteTask(Enum.valueOf(List.class, command.getListType()),command.getIndexes());
                break;
            default:
                break;
	    }
	    saveTasks();
		addToHistory();
	}
	
	public void undo() {
		Command undoCommand = undoHistory.pop();
		
		List type = Enum.valueOf(List.class, undoCommand.getListType());
		
		ArrayList<Integer> indexes = null;
		
		switch (undoCommand.getCommandType()) {
			case ADD:
			    redoHistory.push(undoCommand);
			    ArrayList<Integer> indexToDelete = new ArrayList<Integer>();
			    indexToDelete.add(getLastIndexOf(type));
			    deleteTask(type,indexToDelete);
				break;
			case EDIT:
                //delete task and add previous task at index
			    Task previousTask = undoCommand.getPreviousTasks().get(0);
			    indexes = undoCommand.getIndexes();
			    deleteTask(type,indexes);
			    addToList(Enum.valueOf(List.class, undoCommand.getPreviousListType()),indexes.get(0),previousTask);
			    undoCommand.setListType(type.name());
			    redoHistory.push(undoCommand);
                break;
			case DELETE:
			    redoHistory.push(undoCommand);
			    ArrayList<Task> previousTasks = undoCommand.getPreviousTasks();
			    indexes = undoCommand.getIndexes();
			    for (int i = 0; i < previousTasks.size(); i++) {
			        addToList(type, indexes.get(i), previousTasks.get(i));
			    }
				break;
			default:
				break;
		}
		saveTasks();
	}
	
	public void redo() {
	    Command redoCommand = redoHistory.pop();
	    command = redoCommand;
	    undoHistory.push(command);

        switch (redoCommand.getCommandType()) {
            case ADD:
                addTask(Enum.valueOf(List.class, command.getListType()),command.getTask());             
                break;
            case EDIT:
                editTask(Enum.valueOf(List.class, command.getPreviousListType()),command.getIndexes().get(0));
                break;
            case DELETE:
                deleteTask(Enum.valueOf(List.class, command.getListType()),command.getIndexes());
                break;
            default:
                break;
        }
        saveTasks();
	}
	
	private int getLastIndexOf(List type) {
	    int index = 0;
	    
	    switch (type) {
    	    case FLOATING:
                index = floatingTasks.size();
                break;
            case DATED:
                index = datedTasks.size();
                break;
            case TODAY:
                index = getTodayTasks().size();
            case NEXT_SEVEN_DAYS:
                index = getNextSevenDays().size();
            default:
                break;
	    }
	    index--;
	    return index;
	}
	
   private Task getTaskAtIndex(List type, int index) {
        Task task = null;
        
        switch (type) {
            case FLOATING:
                task = floatingTasks.get(index);
                break;
            case DATED:
                task = datedTasks.get(index);
                break;
            case TODAY:
                task = getTodayTasks().get(index);
            case NEXT_SEVEN_DAYS:
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
	public String parseCommand(String userCommand, List type) {
	    String feedback = null;
	    
	    command = parser.parse(userCommand);
		
		switch (command.getCommandType()) {
            case ADD:
                if (command.getTask().hasDate()) {
                    command.setListType(List.DATED.name());
                } else {
                    command.setListType(List.FLOATING.name());
                }
                feedback = command.getTask().toString();
                break;
            case DELETE:
                ArrayList<Integer> indexArray = command.getIndexes();
                StringBuilder indexes = new StringBuilder();
                for (int i = 0; i < indexArray.size(); i++) {
                    indexes.append(indexArray.get(i));
                    if (i < indexArray.size() - 1) {
                        indexes.append(" ");
                    }
                }
                feedback = indexes.toString();
                command.setListType(type.name());
                break;
            default:
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
	
	/**
	 * Update user's settings.txt file to reflect new file location
	 * 
	 * @param fileLocation
	 *        new location to store user settings
	 */
	public void setFileLocation(String fileLocation) {
		storage.setFileLocation(fileLocation);
	}
	
	public String getFileLocation() {
		return storage.getFileLocation();
	}
	
	public int getUndoCount() {
	    return undoHistory.size();
	}
	
	public int getRedoCount() {
        return redoHistory.size();
    }
}
