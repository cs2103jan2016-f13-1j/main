/**
 * 
 */
package main.data;

/**
 * @author Joleeen
 *
 */
import java.util.ArrayList;

public class Command {
	
	public static final String FLOATING_TAB = "floating";
	public static final String DATED_TAB = "dated";
	
    private String commandType;
    private String tab;
    private Task task;
    private ArrayList<Integer> indexesToDelete;
    
    public Command(String commandType, String tab, Task task) {
        this.commandType = commandType;
        this.tab = tab;
        this.task = task;
    }
    
    public Command(String commandType, ArrayList<Integer> indexesToDelete) {
        this.commandType = commandType;
        this.indexesToDelete = indexesToDelete;
    }
    public String getCommandType() {
        return commandType;
    }
    
    public Task getTask() {
        return task;
    }
    
    public String getTab() {
        return tab;
    }
    
    public void setTab(String tab) {
        this.tab = tab;
    }
    
    public ArrayList<Integer> getIndexes() {
        return indexesToDelete;
    }
}