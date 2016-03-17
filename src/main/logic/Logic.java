/**
 * Summary of public methods that can be called:
 * 
 * Controller();
 * parseCommand(String userCommand, ListType type);
 * editTask(Task task);
 * executeCommand();
 * executeCommand(Task task);
 * executeCommand(ArrayList<Task> tasks);
 * undo();
 * redo();
 * 
 * getAllTasks();
 * getCompletedTasks();
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
import java.util.Comparator;
import java.util.EmptyStackException;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ocpsoft.prettytime.shade.edu.emory.mathcs.backport.java.util.Collections;

import main.data.Command;
import main.data.Task;
import main.parser.CommandParser;
import main.storage.Storage;

public class Logic {
    
    public static enum ListType {
	    ALL, COMPLETED
	}
	
    private static final Logger logger = Logger.getLogger(Logic.class.getName());
    
	private static Logic logic;
	
	private static final int ALL_TASKS_INDEX = 0;
	private static final int COMPLETED_TASKS_INDEX = 1;
	
	private CommandParser parser = null;
	
	private Storage storage = null;
	private Command command = null;
	private Stack<Command> undoHistory = new Stack<Command>();
	private Stack<Command> redoHistory = new Stack<Command>();
    private ArrayList<Task> allTasks = new ArrayList<Task>();
    
    private ArrayList<Task> completedTasks = new ArrayList<Task>();
    
    /**
	 * Initializes a newly created {@code Controller} object.
	 */
    private Logic() {
        parser = new CommandParser();
        storage = Storage.getStorage();
        assert(storage != null);
        
        ArrayList<ArrayList<Task>> tasksFromStorage = storage.readTasks();
        allTasks = tasksFromStorage.get(ALL_TASKS_INDEX);
        completedTasks = tasksFromStorage.get(COMPLETED_TASKS_INDEX);
        assert(allTasks != null);
        assert(completedTasks != null);
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
	public void editTask(Task task) {
	    assert(command != null);
	    ListType type = Enum.valueOf(ListType.class, command.getListType());
	    command.setCommandType(Command.Type.EDIT);
	    assert(command.getTask() != null);
	    
	    replaceTask(task,command.getTask(),type);
		
		command.setPreviousListType(type.name());
		logger.log(Level.INFO,"Edited task " + task.getTitle() + " to " + command.getTask().getTitle());
		sortTasks();
		saveTasks();
		addToHistory();
	}
	
	/**
     * Executes the command that is parsed just before this command is called,
     * and stores it in a {@code Command} history stack.
     * 
     * Used for add operations.
     */
	public String executeCommand() {
        String feedback = null;
        assert(command != null);
        
        ListType type = Enum.valueOf(ListType.class, command.getListType());
        if (command.getCommandType().equals(Command.Type.ADD)) {
            addTask(command.getTask(),type);
            feedback = "Task added!";
        }
        logger.log(Level.INFO,"Executed " + command.getCommandType().name() + " command");
        sortTasks();
        saveTasks();
        addToHistory();
        return feedback;
    }
	
	/**
     * Executes the command that is parsed just before this command is called,
     * and stores it in a {@code Command} history stack.
     * 
     * Used for delete/done/undone operations.
     * 
     * @param   task
     *          the task to be deleted/marked/unmarked
     */
	public String executeCommand(Task task) {
	    String feedback = null;
	    assert(command != null);
	    
	    ListType type = Enum.valueOf(ListType.class, command.getListType());
	    switch (command.getCommandType()) {
            case DELETE:
                deleteTask(task);
                feedback = "Task deleted!";
                break;
            case DONE:
                markTask(task, true, type);
                feedback = "Marked " + command.getIndexes().size() + " tasks as completed";
                break;
            case UNDONE:
                markTask(task, false, type);
                feedback = "Marked " + command.getIndexes().size() + " tasks as uncompleted";
                break;
            default:
                break;
	    }
	    logger.log(Level.INFO,"Executed " + command.getCommandType().name() + " command");
	    sortTasks();
	    saveTasks();
		addToHistory();
		return feedback;
	}
	
	/**
     * Executes the command that is parsed just before this command is called,
     * and stores it in a {@code Command} history stack.
     * 
     * Used for delete/done/undone operations for multiple tasks at once.
     * 
     * @param   tasks
     *          the tasks to be deleted/marked/unmarked
     */
	public String executeCommand(ArrayList<Task> tasks) {
        String feedback = null;
        assert(command != null);
        
        switch (command.getCommandType()) {
            case DELETE:
                deleteMultipleTasks(tasks);
                feedback = "Task deleted!";
                break;
            case DONE:
                markMultipleTasks(tasks, true);
                feedback = "Marked " + command.getIndexes().size() + " tasks as done";
                break;
            case UNDONE:
                markMultipleTasks(tasks, false);
                feedback = "Marked " + command.getIndexes().size() + " tasks as undone";
                break;
            default:
                break;
        }
        logger.log(Level.INFO,"Executed " + command.getCommandType().name() + " command");
        sortTasks();
        saveTasks();
        addToHistory();
        return feedback;
    }
	
	/**
	 * Use to retrieve all tasks that are yet to be done
	 * 
	 * @return  the combined list of tasks
	 */
	public ArrayList<Task> getAllTasks() {
		return allTasks;
	}

    /**
	 * Use to retrieve every task which was marked as done, sorted in latest date first order
	 * @return the list of tasks with dates
	 */
	public ArrayList<Task> getCompletedTasks() {
        return completedTasks;
	}

    public String getFileLocation() {
		return storage.getFileLocation();
	}
    
    public int getRedoCount() {
        return redoHistory.size();
    }

	public int getUndoCount() {
	    return undoHistory.size();
	}

    /**
	 * Evaluates the given command and provide feedback
	 * 
	 * @param   userCommand 
	 * 			the command to be evaluated
	 * 
	 * @param   type
	 * 			the List which is related to the command
	 * 
	 * @return	feedback resulting from the evaluation of the command
	 */
	public String parseCommand(String userCommand, ListType type) {
	    String feedback = null;
	    ArrayList<Integer> indexes = null;
	    StringBuilder sb = new StringBuilder();
	    
	    command = parser.parse(userCommand);
	    assert(command != null);
	    command.setListType(type.name());
	    
		switch (command.getCommandType()) {
            case ADD:
                feedback = command.getTask().toString();
                break;
            case DELETE:
            case DONE:
            case UNDONE:
                indexes = command.getIndexes();
                for (int i : indexes) {
                    sb.append(i + " ");
                }
                feedback = sb.toString().trim();
                break;
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
                addTask(command.getTask(),Enum.valueOf(ListType.class, command.getListType()));
                break;
            case EDIT:
                replaceTask(command.getTask(), command.getPreviousTasks().get(0), Enum.valueOf(ListType.class, command.getListType()));
                break;
            case DELETE:
                for (Task task : command.getPreviousTasks()) {
                    deleteTask(task);
                }
                break;
            case DONE:
                markMultipleTasks(command.getPreviousTasks(), true);
                break;
            case UNDONE:
                markMultipleTasks(command.getPreviousTasks(), false);
                break;
            default:
                break;
        }
        logger.log(Level.INFO,"Redone " + command.getCommandType().name() + " command");
        sortTasks();
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
		
		
		ListType type = Enum.valueOf(ListType.class, undoCommand.getListType());
		ArrayList<Task> previousTasks = null;
		
		switch (undoCommand.getCommandType()) {
			case ADD:
			    redoHistory.push(undoCommand);
                deleteTask(undoCommand.getTask());
				break;
			case EDIT:
			    undoCommand.setListType(type.name());
			    Task previousTask = undoCommand.getPreviousTasks().get(0);
			    Task tempTask = replaceTask(undoCommand.getTask(), previousTask, type);
			    undoCommand.setTask(tempTask);
			    redoHistory.push(undoCommand);
                break;
			case DELETE:
			    redoHistory.push(undoCommand);
			    previousTasks = undoCommand.getPreviousTasks();
			    for (int i = 0; i < previousTasks.size(); i++) {
			        addTask(previousTasks.get(i), type);
			    }
				break;
			case DONE:
			    redoHistory.push(undoCommand);
			    markMultipleTasks(undoCommand.getPreviousTasks(), false);
			    break;
			case UNDONE:
			    redoHistory.push(undoCommand);
			    markMultipleTasks(undoCommand.getPreviousTasks(), true);
			default:
				break;
		}
		logger.log(Level.INFO,"Undone " + undoCommand.getCommandType().name() + " command");
		sortTasks();
		saveTasks();
	}

    private void addTask(Task task, ListType type) {
	    switch (type) {
            case ALL:
                allTasks.add(task);
                break;
            case COMPLETED:
                completedTasks.add(task);
                break;
            default:
                break;
	    }
	}

    private void addToHistory() {
        assert(command != null);
        undoHistory.push(command);
		redoHistory = new Stack<Command>();
    }
	
    private Task replaceTask(Task oldTask, Task newTask, ListType type) {
        System.out.println(oldTask + " with " + newTask);
        ArrayList<Task> previousTasks = new ArrayList<Task>();
        for (int i = 0; i < allTasks.size(); i++) {
            Task t = allTasks.get(i);
            if (t.equals(oldTask)) {
                Task removedTask = allTasks.remove(i);
                previousTasks.add(removedTask);
                i--;
            }
        }

        for (int i = 0; i < completedTasks.size(); i++) {
            Task t = completedTasks.get(i);
            if (t.equals(oldTask)) {
                Task removedTask = completedTasks.remove(i);
                previousTasks.add(removedTask);
                i--;
            }
        }
        
        assert(command != null);
        command.setPreviousTasks(previousTasks);   
        
        switch (type) {
            case ALL:
                allTasks.add(newTask);
                break;
            case COMPLETED:
                completedTasks.add(newTask);
                break;
            default:
                break;
        }
        return newTask;
    }
    
	private void deleteTask(Task task) {
	    ArrayList<Task> previousTasks = new ArrayList<Task>();
        for (int i = 0; i < allTasks.size(); i++) {
            Task t = allTasks.get(i);
            if (t.equals(task)) {
                Task removedTask = allTasks.remove(i);
                previousTasks.add(removedTask);
                i--;
            }
        }

        for (int i = 0; i < completedTasks.size(); i++) {
            Task t = completedTasks.get(i);
            if (t.equals(task)) {
                Task removedTask = completedTasks.remove(i);
                previousTasks.add(removedTask);
                i--;
            }
        }
        
        assert(command != null);
        command.setPreviousTasks(previousTasks);   
	}
	
	private void deleteMultipleTasks(ArrayList<Task> tasks) {
        ArrayList<Task> previousTasks = new ArrayList<Task>();
        for (int i = 0; i < allTasks.size(); i++) {
            Task t1 = allTasks.get(i);
            for (Task t2 : tasks) {
                if (t1.equals(t2)) {
                    Task removedTask = allTasks.remove(i);
                    previousTasks.add(removedTask);
                    i--;
                }
            }
        }

        for (int i = 0; i < completedTasks.size(); i++) {
            Task t1 = completedTasks.get(i);
            for (Task t2 : tasks) {
                if (t1.equals(t2)) {
                    Task removedTask = completedTasks.remove(i);
                    previousTasks.add(removedTask);
                    i--;
                }
            }
        }
        
        assert(command != null);
        command.setPreviousTasks(previousTasks);   
    }
	
	private void markTask(Task task, boolean status, ListType type) {
	    ArrayList<Task> previousTasks = new ArrayList<Task>();
	    for (Task t : allTasks) {
	        if (t.equals(task)) {
	            if (status == true) {
	                t.setIsCompleted();
	            } else {
	                t.setNotCompleted();
	            }
	            previousTasks.add(t);
	        }
	    }
	    
	    for (Task t : completedTasks) {
            if (t.equals(task)) {
                if (status == true) {
                    t.setIsCompleted();
                } else {
                    t.setNotCompleted();
                }
                previousTasks.add(t);
            }
        }
	    assert(command != null);
        command.setPreviousTasks(previousTasks);
    }
	
	private void markMultipleTasks(ArrayList<Task> tasks, boolean status) {
	    ArrayList<Task> previousTasks = new ArrayList<Task>();
        for (int i = 0; i < allTasks.size(); i++) {
            Task t1 = allTasks.get(i);
            for (Task t2 : tasks) {
                if (t1.equals(t2)) {
                    if (status == true) {
                        t1.setIsCompleted();
                    } else {
                        t1.setNotCompleted();
                    }
                    previousTasks.add(t1);
                }
            }
        }
        
        for (int i = 0; i < completedTasks.size(); i++) {
            Task t1 = completedTasks.get(i);
            for (Task t2 : tasks) {
                if (t1.equals(t2)) {
                    if (status == true) {
                        t1.setIsCompleted();
                    } else {
                        t1.setNotCompleted();
                    }
                    previousTasks.add(t1);
                }
            }
        }
        assert(command != null);
        command.setPreviousTasks(previousTasks);
    }
	
	private void saveTasks() {
		try {
		    logger.log(Level.INFO,"Saving tasks");
			ArrayList<ArrayList<Task>> tasks = new ArrayList<ArrayList<Task>>();
			tasks.add(allTasks);
			tasks.add(completedTasks);
			storage.writeTasks(tasks);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void sortTasks() {
	    ArrayList<Task> combinedTasks = new ArrayList<Task>();
	    combinedTasks.addAll(allTasks);
	    combinedTasks.addAll(completedTasks);
	    allTasks = new ArrayList<Task>();
	    completedTasks = new ArrayList<Task>();
        
	    //Separate the two types of tasks
	    for (Task task : combinedTasks) {
	        if (task.isDone()) {
	            completedTasks.add(task);
	        } else {
	            allTasks.add(task);
	        }
	    }
	    
	    Collections.sort(allTasks, new Comparator<Task>() {
            public int compare(Task t1, Task t2) {
                if (!t1.hasDate() && !t2.hasDate()) {
                    //If both are floating tasks, compare the titles
                    return t1.getTitle().compareTo(t2.getTitle());
                } else if (!t1.hasDate() && t2.hasDate()) {
                    //If one is floating and the other is dated, return the floating task
                    return -1;
                } else if (t1.hasDate() && !t2.hasDate()) {
                    //If one is floating and the other is dated, return the floating task
                    return 1;
                } else {
                    //Both tasks has date
                    if (t1.hasStarted() && t2.hasStarted()) {
                        if (t1.getEndDate().compareTo(t2.getEndDate()) == 0) {
                            return t1.getTitle().compareTo(t2.getTitle());
                        } else {
                            //If both tasks already started, return earlier deadline first
                            return t1.getEndDate().compareTo(t2.getEndDate());
                        }
                    } else if (!t1.hasStarted() && !t2.hasStarted()) {
                        //If both have not started yet
                        return t1.getEndDate().compareTo(t2.getEndDate());
                    } else if (t1.hasStarted()) {
                        return -1;
                    } else if (t2.hasStarted()) {
                        return 1;
                    } else {
                        System.out.println("FAIL TO COMPARE");
                        return 0;
                    }
                }
            }
        });
	    
	    Collections.sort(completedTasks, new Comparator<Task>() {
            public int compare(Task t1, Task t2) {
                if (t2.getCompletedDate().compareTo(t1.getCompletedDate()) == 0) {
                    //If both tasks are set completed at the same time, compare the titles
                    return t1.getTitle().compareTo(t2.getTitle());
                } else {
                    //Return the tasks that was completed later first
                    return t2.getCompletedDate().compareTo(t1.getCompletedDate());
                }
            }
        });
	    
	    logger.log(Level.INFO,"Tasks sorted");
	}
}
