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
public class AddCommand implements Command {
    Receiver receiver;
    Task task;
    
    public AddCommand(Receiver receiver, Task task) {
        this.receiver = receiver;
        this.task = task;
    }
    
    /**
     * This method allows you to add a {@code Task} to the 
     * {@code ArrayList} of {@code Task} in memory.
     */   
    public void execute() {
        ArrayList<Task> allTasks = receiver.getAllTasks();
        
        allTasks.add(task);
        
        receiver.setAllTasks(allTasks);
        receiver.initiateSave();
    }
    
    public void undo() {
        ArrayList<Task> allTasks = receiver.getAllTasks();
        
        allTasks.remove(task);
        
        receiver.setAllTasks(allTasks);
        receiver.initiateSave();
    }
}
