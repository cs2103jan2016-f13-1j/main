package main.logic;
import java.util.ArrayList;

import main.data.Task;

/**
 * 
 */

/**
 * @author Bevin Seetoh Jia Jin
 *
 */
public class UndoneCommand implements Command {
    Receiver receiver;
    Task task;
    ArrayList<Task> tasks;
    
    public UndoneCommand(Receiver receiver, Task task) {
        this.receiver = receiver;
        this.task = task;
    }
    
    public UndoneCommand(Receiver receiver, ArrayList<Task> tasks) {
        this.receiver = receiver;
        this.tasks = tasks;
    }
    
    /**
     * This method allows you to mark a single or multiple {@code Task} from the 
     * {@code ArrayList} of {@code Task} in memory as incomplete.
     */
    public void execute() {
        ArrayList<Task> allTasks = receiver.getAllTasks();
        
        if (task != null) {
            for (Task t : allTasks) {
                if (t.equals(task)) {
                    t.setNotCompleted();
                }
            }
        } else if (tasks != null){
            for (Task t1 : allTasks) {
                for (Task t2 : tasks) {
                    if (t1.equals(t2)) {
                        t1.setNotCompleted();
                    }
                }
            }
        }
        
        receiver.setAllTasks(allTasks);
        receiver.initiateSave();
    }
    
    public void undo() {
        ArrayList<Task> allTasks = receiver.getAllTasks();
        
        if (task != null) {
            for (Task t : allTasks) {
                if (t.equals(task)) {
                    t.setIsCompleted();
                    break;
                }
            }
        } else if (tasks != null){
            for (Task t1 : allTasks) {
                for (Task t2 : tasks) {
                    if (t1.equals(t2)) {
                        t1.setIsCompleted();
                    }
                }
            }
        }
        
        receiver.setAllTasks(allTasks);
        receiver.initiateSave();
    }
}
