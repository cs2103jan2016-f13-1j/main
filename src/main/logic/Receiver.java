package main.logic;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
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
        
        allTasks = storage.readTasks();
        assert(allTasks != null);
        
        initializeLists();
        assert(todoTasks != null);
        assert(completedTasks != null);
        
        categorizeTasks(allTasks);
        sortTasks();
        updateCollision();
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
     * This method allows you to add a {@code Task} to the 
     * {@code ArrayList} of {@code Task} in memory.
     * 
     * @param   task
     *          The {@code Task} to add
     */
    public void add(Task task) {
        logger.log(Level.INFO, "add command");
        allTasks.add(task);
        initiateSave();
    }
    
    /**
     * This method allows you to edit a {@code Task} to a new {@code Task} 
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
     * This method allows you to delete a {@code Task} from the 
     * {@code ArrayList} of {@code Task} in memory.
     * 
     * @param   task
     *          The {@code Task} to delete
     */
    public void delete(Task task) {
        logger.log(Level.INFO, "delete command");
        allTasks.remove(task);
        initiateSave();
    }
    
    /**
     * This method allows you to delete multiple {@code Task} from the 
     * {@code ArrayList} of {@code Task} in memory.
     * 
     * @param   tasks
     *          The {@code ArrayList} of {@code Task} to delete
     */
    public void delete(ArrayList<Task> tasks) {
        logger.log(Level.INFO, "delete multiple command");
        for (Task t : tasks) {
            allTasks.remove(t);
        }
        initiateSave();
    }
    
    /**
     * This method allows you to mark a {@code Task} from the 
     * {@code ArrayList} of {@code Task} in memory as completed.
     * 
     * @param   task
     *          The {@code Task} to mark as completed
     */
    public void done(Task task) {
        logger.log(Level.INFO, "done command");
        for (Task t : allTasks) {
            if (t.equals(task)) {
                t.setIsCompleted();
                break;
            }
        }
        initiateSave();
    }
    
    /**
     * This method allows you to mark multiple {@code Task} from the 
     * {@code ArrayList} of {@code Task} in memory as completed.
     * 
     * @param   tasks
     *          The {@code ArrayList} of {@code Task} to be marked as completed
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
     * This method allows you to mark a {@code Task} from the 
     * {@code ArrayList} of {@code Task} in memory as incomplete.
     * 
     * @param   task
     *          The {@code Task} to mark as incomplete
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
     * This method allows you to mark multiple {@code Task} from the 
     * {@code ArrayList} of {@code Task} in memory as incomplete.
     * 
     * @param   tasks
     *          The {@code ArrayList} of {@code Task} to be marked as incomplete
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
    
    /**
     * This method allows you to search for tasks that has description
     * or label found in the given {@code searchTerm}.
     * 
     * @param   searchTerm
     *          The {@code String} of terms to search which are separated by spaces.
     */
    public void search(String searchTerm) {
        logger.log(Level.INFO, "search command for term " + searchTerm);
        String[] searchList = searchTerm.split(" ");
        ArrayList<Task> searchResults = new ArrayList<Task>();
        
        for (Task t : allTasks) {
            boolean found = true;
            for (String term : searchList) {
                if (term.contains("#")) {
                    if (t.getLabel() == null) {
                        found = false;
                    } else if (!("#" + t.getLabel()).toLowerCase().contains(term.toLowerCase())) {
                        found = false;
                    }
                } else if (!t.getTitle().toLowerCase().contains(term.toLowerCase())) {
                    found = false;
                }
            }
            if (found) {
                searchResults.add(t);
            }
        }
        categorizeTasks(searchResults);
        sortTasks();
        setChanged();
        notifyObservers();
    }
    
    /**
     * This method allows you to search for tasks by date which have the
     * same date as the given {@code searchDate}. Not time specific.
     * 
     * @param   searchDate
     *          The {@code Date} search.
     */
    public void search(Date searchDate) {
        logger.log(Level.INFO, "search command for date " + searchDate);
        ArrayList<Task> searchResults = new ArrayList<Task>();
        Calendar dateToSearch = convertDate(searchDate);
        
        for (Task t : allTasks) {
            if (t.hasDateRange()) {
                Calendar startDate = convertDate(t.getStartDate());
                Calendar endDate = convertDate(t.getEndDate());
                if (startDate.equals(dateToSearch) || endDate.equals(dateToSearch)) {
                    searchResults.add(t);
                }
            } else if (t.hasSingleDate()) {
                Calendar singleDate = convertDate(t.getSingleDate());
                if (singleDate.equals(dateToSearch)) {
                    searchResults.add(t);
                }
            }
        }
        categorizeTasks(searchResults);
        sortTasks();
        setChanged();
        notifyObservers();
    }
    
    //Removes all time details from the given Date
    private Calendar convertDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }
    
    /**
     * This method cycles the priority of the task. The priority increases/decreases
     * respectively according to the true/false boolean {@code increase}.
     * @param   task
     *          The {@code Task} to have its priority modified.
     * @param   increase
     *          The {@code boolean} to indicate an increase or decrease in priority.
     */
    public void priority(Task task, boolean increase) {
        for (Task t : allTasks) {
            if (t.equals(task)) {
                t.togglePriority(increase);
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
    
    private void initializeLists() {
        todoTasks = new ArrayList<Task>();
        completedTasks = new ArrayList<Task>();
    }
    
    private void initiateSave() {
        categorizeTasks(allTasks);
        sortTasks();
        updateCollision();
        saveToStorage();
        setChanged();
        notifyObservers();
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
        Collections.sort(todoTasks, new TodoTaskComparator());
        Collections.sort(completedTasks, new CompletedTaskComparator());
        logger.log(Level.INFO,"Tasks sorted");
    }
    
    private void updateCollision() {
        Task previousTask;
        Task currentTask;
        Task nextTask;
        
        for (int i = 0; i < todoTasks.size(); i++) {
            currentTask = todoTasks.get(i);
            previousTask = null;
            nextTask = null;
            
            if (todoTasks.size() > 1) {
                if (i == 0) {
                    nextTask = todoTasks.get(i + 1);
                } else if (i == (todoTasks.size() - 1)) {
                    previousTask = todoTasks.get(i - 1);
                } else {
                    previousTask = todoTasks.get(i - 1);
                    nextTask = todoTasks.get(i + 1);
                }
            }
            scheduler.updateCollision(previousTask, currentTask, nextTask);
        }
    }
    
    private void saveToStorage() {
        logger.log(Level.INFO,"Saving tasks: " + allTasks);
        storage.writeTasks(allTasks);
    }
}
