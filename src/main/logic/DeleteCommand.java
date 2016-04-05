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
public class DeleteCommand implements Command {
    Receiver receiver;
    Task task;
    ArrayList<Task> tasks;
    
    public DeleteCommand(Receiver receiver, Task task) {
        this.receiver = receiver;
        this.task = task;
    }
    
    public DeleteCommand(Receiver receiver, ArrayList<Task> tasks) {
        this.receiver = receiver;
        this.tasks = new ArrayList<Task>();
        this.tasks.addAll(tasks);
    }
    
    /**
     * This method allows you to delete a single or multiple {@code Task} from the 
     * {@code ArrayList} of {@code Task}.
     */
    public void execute() {
        ArrayList<Task> allTasks = receiver.getAllTasks();
        
        if (task != null) {
            allTasks.remove(task);      
        } else if (tasks != null){
            for (Task task : tasks) {
                allTasks.remove(task);
            }
        }
        
        receiver.setAllTasks(allTasks);
        receiver.initiateSave();
    }
    
    public void undo() {
        ArrayList<Task> allTasks = receiver.getAllTasks();

        if (task != null) {
            allTasks.add(task);
        } else if (tasks != null){
            for (Task task : tasks) {
                allTasks.add(task);
            } 
        }
        
        receiver.setAllTasks(allTasks);
        receiver.initiateSave();
    }
}
