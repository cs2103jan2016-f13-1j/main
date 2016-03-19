package main.logic;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import main.storage.Storage;
import main.data.Task;

/**
 * 
 */

/**
 * @author Bevin Seetoh Jia Jin
 *
 */
public class Receiver {
    
    private static final Logger logger = Logger.getLogger(Receiver.class.getName());
    
    private static Receiver receiver;
    
    private Storage storage;
    private ArrayList<Task> allTasks;
    private ArrayList<Task> todoTasks;
    private ArrayList<Task> completedTasks;
    
    private Receiver() {
        storage = Storage.getStorage();
        assert(storage != null);
        
        allTasks = storage.readTasks();
        assert(allTasks != null);
        categorizeTasks();
    }
    
    public static synchronized Receiver getReceiver() {
        if (receiver == null) {
            receiver = new Receiver();
        }
        assert(receiver != null);
        return receiver;
    }
    
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
    
    public void add(Task task) {
        logger.log(Level.INFO, "add command");
        allTasks.add(task);
        initiateSave();
    }
    
    public void edit(Task oldTask, Task newTask) {
        logger.log(Level.INFO, "edit command");
        allTasks.remove(oldTask);
        allTasks.add(newTask);
        initiateSave();
    }
    
    public void delete(Task task) {
        logger.log(Level.INFO, "delete command");
        allTasks.remove(task);
        initiateSave();
    }
    
    public void delete(ArrayList<Task> tasks) {
        logger.log(Level.INFO, "delete multiple command");
        for (Task t : tasks) {
            allTasks.remove(t);
        }
        initiateSave();
    }
    
    public void done(Task task) {
        logger.log(Level.INFO, "done command");
        for (Task t : allTasks) {
            if (t.equals(task)) {
                t.setIsCompleted();
            }
        }
        initiateSave();
    }
    
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
    
    public void undone(Task task) {
        logger.log(Level.INFO, "undone command");
        for (Task t : allTasks) {
            if (t.equals(task)) {
                t.setNotCompleted();
            }
        }
        initiateSave();
    }
    
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

    /**
     * Use to retrieve all tasks
     * 
     * @return  all tasks
     */
    public ArrayList<Task> getAllTasks() {
        return allTasks;
    }
    
    /**
     * Use to retrieve todo tasks
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
    
    public void setFileLocation(String fileLocation) {
        storage.setFileLocation(fileLocation);
        initiateSave();
    }
    
    public String getFileLocation() {
        return storage.getFileLocation();
    }
    
    private void initiateSave() {
        categorizeTasks();
        sortTasks();
        saveToStorage();
    }
    
    private void categorizeTasks() {
        todoTasks = new ArrayList<Task>();
        completedTasks = new ArrayList<Task>();
        
        for (Task task : allTasks) {
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
        try {
            logger.log(Level.INFO,"Saving tasks: " + allTasks);
            storage.writeTasks(allTasks);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
