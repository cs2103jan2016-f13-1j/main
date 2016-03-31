package main.parser;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

import main.data.ParseIndexResult;
import main.data.Task;

/**
 * @author Joleen
 *
 */

public class CommandParser {
	private final int DATE_INDEX = 0;
	private final int DATE_START = 0;
	private final int DATE_END = 1;
	private final int DATE_MAX_SIZE = 2;
	
	private final String REGEX_PREPOSITION_STARTING = "(?i)\\b(from|after|at|on)\\b ?"; 
	private final String REGEX_PREPOSITION_ALL = "(?i)(\\b(from|after|at|on|by|before|to)\\b ?)";
	public final String REGEX_DAYS = "\\b((?i)((mon)(day)?|(tue)(sday|s)?|"
			+ "(wed)(nesday|s)?|(thu)(rsday|rs|r)?|(fri)(day)?|(sat)(urday)?|(sun)(day)?))\\b";
	private final String REGEX_DATE_NUM = "\\b((0?[1-9]|[12][0-9]|3[01])([/|-])(0?[1-9]|1[012]))\\b";	
	private final String REGEX_DATE_TEXT = "\\b(0?[1-9]|[12][0-9]|3[01]) ";
	private final String REGEX_MONTH_TEXT = "((?i)(jan)(uary)?|"
			+ "(feb)(ruary)?|" + "(mar)(ch)?|" + "(apr)(il)?|" + "(may)|"
			+ "(jun)(e)?|" + "(jul)(y)?|" + "(aug)(ust)?|" + "(sep)(tember)?|"
			+ "(oct)(ober)?|" + "(nov)(ember)?|" + "(dec)(ember)?)\\b";
	private final String REGEX_TIME_TWELVE = "((1[012]|0?[1-9])(([:|.][0-5][0-9])?))";
	private final String REGEX_AM_PM = "(?i)(am|pm)";

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
	 * @param inputString
	 * 		   {@code String} input to be processed
	 * @return {@code Task} built
	 * @throws InvalidLabelFormat 
	 * @throws InvalidTitle 
	 */
	public Task parseAdd(String inputString) throws InvalidLabelFormat, InvalidTitle {
		logger.setLevel(Level.OFF);

		assert(inputString != null);
		assert(!inputString.isEmpty());
		logger.log(Level.INFO, "Parsing for ADD command.");

		String title = null;
		String label = null;
		Date startDate = null;
		Date endDate = null;
		
		boolean hasDay = false;
		boolean hasDate = false;
		boolean hasTime = false;
		boolean hasTimeWithoutAmPm = false;
		boolean hasDateRange = false;
		boolean hasPreposition = false;
		boolean hasStartDate = false;
		boolean hasLabel = false;
		int numberOfDate = 0;
		List<Date> dates = new ArrayList<Date>();
		
		title = inputString;
		
		hasLabel = checkForLabel(inputString);
		if (hasLabel) {
			try {
				label = getLabel(inputString);
			} catch (Exception e) {
				throw new InvalidLabelFormat("Invalid label input detected.");
			}
			
			title = removeLabelFromTitle(title, label);
		}
		
		hasDay = checkForDay(inputString);
		hasDate =  checkForDate(inputString)  || checkForDateText(inputString);
		
		hasTime = checkForTime(inputString);
		if (hasTime) {
			hasDateRange = checkForRangeTime(inputString);
			if (hasDateRange) {
				inputString = correctRangeTime(inputString);
			}
		}

		hasPreposition = checkForPrepositions(inputString);
		if (hasPreposition) {
			hasTimeWithoutAmPm = checkForTimeWithoutAmPm(inputString);
			hasStartDate = checkForStartPreposition(inputString);
		} 
		
		if (hasDate && hasTime) {
			dates = parseDateTime(inputString);
		} else if (hasDate) {
			dates = parseDateOnly(inputString);
		} else if (hasDay && hasTime) {
			dates = parseDateTime(inputString);
		} else if (hasDay) {
			dates = parseDayOnly(inputString);
		} else if (hasTime) {
			dates = parseTimeOnly(inputString);
		} 
		
		if (hasPreposition && hasTimeWithoutAmPm) {
			dates = parseDateTime(inputString);
			dates = fixTimeForWithoutAmPm(dates);
		}
		
		numberOfDate = dates.size();
		if (numberOfDate > 0) {
			dates = assignDates(dates, hasPreposition, hasStartDate);
			startDate = dates.get(DATE_START);
			endDate = dates.get(DATE_END);

			if (hasDateRange) {
				title = removeRangeFromTitle(title);
			}
			
			if (hasDay) {
				//quotes not escaped
				title = removeDayFromTitle(title);
			}
			
			title = removeDateFromTitle(title, dates);
		}
		
		Task task = new Task (title, startDate, endDate, label);
		logger.log(Level.INFO, "Task object built.");
		
		if (title.length() == 0) {
			throw new InvalidTitle("Invalid title detected.", task);
		}
		
		return task;
	}

