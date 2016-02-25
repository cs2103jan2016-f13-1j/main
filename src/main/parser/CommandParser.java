import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

/**
 * @author Joleeen
 *
 *
 *Currently assuming that labels are added at the very end, with no more information behind.
 *
 */

public class CommandParser {
    
    private final int LENGTH_DEL = 3;
    private final int LENGTH_DELETE = 6;
    private final int LENGTH_OFFSET = 1;
    
    public Command parse(String commandString) {
        String command = getFirstWord(commandString);
        String commandType = determineCommandType(command);
        return commandPreparations(commandType, commandString);
    }
    
    public String getFirstWord(String commandString) {
        return commandString.split(" ")[0];
    }
    
    public String determineCommandType(String command) {
        if (command.equalsIgnoreCase("delete") || (command.equalsIgnoreCase("del"))) {
            return "delete";
        }
        
        return "add";
    }
    
    public Command commandPreparations(String type, String commandString) {
        switch (type) {
            case "add" :
                return prepareForAdd(type, commandString);
                
            case "delete" :
                return prepareForDel(type, commandString);
                
            default :
                return null;
        }
    }
    
    public Command prepareForAdd(String type, String commandString) {
        String title;
        String tab = "floating";
        Task task;
        ArrayList<String> labels = null;
        Date startDate = null;
        Date endDate = null;
        
        boolean isFloating;
        List<Date> dates = getDate(commandString);
        isFloating = checkIsFloating(dates);
        
        title = commandString;
        
        if (!isFloating) {
            tab = "dated";
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
        Command command = new Command(type, tab, task);
        return command;
    }
    
    public List<Date> getDate(String commandString) {
        PrettyTimeParser parser = new PrettyTimeParser();
        List<Date> dates = parser.parse(commandString);
        return dates;
    }
    
    public boolean checkIsFloating(List<Date> dates) {
        if (dates.size() == 0) {
            return true;
        }
        return false;
    }
    
    public Date prepareStartDate(List<Date> dates) {
        return dates.get(0);
    }
    
    public boolean checkDateRange(List<Date> dates) {
        if (dates.size() == 2) {
            return true;
        }
        return false;
    }
    
    public Date prepareEndDate(List<Date> dates) {
        return dates.get(1);
    }
    
    public boolean checkForLabels(String commandString) {
        if (commandString.contains("#")) {
            return true;
        }
        return false;
    }
    
    public ArrayList<String> extractLabels(String commandString) {
        ArrayList<String> labels = new ArrayList<String>();
        int index = commandString.indexOf("#");
        index = index + LENGTH_OFFSET;
        String substring = commandString.substring(index);
        substring = removeWhiteSpace(substring);
        Collections.addAll(labels, substring.split(","));
        
        return labels;
    }
    
    public String removeWhiteSpace(String string) {
        string = string.replaceAll("\\s","");
        return string;
    }
    
    public String removeLabels(String title) {
        int index = title.indexOf("#");
        index = index - LENGTH_OFFSET;
        return title.substring(0, index);
    }
    
    public Task buildTask(String title, Date startDate, Date endDate, ArrayList<String> labels) {
        Task task = new Task.TaskBuilder(title).setStartDate(startDate).setEndDate(endDate)
        .setLabels(labels).build();
        return task;
    }
    
    public Command prepareForDel(String type, String commandString) {
        String index = retrieveDeleteIndex(commandString);
        String indexType = checkIndexType(index);
        
        ArrayList<Integer> deleteIndexes = new ArrayList<Integer>();
        
        if (indexType.equals("single")) {
            deleteIndexes.add(Integer.parseInt(index));
        } else {
            deleteIndexes = extractMultipleIndex(index, indexType);
        }
        
        Command command = new Command(type, deleteIndexes);
        return command;
    }
    
    public String retrieveDeleteIndex(String commandString) {
        int index = 0;
        String command = getFirstWord(commandString);
        
        if (command.matches("delete")) {
            index = LENGTH_DELETE;
        } else if (command.matches("del")) {
            index = LENGTH_DEL;
        }
        index = index + LENGTH_OFFSET;
        
        commandString = commandString.substring(index, commandString.length());
        commandString = removeWhiteSpace(commandString);
        
        return commandString;
    }
    
    public String checkIndexType(String index) {
        String type;
        
        if (index.contains(",")) {
            type = "multiple";
        } else if (index.contains("-")) {
            type = "range";
        } else {
            type = "single";
        }
        
        return type;
    }
    
    public ArrayList<Integer> extractMultipleIndex(String index, String type) {
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
}