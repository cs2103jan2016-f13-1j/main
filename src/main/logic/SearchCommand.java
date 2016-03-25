package main.logic;

import java.util.Date;

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
    Date searchDate;
    
    public SearchCommand(Receiver receiver, String searchTerm) {
        this.receiver = receiver;
        this.searchTerm = searchTerm;
    }
    
    public SearchCommand(Receiver receiver, Date searchDate) {
        this.receiver = receiver;
        this.searchDate = searchDate;
    }
    
    public void execute() {
        if (searchTerm != null) {
            receiver.search(searchTerm);
        } else if (searchDate != null) {
            receiver.search(searchDate);
        }
    }
    
    public void undo() {
        receiver.search("");
    }
}