	/**
	 * This method checks for indication of label through detection of '#'.
	 * 
	 * @param inputString
	 * 			{@code String} input to be checked
	 * @return {@code boolean} true if found
	 */
	private boolean checkForLabel(String inputString) {
		if (inputString.contains("#")) {
			return true;
		} else {
			return false;
		}
	}

	private String getLabel(String inputString) throws InvalidLabelFormat {
		int index = inputString.indexOf("#");
		index = index + LENGTH_OFFSET;
		String substring = inputString.substring(index);
		String label = substring.trim();
		label = getFirstWord(label);
		return label;
	}

	private String getFirstWord(String inputString) throws InvalidLabelFormat {
		String word = "";
		try {
			word = inputString.split(" ")[0];
		} catch (Exception e ) {
			throw new InvalidLabelFormat();
		}

		if (word.length() == 0) {
			throw new InvalidLabelFormat();
		}

		return word;
	}

	/**
	 * This method removes label from the title.
	 * 
	 * @param title
	 * 			{@code String} input for label to be removed from
	 * @param label
	 * 			{@code String} label to be removed
	 * @return {@code String} label removed
	 */
	private String removeLabelFromTitle(String title, String label) {
		String tag = "#".concat(label);

		int index = title.indexOf(tag);
		index = index + label.length() + LENGTH_OFFSET;

		title = title.replace(tag, "");
		title = removeExtraSpaces(title);
		return title;
	}
	
	private String removeExtraSpaces(String inputString) {
		return inputString.replaceAll("\\s+", " ").trim();
	}
	
	/**
	 * This method checks if a valid day is specified.
	 * 
	 * @param inputString
	 * 			{@code String} input to be check
	 * @return {@code boolean} true if day found
	 */
	private boolean checkForDay(String inputString) {
		String regex = getDayRegex();
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(inputString);
		return matcher.find();
	}
	
	private String getDayRegex() {
		return REGEX_PREPOSITION_ALL + "?" + REGEX_DAYS;
	}
	
	/**
	 * This method checks if a valid date is specified in dd/mm format.
	 * 
	 * @param inputString
	 * 			{@code String} input to be checked
	 * @return {@code boolean} true if date detected
	 */
	private boolean checkForDate(String inputString) {
		String regex = getDateRegex();
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(inputString);        
		return matcher.find();
	}

	private String getDateRegex() {
		return REGEX_PREPOSITION_ALL + "?" + REGEX_DATE_NUM;
	}

	/**
	 * This method checks if a valid date is specified in textual format.
	 * Eg: 26 March
	 * 
	 * @param inputString
	 * 			{@code String} input to be checked
	 * @return {@code boolean} true if date detected
	 */
	private boolean checkForDateText(String inputString) {
		String regex = getDateRegexText();
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(inputString);
		return matcher.find();
	}

	private String getDateRegexText() {
		return REGEX_PREPOSITION_ALL + "?" + REGEX_DATE_TEXT + REGEX_MONTH_TEXT;
	}
	
	/**
	 * This method checks if a valid time is specified.
	 * 24format not supported because can be confused with normal numbers.
	 * 
	 * @param inputString
	 * 			{@code String} input to be check
	 * @return {@code boolean} true if time found
	 */
	public boolean checkForTime(String inputString) {
		String regex = getTimeRegex();
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(inputString);
		return matcher.find();
	}

	private String getTimeRegex() {
		return "\\b" + REGEX_TIME_TWELVE + REGEX_AM_PM;
	}
	
	/**
	 * This method checks if a valid time range is specified.
	 * 
	 * @param inputString
	 * 			{@code String} input to be check
	 * @return {@code boolean} true if time range found
	 */
	private boolean checkForRangeTime(String inputString) {
		String regex = getTimeRangeRegex();
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(inputString);
		return matcher.find();
	}

	private String getTimeRangeRegex() {
		return "\\b" + REGEX_TIME_TWELVE + REGEX_AM_PM + "?" 
				+ "\\s?-\\s?" +
				"\\b" + REGEX_TIME_TWELVE + REGEX_AM_PM
				+ "|" +
				"\\b" + REGEX_TIME_TWELVE + REGEX_AM_PM 
				+ "\\s?-\\s?" +
				"\\b" + REGEX_TIME_TWELVE + REGEX_AM_PM + "?\\b";
	}

	/**
	 * This method corrects ranged time for date parsing.
	 * 
	 * @param inputString
	 * 			{@code String} input to be corrected
	 * @return {@code  String} with time range corrected
	 */
	private String correctRangeTime(String inputString) {
		inputString = inputString.replaceAll("()-()","$1 - $2");
		return inputString;
	}
	
	/**
	 * This method checks the {@code String} taken in for prepositions.
	 * 
	 * @param inputString
	 * 			{@code String} input to be checked
	 * @return {@code boolean} true if prepositions are detected
	 */
	private boolean checkForPrepositions(String inputString) {
		String regex = REGEX_PREPOSITION_ALL;
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(inputString);
		return matcher.find();
	}
	
	/**
	 * This method checks if a valid time is specified.
	 * Because this is used when preposition is detected,
	 * am/pm is not needed
	 * 
	 * @param inputString
	 * 			{@code String} input to be check
	 * @return {@code boolean} true if time found
	 */
	private boolean checkForTimeWithoutAmPm(String inputString) {
		String regex = getTimeRegexWithoutAmPm();
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(inputString);
		return matcher.find();
	}

