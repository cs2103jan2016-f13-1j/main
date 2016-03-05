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
    private final int PREPOSITION_FROM_LENGTH = 4;
    private final int PREPOSITION_OTHER_LENGTH = 2;
    
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
        	title = removeDateFromTitle(title, numberOfDate);
        }

        task = buildTask(title, startDate, endDate, label);
        Command command = new Command(type, tab, task);
        return command;
    }
    
    private ArrayList<String> populatePrepositions() {
    	ArrayList<String> prepositions = new ArrayList<String>();
    	prepositions.add("from");
    	prepositions.add("at");
    	prepositions.add("on");
    	prepositions.add("by");
    	prepositions.add("before");
    	
    	return prepositions;
    }
    
    private boolean checkForPrepositions(String commandString) {
    	ArrayList<String> prepositions = new ArrayList<String>();
	    prepositions = populatePrepositions();
	    
    	List<String> words = new ArrayList<String>(Arrays.asList(commandString.split(" ")));
    	
    	for (int i = 0; i < prepositions.size(); i++ ) {
    		if (words.contains(prepositions.get(i))) {
    			return true;
    		}
    	}
    	return false;
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
    
    private String removeWhiteSpace(String string) {
        string = string.replaceAll("\\s","");
        return string;
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
    
    
    /**
     * Removes date and time information in title.
     * 1) It removes preposition together with the date.
     * 2) Checks and removes residual date information.
     * 
     * @param title
     * 			title to be modified
     * @param numberOfDate
     * 			number of dates detected
     * @return title string without date/time
     */
    private String removeDateFromTitle(String title, int numberOfDate) {
		int index = getPrepositionsIndex(title);
    	title = removePrepositionAndDate(title, numberOfDate, index);
    	
    	boolean isParsable = checkForParsableTitle(title);
    	if (isParsable) {
    		title = removeParsableWord(title, index);
    	}
    	
    	return title;
    }
    
	/**
	 * Removes preposition together with the date present in title string.
	 * Pattern:
	 * 		"from * to *"
	 * 		"at *"
	 * 		"on *"
	 * 		"by *"
	 * 		"before *"
	 * 
	 * @param title
	 * 			title to be modified
	 * @param numberOfDate
	 * 			number of dates detected
	 * @param index
	 * 			index of previously detected preposition
	 * @return title string without time/date
	 */
	private String removePrepositionAndDate(String title, int numberOfDate, int index) {
    	String dateString = "";
    	List<String> words = new ArrayList<String>(Arrays.asList(title.split(" ")));

		int upperBound = 0;
		
    	if (numberOfDate == DATE_MAX_SIZE) {
    		upperBound = PREPOSITION_FROM_LENGTH;
    	} else if (numberOfDate > 0) {
    		upperBound = PREPOSITION_OTHER_LENGTH;
    	}
    	
    	for (int i = 0; i < upperBound; i++) {
			dateString = dateString.concat(" ");
			dateString = dateString.concat(words.get(index+i));
		}
    	
    	title = title.replace(dateString, "");
    	return title;
    }
    
	
    /**
     * Checks in case there are still date/time information in title
     * 
     * @param title
     * 			title string
     * @return true if detected
     */
    private boolean checkForParsableTitle(String title) {
    	List<Date> dates = parseDate(title);
        int numberOfDate = dates.size();
        if (numberOfDate > 0) {
        	return true;
        } else {        
        	return false;
        }
    }
    
	/**
	 * Starts checking base on where the previous preposition is found.
	 * Checks and removes residual date/time near the removed preposition.
	 * 
	 * @param title
	 * 			title string with residual date/time
	 * @param index
	 * 			index of previously detected and removed preposition
	 * @return clean title string without date/time
	 */
	private String removeParsableWord(String title, int index) {
		//index is the starting point for checks because it is previously position of preposition
		List<String> words = new ArrayList<String>(Arrays.asList(title.split(" ")));
		String toRemove = "";
		int bound = words.size();
		
		//index is used; when word is remove, the remaining array shifts up
		//thus index never changes
		//rest of the string not checked because too far away from preposition
		for (int i=index; i < bound; i++) {
			List<Date> dates = parseDate(words.get(index));
			int numberOfDate = dates.size();
			
			if (numberOfDate > 0) {
				toRemove = toRemove.concat(" ");
				toRemove = toRemove.concat(words.get(index));
				words.remove(index);
			} else {
				break;
			}
		}
	
		title = title.replace(toRemove,"");
		return title;
	}
    
    private int getPrepositionsIndex(String commandString) {
    	ArrayList<String> prepositions = new ArrayList<String>();
	    prepositions = populatePrepositions();

    	List<String> words = new ArrayList<String>(Arrays.asList(commandString.split(" ")));
    	int index = 0;
    	
    	for (int i = 0; i < prepositions.size(); i++ ) {
    		if (words.contains(prepositions.get(i))) {
    			index = words.indexOf(prepositions.get(i));
    			break;
    		}
    	}
    	
    	return index;
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