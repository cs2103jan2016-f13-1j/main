import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

/**
 * @author Joleeen
 *
 *
 *Currently assuming that label are added at the very end, with no more information behind.
 *
 */

public class CommandParser {
    
    private final int LENGTH_DEL = 3;
    private final int LENGTH_DELETE = 6;
    private final int LENGTH_OFFSET = 1;
    private final int DATE_END = 0;
    private final int DATE_START_RANGED = 0;
    private final int DATE_END_RANGED = 1;
    private final int DATE_MAX_SIZE = 2;
    
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
        } else {
            return "add";
        }
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
        String tab = "floating";
        String title = null;
        Task task = null;
        String label = null;
        Date startDate = null;
        Date endDate = null;
        boolean isLabelPresent;
        
        List<Date> dates = parseDate(commandString);
        int numberOfDate = dates.size();
        
        if (numberOfDate > 0) {
            tab = "dated";
            endDate = getDate(dates, DATE_END);
        }
        
        if (numberOfDate == DATE_MAX_SIZE) {
            startDate = getDate(dates, DATE_START_RANGED);
            endDate = getDate(dates, DATE_END_RANGED);
        }
        
        title = commandString;
        
        isLabelPresent = checkForLabel(commandString);
        if (isLabelPresent) {
            label = extractLabel(commandString);
            title = removeLabel(title);
        }
        
        task = buildTask(title, startDate, endDate, label);
        Command command = new Command(type, tab, task);
        return command;
    }
    
    public List<Date> parseDate(String commandString) {
        PrettyTimeParser parser = new PrettyTimeParser();
        List<Date> dates = parser.parse(commandString);
        return dates;
    }
    
    public Date getDate(List<Date> dates, int index) {
        return dates.get(index);
    }
    
    public boolean checkForLabel(String commandString) {
        if (commandString.contains("#")) {
            return true;
        } else {
            return false;
        }
    }
    
    public String extractLabel(String commandString) {
        int index = commandString.indexOf("#");
        index = index + LENGTH_OFFSET;
        String substring = commandString.substring(index);
        String label = substring.trim();
        return label;
    }
    
    public String removeWhiteSpace(String string) {
        string = string.replaceAll("\\s","");
        return string;
    }
    
    public String removeLabel(String title) {
        int index = title.indexOf("#");
        index = index - LENGTH_OFFSET;
        return title.substring(0, index);
    }
    
    public Task buildTask(String title, Date startDate, Date endDate, String label) {
        Task task = new Task.TaskBuilder(title).setStartDate(startDate).setEndDate(endDate)
        .setLabel(label).build();
        return task;
    }
    
    public Command prepareForDel(String type, String commandString) {
        String index = getIndexString(commandString);
        
        ArrayList<Integer> indexesToDelete = new ArrayList<Integer>();
        indexesToDelete = extractIndex(index);
        
        Command command = new Command(type, indexesToDelete);
        return command;
    }
    
    public String getIndexString(String commandString) {
        int index = 0;
        String command = getFirstWord(commandString);
        
        if (command.matches("delete")) {
            index = LENGTH_DELETE;
        } else if (command.matches("del")) {
            index = LENGTH_DEL;
        }
        index = index + LENGTH_OFFSET;
        
        String indexToDelete = commandString.substring(index, commandString.length());
        indexToDelete = removeWhiteSpace(indexToDelete);
        
        return indexToDelete;
    }
    
    public ArrayList<Integer> extractIndex(String index) {
        ArrayList<String> indexes = new ArrayList<String>();
        ArrayList<String> tempRangedIndexes = new ArrayList<String>();
        ArrayList<Integer> multipleIndexes = new ArrayList<Integer>();
        ArrayList<Integer> rangedIndexes = new ArrayList<Integer>();
        
        Collections.addAll(indexes, index.split(","));
        
        for (int i = 0; i < indexes.size(); i++) {
            if (indexes.get(i).contains("-")) {
                Collections.addAll(tempRangedIndexes, indexes.get(i).split("-"));
                
                for (int j = 0; j < tempRangedIndexes.size(); j++) {
                    rangedIndexes.add(Integer.parseInt(tempRangedIndexes.get(j)));
                }
                
                for (int k = rangedIndexes.get(0); k <= rangedIndexes.get(1); k++) {
                    multipleIndexes.add(k);
                }
                
                tempRangedIndexes.clear();
                rangedIndexes.clear();
            } else {
                multipleIndexes.add(Integer.parseInt(indexes.get(i)));
            }
        }
        
        return multipleIndexes;
    }
}