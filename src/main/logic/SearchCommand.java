package main.logic;

/**
 * 
 */

/**
 * @author Bevin Seetoh Jia Jin
 *
 */
public class SearchCommand implements Command {
    Receiver receiver;
    String searchTerm;
    
    public SearchCommand(Receiver receiver, String searchTerm) {
        this.receiver = receiver;
        this.searchTerm = searchTerm;
    }
    
    public void execute() {
        receiver.search(searchTerm);
    }
    
    public void undo() {
        receiver.clearSearch();
    }
}
