package main.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;

import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

import main.data.Command;
import main.data.Command.Type;
import main.data.Task;

/**
 * @author Joleen
 *
 */

public class CommandParser {
    private final int LENGTH_DEL = 3;
    private final int LENGTH_DELETE = 6;
    private final int LENGTH_DONE = 4;
    private final int LENGTH_OFFSET = 1;
    private final int DATE_INDEX = 0;
    private final int DATE_START_RANGED = 0;
    private final int DATE_END_RANGED = 1;
    private final int DATE_MAX_SIZE = 2;
    private final int INDEX_OFFSET = 1;
    private final String STRING_AM = "am";
    private final String STRING_PM = "pm";
    private final String STRING_TWELVE = "12";
    private final boolean PREPOSITION_ALL = true;
    private final boolean PREPOSITION_SELECTIVE = false;
    
    public Command parse(String commandString) {
        String command = getFirstWord(commandString);
        Type commandType = getCommandType(command);
        return commandPreparations(commandType, commandString);
    }
    
    private String getFirstWord(String commandString) {
        return commandString.split(" ")[0];
    }
    
    private Type getCommandType(String command) {
        if (command.equalsIgnoreCase("delete") || (command.equalsIgnoreCase("del"))) {
            return Command.Type.DELETE;
        } else if (command.equalsIgnoreCase("done")) {
        	return Command.Type.DONE;
        } else {
        	return Command.Type.ADD;
        }
    }
    
