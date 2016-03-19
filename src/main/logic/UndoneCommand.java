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
    
    public void execute() {
        if (task != null) {
            receiver.undone(task);
        } else if (tasks != null){
            receiver.undone(tasks);
        }
    }
    
    public void undo() {
        if (task != null) {
            receiver.done(task);
        } else if (tasks != null){
            receiver.done(tasks);
        }
    }
    
    public void redo() {
        execute();
    }
}
