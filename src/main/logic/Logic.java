/**
 * Summary of public methods that can be called:
 * 
 * Controller();
 * parseCommand(String userCommand, ListType type);
 * editTask(int index);
 * executeCommand();
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
	public void editTask(int index) {
	    int arrayIndex = index - 1;
	    assert(command != null);
	    ListType type = Enum.valueOf(ListType.class, command.getListType());
	    command.setCommandType(Command.Type.EDIT);
	    command.setPreviousTasks(new ArrayList<Task>());
	    command.getPreviousTasks().add(getTaskAtIndex(type,arrayIndex));
	    command.setIndexes(new ArrayList<Integer>());
	    command.getIndexes().add(index);
	    
	    assert(command.getTask() != null);
		deleteTask(command.getIndexes(),type);
		addTask(command.getTask(),type);
		
		command.setPreviousListType(type.name());
		logger.log(Level.INFO,"Edited task at index " + index + " to " + command.getTask().getTitle());
		sortTasks();
		saveTasks();
		addToHistory();
	}
	
	/**
	 * Executes the last command stored when parseCommand was called,
	 * and stores it in a {@code Command} history stack.
	 * 
	 * Used for add and delete operations.
	 */
	public String executeCommand() {
	    String feedback = null;
	    assert(command != null);
	    
	    ListType type = Enum.valueOf(ListType.class, command.getListType());
	    switch (command.getCommandType()) {
            case ADD:
                addTask(command.getTask(),type);
                feedback = "Task added!";
                break;
            case DELETE:
                deleteTask(command.getIndexes(),type);
                feedback = "Task deleted!";
                break;
            case DONE:
                markTask(command.getIndexes(),true, type);
                feedback = "Marked " + command.getIndexes().size() + " tasks as completed";
                break;
            case UNDONE:
                markTask(command.getIndexes(),false, type);
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
	    ArrayList<Integer> indexArray = null;
	    String indexes = null;
	    
	    command = parser.parse(userCommand);
	    assert(command != null);
	    command.setListType(type.name());
	    
		switch (command.getCommandType()) {
            case ADD:
                feedback = command.getTask().toString();
                break;
            case DELETE:
                indexArray = command.getIndexes();
                
                if (indexArray.size() == 1) {
                    int arrayIndex = indexArray.get(0) - 1;
                    feedback = getTaskAtIndex(type, arrayIndex).getTitle();
                } else {
                    String message = userCommand.toLowerCase();
                    String del = "del ";
                    String delete = "delete ";
                    if (message.contains(del)) {
                        indexes = message.substring(del.length());
                    } else if (message.contains("delete")) {
                        indexes = message.substring(delete.length());
                    }
                    feedback = indexes + " " + "(" + indexArray.size() + " tasks)";
                }
                break;
            case DONE:
                indexArray = command.getIndexes();
                
                if (indexArray.size() == 1) {
                    int arrayIndex = indexArray.get(0) - 1;
                    feedback = getTaskAtIndex(type, arrayIndex).getTitle();
                } else {
                    String message = userCommand.toLowerCase();
                    String done = "done ";
                    if (message.contains(done)) {
                        indexes = message.substring(done.length());
                    } 
                    feedback = indexes + " " + "(" + indexArray.size() + " tasks)";
                }
                break;
            case UNDONE:
                indexArray = command.getIndexes();
                
                if (indexArray.size() == 1) {
                    feedback = getTaskAtIndex(type, indexArray.get(0)).getTitle();
                } else {
                    String message = userCommand.toLowerCase();
                    String undone = "undone ";
                    if (message.contains(undone)) {
                        indexes = message.substring(undone.length());
                    } 
                    feedback = indexes + " " + "(" + indexArray.size() + " tasks)";
                }
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
                editTask(command.getIndexes().get(0));
                break;
            case DELETE:
                deleteTask(command.getIndexes(),Enum.valueOf(ListType.class, command.getListType()));
                break;
            case DONE:
                markTask(command.getIndexes(),true, Enum.valueOf(ListType.class, command.getListType()));
                break;
            case UNDONE:
                markTask(command.getIndexes(),false, Enum.valueOf(ListType.class, command.getListType()));
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
                deleteFromList(ListType.ALL, undoCommand.getTask());
				break;
			case EDIT:
			    undoCommand.setListType(type.name());
                redoHistory.push(undoCommand);
			    Task previousTask = undoCommand.getPreviousTasks().get(0);
			    deleteFromList(type, undoCommand.getTask());
		        addTask(previousTask, type);
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
			    previousTasks = undoCommand.getPreviousTasks();
			    for (int i = 0; i < previousTasks.size(); i++) {
                    markFromList(ListType.COMPLETED, previousTasks.get(i), false);
                }
			    break;
			case UNDONE:
			    redoHistory.push(undoCommand);
                previousTasks = undoCommand.getPreviousTasks();
                for (int i = 0; i < previousTasks.size(); i++) {
                    markFromList(ListType.COMPLETED, previousTasks.get(i), true);
                }
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

	private void deleteFromList(ListType type, Task task) {
        ArrayList<Task> tasks = new ArrayList<Task>();
        
        switch (type) {
            case ALL:
                tasks = allTasks;
                break;
            case COMPLETED:
                tasks = completedTasks;
                break;
            default:
                break;
        }
        
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            if (t.compareTo(task) == 0) {
                tasks.remove(i);
                i--;
            }
        }
    }
	
	private void deleteTask(ArrayList<Integer> indexes, ListType type) {
		switch (type) {
		    case ALL:
		        deleteTasksFromList(allTasks, indexes);
				break;
		    case COMPLETED:
		        deleteTasksFromList(completedTasks, indexes);
		        break;
			default:
				break;
		}
	}
	
	private void deleteTasksFromList(ArrayList<Task> tasks, ArrayList<Integer> indexes) {
        int j = 0;
        ArrayList<Task> previousTasks = new ArrayList<Task>();
        for (int i = 0; i < indexes.size(); i++) {
            int indexToDelete = indexes.get(i-j);
            indexToDelete--;
            Task removedTask = tasks.remove(indexToDelete);
            previousTasks.add(removedTask);
            j++;
        }
        assert(command != null);
        command.setPreviousTasks(previousTasks);
    }
	
	private Task getTaskAtIndex(ListType type, int index) {
        Task task = null;
        
        switch (type) {
            case ALL:
                task = allTasks.get(index);
                break;
            case COMPLETED:
                task = completedTasks.get(index);
                break;
            default:
                break;
        }
        return task;
    }
	
	private void markFromList(ListType type, Task task, boolean status) {
        ArrayList<Task> tasks = new ArrayList<Task>();
        
        switch (type) {
            case ALL:
                tasks = allTasks;
                break;
            case COMPLETED:
                tasks = completedTasks;
                break;
            default:
                break;
        }
        
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            if (t.getTitle().equals(task.getTitle())) {
                t.setDone(status);
            }
        }
    }
	
	private void markTask(ArrayList<Integer> indexes, boolean status, ListType type) {
        switch (type) {
            case ALL:
                markTasksFromList(allTasks, indexes, status);
                break;
            case COMPLETED:
                markTasksFromList(completedTasks, indexes, status);
                break;    
            default:
                break;
        }
    }
	
	private void markTasksFromList(ArrayList<Task> tasks, ArrayList<Integer> indexes, boolean status) {
        ArrayList<Task> previousTasks = new ArrayList<Task>();
        for (int i = 0; i < indexes.size(); i++) {
            int indexToMark = indexes.get(i);
            indexToMark--;
            Task task = tasks.get(indexToMark);
            task.setDone(status);
            
            if (status == true) {
                task.setIsCompleted();
            } else {
                task.setNotCompleted();
            }
            
            previousTasks.add(task);
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
