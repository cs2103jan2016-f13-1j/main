package main.logic;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;

import main.storage.Storage;
import main.data.Task;

/**
 * This is a singleton class
 */

/**
 * @author Bevin Seetoh Jia Jin
 *
 */
public class Receiver extends Observable {
    
    private static final Logger logger = Logger.getLogger(Receiver.class.getName());
    
    private static Receiver receiver;
    
    private Storage storage;
    private ArrayList<Task> allTasks;
    private ArrayList<Task> todoTasks;
    private ArrayList<Task> completedTasks;
    
    private Receiver() {
        storage = Storage.getInstance();
        assert(storage != null);
        
        allTasks = storage.readTasks();
        assert(allTasks != null);
        categorizeTasks(allTasks);
    }
    
    /**
     * A static method to initialize an instance of the {@code Receiver} class.
     * 
     * @return   An instance of the {@code Receiver} class
     */
    public static synchronized Receiver getInstance() {
        if (receiver == null) {
            receiver = new Receiver();
        }
        assert(receiver != null);
        return receiver;
    }
    
    /**
     * Prevents attempts to clone this singleton class.
     */
    public Object clone() throws CloneNotSupportedException {
        logger.log(Level.WARNING, "Clone not supported. This is a singleton class.");
        throw new CloneNotSupportedException();
    }
    
    /**
     * This method allows you to add a {@code task} to the 
     * {@code ArrayList} of {@code Task} in memory.
     * 
     * @param   task
     *          The {@code task} to add
     */
    public void add(Task task) {
        logger.log(Level.INFO, "add command");
        allTasks.add(task);
        initiateSave();
    }
    
    /**
     * This method allows you to edit a {@code task} to a new {@code task} 
     * in the {@code ArrayList} of {@code Task} in memory.
     * 
     * @param   oldTask
     *          {@code Task} to be replaced remove
     * @param   newTask
     *          {@code Task} new task to be added
     */
    public void edit(Task oldTask, Task newTask) {
        logger.log(Level.INFO, "edit command");
        allTasks.remove(oldTask);
        allTasks.add(newTask);
        initiateSave();
    }
    
    /**
     * This method allows you to delete a {@code task} from the 
     * {@code ArrayList} of {@code Task} in memory.
     * 
     * @param   task
     *          The {@code task} to delete
     */
    public void delete(Task task) {
        logger.log(Level.INFO, "delete command");
        allTasks.remove(task);
        initiateSave();
    }
    
    /**
     * This method allows you to delete multiple {@code task} from the 
     * {@code ArrayList} of {@code Task} in memory.
     * 
     * @param   tasks
     *          The {@code ArrayList} of {@code task} to delete
     */
    public void delete(ArrayList<Task> tasks) {
        logger.log(Level.INFO, "delete multiple command");
        for (Task t : tasks) {
            allTasks.remove(t);
        }
        initiateSave();
    }
    
    /**
     * This method allows you to mark a {@code task} from the 
     * {@code ArrayList} of {@code Task} in memory as completed.
     * 
     * @param   task
     *          The {@code task} to mark as completed
     */
    public void done(Task task) {
        logger.log(Level.INFO, "done command");
        for (Task t : allTasks) {
            if (t.equals(task)) {
                t.setIsCompleted();
            }
        }
        initiateSave();
    }
    
    /**
     * This method allows you to mark multiple {@code task} from the 
     * {@code ArrayList} of {@code Task} in memory as completed.
     * 
     * @param   tasks
     *          The {@code ArrayList} of {@code task} to be marked as completed
     */
    public void done(ArrayList<Task> tasks) {
        logger.log(Level.INFO, "done multiple command");
        for (Task t1 : allTasks) {
            for (Task t2 : tasks) {
                if (t1.equals(t2)) {
                    t1.setIsCompleted();
                }
            }
        }
        initiateSave();
    }
    
    /**
     * This method allows you to mark a {@code task} from the 
     * {@code ArrayList} of {@code Task} in memory as incomplete.
     * 
     * @param   task
     *          The {@code task} to mark as incomplete
     */
    public void undone(Task task) {
        logger.log(Level.INFO, "undone command");
        for (Task t : allTasks) {
            if (t.equals(task)) {
                t.setNotCompleted();
            }
        }
        initiateSave();
    }
    
    /**
     * This method allows you to mark multiple {@code task} from the 
     * {@code ArrayList} of {@code Task} in memory as incomplete.
     * 
     * @param   tasks
     *          The {@code ArrayList} of {@code task} to be marked as incomplete
     */
    public void undone(ArrayList<Task> tasks) {
        logger.log(Level.INFO, "undone multiple command");
        for (Task t1 : allTasks) {
            for (Task t2 : tasks) {
                if (t1.equals(t2)) {
                    t1.setNotCompleted();
                }
            }
        }
        initiateSave();
    }
    
    public void search(String searchTerm) {
        logger.log(Level.INFO, "search command for " + searchTerm);
        String[] searchList = searchTerm.split(" ");
        ArrayList<Task> searchResults = new ArrayList<Task>();
        boolean found = true;
        for (Task task : allTasks) {
            for (String term : searchList) {
                if (!task.getTitle().contains(term)) {
                    found = false;
                }
            }
            if (found) {
                searchResults.add(task);
            }
        }
        categorizeTasks(searchResults);
        sortTasks();
        setChanged();
        notifyObservers();
    }
    
    public void clearSearch() {
        categorizeTasks(allTasks);
        sortTasks();
        setChanged();
        notifyObservers();
    }
    
    /**
     * Use to retrieve all tasks
     * 
     * @return  all tasks
     */
    public ArrayList<Task> getAllTasks() {
        return allTasks;
    }
    
    /**
     * Use to retrieve ToDo tasks
     * 
     * @return  todo tasks
     */
    public ArrayList<Task> getTodoTasks() {
        return todoTasks;
    }
    
    /**
     * Use to retrieve completed tasks
     * 
     * @return  completed tasks
     */
    public ArrayList<Task> getCompletedTasks() {
        return completedTasks;
    }
    
    /**
     * This methods sends an instruction to the {@code Storage} class
     * to update its settings.txt file
     * 
     * @param  fileLocation
     *         The location to save the output file
     */
    public void setFileLocation(String fileLocation) {
        storage.setFileLocation(fileLocation, allTasks);
        initiateSave();
    }
    
    /**
     * This method returns the current path of the output file.
     * 
     * @return   A {@code String} indicating the file path
     */
    public String getFilePath() {
        return storage.getFilePath();
    }
    
    private void initiateSave() {
        categorizeTasks(allTasks);
        sortTasks();
        saveToStorage();
        setChanged();
        notifyObservers();
    }
    
    private void categorizeTasks(ArrayList<Task> tasks) {
        todoTasks = new ArrayList<Task>();
        completedTasks = new ArrayList<Task>();
        
        for (Task task : tasks) {
            if (task.isDone()) {
                completedTasks.add(task);
            } else {
                todoTasks.add(task);
            }
        }
    }
    
    private void sortTasks() {
        Collections.sort(todoTasks, new lastAddedFirst());
        Collections.sort(completedTasks, new LastCompletedFirst());
        logger.log(Level.INFO,"Tasks sorted");
    }
    
    private void saveToStorage() {
        logger.log(Level.INFO,"Saving tasks: " + allTasks);
        storage.writeTasks(allTasks);
    }
}
