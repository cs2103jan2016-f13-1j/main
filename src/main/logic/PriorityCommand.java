package main.logic;

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
        receiver.priority(task, true);
    }
    
    public void undo() {
        receiver.priority(task, false);
    }
}
