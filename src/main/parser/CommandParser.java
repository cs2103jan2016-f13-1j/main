package main.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;

import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

import main.data.Task;

/**
 * @author Joleen
 *
 */

public class CommandParser {
    private final boolean PREPOSITION_ALL = true;
    private final boolean PREPOSITION_SELECTIVE = false;
    private final int DATE_INDEX = 0;
    private final int DATE_START_RANGED = 0;
    private final int DATE_END_RANGED = 1;
    private final int DATE_MAX_SIZE = 2;
    private final String DATE_STRING_PATTERN = "(0?[1-9]|[12][0-9]|3[01])(/|-)(0?[1-9]|1[012])";
    private final String TIME_STRING_PATTERN = "(0?[1-9]|[1][0-2])(:[0-5][0-9])?(am|pm)";
    private final String STRING_AM = "am";
    private final String STRING_PM = "pm";
    private final String STRING_TWELVE = "12";
    private final int DOUBLE_DIGIT = 10;
    private final int LENGTH_OFFSET = 1;
    private final int INDEX_OFFSET = 1;

    private static final Logger logger = Logger.getLogger(CommandParser.class.getName());  
    
    /**
     * This method builds a {@code Task} object.
     * 
     * Tasks without date do not have any date/time specified.
     * Words with prepositions might not be dated.
     * Words without prepositions is dated if time is explicitly specified.
     * 
     * @param commandString
     * 			user input {@code String}
     * @return {@code Task} built
     * @throws InvalidLabelFormat 
     */
    public Task parseAdd(String commandString) throws InvalidLabelFormat {
    	logger.setLevel(Level.OFF);
    	
    	assert(commandString != null);
    	assert(!commandString.isEmpty());
        logger.log(Level.INFO, "Parsing for ADD command.");
        
        String title = null;
        String label = null;
        int numberOfDate = 0;
        Date startDate = null;
        Date endDate = null;
        boolean hasStartDate = false;
        boolean isLabelPresent;
        boolean hasPreposition;
        boolean hasTime;
        title = commandString;

        hasPreposition = checkForPrepositions(commandString, PREPOSITION_ALL);
        hasTime = checkForTime(commandString);
        
        if (hasPreposition || hasTime) {
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
            	
            	if (hasTime && hasPreposition == false) {
            		startDate = getDate(dates, DATE_INDEX);
            		endDate = null;
        		}
            	
                title = removeDateFromTitle(title, startDate, endDate);
            }
        }
        
        isLabelPresent = checkForLabel(commandString);
        if (isLabelPresent) {
        	try {
        		label = extractLabel(commandString);
        	} catch (Exception e) {
        		throw new InvalidLabelFormat("Invalid label input detected.");
        	}
            title = removeLabelFromTitle(title, label);
        }

