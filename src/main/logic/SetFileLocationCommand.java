package main.logic;

/**
 * 
 */

/**
 * @author Bevin Seetoh Jia Jin
 *
 */
public class SetFileLocationCommand implements Command {
    Receiver receiver;
    String oldLocation;
    String newLocation;
    
    public SetFileLocationCommand(Receiver receiver, String newLocation) {
        this.receiver = receiver;
        this.oldLocation = receiver.getFileLocation();
        this.newLocation = newLocation;
    }
    
    public void execute() {
        receiver.setFileLocation(newLocation);
    }
    
    public void undo() {
        receiver.setFileLocation(oldLocation);
    }
    
    public void redo() {
        execute();
    }
}
