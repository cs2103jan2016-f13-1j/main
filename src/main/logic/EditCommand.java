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
public class EditCommand implements Command {
    Receiver receiver;
    Task oldTask;
    Task newTask;
    
    public EditCommand(Receiver receiver, Task oldTask, Task newTask) {
        this.receiver = receiver;
        this.oldTask = oldTask;
        this.newTask = newTask;
    }
    
    public void execute() {
        edit(oldTask, newTask);
    }
    
    public void undo() {
        edit(newTask, oldTask);
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
    private void edit(Task oldTask, Task newTask) {
        ArrayList<Task> allTasks = receiver.getAllTasks();
        
        allTasks.remove(oldTask);
        allTasks.add(newTask);
        
        receiver.setAllTasks(allTasks);
        receiver.initiateSave();
    }
}
