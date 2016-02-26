/**
 * 
 */
package main.parser;

/**
 * @author Joleen
 *
 */
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

import main.data.Command;
import main.data.Task;

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
    
    private String getFirstWord(String commandString) {
        return commandString.split(" ")[0];
    }
    
    private String determineCommandType(String command) {
        if (command.equalsIgnoreCase("delete") || (command.equalsIgnoreCase("del"))) {
            return "delete";
        } else {
            return "add";
        }
    }
    
    private Command commandPreparations(String type, String commandString) {
        switch (type) {
            case "add" :
                return prepareForAdd(type, commandString);
                
            case "delete" :
                return prepareForDel(type, commandString);
                
            default :
                return null;
        }
    }
    
    private Command prepareForAdd(String type, String commandString) {
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
            title = removeLabel(title, label);
        }
        
        task = buildTask(title, startDate, endDate, label);
        Command command = new Command(type, tab, task);
        return command;
    }
    
    private List<Date> parseDate(String commandString) {
        PrettyTimeParser parser = new PrettyTimeParser();
        List<Date> dates = parser.parse(commandString);
        
        //test data to simulate working parser
        //List<Date> dates = new ArrayList<Date>();
        
        /*
        if (commandString.contains("pm")) {
            SimpleDateFormat df = new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy");
            String dateString = "Thu Feb 25 19:00:00 SGT 2016";
            try {
                Date date = df.parse(dateString);
                dates.add(date);
            } catch (ParseException e) {
            }
        }
        */
        return dates;
    }
    
    private Date getDate(List<Date> dates, int index) {
        return dates.get(index);
    }
    
    private boolean checkForLabel(String commandString) {
        if (commandString.contains("#")) {
            return true;
        } else {
            return false;
        }
    }
    
    private String extractLabel(String commandString) {
        int index = commandString.indexOf("#");
        index = index + LENGTH_OFFSET;
        String substring = commandString.substring(index);
        String label = substring.trim();
        label = getFirstWord(label);
        return label;
    }
    
    private String removeWhiteSpace(String string) {
        string = string.replaceAll("\\s","");
        return string;
    }
    
    private String removeLabel(String title, String label) {
    	String tag = "#".concat(label);
	 	
	 	int index = title.indexOf(tag);
	 	index = index + label.length() + LENGTH_OFFSET;

	 	if (title.length() != index) {
	 		tag = tag.concat(" ");
	 	} else {
	 		tag = " ".concat(tag);
	 	}
	 	
	 	title = title.replace(tag, "");
        return title;
    }
    
    private Task buildTask(String title, Date startDate, Date endDate, String label) {
        Task task = new Task.TaskBuilder(title).setStartDate(startDate).setEndDate(endDate)
        .setLabel(label).build();
        return task;
    }
    
    private Command prepareForDel(String type, String commandString) {
        String index = getIndexString(commandString);
        
        ArrayList<Integer> indexesToDelete = new ArrayList<Integer>();
        indexesToDelete = extractIndex(index);
        
        Command command = new Command(type, indexesToDelete);
        return command;
    }
    
    private String getIndexString(String commandString) {
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
    
    private ArrayList<Integer> extractIndex(String index) {
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