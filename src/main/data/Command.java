package main.data;

import java.util.ArrayList;

/**
 * @author Joleen
 *
 */

public class Command {
    
    public static enum Type {
        ADD, EDIT, DELETE, DONE, UNDONE
    }
    
    private Type commandType = null;
    private Task task = null;
    private ArrayList<Integer> indexes = new ArrayList<Integer>();
    private String listType = null;
    private String previousListType = null;
    private ArrayList<Task> previousTasks = new ArrayList<Task>();
    
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
    
    public ArrayList<Integer> getIndexes() {
        return indexes;
    }
    
    public void setIndexes(ArrayList<Integer> indexes) {
        this.indexes = indexes;
    }
    
    public String getListType() {
        return listType;
    }
    
    public void setListType(String listType) {
        this.listType = listType;
    }
    
    public String getPreviousListType() {
        return previousListType;
    }

    public void setPreviousListType(String previousListType) {
        this.previousListType = previousListType;
    }

    public ArrayList<Task> getPreviousTasks() {
        return previousTasks;
    }

    public void setPreviousTasks(ArrayList<Task> previousTasks) {
        this.previousTasks = previousTasks;
    }
}