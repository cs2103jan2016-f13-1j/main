package main.logic;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;

import main.data.Task;
import main.storage.Storage;

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
    
    private ScheduleManager scheduler;
    
    private Receiver() {
        storage = Storage.getInstance();
        scheduler = new ScheduleManager();
        assert(storage != null);
        
        loadFromStorage();
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
     * Use to retrieve all tasks
     * 
     * @return  all tasks
     */
    public ArrayList<Task> getAllTasks() {
        return allTasks;
    }
    
    /**
     * Use to set {@code allTasks} with a new {@code ArrayList}
     * @param   tasks
     *          The new list of all Tasks
     */
    public void setAllTasks(ArrayList<Task> tasks) {
        allTasks = tasks;
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
     * Use to set {@code todoTasks} with a new {@code ArrayList}
     * @param   tasks
     *          The new list of todo Tasks
     */
    public void setTodoTasks(ArrayList<Task> tasks) {
        todoTasks = tasks;
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
     * Use to set {@code completedTasks} with a new {@code ArrayList}
     * @param   tasks
     *          The new list of completed Tasks
     */
    public void setCompletedTasks(ArrayList<Task> tasks) {
        completedTasks = tasks;
    }
    
    public Storage getStorage() {
        return storage;
    }
    
    /**
     * This method returns the current path of the output file.
     * 
     * @return   A {@code String} indicating the file path
     */
    public String getFilePath() {
        return storage.getFilePath();
    }
    
    public String getFileDir() {
        return storage.getFileDir();
    }
    
    public void setFilePath(String path) {
        storage.setFileLocation(path);
        loadFromStorage();
        updateObservers();
    }
    
    private void initializeLists() {
        todoTasks = new ArrayList<Task>();
        completedTasks = new ArrayList<Task>();
    }
    
    public void initiateSave() {
        categorizeTasks(allTasks);
        sortTasks();
        updateCollision();
        saveToStorage();
        updateObservers();
    }
    
    public void updateObservers() {
        setChanged();
        notifyObservers();
    }
    
    private void loadFromStorage() {
        allTasks = storage.readTasks();
        assert(allTasks != null);
        
        initializeLists();
        assert(todoTasks != null);
        assert(completedTasks != null);
        
        categorizeTasks(allTasks);
        sortTasks();
        updateCollision();
    }
    
    private void categorizeTasks(ArrayList<Task> tasks) {
        todoTasks.clear();
        completedTasks.clear();
        
        for (Task task : tasks) {
            if (task.isDone()) {
                completedTasks.add(task);
            } else {
                todoTasks.add(task);
            }
        }
    }
    
    private void sortTasks() {
        logger.log(Level.INFO,"Sorting tasks");
        Collections.sort(todoTasks, new TodoTaskComparator());
        Collections.sort(completedTasks, new CompletedTaskComparator());
        logger.log(Level.INFO,"Tasks sorted");
    }
    
    private void updateCollision() {
        scheduler.updateTodoCollision(todoTasks);
        scheduler.updateCompletedCollision(completedTasks);
    }
    
    private void saveToStorage() {
        logger.log(Level.INFO,"Saving tasks: " + allTasks);
        storage.writeTasks(allTasks);
    }
}
