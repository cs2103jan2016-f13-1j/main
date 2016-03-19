package main.logic;

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
        receiver.delete(task);
    }
    
    public void redo() {
        execute();
    }
}