        Task task = new Task (title, startDate, endDate, label);
        logger.log(Level.INFO, "Task object built.");
        return task;
    }
    
    /**
     * This method checks the {@code String} taken in with a list of prepositions.
     * 
     * @param commandString
     * 			{@code String} user input
     * @param type
     * 			{@code Boolean} flag for type of list checked against.
     * 			It should be comprehensive and the {@code Boolean} should be true.
     * @return {@code Boolean} indicating if prepositions are detected in {@code String}
     */
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
     * This method generate a list of pre-defined prepositions to be used.
     * 
     * @param prepositionType
     * 			{@code Boolean} flag indicating the list type.
     * 			True for the whole list while false for a partial list.
     * @return {@code ArrayList<String>} containing prepositions
     */
    private ArrayList<String> populatePrepositions(boolean prepositionType) {
    	ArrayList<String> prepositions = new ArrayList<String>();
    	
    	if (prepositionType) {
	    	prepositions.add("from");
	    	prepositions.add("at");
	    	prepositions.add("on");
	    	prepositions.add("by");
	    	prepositions.add("before");
	    	prepositions.add("after");
	    	prepositions.add("to");
	    	prepositions.add("-");
    	} else {
    		prepositions.add("from");
        	prepositions.add("after");
        	prepositions.add("at");
        	prepositions.add("on");
    	}
    	
    	return prepositions;
    }
    
    /**
     * Check if valid time is specified.
     * 24format not supported because can be confused with normal numbers.
     * 
     * @param commandString
     * 			{@code String user input}
     * @return {@code Boolean} if time found
     */
    private boolean checkForTime(String commandString) {
    	boolean match = false;
    	List<String> words = new ArrayList<String>(Arrays.asList(commandString.toLowerCase().split(" ")));
    	
    	for (int i = 0; i< words.size(); i++) {
			match = Pattern.matches(TIME_STRING_PATTERN, words.get(i));
			if (match) {
				return true;
			}
    	}
    	return false;		
    }
    
    /**
     * This method corrects user input of dd/mm into mm/dd for date parsing.
     * 
     * @param commandString
     * 			{@code String} user input
     * @return {@code String} with the date fields swapped
     */
    private String detectAndCorrectDateInput(String commandString) {
		boolean match = false;
		String swapped = "";
		
		//Preserve capitalization by not using toLowerCase
		List<String> words = new ArrayList<String>(Arrays.asList(commandString.split(" ")));
		
		for (int i = 0; i< words.size(); i++) {
			match = Pattern.matches(DATE_STRING_PATTERN, words.get(i));
			if (match) {
				if (words.get(i).contains("/")) {
					List<String> date = new ArrayList<String>(Arrays.asList(words.get(i).split("/")));
					swapped = date.get(1).concat("/").concat(date.get(0));
				} else if (words.get(i).contains("-")) {
					List<String> date = new ArrayList<String>(Arrays.asList(words.get(i).split("-")));
					swapped = date.get(1).concat("-").concat(date.get(0));
				}
				
				words.set(i, swapped);
			}
		}

		return String.join(" ", words);
	}
    
    private List<Date> parseDate(String commandString) {
        PrettyTimeParser parser = new PrettyTimeParser();
        List<Date> dates = parser.parse(commandString);
        
        Date now = new Date();
        Date update = null;
        
        Calendar today = Calendar.getInstance();
        today.setTime(now);
        
        for (int i = 0; i < dates.size(); i++) {
        	if (dates.get(i).before(now)) {
        		Calendar cal = Calendar.getInstance();
		    	cal.setTime(dates.get(i));
        		
		    	if (!checkIsInfoPresent(commandString, dates)) {
		    		if (checkForTime(commandString)) {
    			    	cal.add(Calendar.DATE, 1);
    			    	update = cal.getTime();
    			    	dates.set(i,update);
            		} else {
    			    	cal.add(Calendar.HOUR_OF_DAY, 12);
    					update = cal.getTime();
    					dates.set(i,update);
    				}
		    	}
        	}
        	
        	if (dates.size() == 2 && update != null) {
        		now = update;
        	}
        }
        return dates;
    }
    
    private Date getDate(List<Date> dates, int index) {
        return dates.get(index);
    }
    
    /**
     * This method removes date information from the {@code String} taken in.
     * 
     * @param title
     * 			{code Task} title
     * @param startDate
     * 			{code Task} start date
     * @param endDate
     * 			{@code Task} end date
     * @return {@code String} without date information
     */
    private String removeDateFromTitle(String title, Date startDate, Date endDate) {       
    	List<Date> datesList = parseDate(detectAndCorrectDateInput(title));
    	int numberOfDate = datesList.size();
        LocalDateTime dateTime;
        
        if (startDate != null) {
        	dateTime = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        } else {
        	dateTime = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        
        for (int i = 0; i < DATE_MAX_SIZE; i++) {
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
             } else {
            	 break;
             }
        }
    	return title;
    }

	private ArrayList<String> getPossibleDates(LocalDateTime dateTime) {
		ArrayList<String> dates = new ArrayList<String>();
		dates.add(Integer.toString(dateTime.getDayOfMonth()));
		dates.add(dateTime.format(DateTimeFormatter.ofPattern("d/M")));
		dates.add(dateTime.format(DateTimeFormatter.ofPattern("d/MM")));
		dates.add(dateTime.format(DateTimeFormatter.ofPattern("dd/M")));
		dates.add(dateTime.format(DateTimeFormatter.ofPattern("dd/MM")));
		dates.add(dateTime.format(DateTimeFormatter.ofPattern("d-M")));
		dates.add(dateTime.format(DateTimeFormatter.ofPattern("d-MM")));
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
		
		int date = dateTime.getDayOfMonth();
		int month = dateTime.getMonthValue();
		LocalDateTime today = LocalDateTime.now();
		if (month == today.getMonthValue()) {
			if (date == today.getDayOfMonth()) {
				days.add("today");
			} else if (date == (today.getDayOfMonth()+1)) {
				days.add("tomorrow");
			}
		}
		return days;
	}
    
	private ArrayList<String> getPossibleTimes(LocalDateTime dateTime) {
		ArrayList<String> timings = new ArrayList<String>();
		int hour = dateTime.getHour();
		int min = dateTime.getMinute();
		
		assert(hour >= 0);
		assert(min >= 0);
		
		String minute = ":";
		if (min < DOUBLE_DIGIT) {
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
     * This method checks for and removes {@code ArrayList<String>} of targeted word from {@code String} title.
     * 
     * If word to be removed is found, it checks if the word before it is a preposition.
     * If preposition found, both are removed.
     * Else, only the matching word is removed.
     * 
     * @param title
     * 			{@code String} to be checked
     * @param toBeRemoved
     * 			{@code ArrayList<String>} of words to be removed
     * @return {@code String} with targeted words removed
     */
    private String checkAndRemove(String title, ArrayList<String> toBeRemoved) {
    	int index;
    	boolean isPreposition;
    
    	for (int i = 0; i < toBeRemoved.size(); i++) {
    		String toBeReplaced = "";
        	List<String> words = new ArrayList<String>(Arrays.asList(title.toLowerCase().split(" ")));
        
        	if (words.contains(toBeRemoved.get(i))) {
    			toBeReplaced = toBeReplaced.concat(toBeRemoved.get(i));

    			index = words.indexOf(toBeRemoved.get(i));
    			if (index != 0) {
	    			index = index - INDEX_OFFSET;
	    			isPreposition = checkIsPreposition(title, index, PREPOSITION_ALL);
	
	    			if (isPreposition) {
	    				toBeReplaced = words.get(index).concat(" ").concat(toBeReplaced);
	    			}
    			}
    		}
        	
    		//remove regardless of case
        	toBeReplaced = "(?i)".concat(toBeReplaced); 
        	title = title.replaceAll(toBeReplaced, "");
    	}
    	
    	return title.replaceAll("\\s+", " ").trim();
    }

    private boolean checkIsInfoPresent(String title, List<Date> dateParsed) {
    	 LocalDateTime dateTime;
         dateTime = dateParsed.get(0).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
         
         for (int i = 0; i < DATE_MAX_SIZE; i++) {
        	 ArrayList<String> dates = getPossibleDates(dateTime);
             ArrayList<String> months = getPossibleMonths(dateTime);
             ArrayList<String> days = getPossibleDays(dateTime);
             
          	if (isInfoPresent(title, dates)) {
          		return true;
          	} else if (isInfoPresent(title, months)) {
          		return true;
          	} else if (isInfoPresent(title, days)) {
          		return true;
          	}

             if (dateParsed.size() == DATE_MAX_SIZE) {
            	 dateTime = dateParsed.get(1).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
             } else {
            	 return false;
             }
        }
         return false;         
    }
    
    private boolean isInfoPresent(String title, ArrayList<String> toBeRemoved) {
    	for (int i = 0; i < toBeRemoved.size(); i++) {
        	List<String> words = new ArrayList<String>(Arrays.asList(title.toLowerCase().split(" ")));
        
        	if (words.contains(toBeRemoved.get(i))) {
    			return true;
        	}
    	}
    	return false;
    }
    
    /**
     * This method checks if a word is a preposition.
     * 
     * @param title
     * 			{@code String} title
     * @param index
     * 			{@code int} index of the word from its title
     * @return {@code Boolean} true if preposition is found
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
    
    private boolean checkForLabel(String commandString) {
        if (commandString.contains("#")) {
            return true;
        } else {
            return false;
        }
    }
    
    private String extractLabel(String commandString) throws InvalidLabelFormat{
        int index = commandString.indexOf("#");
        index = index + LENGTH_OFFSET;
        String substring = commandString.substring(index);
        String label = substring.trim();
        label = getFirstWord(label);
        return label;
    }
    
    private String getFirstWord(String commandString) throws InvalidLabelFormat {
    	String word = "";
    	try {
    		word = commandString.split(" ")[0];
    	} catch (Exception e ) {
    		throw new InvalidLabelFormat();
    	}
    	
    	if (word.length() == 0) {
    		throw new InvalidLabelFormat();
    	}
    	
    	return word;
    }
    
    private String removeLabelFromTitle(String title, String label) {
        String tag = "#".concat(label);
        
        int index = title.indexOf(tag);
        index = index + label.length() + LENGTH_OFFSET;
        
        /*
        if (title.length() != index) {
            tag = tag.concat(" ");
        } else if (title.length() == index) {
            tag = " ".concat(tag);
        }
        */
        title = title.replace(tag, "");
        return title.replaceAll("\\s+", " ").trim();
    }
    
    
    public Task parseEdit(Task oldTask, String commandString) throws InvalidLabelFormat {
        int numberOfDate = 0;
        boolean hasStartDate = false;
    
        String title = oldTask.getTitle();
        String label = oldTask.getLabel();
        Date newStart = oldTask.getStartDate();
        Date newEnd = oldTask.getEndDate();
        Date createdDate = oldTask.getCreatedDate();
        
        Date startDate = null;
        Date endDate = null;
        
    	//remove edit command
    	commandString = removeFirst(commandString);
    	
    	String word = getFirstWord(commandString);
    	boolean isIndex = isIndex(word);
    	if (isIndex) {
    		commandString = removeFirst(commandString);
    	}
    	
    	boolean isLabelPresent = false;
    	isLabelPresent = checkForLabel(commandString);
        if (isLabelPresent) {
        	try {
        		label = extractLabel(commandString);
        		commandString = removeLabelFromTitle(commandString, label);
        	} catch (Exception e) {
        		throw new InvalidLabelFormat("Invalid label input detected.");
        	}
        }

        boolean hasPreposition = checkForPrepositions(commandString, PREPOSITION_ALL);
        boolean hasTime = checkForTime(commandString);
        System.out.println("here");
        if (hasPreposition || hasTime) {
        	System.out.println("before date parsing");
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
            	
            	if (hasTime && hasPreposition == false) {
            		startDate = getDate(dates, DATE_INDEX);
            		endDate = null;
        		}
            	
                commandString = removeDateFromTitle(commandString, startDate, endDate);
            }
        }
        
        
        	if (startDate != null && endDate != null) {
        		newStart = startDate;
        		newEnd = endDate;
        	} else {
        		if (startDate != null) {
        			newStart = startDate;
        			newEnd = null;
                }
                
                if (endDate != null) {
                	newStart = null;
                	newEnd = endDate;
                }
        	}
		
        
        if (commandString.length() > 0) {
        	title = commandString;
        }       
    	
        Task newTask = new Task(title, newStart, newEnd, label, createdDate);
    	return newTask;
    }
    
    private boolean isIndex(String word) {
    	try {
    		Integer.parseInt(word);
    	} catch(NumberFormatException e) {
    		return false;
    	}
    	return true;
    }
    
    private String removeFirst(String string) throws InvalidLabelFormat {
    	String first = getFirstWord(string);
    	int index = first.length() + LENGTH_OFFSET;
    	return string.substring(index, string.length());
    }
    

    
    /**
     * This method detects the types of indexes and processes them.
     * 
     * @param commandString
     * 			{@code String} user input
     * @return {@code ArrayList<Integer>} of index(es)
     * @throws InvalidTaskIndexFormat if format is invalid
     */
    public ArrayList<Integer> parseIndexes(String commandString) throws InvalidTaskIndexFormat {
    	try {
	    	logger.log(Level.INFO, "Parsing indexes.");
	    	String indexString = getIndexString(commandString);
	
	    	ArrayList<Integer> indexes = new ArrayList<Integer>();
	    	indexes = extractIndex(indexString);
	    	logger.log(Level.INFO, "Indexes retrieved.");
	    	return indexes;
    	} catch (Exception e) {
    		throw new InvalidTaskIndexFormat("Invalid indexes input detected.");
    	}
    }
    
    private String getIndexString(String commandString) throws InvalidLabelFormat{
        int index = 0;
        String command = getFirstWord(commandString); //will not fail because without command UI won't call
        
        index = command.length() + LENGTH_OFFSET;
        
        String indexString = commandString.substring(index, commandString.length());
        indexString = removeWhiteSpace(indexString);
        assert(!indexString.isEmpty());
        return indexString;
    }
    
    private String removeWhiteSpace(String string) {
        string = string.replaceAll("\\s","");
        assert(!string.isEmpty());
        return string;
    }
    
    /**
     * This method obtains all numbers based on {@code String} taken in.
     * 
     * @param index
     * 			{@code String} of index
     * @return {@code ArrayList<Integer>} of index(es) 
     * @throws InvalidTaskIndexFormat 
     */
    private ArrayList<Integer> extractIndex(String index) throws InvalidTaskIndexFormat {
        ArrayList<String> indexes = new ArrayList<String>();
        ArrayList<String> tempRangedIndexes = new ArrayList<String>();
        ArrayList<Integer> multipleIndexes = new ArrayList<Integer>();
        ArrayList<Integer> rangedIndexes = new ArrayList<Integer>();
        
        Collections.addAll(indexes, index.split(","));
        
        for (int i = 0; i < indexes.size(); i++) {
        	if (indexes.get(i).contains("-")) {
        		Collections.addAll(tempRangedIndexes, indexes.get(i).split("-"));
        		
        		//remove all empty after splitting
        		//else will cause parseInt to fail
        		tempRangedIndexes = removeEmpty(tempRangedIndexes);
        		rangedIndexes = getRangedIndexes(tempRangedIndexes);
        		
        		if (rangedIndexes.size() == 1) {
        			throw new InvalidTaskIndexFormat();
        		}

        		multipleIndexes.addAll(getMultipleIndexes(rangedIndexes));
        		
        		tempRangedIndexes.clear();
        		rangedIndexes.clear();
        	} else {
        		int indexToAdd;
        		indexToAdd = Integer.parseInt(indexes.get(i));
        		multipleIndexes.add(indexToAdd);
        	}
        }
        Collections.sort(multipleIndexes);
        return multipleIndexes;
    }
    
    private ArrayList<String> removeEmpty(ArrayList<String> arrayStrings) {
    	ArrayList<String> empty = new ArrayList<String>();
		empty.add("");
		arrayStrings.removeAll(empty);
		return arrayStrings;
    }
    
    private ArrayList<Integer> getRangedIndexes(ArrayList<String> arrayStrings) {
    	ArrayList<Integer> ranged = new ArrayList<Integer>();
    	
    	for (int i = 0; i < arrayStrings.size(); i++) {
			ranged.add(Integer.parseInt(arrayStrings.get(i)));
		}
    	
    	return ranged;    	
    }
    
    private ArrayList<Integer> getMultipleIndexes(ArrayList<Integer> arrayIntegers) {
    	ArrayList<Integer> multiple = new ArrayList<Integer>();
    	int start, end;
    	
    	for (int i = 0; i < arrayIntegers.size() - 1; i++) {
    		if (arrayIntegers.get(i) < arrayIntegers.get(i+1)) {
    			start = arrayIntegers.get(i);
    			end = arrayIntegers.get(i+1);
    		} else {
    			start = arrayIntegers.get(i+1);
    			end = arrayIntegers.get(i);
    		}
    		
    		for (int j = start; j <= end; j++) {
    			if (!multiple.contains(j)) {
    				multiple.add(j);
    			}
    		}
		}
    	
    	return multiple;
    }
    
    @SuppressWarnings("serial")
	public class InvalidTaskIndexFormat extends Exception {
    	public InvalidTaskIndexFormat() {
    		logger.log(Level.WARNING, "NumberFormatException: Indexes cannot be parsed by parser.");
    		logger.log(Level.WARNING, "InvalidTaskIndexFormat exception thrown.");
    	}

    	public InvalidTaskIndexFormat(String message) {
    		super (message);
    		logger.log(Level.WARNING, "NumberFormatException: Indexes cannot be parsed by parser.");
    		logger.log(Level.WARNING, "InvalidTaskIndexFormat exception thrown.");    		
    	}
    }
    
    @SuppressWarnings("serial")
    public class InvalidLabelFormat extends Exception {
    	public InvalidLabelFormat() {
    		logger.log(Level.WARNING, "Label cannot be parsed by parser.");
    		logger.log(Level.WARNING, "InvalidLabelFormat exception thrown.");
    	}

    	public InvalidLabelFormat(String message) {
    		super (message);
    		logger.log(Level.WARNING, "Label cannot be parsed by parser.");
    		logger.log(Level.WARNING, "InvalidLabelFormat exception thrown.");    		
    	}
    }
}