//@@author A0134234R

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
public class DoneCommand implements Command {
    Receiver receiver;
    Task task;
    ArrayList<Task> tasks;
    
    public DoneCommand(Receiver receiver, Task task) {
        this.receiver = receiver;
        this.task = task;
    }
    
    public DoneCommand(Receiver receiver, ArrayList<Task> tasks) {
        this.receiver = receiver;
        this.tasks = new ArrayList<Task>();
        this.tasks.addAll(tasks);
    }
    
    /**
     * This method allows you to mark a single or multiple {@code Task} from the 
     * {@code ArrayList} of {@code Task} in memory as completed.
     */
    public void execute() {
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
    
    public void undo() {
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
}
