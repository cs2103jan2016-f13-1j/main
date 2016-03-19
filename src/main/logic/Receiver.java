package main.logic;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    
    Storage storage;
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
     * Replaces current task list with given task list
     * 
     * @param  new list of tasks
     */
    public void setAllTasks(ArrayList<Task> tasks) {
        allTasks = tasks;
        initiateSave();
    }
    
    public void save() {
        initiateSave();
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
        Collections.sort(todoTasks, new Comparator<Task>() {
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
    
    private void saveToStorage() {
        try {
            logger.log(Level.INFO,"Saving tasks");
            storage.writeTasks(allTasks);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
