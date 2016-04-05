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
public class PriorityCommand implements Command {
    Receiver receiver;
    Task task;
    
    public PriorityCommand(Receiver receiver, Task task) {
        this.receiver = receiver;
        this.task = task;
    }
    
    public void execute() {
        priority(task, true);
    }
    
    public void undo() {
        priority(task, false);
    }
    
    /**
     * This method cycles the priority of the task. The priority increases/decreases
     * respectively according to the true/false boolean {@code increase}.
     * @param   task
     *          The {@code Task} to have its priority modified.
     * @param   increase
     *          The {@code boolean} to indicate an increase or decrease in priority.
     */
    private void priority(Task task, boolean increase) {
        ArrayList<Task> allTasks = receiver.getAllTasks();
        
        for (Task t : allTasks) {
            if (t.equals(task)) {
                t.togglePriority(increase);
            }
        }
        
        receiver.setAllTasks(allTasks);
        receiver.initiateSave();
    }
}
