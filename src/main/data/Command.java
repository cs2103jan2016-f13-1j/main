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
    
    public static enum Type {
        ADD, EDIT, DELETE, DONE
    }
    
    private Type commandType = null;
    private String tab = null;
    private Task task = null;
    private String previousTab = null;
    private ArrayList<Task> previousTasks = new ArrayList<Task>();
    private ArrayList<Integer> indexes = new ArrayList<Integer>();
    
    public Command(Type commandType, Task task) {
        this.commandType = commandType;
        this.task = task;
    }
    
    public Command(Type commandType, ArrayList<Integer> indexes) {
        this.commandType = commandType;
        this.indexes = indexes;
    }
    public Type getCommandType() {
        return commandType;
    }
    
    public void setCommandType(Type commandType) {
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
    
    public void setIndexes(ArrayList<Integer> indexes) {
        this.indexes = indexes;
    }

    public ArrayList<Task> getPreviousTasks() {
        return previousTasks;
    }

    public void setPreviousTasks(ArrayList<Task> previousTasks) {
        this.previousTasks = previousTasks;
    }

    public String getPreviousTab() {
        return previousTab;
    }

    public void setPreviousTab(String previousTab) {
        this.previousTab = previousTab;
    }
}