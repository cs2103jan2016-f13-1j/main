/**
 *
 */
package main.parser;


/**
 * @author Joleen
 *
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.time.*;
import java.time.format.TextStyle;

import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

import main.data.Command;
import main.data.Task;

public class CommandParser {
    private final int LENGTH_DEL = 3;
    private final int LENGTH_DELETE = 6;
    private final int LENGTH_DONE = 4;
    private final int LENGTH_OFFSET = 1;
    private final int DATE_END = 0;
    private final int DATE_START_RANGED = 0;
    private final int DATE_END_RANGED = 1;
    private final int DATE_MAX_SIZE = 2;
    private final int INDEX_OFFSET = 1;
    
    public Command parse(String commandString) {
        String command = getFirstWord(commandString);
        String commandType = getCommandType(command);
        return commandPreparations(commandType, commandString);
    }
    
    private String getFirstWord(String commandString) {
        return commandString.split(" ")[0];
    }
    
    private String getCommandType(String command) {
        if (command.equalsIgnoreCase("delete") || (command.equalsIgnoreCase("del"))) {
            return "delete";
        } else if (command.equalsIgnoreCase("done")) {
        	return "done";
        } else {
        	return "add";
        }
    }
    
    private Command commandPreparations(String type, String commandString) {
        switch (type) {
            case "add" :
                return prepareForAdd(type, commandString);
                
            case "delete" :
                return prepareIndexes(type, commandString);
                
            case "done" :
            	return prepareIndexes(type, commandString);
            	
            default :
                return null;
        }
    }
    
    /**
     * Find out what kind of task is to be added.
     * Words without prepositions are considered floating tasks.
     * Words with prepositions might not be dated.
     * Dated task will always contain prepositions.
     * 
     * @param type
     * 			command type
     * @param commandString
     * 			user input string
     * @return command object with the type of command and a task.
     */
    private Command prepareForAdd(String type, String commandString) {
        String tab = "floating";
        String title = null;
        Task task = null;
        String label = null;
        int numberOfDate = 0;
        Date startDate = null;
        Date endDate = null;
        boolean isLabelPresent;
        boolean hasPreposition;
        title = commandString;

        hasPreposition = checkForPrepositions(commandString);
        if (hasPreposition) {
        	List<Date> dates = parseDate(commandString);
            numberOfDate = dates.size();
            
            if (numberOfDate > 0) {
                tab = "dated";
                endDate = getDate(dates, DATE_END);
            }
            
            if (numberOfDate == DATE_MAX_SIZE) {
                startDate = getDate(dates, DATE_START_RANGED);
                endDate = getDate(dates, DATE_END_RANGED);
            }
        }
        
        isLabelPresent = checkForLabel(commandString);
        if (isLabelPresent) {
            label = extractLabel(commandString);
            title = removeLabelFromTitle(title, label);
        }
        
        if (tab.equals("dated")) {
        	title = removeDateFromTitle(title, startDate, endDate);
        }

        task = buildTask(title, startDate, endDate, label);
        Command command = new Command(type, tab, task);
        
        return command;
    }
    
    private boolean checkForPrepositions(String commandString) {
    	ArrayList<String> prepositions = new ArrayList<String>();
	    prepositions = populatePrepositions();
	    
    	List<String> words = new ArrayList<String>(Arrays.asList(commandString.toLowerCase().split(" ")));
    	
    	for (int i = 0; i < prepositions.size(); i++ ) {
    		if (words.contains(prepositions.get(i))) {
    			return true;
    		}
    	}
    	return false;
    }
    
    private ArrayList<String> populatePrepositions() {
    	ArrayList<String> prepositions = new ArrayList<String>();
    	prepositions.add("from");
    	prepositions.add("at");
    	prepositions.add("on");
    	prepositions.add("by");
    	prepositions.add("before");
    	prepositions.add("to");
    	prepositions.add("-");
    	
    	return prepositions;
    }
    
    private List<Date> parseDate(String commandString) {
        PrettyTimeParser parser = new PrettyTimeParser();
        List<Date> dates = parser.parse(commandString);
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
   
    private String removeLabelFromTitle(String title, String label) {
        String tag = "#".concat(label);
        
        int index = title.indexOf(tag);
        index = index + label.length() + LENGTH_OFFSET;
        
        if (title.length() != index) {
            tag = tag.concat(" ");
        } else if (title.length() == index) {
            tag = " ".concat(tag);
        }
        
        title = title.replace(tag, "");
        return title;
    }
    
    public String removeDateFromTitle(String title, Date startDate, Date endDate) {
    	List<Date> dates = parseDate(title);
        int numberOfDate = dates.size();
   
        LocalDateTime dateTime = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        Locale locale = Locale.getDefault();

        for (int i = 0; i < numberOfDate; i++) {
        	 String date = Integer.toString(dateTime.getDayOfMonth());
             
             ArrayList<String> months = new ArrayList<String>();
             Month month = dateTime.getMonth();
             months.add(month.toString().toLowerCase());
             months.add(month.getDisplayName(TextStyle.SHORT, locale).toLowerCase());

             DayOfWeek day = dateTime.getDayOfWeek();
             ArrayList<String> days = new ArrayList<String>();
             days.add(day.toString().toLowerCase());
             days.add(day.getDisplayName(TextStyle.SHORT, locale).toLowerCase());
             
             ArrayList<String> hours = new ArrayList<String>();
             int time = dateTime.getHour();
             hours.add(Integer.toString(time));
             
             if (time < 12) {
            	 if (time == 0) {
            		 hours.add("12am");
            	 } else {
            		 hours.add(Integer.toString(time).concat("am"));
            	 }
             } else if (time >= 12) {	
            	 time = time - 12;
            	 if (time == 0) {
            		 hours.add("12pm");
            	 } else {
            		 hours.add(Integer.toString(time));
            		 hours.add(Integer.toString(time).concat("pm"));	
            	 }
             }
             
             String minute = Integer.toString(dateTime.getMinute());

             title = checkAndRemove(title, date);
             
             for (int j = 0; j < months.size(); j++) {
             	title = checkAndRemove(title, months.get(j));
             }
             
             for (int j = 0; j < days.size(); j++) {
             	title = checkAndRemove(title, days.get(j));
             }
             
             for (int j = 0; j < hours.size(); j++) {
             	title = checkAndRemove(title, hours.get(j));
             }
             
             title = checkAndRemove (title, minute);
             
             if (numberOfDate == 2) {
            	 dateTime = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
             }
        }
    	return title;
    }
    
    private String checkAndRemove(String title, String toBeRemoved) {
    	String toBeReplaced = "";
    	int index;
    	boolean isPreposition;
    	
    	List<String> words = new ArrayList<String>(Arrays.asList(title.toLowerCase().split(" ")));

    	if (words.contains(toBeRemoved)) {
    		toBeReplaced = toBeReplaced.concat(" ");
    		toBeReplaced = toBeReplaced.concat(toBeRemoved);
    		
    		index = words.indexOf(toBeRemoved);
    		index = index - INDEX_OFFSET;
    		isPreposition = checkIsPreposition(title, index);
    		
    		if (isPreposition) {
    			toBeReplaced = words.get(index).concat(toBeReplaced);
    			toBeReplaced = " ".concat(toBeReplaced);
    		}
    	}
    	
    	//remove regardless of case
    	toBeReplaced = "(?i)".concat(toBeReplaced); 
    	title = title.replaceAll(toBeReplaced, "");
    	return title;
    }
    
    
    /**
     * Check if a word is a preposition
     * 
     * @param title
     * 			title string
     * @param index
     * 			index of the word to be checked
     * @return	true if preposition found
     */
    private boolean checkIsPreposition(String title, int index) {
    	ArrayList<String> prepositions = new ArrayList<String>();
	    prepositions = populatePrepositions();

    	List<String> words = new ArrayList<String>(Arrays.asList(title.toLowerCase().split(" ")));
    	for (int i = 0; i < prepositions.size(); i++ ) {
    		if (words.get(index).matches(prepositions.get(i))) {
    			return true;
    		}
    	}
    	return false;
    }
   
    private Task buildTask(String title, Date startDate, Date endDate, String label) {
        Task task = new Task.TaskBuilder(title).setStartDate(startDate).setEndDate(endDate)
        .setLabel(label).build();
        return task;
    }
    
    /**
     * Detect the types of indexes before processing them
     * 
     * @param type
     * 			command type
     * @param commandString
     * 			user input string
     * @return a command object with the type of command and index(es)
     */
    private Command prepareIndexes(String type, String commandString) {
        String indexString = getIndexString(commandString);
        
        ArrayList<Integer> indexes = new ArrayList<Integer>();
        indexes = extractIndex(indexString);
        
        Command command = new Command(type, indexes);
        return command;
    }
    
    private String getIndexString(String commandString) {
        int index = 0;
        String command = getFirstWord(commandString);
        
        if (command.matches("delete")) {
            index = LENGTH_DELETE;
        } else if (command.matches("del")) {
            index = LENGTH_DEL;
        } else if (command.matches("done")) {
        	index = LENGTH_DONE;
        }
        index = index + LENGTH_OFFSET;
        
        String indexString = commandString.substring(index, commandString.length());
        indexString = removeWhiteSpace(indexString);
        
        return indexString;
    }
    
    private String removeWhiteSpace(String string) {
        string = string.replaceAll("\\s","");
        return string;
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