    private Command commandPreparations(Type type, String commandString) {
        switch (type) {
            case ADD :
                return prepareForAdd(type, commandString);
                
            case DELETE :
                return prepareIndexes(type, commandString);
                
            case DONE :
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
     * 			command type that is determined
     * @param commandString
     * 			command string from user input
     * @return Command with the type of command and task
     */
    private Command prepareForAdd(Type type, String commandString) {
        String title = null;
        Task task = null;
        String label = null;
        int numberOfDate = 0;
        Date startDate = null;
        Date endDate = null;
        boolean hasStartDate = false;
        boolean isLabelPresent;
        boolean hasPreposition;
        title = commandString;

        hasPreposition = checkForPrepositions(commandString, PREPOSITION_ALL);
        if (hasPreposition) {
        	commandString = detectAndCorrectDateInput(commandString);
        	List<Date> dates = parseDate(commandString);
            numberOfDate = dates.size();
            
            if (numberOfDate > 0) {
            	if (numberOfDate == DATE_MAX_SIZE) {
                    startDate = getDate(dates, DATE_START_RANGED);
                    endDate = getDate(dates, DATE_END_RANGED);
                } else {
                	hasStartDate = checkForPrepositions(commandString, PREPOSITION_SELECTIVE);
                	if (hasStartDate) {
                		startDate = getDate(dates, DATE_INDEX);
                	} else {
                		endDate = getDate(dates, DATE_INDEX);
                	}
                }
            	 
                title = removeDateFromTitle(title, startDate, endDate);
            }
        }
        
        isLabelPresent = checkForLabel(commandString);
        if (isLabelPresent) {
            label = extractLabel(commandString);
            title = removeLabelFromTitle(title, label);
        }

        task = buildTask(title, startDate, endDate, label);
        Command command = new Command(type, task);
        return command;
    }
    
    private boolean checkForPrepositions(String commandString, boolean type) {
    	ArrayList<String> prepositions = new ArrayList<String>();
	    prepositions = populatePrepositions(type);
	    
    	List<String> words = new ArrayList<String>(Arrays.asList(commandString.toLowerCase().split(" ")));
    	
    	for (int i = 0; i < prepositions.size(); i++ ) {
    		if (words.contains(prepositions.get(i))) {
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * Generate a list of pre-defined prepositions to be used.
     * 
     * @return ArrayList<String> containing prepositions
     */
    private ArrayList<String> populatePrepositions(boolean prepositionType) {
    	ArrayList<String> prepositions = new ArrayList<String>();
    	
    	if (prepositionType) {
	    	prepositions.add("from");
	    	prepositions.add("at");
	    	prepositions.add("on");
	    	prepositions.add("by");
	    	prepositions.add("before");
	    	prepositions.add("to");
	    	prepositions.add("-");
	    	prepositions.add("after");
    	} else {
    		prepositions.add("from");
        	prepositions.add("after");
    	}
    	
    	return prepositions;
    }
    
    /**
     * Corrects user input of dd/mm into mm/dd for parser
     * 
     * @param commandString
     * 			user input string
     * @return String with the date fields swapped
     */
    private String detectAndCorrectDateInput(String commandString) {
		boolean match = false;
		String swapped = "";
		
		//Preserve capitalization by not using toLowerCase
		List<String> words = new ArrayList<String>(Arrays.asList(commandString.split(" ")));
		
		String pattern = "(0?[1-9]|[12][0-9]|3[01])(/|-)(0?[1-9]|1[012])";
		
		for (int i = 0; i< words.size(); i++) {
			match = Pattern.matches(pattern, words.get(i));
			if (match) {
				if (words.get(i).contains("/")) {
					List<String> date = new ArrayList<String>(Arrays.asList(words.get(i).split("/")));
					swapped = date.get(1).concat("/").concat(date.get(0));
				} else if (words.get(i).contains("-")) {
					List<String> date = new ArrayList<String>(Arrays.asList(words.get(i).split("-")));
					swapped = date.get(1).concat("-").concat(date.get(0));
				}
				
				words.set(i, swapped);
				break;
			}
		}

		return String.join(" ", words);
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
    
    /**
     * From the date(s) obtained from title, different formats are generated.
     * Date information is removed from the title.
     * 
     * @param title
     * 			task's title
     * @param startDate
     * 			task's start date
     * @param endDate
     * 			task's end date
     * @return String title without date information
     */
    private String removeDateFromTitle(String title, Date startDate, Date endDate) {
    	List<Date> datesList = parseDate(title);
        int numberOfDate = datesList.size();
        LocalDateTime dateTime;
        
        if (startDate != null) {
        	dateTime = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        } else {
        	dateTime = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }   

        for (int i = 0; i < numberOfDate; i++) {
        	 ArrayList<String> dates = getPossibleDates(dateTime);
             ArrayList<String> months = getPossibleMonths(dateTime);
             ArrayList<String> days = getPossibleDays(dateTime);
             ArrayList<String> timings = getPossibleTimes(dateTime);

             title = checkAndRemove(title, dates);
             title = checkAndRemove(title, months);
             title = checkAndRemove(title, days);
             title = checkAndRemove(title, timings);
             
             if (numberOfDate == DATE_MAX_SIZE) {
            	 dateTime = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
             }
        }
    	return title;
    }

	private ArrayList<String> getPossibleDates(LocalDateTime dateTime) {
		ArrayList<String> dates = new ArrayList<String>();
		dates.add(Integer.toString(dateTime.getDayOfMonth()));
		dates.add(dateTime.format(DateTimeFormatter.ofPattern("dd/M")));
		dates.add(dateTime.format(DateTimeFormatter.ofPattern("dd/MM")));
		dates.add(dateTime.format(DateTimeFormatter.ofPattern("dd-M")));
		dates.add(dateTime.format(DateTimeFormatter.ofPattern("dd-MM")));
		return dates;
	}
	
	private ArrayList<String> getPossibleMonths(LocalDateTime dateTime) {
		Locale locale = Locale.getDefault();
		ArrayList<String> months = new ArrayList<String>();
		Month month = dateTime.getMonth();
		months.add(month.toString().toLowerCase());
		months.add(month.getDisplayName(TextStyle.SHORT, locale).toLowerCase());
		return months;
	}
	
	private ArrayList<String> getPossibleDays(LocalDateTime dateTime) {
		Locale locale = Locale.getDefault();
		DayOfWeek day = dateTime.getDayOfWeek();
		ArrayList<String> days = new ArrayList<String>();
		days.add(day.toString().toLowerCase());
		days.add(day.getDisplayName(TextStyle.SHORT, locale).toLowerCase());
		return days;
	}
    
	private ArrayList<String> getPossibleTimes(LocalDateTime dateTime) {
		ArrayList<String> timings = new ArrayList<String>();
		int hour = dateTime.getHour();
		int min = dateTime.getMinute();

		String minute = ":";
		if (min == 0) {
			minute = minute.concat("0");
		}

		minute = minute.concat(Integer.toString(dateTime.getMinute()));

		timings.add(Integer.toString(hour));
		timings.add(Integer.toString(hour).concat(minute));

		if (hour < 12) {
			if (hour == 0) {
				String temp = STRING_TWELVE;
				timings.add(temp.concat(STRING_AM));
				timings.add(temp.concat(minute).concat(STRING_AM));
			} else {
				timings.add(Integer.toString(hour).concat(STRING_AM));
				timings.add(Integer.toString(hour).concat(minute).concat(STRING_AM));
			}
		} else if (hour >= 12) {	
			hour = hour - 12;
			if (hour == 0) {
				String temp = STRING_TWELVE;
				timings.add(temp.concat(STRING_PM));
				timings.add(temp.concat(minute).concat(STRING_PM));
			} else {
				timings.add(Integer.toString(hour));
				timings.add(Integer.toString(hour).concat(STRING_PM));
				timings.add(Integer.toString(hour).concat(minute).concat(STRING_PM));	
			}
		}
		return timings;
	}
	
    /**
     * Checks for and removes targeted word from title.
     * If word to be removed is found, it checks if the word before it is a preposition.
     * If preposition found, both are removed.
     * Else, only the matching word is removed.
     * 
     * @param title
     * 			title string to be checked
     * @param toBeRemoved
     * 			words to be removed
     * @return String with targeted words removed
     */
    private String checkAndRemove(String title, ArrayList<String> toBeRemoved) {
    	String toBeReplaced = "";
    	int index;
    	boolean isPreposition;
    	
    	List<String> words = new ArrayList<String>(Arrays.asList(title.toLowerCase().split(" ")));

    	for (int i = 0; i < toBeRemoved.size(); i++) {
    		if (words.contains(toBeRemoved.get(i))) {
    			toBeReplaced = toBeReplaced.concat(" ");
    			toBeReplaced = toBeReplaced.concat(toBeRemoved.get(i));

    			index = words.indexOf(toBeRemoved.get(i));
    			index = index - INDEX_OFFSET;
    			isPreposition = checkIsPreposition(title, index, PREPOSITION_ALL);

    			if (isPreposition) {
    				toBeReplaced = words.get(index).concat(toBeReplaced);
    				toBeReplaced = " ".concat(toBeReplaced);
    			}
    		}
    		
    		//remove regardless of case
        	toBeReplaced = "(?i)".concat(toBeReplaced); 
        	title = title.replaceAll(toBeReplaced, "");
    	}
    	
    	return title;
    }
    
    /**
     * Check if a word is a preposition
     * 
     * @param title
     * 			title string
     * @param index
     * 			index of the word to be checked
     * @return Boolean true if preposition found
     */
    private boolean checkIsPreposition(String title, int index, boolean type) {
    	ArrayList<String> prepositions = new ArrayList<String>();
	    prepositions = populatePrepositions(type);

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
     * Detects the types of indexes and processes them.
     * 
     * @param type
     * 			command type
     * @param commandString
     * 			user input string
     * @return Command with the type of command and index(es)
     */
    private Command prepareIndexes(Type type, String commandString) {
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
    
    /**
     * Obtain all numbers based on index string given.
     * 
     * @param index
     * 			index string
     * @return ArrayList<Integer> of index(es) but decremented by 1
     */
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
                    multipleIndexes.add(k - INDEX_OFFSET);
                }
                
                tempRangedIndexes.clear();
                rangedIndexes.clear();
            } else {
            	int indexToAdd;
            	indexToAdd = Integer.parseInt(indexes.get(i));
            	indexToAdd = indexToAdd - INDEX_OFFSET;
                multipleIndexes.add(indexToAdd);
            }
        }
        
        return multipleIndexes;
    }
}