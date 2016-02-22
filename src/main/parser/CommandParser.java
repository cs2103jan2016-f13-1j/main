package main.parser;

import main.data.Command;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

/**
 * @author Joleeen
 *
 *
 *Currently assumming that labels are added at the very end, with no more information behind.
 *
 */

public class CommandParser {
    
    private static final int LENGTH_DEL = 3;
    private static final int LENGTH_DELETE = 6;
    
    static String commandType;
    static Task task;
    static ArrayList<Integer> deleteIndexes;
    
    public void parse(String commandString) {
        //take in commandString
        //extract first word
        //determine command type
        //set command type
        //according to command, implement diff
        
        String command = getFirstWord(commandString);
        determineCommandType(command);
        commandPreparations(commandType, commandString);
    }
    
    public static String getFirstWord(String commandString) {
        return commandString.split(" ")[0];
    }
    
    public static void determineCommandType(String command) {
        if (command.equalsIgnoreCase("delete") || (command.equalsIgnoreCase("del"))) {
            commandType = "delete";
        } else if (command.equalsIgnoreCase("add")) {
            commandType = "add";
        }
    }
    
    public static void commandPreparations(String type, String commandString) {
        switch (type) {
            case "add" :
                prepareForAdd(commandString);
                break;
                
            case "delete" :
                prepareForDel(commandString);
                break;
                
            default :
                break;
        }
    }
    
    public static void prepareForAdd(String commandString) {
        String title = "";
        ArrayList<String> labels = null;
        Date startDate = null;
        Date endDate = null;
        
        boolean isFloating;
        List<Date> dates = getDate(commandString);
        isFloating = checkIsFloating(dates);
        
        title = commandString;
        
        if (!isFloating) {
            startDate = prepareStartDate(dates);
            
            boolean isDateRanged;
            isDateRanged = checkDateRange(dates);
            
            if (isDateRanged) {
                endDate = prepareEndDate(dates);
            }
        }
        
        boolean isLabelsPresent;
        isLabelsPresent = checkForLabels(commandString);
        if (isLabelsPresent) {
            labels = extractLabels(commandString);
            title = removeLabels(title);
        }
        
        task = buildTask(title, startDate, endDate, labels);
    }
    
    public static List<Date> getDate(String commandString) {
        PrettyTimeParser parser = new PrettyTimeParser();
        List<Date> dates = parser.parse(commandString);
        return dates;
    }
    
    public static boolean checkIsFloating(List<Date> dates) {
        if (dates.size() == 0) {
            return true;
        }
        return false;
    }
    
    public static Date prepareStartDate(List<Date> dates) {
        return dates.get(0);
    }
    
    public static boolean checkDateRange(List<Date> dates) {
        if (dates.size() == 2) {
            return true;
        }
        return false;
    }
    
    public static Date prepareEndDate(List<Date> dates) {
        return dates.get(1);
    }
    
    public static boolean checkForLabels(String commandString) {
        if (commandString.contains("#")) {
            return true;
        }
        return false;
    }
    
    public static ArrayList<String> extractLabels(String commandString) {
        ArrayList<String> labels = new ArrayList<String>();
        int index = commandString.indexOf("#");
        index = index + 1; //offset
        String substring = commandString.substring(index);
        Collections.addAll(labels, substring.split(",[ ]*")); //for spaces
        
        return labels;
    }
    
    public static String removeLabels(String title) {
        int index = title.indexOf("#");
        index = index - 1; //offset
        return title.substring(0, index);
    }
    
    public static Task buildTask(String title, Date startDate, Date endDate, ArrayList<String> labels) {
        Task task = new Task.TaskBuilder(title).setStartDate(startDate).setEndDate(endDate)
        .setLabels(labels).build();
        return task;
    }
    
    public static void prepareForDel(String commandString) {
        String index = retrieveDeleteIndex(commandString);
        String type = checkIndexType(index);
        
        if (type.equals("single")) {
            deleteIndexes.add(Integer.parseInt(index));
        } else {
            deleteIndexes = extractMultipleIndex(index, type);
        }
    }
    
    public static String retrieveDeleteIndex(String commandString) {
        int index = 0;
        String command = getFirstWord(commandString);
        
        if (command.matches("delete")) {
            index = LENGTH_DELETE;
        } else if (command.matches("del")) {
            index = LENGTH_DEL;
        }
        index = index + 1; //offset
        
        commandString = commandString.substring(index, commandString.length());
        commandString = removeWhiteSpace(commandString);
        
        return commandString;
    }
    
    public static String removeWhiteSpace(String string) {
        string = string.replaceAll("\\s","");
        return string;
    }
    
    public static String checkIndexType(String index) {
        String type = "";
        if (index.contains(",")) {
            type = "multiple";
        } else if (index.contains("-")) {
            type = "range";
        } else {
            type = "single";
        }
        
        return type;
    }
    
    public static ArrayList<Integer> extractMultipleIndex(String index, String type) {
        ArrayList<String> indexes = new ArrayList<String>();
        ArrayList<Integer> multipleIndexes = new ArrayList<Integer>();
        ArrayList<Integer> rangedIndexes = new ArrayList<Integer>();
        
        if (type.equals("multiple")) {
            Collections.addAll(indexes, index.split(","));
            for (int i = 0; i < indexes.size(); i++) {
                multipleIndexes.add(Integer.parseInt(indexes.get(i)));
            }
        } else {
            Collections.addAll(indexes, index.split("-"));
            for (int i = 0; i < indexes.size(); i++) {
                rangedIndexes.add(Integer.parseInt(indexes.get(i)));
            }
            
            for (int i = rangedIndexes.get(0); i <= rangedIndexes.get(1); i++) {
                multipleIndexes.add(i);
            }
        }
        return multipleIndexes;
    }
    
    //getters and setters below
    public String getCommandType() {
        return commandType;
    }
    
    public Task getTask() {
        return task;
    }
    
    public ArrayList<Integer> getDeleteIndex() {
        return deleteIndexes;
    }
    
    public static void setCommandType(String type) {
        commandType = type;
    }
    
    public static void setTask(Task taskBuilt) {
        task = taskBuilt;
    }
    
    public static void setDeleteIndex(ArrayList<Integer> index) {
        deleteIndexes = index;
    }
    
    //Main to be removed
    public static void main(String[] args) {
        CommandParser parser = new CommandParser();
        parser.parse("cook dinner #home, personal");
        System.out.println(parser.getTask().getTitle());
        System.out.println(parser.getTask().getLabels());
        System.out.println();
        
        parser.parse("do assignment from 5 - 7pm");
        System.out.println(parser.getTask().getTitle());
        System.out.println(parser.getTask().getStartDate());
        System.out.println(parser.getTask().getEndDate());
        System.out.println();
        
        parser.parse("attend meeting from 14 - 16 on Weds #important");
        System.out.println(parser.getTask().getTitle());
        System.out.println(parser.getTask().getStartDate());
        System.out.println(parser.getTask().getEndDate());
        System.out.println(parser.getTask().getLabels());
        System.out.println(parser.getTask().toString());
        System.out.println();
        
        parser.parse("delete 1-10");
        System.out.println(deleteIndexes);
        
        parser.parse("delete 1 - 10");
        System.out.println(deleteIndexes);
        
        parser.parse("del 1,2,3,4,5");
        System.out.println(deleteIndexes);
        
        parser.parse("del 1 , 2 , 3 , 4 , 5");
        System.out.println(deleteIndexes);
    }
    
}
