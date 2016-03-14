/**
 * TO BE DONE: mark tasks as done, sorting
 * 
 * Summary of public methods that can be called:
 * 
 * Controller();
 * parseCommand(String userCommand, String tab);
 * editTask(Logic.List, int index);
 * executeCommand();
 * undo();
 * redo();
 * 
 * getFloatingTasks();
 * getDatedTasks();
 * getTodayTasks();
 * getThisWeek();
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
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import main.data.Command;
import main.data.Task;
import main.parser.CommandParser;
import main.storage.Storage;

public class Logic {
    
    public static enum List {
	    ALL, FLOATING, DATED, TODAY, THIS_WEEK
	}
	
    private static final Logger logger = Logger.getLogger(Logic.class.getName());
    
	private static Logic logic;
	
	private static final int FLOATING_TASKS_INDEX = 0;
	private static final int DATED_TASKS_INDEX = 1;
	
	private CommandParser parser = null;
	private Storage storage = null;
	
	private Command command = null;
	private Stack<Command> undoHistory = new Stack<Command>();
	private Stack<Command> redoHistory = new Stack<Command>();
	private ArrayList<Task> floatingTasks = new ArrayList<Task>();
	
    private ArrayList<Task> datedTasks = new ArrayList<Task>();
    
    /**
	 * Initializes a newly created {@code Controller} object.
	 */
    private Logic() {
        parser = new CommandParser();
        storage = Storage.getStorage();
        assert(storage != null);
        
        ArrayList<ArrayList<Task>> tasksFromStorage = storage.readTasks();;
        floatingTasks = tasksFromStorage.get(FLOATING_TASKS_INDEX);
        datedTasks = tasksFromStorage.get(DATED_TASKS_INDEX);
        assert(floatingTasks != null);
        assert(datedTasks != null);
    }
    
    public static synchronized Logic getLogic() {
        if (logic == null) {
            logic = new Logic();
        }
        assert(logic != null);
        return logic;
    }

    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
	
	/**
	 * Edits the task in the respective {@code tab} at position {@code index}
	 * @param 	tab
	 * 			the tab where the task is at
	 * @param 	index
	 * 			the index of the task
	 */
	public void editTask(List type, int index) {
	    assert(command != null);
	    command.setCommandType(Command.Type.EDIT);
	    command.setPreviousTasks(new ArrayList<Task>());
	    command.getPreviousTasks().add(getTaskAtIndex(type,index));
	    command.setIndexes(new ArrayList<Integer>());
	    command.getIndexes().add(index);
	    
	    assert(command.getTask() != null);
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
		logger.log(Level.INFO,"Edited index " + index + " to " + task.getTitle());
		saveTasks();
		addToHistory();
	}
	
	/**
	 * Executes the last command stored when parseCommand was called,
	 * and stores it in a {@code Command} history stack.
	 * 
	 * Used for add and delete operations.
	 */
	public void executeCommand() {
	    assert(command != null);
	    switch (command.getCommandType()) {
            case ADD:
                addTask(Enum.valueOf(List.class, command.getListType()),command.getTask());
                break;
            case DELETE:
                deleteTask(Enum.valueOf(List.class, command.getListType()),command.getIndexes());
                break;
            case DONE:
                markTask(Enum.valueOf(List.class, command.getListType()),command.getIndexes(), true);
                break;
            default:
                break;
	    }
	    logger.log(Level.INFO,"Executed " + command.getCommandType().name() + " command");
	    saveTasks();
		addToHistory();
	}
	
	/**
	 * Combines all floating and dated tasks
	 * 
	 * @return  the combined list of tasks
	 */
	public ArrayList<Task> getAllTasks() {
		ArrayList<Task> tasks = new ArrayList<Task>();
		tasks.addAll(floatingTasks);
		tasks.addAll(datedTasks);
		return tasks;
	}

    /**
	 * Use to retrieve every task which has the date field
	 * @return the list of tasks with dates
	 */
	public ArrayList<Task> getDatedTasks() {
        return datedTasks;
	}

    public String getFileLocation() {
		return storage.getFileLocation();
	}
    
    /**
	 * @return the list of floating tasks
	 */
	public ArrayList<Task> getFloatingTasks() {
        return floatingTasks;
	}
    
    public int getRedoCount() {
        return redoHistory.size();
    }
	
    /**
	 * Creates a new list of tasks that are within the next seven days
	 * of the current system date
	 * 
	 * @return the list of tasks due in the next seven days
	 */
	public ArrayList<Task> getThisWeek() {
		ArrayList<Task> result = new ArrayList<Task>();
		
		for (Task task : getDatedTasks()) {
		    if (task.isThisWeek()) {
		        result.add(task);
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
		ArrayList<Task> result = new ArrayList<Task>();
		
		for (Task task : getDatedTasks()) {
			if (task.isToday()) {
				result.add(task);
			}
		}
		return result;
	}
    
	public int getUndoCount() {
	    return undoHistory.size();
	}

    /**
	 * Evaluates the given command and provide feedback
	 * 
	 * Examples of use: 
     * add - parseCommand("cook dinner", null);
     * add - parseCommand("cook dinner #home", Logic.List.FLOATING);
     * delete - parseCommand("delete 5,6-7", Logic.List.FLOATING);
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
	    ArrayList<Integer> indexArray = null;
	    StringBuilder indexes = null;
	    
	    command = parser.parse(userCommand);
	    assert(command != null);
	    command.setListType(type.name());
	    
		switch (command.getCommandType()) {
            case ADD:
                feedback = command.getTask().toString();
                break;
            case DELETE:
                indexArray = command.getIndexes();
                indexes = new StringBuilder();
                for (int i = 0; i < indexArray.size(); i++) {
                    indexes.append(indexArray.get(i));
                    if (i < indexArray.size() - 1) {
                        indexes.append(" ");
                    }
                }
                feedback = indexes.toString();
                break;
            case DONE:
                indexArray = command.getIndexes();
                indexes = new StringBuilder();
                for (int i = 0; i < indexArray.size(); i++) {
                    indexes.append(indexArray.get(i));
                    if (i < indexArray.size() - 1) {
                        indexes.append(" ");
                    }
                }
                feedback = indexes.toString();
            default:
                break;
		}
		
		return feedback;
	}

    public void redo() {
	    if (redoHistory.size() == 0) {
            throw new EmptyStackException();
        }
        assert(redoHistory.size() > 0);
        
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
            case DONE:
                markTask(Enum.valueOf(List.class, command.getListType()),command.getIndexes(), true);
            default:
                break;
        }
        logger.log(Level.INFO,"Redone " + command.getCommandType().name() + " command");
        saveTasks();
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

    public void undo() {
	    if (undoHistory.size() == 0) {
	        throw new EmptyStackException();
	    }
	    assert(undoHistory.size() > 0);
	    
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
			case DONE:
			    redoHistory.push(undoCommand);
			    indexes = undoCommand.getIndexes();
			    for (int i = 0; i < indexes.size(); i++) {
			        markTask(Enum.valueOf(List.class, undoCommand.getListType()),undoCommand.getIndexes(), false);
                }
			default:
				break;
		}
		logger.log(Level.INFO,"Undone " + undoCommand.getCommandType().name() + " command");
		saveTasks();
	}

	private void addTask(List type, Task task) {
		if (task.hasDate()) {
		    datedTasks.add(task);
        } else {
            floatingTasks.add(task);
        }
	}
	
	private void addToHistory() {
        assert(command != null);
        undoHistory.push(command);
		redoHistory = new Stack<Command>();
    }
	
	private void addToList(List type, int index, Task task) {
		switch (type) {
		    case ALL:
		        if (task.hasDate()) {
		            datedTasks.add(index, task);
		        } else {
		            floatingTasks.add(index, task);
		        }
		        break;
			case TODAY:
                addToToday(index, task);
			case THIS_WEEK:
                addToThisWeek(index, task);
			default:
				break;
		}
	}
	
	private void addToThisWeek(int index, Task task) {
        ArrayList<Task> temp = new ArrayList<Task>();
        ArrayList<Task> newThisWeekTasks = getThisWeek();
        newThisWeekTasks.add(index,task);
        temp.addAll(getTodayTasks());
        temp.addAll(newThisWeekTasks);
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
        temp.addAll(getThisWeek());
        datedTasks = new ArrayList<Task>();
        for (Task t : temp) {
            datedTasks.add(t);
        }
   }

	private void deleteTask(List type, ArrayList<Integer> indexes) {
		switch (type) {
		    case ALL:
		        deleteTasksFromAll(indexes);
				break;
			case TODAY:
                deleteTasksFromToday(indexes);
                break;
			case THIS_WEEK:
			    deleteTasksFromThisWeek(indexes);
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
		assert(command != null);
		command.setPreviousTasks(previousTasks);
		return list;
	}
	
	private void deleteTasksFromAll(ArrayList<Integer> indexes) {
	    ArrayList<Integer> floatingIndexes = new ArrayList<Integer>();
        ArrayList<Integer> datedIndexes = new ArrayList<Integer>();
        int size = floatingTasks.size();
        for (int i : indexes) {
            if (i < size) {
                floatingIndexes.add(i);
            } else {
                datedIndexes.add(i - size);
            }
        }
        
        int j = 0;
        ArrayList<Task> previousTasks = new ArrayList<Task>();
        for (int i = 0; i < floatingIndexes.size(); i++) {
            int indexToDelete = floatingIndexes.get(i-j);
            Task removedTask = floatingTasks.remove(indexToDelete);
            previousTasks.add(removedTask);
            j++;
        }
        j = 0;
        for (int i = 0; i < datedIndexes.size(); i++) {
            int indexToDelete = datedIndexes.get(i-j);
            Task removedTask = datedTasks.remove(indexToDelete);
            previousTasks.add(removedTask);
            j++;
        }
        assert(command != null);
        command.setPreviousTasks(previousTasks);
    }
	
	private void deleteTasksFromThisWeek(ArrayList<Integer> indexes) {
        ArrayList<Task> temp = new ArrayList<Task>();
        ArrayList<Task> newThisWeekTasks = deleteTasksFromList(getThisWeek(), indexes);
        temp.addAll(getTodayTasks());
        temp.addAll(newThisWeekTasks);
        datedTasks = new ArrayList<Task>();
        for (Task task : temp) {
        	datedTasks.add(task);
        }
    }
	
	private void deleteTasksFromToday(ArrayList<Integer> indexes) {
        ArrayList<Task> temp = new ArrayList<Task>();
        ArrayList<Task> newTodayTasks = deleteTasksFromList(getTodayTasks(), indexes);
        temp.addAll(newTodayTasks);
        temp.addAll(getThisWeek());
        datedTasks = new ArrayList<Task>();
        for (Task task : temp) {
        	datedTasks.add(task);
        }
    }
	
	private int getLastIndexOf(List type) {
	    int index = 0;
	    
	    switch (type) {
    	    case ALL:
    	        index = getAllTasks().size();
    	        break;
    	    case FLOATING:
                index = floatingTasks.size();
                break;
            case DATED:
                index = datedTasks.size();
                break;
            case TODAY:
                index = getTodayTasks().size();
            case THIS_WEEK:
                index = getThisWeek().size();
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
                case THIS_WEEK:
                    task = getThisWeek().get(index);
                default:
                    break;
            }
            return task;
        }
	
	private void markTask(List type, ArrayList<Integer> indexes, boolean status) {
        switch (type) {
            case ALL:
                markTasksFromAll(indexes, status);
                break;
            case TODAY:
                markTasksFromToday(indexes, status);
                break;
            case THIS_WEEK:
                markTasksFromThisWeek(indexes, status);
                break;
            default:
                break;
        }
    }
	
	private ArrayList<Task> markTasksFromList(ArrayList<Task> list, ArrayList<Integer> indexes, boolean status) {
        ArrayList<Task> previousTasks = new ArrayList<Task>();
        for (int i = 0; i < indexes.size(); i++) {
            int indexToMark = indexes.get(i);
            Task task = list.get(indexToMark);
            task.setDone(status);
            previousTasks.add(task);
        }
        assert(command != null);
        command.setPreviousTasks(previousTasks);
        return list;
    }
	
   private void markTasksFromAll(ArrayList<Integer> indexes, boolean status) {
        ArrayList<Integer> floatingIndexes = new ArrayList<Integer>();
        ArrayList<Integer> datedIndexes = new ArrayList<Integer>();
        int size = floatingTasks.size();
        for (int i : indexes) {
            if (i < size) {
                floatingIndexes.add(i);
            } else {
                datedIndexes.add(i - size);
            }
        }
        
        ArrayList<Task> previousTasks = new ArrayList<Task>();
        for (int i = 0; i < floatingIndexes.size(); i++) {
            int indexToMark = floatingIndexes.get(i);
            Task task = floatingTasks.get(indexToMark);
            task.setDone(status);
            previousTasks.add(task);
        }
        for (int i = 0; i < datedIndexes.size(); i++) {
            int indexToMark = datedIndexes.get(i);
            Task task = datedTasks.get(indexToMark);
            task.setDone(status);
            previousTasks.add(task);
        }
        assert(command != null);
        command.setPreviousTasks(previousTasks);
    }
	
	private void markTasksFromThisWeek(ArrayList<Integer> indexes, boolean status) {
        ArrayList<Task> temp = new ArrayList<Task>();
        ArrayList<Task> newThisWeekTasks = markTasksFromList(getThisWeek(), indexes, status);
        temp.addAll(getTodayTasks());
        temp.addAll(newThisWeekTasks);
        datedTasks = new ArrayList<Task>();
        for (Task task : temp) {
            datedTasks.add(task);
        }
    }
	
	private void markTasksFromToday(ArrayList<Integer> indexes, boolean status) {
        ArrayList<Task> temp = new ArrayList<Task>();
        ArrayList<Task> newTodayTasks = markTasksFromList(getTodayTasks(), indexes, status);
        temp.addAll(newTodayTasks);
        temp.addAll(getThisWeek());
        datedTasks = new ArrayList<Task>();
        for (Task task : temp) {
            datedTasks.add(task);
        }
    }
	
	private void saveTasks() {
		try {
			ArrayList<ArrayList<Task>> tasks = new ArrayList<ArrayList<Task>>();
			tasks.add(floatingTasks);
			tasks.add(datedTasks);
			storage.writeTasks(tasks);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
