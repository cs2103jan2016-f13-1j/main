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
    
    public void execute() {
        receiver.add(task);
    }
    
    public void undo() {
        ArrayList<Task> allTasks = receiver.getAllTasks();
        allTasks.remove(task);
        receiver.setAllTasks(allTasks);
    }
    
    public void redo() {
        execute();
    }
}
