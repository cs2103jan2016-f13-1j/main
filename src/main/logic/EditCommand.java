package main.logic;
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
        receiver.edit(oldTask, newTask);
    }
    
    public void undo() {
        receiver.edit(newTask, oldTask);
    }
}