	private String getTimeRegexWithoutAmPm() {
		return REGEX_PREPOSITION_ALL + "\\b " + REGEX_TIME_TWELVE + "\\b$";
	}
	
	/**
	 * This method checks the {@code String} taken in for prepositions.
	 * More specifically, it checks if there is preposition that indicates a starting time.
	 * 
	 * @param inputString
	 * 			{@code String} input to be checked
	 * @return {@code boolean} true if preposition found
	 */
	private boolean checkForStartPreposition(String inputString) {
		String regex = REGEX_PREPOSITION_STARTING;
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(inputString);
		return matcher.find();
	}
	
	/**
	 * This method uses PrettyTimeParser to generate dates from {@code String} inputString.
	 * @param inputString
	 * 			{@code String} input to be parsed
	 * @return {@code List<Date>} of dates generated if possible
	 */
	private List<Date> parseDateTime(String inputString) {
		PrettyTimeParser parser = new PrettyTimeParser();
		inputString = correctDateNumFormat(inputString);
		List<Date> dates = parser.parse(inputString);
		return dates;
	}
	
	/**
	 * This method corrects dd/mm into mm/dd for date parsing.
	 * 
	 * @param inputString
	 * 			{@code String} input to be corrected
	 * @return {@code String} with the date corrected
	 */
	private String correctDateNumFormat(String inputString) {
		boolean match = false;
		String swapped = "";

		//Preserve capitalization by not using toLowerCase
		List<String> words = new ArrayList<String>(Arrays.asList(inputString.split(" ")));

		for (int i = 0; i< words.size(); i++) {
			match = Pattern.matches(REGEX_DATE_NUM, words.get(i));
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
	
	/**
	 * This method parses the date only.
	 * Since time is not specified, it is set to 12am.
	 * 
	 * @param inputString
	 * 			{@code String} input to be parsed
	 * @return {@code List<Date>} of dates
	 */
	private List<Date> parseDateOnly(String inputString) {
		List<Date> dates = parseDateTime(inputString);
		int size = dates.size();
		for (int i = 0; i < size; i++) {
			dates.add(setTimeToZero(dates.get(i)));
		}
		
		dates.remove(0);
		if (size == DATE_MAX_SIZE) {
			dates.remove(0);	
		}
		
		return dates;
	}
	
	private Date setTimeToZero(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
	
	private List<Date> parseDayOnly(String inputString) {
		List<Date> dates = parseDateOnly(inputString);
		dates = fixDayRange(dates);

		return dates;
	}
	
	/**
	 * This method ensures that range are sequential.
	 * Corrects date parsed by PrettyTime.
	 * The end will come after the start.
	 * 
	 * @param dates
	 * 			{@code List<Date>>} to be corrected
	 * @return {@code List<Date>} corrected dates
	 */
	private List<Date> fixDayRange(List<Date> dates) {
		if (dates.size() == DATE_MAX_SIZE) {
			Date start = dates.get(DATE_START);
			Date end = dates.get(DATE_END);
			if (start.after(end)) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(end);
				cal.add(Calendar.WEEK_OF_YEAR, 1);
				dates.set(DATE_END, cal.getTime());
			}
		}
		return dates;
	}
	
	private List<Date> parseTimeOnly(String inputString) {
		List<Date> dates = parseDateTime(inputString);
		dates = fixTimeToNearest(dates, false);
		return dates;
	}
	
	/**
	 * This method sets the time if it has past and if no date is specified.
	 * It takes the next nearest time if possible.
	 * Else, it will be considered as past.
	 * 
	 * Eg:
	 * If now is 1pm, and 12pm is specified,
	 * it will be past because the next nearest 12 is 12am.
	 * However, if 12am is specified, it will be 12am of the next day.
	 * 
	 * @param dates
	 * 			{@code List<Date>} dates to be parsed
	 * @param hasDate
	 * 			{@code boolean} indicating if date is specified
	 * @return {@code List<Date>} of dates
	 */
	private List<Date> fixTimeToNearest(List<Date> dates, boolean hasDate) {
		Date now = new Date();
		Calendar currentDate = Calendar.getInstance();
		currentDate.setTime(now);
		
		Calendar date = Calendar.getInstance();
		if (!hasDate) {
			for (int i = 0; i < dates.size(); i++) {
				if (dates.get(i).before(now)) {
				    //date has past, need to check next nearest
					currentDate.add(Calendar.HOUR_OF_DAY, 12);
					
					date.setTime(dates.get(i));
					date.add(Calendar.DATE, 1);
					
					//if date is before, nearest have not past
					if (date.before(currentDate)) {
						dates.set(i,date.getTime());
					}
				}
				
				if (dates.size() == 2) {
					//update to start
					now = dates.get(i);
				}
			}
		}
		return dates;
	}
	
	/**
	 * This method sets the time for time specified without am/pm due to the presence of preposition.
	 * It will take the next possible time since am/pm is not specified.
	 * 
	 * Eg:
	 * If now is 1pm, and 10 is specified,
	 * it will be parsed as 10pm today.
	 * 
	 * If now is 10 pm, and 1 is specified,
	 * it will be parsed as 1am tomorrow.
	 * 
	 * @param dates
	 * 			{@code List<Date>} dates to be parsed
	 * @param hasDate
	 * 			{@code boolean} indicating if date is specified
	 * @return {@code List<Date>} of dates
	 */
	private List<Date> fixTimeForWithoutAmPm(List<Date> dates) {
		Date now = new Date();
		Calendar currentDate = Calendar.getInstance();
		currentDate.setTime(now);
		
		Calendar date = Calendar.getInstance();
		
			for (int i = 0; i < dates.size(); i++) {
				if (dates.get(i).before(now)) {
					//time has past, need to check next nearest
					date.setTime(dates.get(i));
					date.add(Calendar.HOUR_OF_DAY, 12);
					
					//if time is after current, still within the day
					if (date.after(currentDate)) {
						dates.set(i,date.getTime());
					} else {
						//time is before current, time has past
						//plus 12 hours to get to next nearest
						date.add(Calendar.HOUR_OF_DAY, 12);
						dates.set(i,date.getTime());
					}
				}
				
				if (dates.size() == 2) {
					//if is range, check against the fixed start date
					now = dates.get(i);
				}
			}
		return dates;
	}
	
	/**
	 * This method determines the start and end date for a task.
	 * 
	 * @param dates
	 * 			{@code List<Date} dates obtained from parsing
	 * @param hasPreposition
	 * 			{@code boolean} indicate if preposition is detected 
	 * @param hasStartDate
	 * 			{@code boolean} indicate if start date is detected through detection of preposition
	 * @return {@code List<Date>} of determined dates
	 */
	private List<Date> assignDates(List<Date> dates, boolean hasPreposition, boolean hasStartDate) {
		List<Date> assigned = new ArrayList<Date>();
		int numberOfDate = dates.size();
		
		if (numberOfDate == DATE_MAX_SIZE) {
			assigned.add(DATE_START,getDate(dates, DATE_START));
			assigned.add(DATE_END,getDate(dates, DATE_END));
		} else {
			if (hasPreposition) {
				if (hasStartDate) {
					assigned.add(DATE_START,getDate(dates, DATE_INDEX));
					assigned.add(DATE_END, null);
				} else {
					assigned.add(DATE_START, null);
					assigned.add(DATE_END,getDate(dates, DATE_INDEX));
				}
			} else {
				//no preposition
				//one date/time only
				//assume start
				assigned.add(DATE_START,getDate(dates, DATE_INDEX));
				assigned.add(DATE_END,null);
			}
		}
		return assigned;
	}
	
	private Date getDate(List<Date> dates, int index) {
		return dates.get(index);
	}
	
	/**
	 * This method removes the time range specified in {@code String} taken in.
	 * The regular expression used includes an optional preposition in front of the time range.
	 * If preposition exist, it will be removed.
	 * 
	 * @param title
	 * 			{@code String} input that has range to be removed
	 * @return {@code String} with time range removed
	 */
	private String removeRangeFromTitle(String title) {
		String regex = "(" + REGEX_PREPOSITION_ALL + "?)(" + getTimeRangeRegex()+ ")";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(title);
		while (matcher.find()) {
			title = title.replaceAll(matcher.group(), "");
		}
		title = removeExtraSpaces(title);
		return title;
	}
	
	/**
	 * This method removes day detected in {@code String} taken in.
	 * 
	 * @param title
	 * 			{@code String} input that has day to be removed
	 * @return {@code String} with day removed
	 */
	private String removeDayFromTitle(String title) {
		String regex = getDayRegex();
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(title);
		while (matcher.find()) {
			title = title.replaceAll(matcher.group(), "");
		}
		title = removeExtraSpaces(title);
		return title;
	}
	
	/**
	 * This method removes date information from the {@code String} taken in.
	 * 
	 * @param title
	 * 			{@code String} containing information
	 * @param startDate
	 * 			{@code Date} start date
	 * @param endDate
	 * 			{@code Date} end date
	 * @return {@code String} without date information
	 */
	private String removeDateFromTitle(String title, List<Date> datesList) {   
		LocalDateTime dateTime;
		
		Date startDate = datesList.get(DATE_START);
		Date endDate = datesList.get(DATE_END);

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

			if (startDate != null && endDate != null) {
				dateTime = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
			} else {
				break;
			}
		}
		return title;
	}

	/**
	 * This method generates an {@code ArrayList<String} of possible date formats from {@code LocalDateTime}.
	 * 
	 * @param dateTime
	 * 			{@code LocalDateTime} to generate different date formats
	 * @return {@code ArrayList<String} of possible date formats
	 */
	private ArrayList<String> getPossibleDates(LocalDateTime dateTime) {
		ArrayList<String> dates = new ArrayList<String>();
		String date = Integer.toString(dateTime.getDayOfMonth());
		dates.add(date);
		dates.add(date.concat("st"));
		dates.add(date.concat("nd"));
		dates.add(date.concat("rd"));
		dates.add(date.concat("th"));
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

	/**
	 * This method generates an {@code ArrayList<String} of possible month formats from {@code LocalDateTime}.
	 * 
	 * @param dateTime
	 * 			{@code LocalDateTime} to generate different month formats
	 * @return {@code ArrayList<String} of possible month formats
	 */
	private ArrayList<String> getPossibleMonths(LocalDateTime dateTime) {
		Locale locale = Locale.getDefault();
		ArrayList<String> months = new ArrayList<String>();
		Month month = dateTime.getMonth();
		months.add(month.toString().toLowerCase());
		months.add(month.getDisplayName(TextStyle.SHORT, locale).toLowerCase());
		return months;
	}

	/**
	 * This method generates an {@code ArrayList<String} of possible day formats from {@code LocalDateTime}.
	 * 
	 * @param dateTime
	 * 			{@code LocalDateTime} to generate different day formats
	 * @return {@code ArrayList<String} of possible day formats
	 */
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
	
	/**
	 * This method generates an {@code ArrayList<String} of possible time formats from {@code LocalDateTime}.
	 * 
	 * @param dateTime
	 * 			{@code LocalDateTime} to generate different time formats
	 * @return {@code ArrayList<String} of possible time formats
	 */
	private ArrayList<String> getPossibleTimes(LocalDateTime dateTime) {
		ArrayList<String> timings = new ArrayList<String>();
		int hour = dateTime.getHour();
		int min = dateTime.getMinute();

		assert(hour >= 0);
		assert(min >= 0);

		String colon = ":";
		String dot = ".";
		if (min < DOUBLE_DIGIT) {
			colon = colon.concat("0");
			dot = dot.concat("0");
		}

		colon = colon.concat(Integer.toString(dateTime.getMinute()));
		dot = dot.concat(Integer.toString(dateTime.getMinute()));

		timings.add(Integer.toString(hour));
		timings.add(Integer.toString(hour).concat(colon));
		timings.add(Integer.toString(hour).concat(dot));

		if (hour < 12) {
			if (hour == 0) {
				String temp = STRING_TWELVE;
				timings.add(temp.concat(STRING_AM));
				timings.add(temp.concat(colon).concat(STRING_AM));
				timings.add(temp.concat(dot).concat(STRING_AM));
			} else {
				timings.add(Integer.toString(hour).concat(STRING_AM));
				timings.add(Integer.toString(hour).concat(colon).concat(STRING_AM));
				timings.add(Integer.toString(hour).concat(dot).concat(STRING_AM));
			}
		} else if (hour >= 12) {	
			hour = hour - 12;
			if (hour == 0) {
				String temp = STRING_TWELVE;
				timings.add(temp.concat(STRING_PM));
				timings.add(temp.concat(colon).concat(STRING_PM));
				timings.add(temp.concat(dot).concat(STRING_PM));
			} else {
				timings.add(Integer.toString(hour));
				timings.add(Integer.toString(hour).concat(STRING_PM));
				timings.add(Integer.toString(hour).concat(colon).concat(STRING_PM));
				timings.add(Integer.toString(hour).concat(dot).concat(STRING_PM));
			}
		}
		return timings;
	}

	/**
	 * This method checks for and removes {@code ArrayList<String>} of targeted word from {@code String}.
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
		int index = 0;
		boolean isPreposition = false;

		for (int i = 0; i < toBeRemoved.size(); i++) {
			String toBeReplaced = "";
			List<String> words = new ArrayList<String>(Arrays.asList(title.toLowerCase().split(" ")));

			if (words.contains(toBeRemoved.get(i))) {
				toBeReplaced = toBeReplaced.concat(toBeRemoved.get(i));

				index = words.indexOf(toBeRemoved.get(i));
				if (index != 0) {
					index = index - INDEX_OFFSET;
					String word = getWord(title, index);
					word = word.concat(" ");

					isPreposition = checkForPrepositions(word);
					if (isPreposition) {
						toBeReplaced = word.concat(toBeReplaced);
					}
				}
			}

			toBeReplaced = "(?i)".concat(toBeReplaced); 
			title = title.replaceAll(toBeReplaced, "");
		}
		title = removeExtraSpaces(title);
		return title;
	}
	
	/**
	 * This method gets a word from {@code String} string base on {@code int} index.
	 * 
	 * @param title
	 * 			{@code String} input to obtain word from
	 * @param index
	 * 			{@code int} index of word to be obtained
	 * @return {@code String} word obtained
	 */
	private String getWord(String title, int index) {
		List<String> words = new ArrayList<String>(Arrays.asList(title.toLowerCase().split(" ")));
		String word = words.get(index);
		return word;
	}
	
	// =============================
	// Edit's stuff
	// =============================

	/**
	 * This method parses {@code String} for date.
	 * 
	 * @param inputString
	 * 			{@code String} input to be parsed
	 * @return {@code Date} if date found, {@code null} if date is not found
	 */
	public Date getDateForSearch(String inputString) {
		List<Date> dates = parseDateTime(inputString);
		if (dates.size() == 0) {
			return null;
		} else {
			return dates.get(0);
		}
	}
	
	/**
	 * This method detects if index is present in edit command.
	 * 
	 * @param inputString
	 * 			{@code String} input for index to be obtained
	 * @return {@code int} index if found, {@code int} -1 if index is not found
	 */
	public int getIndexForEdit(String inputString) {
		ArrayList<String> index = new ArrayList<String>();
		inputString = removeDateTime(inputString);

		Collections.addAll(index, inputString.split(" "));
		
		if (index.size() == 1) {
			return -1;
		} else {
			try {
				return (Integer.parseInt(index.get(1)));
			} catch (Exception e) {
				return -1;
			}
		}
	}
	
	/**
	 * This method removes date and time information from {@code String}.
	 * 
	 * @param inputString
	 * 			{@code String} input for information to be removed
	 * @return {@code String} without date and time information
	 */
	private String removeDateTime(String inputString) {
		boolean hasTime = false;
		boolean hasDateRange = false;
		boolean hasDate = false;
		String regex = "";

		hasTime = checkForTime(inputString);
		if (hasTime) {
			hasDateRange = checkForRangeTime(inputString);

			if (hasDateRange) {
				inputString = removeRangeFromTitle(inputString);
			}

			regex = getTimeRegex();
			inputString = removeRegex(regex, inputString);
		}

		hasDate = checkForDate(inputString);
		if (hasDate) {
			regex = getDateRegex();
			inputString = removeRegex(regex, inputString);
			hasDate = false;
		}

		hasDate = checkForDateText(inputString);
		if (hasDate) {
			regex = getDateRegexText();
			inputString = removeRegex(regex, inputString);
		}

		return inputString;
	}

	/**
	 * This method takes in {@code String} regex and removes if from {@code String} input.
	 * 
	 * @param regex
	 * 			{@code String} expressions to be removed
	 * @param inputString
	 * 			{@code String} input for expression to be removed
	 * @return {@code String} without expression 
	 */
	private String removeRegex(String regex, String inputString) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(inputString);

		while (matcher.find()) {
			String match = matcher.group();
			inputString = inputString.replaceAll(match,"");
			inputString = removeExtraSpaces(inputString); 		
		}
		
		return inputString;
	}

	/**
	 * This method allows for editing of exisitng tasks.
	 * 
	 * @param oldTask
	 * 			{@code Task} to be edited
	 * @param inputString
	 * 			{@code String} of new information
	 * @return {@code Task} edited task
	 * @throws InvalidLabelFormat
	 */
	public Task parseEdit(Task oldTask, String inputString) throws InvalidLabelFormat {		
		String newTitle = oldTask.getTitle();
		String newLabel = oldTask.getLabel();
		Date newStart = oldTask.getStartDate();
		Date newEnd = oldTask.getEndDate();
		Date createdDate = oldTask.getCreatedDate();
		int priority = oldTask.getPriority();
		
		Task editedTask;
		try {
			editedTask = parseAdd(inputString);
		} catch (InvalidTitle exception) {
			//exception doesn't matter if edit is using parseAdd
			editedTask = exception.getTask();
		}
		
		if (editedTask.getTitle().length() != 0) {
			newTitle = editedTask.getTitle();
		}
		
		if (editedTask.getLabel() != null) {
			newLabel = editedTask.getLabel();
		}
		
		boolean hasTime = false;
		boolean hasDate = false;
		boolean hasPreposition = false;
		boolean hasTimeWithoutAmPm = false;
		List<Date> dates = new ArrayList<Date>();
		
		hasDate =  checkForDate(inputString)  || checkForDateText(inputString);
		hasTime = checkForTime(inputString) || checkForRangeTime(inputString);
		
		if (!hasTime) {
			hasPreposition = checkForPrepositions(inputString);
			if (hasPreposition) {
				hasTimeWithoutAmPm = checkForTimeWithoutAmPm(inputString);
				if (hasTimeWithoutAmPm) {
					hasTime = true;
				}
			} 
		}
		
		if (editedTask.hasDate()) {			
			if (hasDate && !hasTime) {
				//only date, reuse time
				dates = reuseTime(editedTask, oldTask);
			} else if (!hasDate && hasTime) {
				//only time, reuse date
				dates = reuseDate(editedTask, oldTask);
			} else {
				//have both
				//update by overwriting
				dates.add(editedTask.getStartDate());
				dates.add(editedTask.getEndDate());
			}
			
			newStart = dates.get(DATE_START);
			newEnd = dates.get(DATE_END);
		}
		
		Task newTask = new Task(newTitle, newStart, newEnd, newLabel);
		newTask.setCreatedDate(createdDate);
		newTask.setPriority(priority);
		return newTask;
	}
	
	/**
	 * This method duplicates the time information in {@code Task} oldTask to {@code Task} editedTask.
	 * 
	 * @param editedTask
	 * 			{@code Task} edited task that needs the old time information
	 * @param oldTask
	 * 			{@code Task} with the old time information
	 * @return {@code List<Date>} of updated dates
	 */
	private List<Date> reuseTime(Task editedTask, Task oldTask) {
		List<Date> dates = new ArrayList<Date>();
		Calendar reuse = Calendar.getInstance();
		Calendar latest = Calendar.getInstance();
		
		Date startDate = editedTask.getStartDate();
		Date endDate = editedTask.getEndDate();
		Date oldStart = oldTask.getStartDate();
		Date oldEnd = oldTask.getEndDate();
	
		if (startDate != null) {
			if (oldStart != null) {  
				reuse.setTime(oldStart);
			} else if (oldEnd != null) {
				reuse.setTime(oldEnd);
			}
			
			latest = setHourMin(reuse, latest, startDate);
			oldStart = latest.getTime();
		} 

		if (endDate != null) {
			if (oldEnd != null) {  
				reuse.setTime(oldEnd);
			} else if (oldStart != null) {
				reuse.setTime(oldStart);
			}

			latest = setHourMin(reuse, latest, endDate);
			oldEnd = latest.getTime();
		}

		if (!oldTask.hasDate()) {
			//floating task
			//reset time here else it will take current
			if (oldStart != null) {
				oldStart = setTimeToZero(oldStart);
			}
			if (oldEnd != null) {
				oldEnd = setTimeToZero(oldEnd);
			}
		}
		
		//if edited is null, overwrite
		if (startDate == null) {
			oldStart = null;
		}
		
		if (endDate == null) {
			oldEnd = null;
		}
	
		dates.add(oldStart);
		dates.add(oldEnd);
		return dates;		
	}
	
	/**
	 * This method gets the hour and minutes from {@code Calendar} reuse and sets it in {@code Calendar} latest.
	 * 
	 * @param reuse
	 * 			{@code Calendar} for hour and minutes to be obtained
	 * @param latest
	 * 			{@code Calendar} for hour and minutes to be set
	 * @param date
	 * 			{@code Date} date to be set for {@code Calendar} latest
	 * @return {@code Calendar} with updated hour and minutes
	 */
	private Calendar setHourMin(Calendar reuse, Calendar latest, Date date) {
		int hour = reuse.get(Calendar.HOUR);
		int min = reuse.get(Calendar.MINUTE);
		int ampm = reuse.get(Calendar.AM_PM);

		latest.setTime(date);
		latest.set(Calendar.HOUR, hour);
		latest.set(Calendar.MINUTE, min);
		latest.set(Calendar.AM_PM, ampm);

		return latest;
	}
	
	/**
	 * This method duplicates the date information in {@code Task} oldTask to {@code Task} editedTask.
	 * 
	 * @param editedTask
	 * 			{@code Task} edited task that needs the old date information
	 * @param oldTask
	 * 			{@code Task} with the old date information
	 * @return {@code List<Date>} of updated dates
	 */
	private List<Date> reuseDate(Task editedTask, Task oldTask) {
		List<Date> dates = new ArrayList<Date>();
		Calendar reuse = Calendar.getInstance();
		Calendar latest = Calendar.getInstance();
		
		Date startDate = editedTask.getStartDate();
		Date endDate = editedTask.getEndDate();
		Date oldStart = oldTask.getStartDate();
		Date oldEnd = oldTask.getEndDate();
		
		if (startDate != null) {
			if (oldStart != null) {  
				reuse.setTime(oldStart);
			} else if (oldEnd != null) {
				reuse.setTime(oldEnd);
			}
			
			latest = setDayMonth(reuse, latest, startDate);
			oldStart = latest.getTime();
		}
		
		if (endDate != null) {
			if (oldEnd != null) {  
				reuse.setTime(oldEnd);
			} else if (oldStart != null) {
				reuse.setTime(oldStart);
			}
			
			latest = setDayMonth(reuse, latest, endDate);
			oldEnd = latest.getTime();
		}
		
		if (startDate == null) {
			oldStart = null;
		}
		
		if (endDate == null) {
			oldEnd = null;
		}
		
		dates.add(oldStart);
		dates.add(oldEnd);
		return dates;		
	}
	
	/**
	 * This method gets the day and month from {@code Calendar} reuse and sets it in {@code Calendar} latest.
	 * 
	 * @param reuse
	 * 			{@code Calendar} for day and month to be obtained
	 * @param latest
	 * 			{@code Calendar} for day and month to be set
	 * @param date
	 * 			{@code Date} date to be set for {@code Calendar} latest
	 * @return {@code Calendar} with updated day and month
	 */
	private Calendar setDayMonth(Calendar reuse, Calendar latest, Date date) {
		int day = reuse.get(Calendar.DAY_OF_MONTH);
		int month = reuse.get(Calendar.MONTH);

		latest.setTime(date);
		latest.set(Calendar.DAY_OF_MONTH, day);
		latest.set(Calendar.MONTH, month);

		return latest;
	}	

	// =============================
	// Parsing Indexes
	// =============================

	/**
	 * This method detects the types of indexes and processes them.
	 * 
	 * @param inputString
	 * 			{@code String} input to be processed
	 * @return {@code ArrayList<Integer>} of index(es)
	 * @throws InvalidTaskIndexFormat if format is invalid
	 */
	public ParseIndexResult parseIndexes(String inputString, int maxSize) throws InvalidTaskIndexFormat {
		assert (maxSize > 0);
		
		try {
			logger.log(Level.INFO, "Parsing indexes.");
			
			String indexString = getStringWithoutCommand(inputString);
			indexString = removeExtraSpaces(indexString);
			ArrayList<Integer> indexes = new ArrayList<Integer>();
			indexes = getIndex(indexString);
			ParseIndexResult indexResult = validateIndexes(indexes, maxSize);
			logger.log(Level.INFO, "Indexes retrieved.");
			return indexResult;
		} catch (Exception e) {
			throw new InvalidTaskIndexFormat("Invalid indexes input detected.");
		}
	}

	private String getStringWithoutCommand(String commandString) throws InvalidLabelFormat{
		int index = 0;
		String command = getFirstWord(commandString); //will not fail because without command UI won't call
		index = command.length() + LENGTH_OFFSET;
		String indexString = commandString.substring(index, commandString.length());
		assert(!indexString.isEmpty());
		return indexString;
	}

	/**
	 * This method obtains all numbers based on {@code String} taken in.
	 * 
	 * @param index
	 * 			{@code String} to be processed
	 * @return {@code ArrayList<Integer>} of index(es) 
	 * @throws InvalidTaskIndexFormat 
	 */
	private ArrayList<Integer> getIndex(String index) throws InvalidTaskIndexFormat {
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
				int indexToAdd = Integer.parseInt(indexes.get(i));
				if (!multipleIndexes.contains(indexToAdd)) {
					multipleIndexes.add(indexToAdd);
				}
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

	/**
	 * This method gets the range of indexes.
	 * 
	 * @param arrayStrings
	 * 			{@code ArrayList<String>} to be processed
	 * @return {@code ArrayList<Integer>} of index(es) range
	 */
	private ArrayList<Integer> getRangedIndexes(ArrayList<String> arrayStrings) {
		ArrayList<Integer> ranged = new ArrayList<Integer>();

		for (int i = 0; i < arrayStrings.size(); i++) {
			ranged.add(Integer.parseInt(arrayStrings.get(i)));
		}

		return ranged;    	
	}

	/**
	 * This method gets multiple indexes.
	 * 
	 * @param arrayIntegers
	 * 			{@code ArrayList<Integer>} to be processed
	 * @return {@code ArrayList<Integer>} of indexes
	 */
	private ArrayList<Integer> getMultipleIndexes(ArrayList<Integer> arrayIntegers) {
		ArrayList<Integer> multiple = new ArrayList<Integer>();
		int start, end;
		int possibleRange = arrayIntegers.size() - 1;

		for (int i = 0; i < possibleRange; i++) {
			//Check and fix range for descending cases
			if (arrayIntegers.get(i) < arrayIntegers.get(i+1)) {
				start = arrayIntegers.get(i);
				end = arrayIntegers.get(i+1);
			} else {
				start = arrayIntegers.get(i+1);
				end = arrayIntegers.get(i);
			}
			
			//prevent duplicates from being added twice
			for (int j = start; j <= end; j++) {
				if (!multiple.contains(j)) {
					multiple.add(j);
				}
			}
		}

		return multiple;
	}
	
	private ParseIndexResult validateIndexes(ArrayList<Integer> indexes, int maxSize) {
		ArrayList<Integer> validIndexes = new ArrayList<Integer>();
		ArrayList<String> invalidIndexes = new ArrayList<String>();
		
		for (int i = 0; i < indexes.size(); i++ ) {
			int index = indexes.get(i);
			if (index > maxSize) {
				invalidIndexes.add(Integer.toString(index));
			} else {
				validIndexes.add(index);
			}
		}
		
		ParseIndexResult indexesResult = new ParseIndexResult();
		if (invalidIndexes.size() > 0) {
			indexesResult.setHasInvalid(true);
			indexesResult.setInvalidIndexes(invalidIndexes);
		}
		
		if (validIndexes.size() > 0) {
			indexesResult.setHasValid(true);
			indexesResult.setValidIndexes(validIndexes);
		}
		
		return indexesResult;
	}


	// =============================
	// Exceptions
	// =============================

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
	
	@SuppressWarnings("serial")
	public class InvalidTimeFormat extends Exception {
		public InvalidTimeFormat() {
			logger.log(Level.WARNING, "Time cannot be parsed by parser.");
			logger.log(Level.WARNING, "InvalidTimeFormat exception thrown.");
		}

		public InvalidTimeFormat(String message) {
			super (message);
			logger.log(Level.WARNING, "Time cannot be parsed by parser.");
			logger.log(Level.WARNING, "InvalidTimeFormat exception thrown.");    		
		}
	}
	
	@SuppressWarnings("serial")
	public class InvalidTitle extends Exception {
		Task task;
		public InvalidTitle() {
			logger.log(Level.WARNING, "Title cannot be parsed by parser.");
			logger.log(Level.WARNING, "InvalidTitle exception thrown.");
		}

		public InvalidTitle(String message, Task task) {
			super (message);
			this.task = task;
			logger.log(Level.WARNING, "Title cannot be parsed by parser.");
			logger.log(Level.WARNING, "InvalidTitle exception thrown.");    		
		}
		
		public Task getTask() {
			return task;
		}
	}
}