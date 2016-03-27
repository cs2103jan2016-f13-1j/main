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

import main.data.Task;

/**
 * @author Joleen
 *
 */

public class CommandParser {
	private final int DATE_INDEX = 0;
	private final int DATE_START_RANGED = 0;
	private final int DATE_END_RANGED = 1;
	private final int DATE_MAX_SIZE = 2;
	//24 ([01]?[0-9]|2[0-3]):[0-5][0-9]
	
	private final String REGEX_PREPOSITION_STARTING = "(?i)\\b(from|after|at|on)\\b ?"; 
	private final String REGEX_PREPOSITION_ALL = "(?i)(\\b(from|after|at|on|by|before|to)\\b ?)";
	private final String REGEX_DATE_NUM = "\\b((0?[1-9]|[12][0-9]|3[01])([/|-])(0?[1-9]|1[012]))\\b";	
	private final String REGEX_DATE_TEXT = "\\b(0?[1-9]|[12][0-9]|3[01]) ";
	private final String REGEX_MONTH_TEXT = "((?i)(jan)(uary)?|"
			+ "(feb)(ruary)?|" + "(mar)(ch)?|" + "(apr)(il)?|" + "(may)|"
			+ "(jun)(e)?|" + "(jul)(y)?|" + "(aug)(ust)?|" + "(sep)(tember)?|"
			+ "(oct)(ober)?|" + "(nov)(ember)?|" + "(dec)(ember)?)\\b";
	private final String REGEX_TIME_TWELVE = "\\b((1[012]|0?[1-9])(([:|.][0-5][0-9])?))";
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
	 */
	public Task parseAdd(String inputString) throws InvalidLabelFormat {
		logger.setLevel(Level.OFF);

		assert(inputString != null);
		assert(!inputString.isEmpty());
		logger.log(Level.INFO, "Parsing for ADD command.");

		String title = null;
		String label = null;
		Date startDate = null;
		Date endDate = null;
		
		boolean hasTime = false;
		boolean hasDateRange = false;
		boolean hasPreposition = false;
		boolean hasStartDate = false;
		boolean hasLabel = false;
		int numberOfDate = 0;
		
		title = inputString;

		hasTime = checkForTime(inputString);
		if (hasTime) {
			hasDateRange = checkForRangeTime(inputString);
		}

		hasPreposition = checkForPrepositions(inputString);

		if (hasPreposition || hasTime) {
			if (hasDateRange) {
				inputString = correctRangeTime(inputString);
			}

			List<Date> dates = parseDateExtra(inputString);
			numberOfDate = dates.size();

			if (numberOfDate > 0) {
				if (numberOfDate == DATE_MAX_SIZE) {
					startDate = getDate(dates, DATE_START_RANGED);
					endDate = getDate(dates, DATE_END_RANGED);
				} else {
					hasStartDate = checkForStartPreposition(inputString);
					
					if (hasStartDate) {
						startDate = getDate(dates, DATE_INDEX);
					} else {
						endDate = getDate(dates, DATE_INDEX);
					}
				}

				if (hasTime == true && hasPreposition == false) {
					startDate = getDate(dates, DATE_INDEX);
					endDate = null;
				}

				if (hasDateRange) {
					title = removeRangeFromTitle(title);
				}

				title = removeDateFromTitle(title, startDate, endDate);
			}
		}

		hasLabel = checkForLabel(inputString);
		if (hasLabel) {
			try {
				label = getLabel(inputString);
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
	 * This method checks if a valid time is specified.
	 * 24format not supported because can be confused with normal numbers.
	 * 
	 * @param inputString
	 * 			{@code String} input to be check
	 * @return {@code boolean} true if time found
	 */
	private boolean checkForTime(String inputString) {
		String regex = getTimeRegex();
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(inputString);
		return matcher.find();
	}

	private String getTimeRegex() {
		return REGEX_TIME_TWELVE + REGEX_AM_PM;
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
		return REGEX_TIME_TWELVE + REGEX_AM_PM + "?" 
				+ "\\s?-\\s?" +
				REGEX_TIME_TWELVE + REGEX_AM_PM
				+ "|" +
				REGEX_TIME_TWELVE + REGEX_AM_PM 
				+ "\\s?-\\s?" +
				REGEX_TIME_TWELVE + REGEX_AM_PM + "?\\b";
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
	
	//here
	private List<Date> parseDateExtra(String inputString) {
		List<Date> dates = parseDate(inputString);
		
		Date now = new Date();
		Date update = null;

		Calendar today = Calendar.getInstance();
		today.setTime(now);

		for (int i = 0; i < dates.size(); i++) {
			if (dates.get(i).before(now)) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(dates.get(i));

				//not present means date not specified
				//can check time according to now
				//else leave it as overdue
				if (!checkForDate(inputString) && !checkForDateText(inputString)) {
					if (checkForTime(inputString)) {
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
	
	/**
	 * This method uses PrettyTimeParser to generate dates from {@code String} inputString.
	 * @param inputString
	 * 			{@code String} input to be parsed
	 * @return {@code List<Date>} of dates generated if possible
	 */
	private List<Date> parseDate(String inputString) {
		PrettyTimeParser parser = new PrettyTimeParser();
		inputString = correctDateNumFormat(inputString);
		List<Date> dates = parser.parse(inputString);
		return dates;
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

	private Date getDate(List<Date> dates, int index) {
		return dates.get(index);
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
	private String removeDateFromTitle(String title, Date startDate, Date endDate) {   
		LocalDateTime dateTime;
		List<Date> datesList = parseDateExtra(title);
		int numberOfDate = datesList.size();

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

	private String removeExtraSpaces(String inputString) {
		return inputString.replaceAll("\\s+", " ").trim();
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
		String regex = "(" + REGEX_PREPOSITION_ALL + "?)" + getTimeRangeRegex();;
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(title);
		if (matcher.find()) {
			title = title.replaceAll(matcher.group(), "");
		}
		title = removeExtraSpaces(title);
		return title;
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

	/**
	 * This method parses {@code String} for date.
	 * 
	 * @param inputString
	 * 			{@code String} input to be parsed
	 * @return {@code Date} if date found, {@code null} if date is not found
	 */
	public Date getDateForSearch(String inputString) {
		List<Date> dates = parseDate(inputString);
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
			hasDate = false;
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

	public Task parseEdit(Task oldTask, String commandString) throws InvalidLabelFormat {
		int numberOfDate = 0;
		boolean hasStartDate = false;
		String original = commandString;

		String title = oldTask.getTitle();
		String label = oldTask.getLabel();
		int priority = oldTask.getPriority();
		Date newStart = oldTask.getStartDate();
		Date newEnd = oldTask.getEndDate();
		Date createdDate = oldTask.getCreatedDate();

		Date startDate = null;
		Date endDate = null;

		boolean isLabelPresent = false;
		isLabelPresent = checkForLabel(commandString);
		if (isLabelPresent) {
			try {
				label = getLabel(commandString);
				commandString = removeLabelFromTitle(commandString, label);
			} catch (Exception e) {
				throw new InvalidLabelFormat("Invalid label input detected.");
			}
		}

		boolean hasPreposition = checkForPrepositions(commandString);
		boolean hasTime = checkForTime(commandString);
		boolean hasDateRange = false;
		boolean hasDate = false;

		if (hasTime) {
			hasDateRange = checkForRangeTime(commandString);
		}

		hasDate = checkForDate(commandString) || !checkForDateText(commandString);

		if (hasPreposition || hasTime || hasDate) {
			if (hasDateRange) {
				commandString = correctRangeTime(commandString);
			}

			List<Date> dates = parseDateExtra(commandString);
			numberOfDate = dates.size();
			if (numberOfDate > 0) {
				if (numberOfDate == DATE_MAX_SIZE) {
					startDate = getDate(dates, DATE_START_RANGED);
					endDate = getDate(dates, DATE_END_RANGED);
				} else {
					hasStartDate = checkForStartPreposition(commandString);
					if (hasStartDate) {
						startDate = getDate(dates, DATE_INDEX);
					} else {
						endDate = getDate(dates, DATE_INDEX);
					}
				}

				//dinner 7pm
				if ((hasTime == true || hasDate == true) && hasPreposition == false) {
					startDate = getDate(dates, DATE_INDEX);
					endDate = null;
				}

				if (hasDateRange) {
					commandString = removeRangeFromTitle(commandString);
				}

				commandString = removeDateFromTitle(commandString, startDate, endDate);
			}
		}

		//if date not detected in title
		//reuse date and month from old task
		if (!checkForDate(original) && !checkForDateText(original)) {
			Calendar newCal = Calendar.getInstance();
			Calendar currentCal = Calendar.getInstance();
			if (startDate != null) {
				//if old field only have one date
				//but now have range
				//borrow from the other field
				if (newStart != null) {  
					newCal.setTime(newStart);
				} else if (newEnd != null) {
					newCal.setTime(newEnd);
				}
				currentCal = setDayMonth(newCal, currentCal, startDate);
				newStart = currentCal.getTime();
			} else {
				newStart = null;
			}

			if (endDate != null) {
				if (newEnd != null) {  
					newCal.setTime(newEnd);
				} else if (newStart != null) {
					newCal.setTime(newStart);
				}

				currentCal = setDayMonth(newCal, currentCal, endDate);
				newEnd = currentCal.getTime();
			} else {
				newEnd = null;
			}
		} else if (!checkForTime(original) && !checkForRangeTime(original)) {
			//if time not detected in title
			//reuse time from old task
			/*
    		System.out.println(startDate);
    		System.out.println(endDate);
			 */
			Calendar newCal = Calendar.getInstance();
			Calendar currentCal = Calendar.getInstance();

			//somewhere here

			if (startDate != null) {
				if (newStart != null) {  
					newCal.setTime(newStart);
				} else if (newEnd != null) {
					newCal.setTime(newEnd);
				}

				currentCal = setHourMin(newCal, currentCal, startDate);
				newStart = currentCal.getTime();
			} else {
				newStart = null;
			}

			if (endDate != null) {
				if (newEnd != null) {  
					newCal.setTime(newEnd);
				} else if (newStart != null) {
					newCal.setTime(newStart);
				}

				currentCal = setHourMin(newCal, currentCal, endDate);
				newEnd = currentCal.getTime();
			} else {
				newEnd = null;
			}
		} else {
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
		}

		if (commandString.length() > 0) {
			title = commandString;
		}       

		Task newTask = new Task(title, newStart, newEnd, label);
		newTask.setCreatedDate(createdDate);
		newTask.setPriority(priority);
		return newTask;
	}

	private Calendar setDayMonth(Calendar newCal, Calendar currentCal, Date date) {
		int day = newCal.get(Calendar.DAY_OF_MONTH);
		int month = newCal.get(Calendar.MONTH);

		currentCal.setTime(date);
		currentCal.set(Calendar.DAY_OF_MONTH, day);
		currentCal.set(Calendar.MONTH, month);

		return currentCal;
	}

	private Calendar setHourMin(Calendar newCal, Calendar currentCal, Date date) {
		int hour = newCal.get(Calendar.HOUR);
		int min = newCal.get(Calendar.MINUTE);
		int ampm = newCal.get(Calendar.AM_PM);

		currentCal.setTime(date);
		currentCal.set(Calendar.HOUR, hour);
		currentCal.set(Calendar.MINUTE, min);
		currentCal.set(Calendar.AM_PM, ampm);

		return currentCal;
	}

	// =============================
	// Parsing Indexes
	// =============================

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
			String indexString = getStringWithoutCommand(commandString);
			indexString = removeWhiteSpace(indexString);

			ArrayList<Integer> indexes = new ArrayList<Integer>();
			indexes = extractIndex(indexString);
			logger.log(Level.INFO, "Indexes retrieved.");
			return indexes;
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
		int possibleRange = arrayIntegers.size() - 1;

		for (int i = 0; i < possibleRange; i++) {
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
}