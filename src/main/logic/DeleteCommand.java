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
        this.tasks = tasks;
    }
    
    public void execute() {
        if (task != null) {
            receiver.delete(task);
        } else if (tasks != null){
            receiver.delete(tasks);
        }
    }
    
    public void undo() {
        if (task != null) {
            ArrayList<Task> allTasks = receiver.getAllTasks();
            allTasks.add(task);
            receiver.setAllTasks(allTasks);
        } else if (tasks != null){
            ArrayList<Task> allTasks = receiver.getAllTasks();
            for (Task t : tasks) {
                allTasks.add(t);
            }
            receiver.setAllTasks(allTasks);
        }
    }
    
    public void redo() {
        execute();
    }
}
