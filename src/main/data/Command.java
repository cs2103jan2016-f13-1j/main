/**
 * 
 */
package main.data;

/**
 * @author Joleen
 *
 */
import java.util.ArrayList;

public class Command {
    
    public static final String FLOATING_TAB = "floating";
    public static final String DATED_TAB = "dated";
    
    private String commandType = null;
    private String tab = null;
    private Task task = null;
    private ArrayList<Task> previousTasks = new ArrayList<Task>();
    private ArrayList<Integer> indexes = new ArrayList<Integer>();
    
    public Command(String commandType, String tab, Task task) {
        this.commandType = commandType;
        this.tab = tab;
        this.task = task;
    }
    
    public Command(String commandType, ArrayList<Integer> indexes) {
        this.commandType = commandType;
        this.indexes = indexes;
    }
    public String getCommandType() {
        return commandType;
    }
    
    public void setCommandType(String commandType) {
    	this.commandType = commandType;
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
        return indexes;
    }

    public ArrayList<Task> getPreviousTasks() {
        return previousTasks;
    }

    public void setPreviousTasks(ArrayList<Task> previousTasks) {
        this.previousTasks = previousTasks;
    }
}