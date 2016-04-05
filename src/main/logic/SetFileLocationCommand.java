//@@author A0134234R

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
        this.oldLocation = receiver.getFilePath();
        this.newLocation = newLocation;
    }
    
    public void execute() {
        setFileLocation(newLocation);
    }
    
    public void undo() {
        setFileLocation(oldLocation);
    }
    
    /**
     * This methods sends an instruction to the {@code Storage} class
     * to update its settings.txt file
     * 
     * @param  fileLocation
     *         The location to save the output file
     */
    private void setFileLocation(String fileLocation) {
        receiver.setFilePath(fileLocation);
    }
}
