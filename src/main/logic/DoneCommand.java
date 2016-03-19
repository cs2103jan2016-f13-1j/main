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
        this.tasks = tasks;
    }
    
    public void execute() {
        if (task != null) {
            receiver.done(task);
        } else if (tasks != null){
            receiver.done(tasks);
        }
    }
    
    public void undo() {
        if (task != null) {
            receiver.undone(task);
        } else if (tasks != null){
            receiver.undone(tasks);
        }
    }
    
    public void redo() {
        execute();
    }
}
