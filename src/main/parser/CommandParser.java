//@@author A0126297X
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
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

import main.data.ParseIndexResult;
import main.data.Task;
import main.parser.exceptions.*;

public class CommandParser {
    private final String REGEX_PREPOSITION_ALL = "(?i)(\\b(from|after|at|on|by|before|to)\\b ?)";
    private final String REGEX_PREPOSITION_START = "(?i)\\b(from|after|at|on)\\b ?";
    private final String REGEX_DATE_NUM = "\\b((0?[1-9]|[12][0-9]|3[01])([/|-])(0?[1-9]|1[012]))\\b";
    private final String REGEX_DATE_NUM_YEAR = "\\b((0?[1-9]|[12][0-9]|3[01])([/|-])(0?[1-9]|1[012])([/|-])((19|20)?\\d\\d))\\b";
    private final String REGEX_DATE_TEXT = "\\b((0?[1-9]|[12][0-9]|3[01]) ?)";
    private final String REGEX_MONTH_TEXT = "((?i)(jan)(uary)?|" + "(feb)(ruary)?|" + "(mar)(ch)?|" + "(apr)(il)?|"
            + "(may)|" + "(jun)(e)?|" + "(jul)(y)?|" + "(aug)(ust)?|" + "(sep)(tember)?|" + "(oct)(ober)?|"
            + "(nov)(ember)?|" + "(dec)(ember)?)\\b";
    private final String REGEX_YEAR = "\\b ?((19|20)?\\d\\d)\\b";
    private final String REGEX_TIME_TWELVE = "((1[012]|0?[1-9])(([:|.][0-5][0-9])?))";
    private final String REGEX_TIME_PERIOD = "(?i)(am|pm)";
    private final String REGEX_DAYS = "\\b((?i)((mon)(day)?|(tue)(sday|s)?|"
            + "(wed)(nesday|s)?|(thu)(rsday|rs|r)?|(fri)(day)?|(sat)(urday)?|(sun)(day)?))\\b";
    private final String REGEX_WORD_DATED = "\\b(today|tdy|tonight|tomorrow|tmr|tml|tmrw)\\b";
    private final String REGEX_WORD_TIMED = "\\b(morning|afternoon|evening|midnight)\\b";
    private final String REGEX_PRIORITY = "\\b(?i)((priority) ?)(low|l|medium|m|mid|med|high|h)\\b";

    private final int DATE_INDEX = 0;
    private final int DATE_START = 0;
    private final int DATE_END = 1;
    private final int DATE_MAX_SIZE = 2;
    private final int DATE_FIELD_FULL = 3;

    private final String STRING_AM = "am";
    private final String STRING_PM = "pm";
    private final String STRING_TWELVE = "12";
    private final String STRING_NEXT_WEEK = "next week";

    private final int PRIORITY_LENGTH = 8;
    private final int PRIORITY_LOW = 1;
    private final int PRIORITY_MID = 2;
    private final int PRIORITY_HIGH = 3;
    
    private final int DOUBLE_DIGIT = 10;
    private final int LENGTH_OFFSET = 1;
    private final int INDEX_OFFSET = 1;

    private static final Logger logger = Logger.getLogger(CommandParser.class.getName());

    /**
     * This method builds a {@code Task} object.
     * 
     * Tasks without date do not have any date/time specified.
     * If a task has only the date specified, the time is default set to 12am.
     * Words with prepositions might not be dated.
     * Words without prepositions is dated if time is explicitly specified.
     * 
     * @param inputString
     *            {@code String} input to be processed
     * @return {@code Task} built
     * @throws InvalidLabelFormat
     * @throws InvalidTitle
     */
    public Task parseAdd(String inputString) throws InvalidLabelFormat, InvalidTitle {
        assert (inputString != null);
        logger.log(Level.INFO, "Parsing for ADD command.");

        String title = null;
        String label = null;
        Date startDate = null;
        Date endDate = null;
        int priority = 0;

        boolean hasDay = false;
        boolean hasDate = false;
        boolean hasYear = false;
        boolean hasTime = false;
        boolean hasTimeRange = false;
        boolean hasPreposition = false;
        boolean hasTimeWithoutAmPm = false;
        boolean hasStartDate = false;
        boolean hasPriority = false;
        boolean hasLabel = false;
        boolean isDatedOnly = false;

        int numberOfDate = 0;
        List<Date> dates = new ArrayList<Date>();
        String regex = null;

        // check for day
        hasDay = checkForRegexMatch(getDayRegex(), inputString);

        // check for date with year in num format
        hasDate = checkForRegexMatch(getDateWithYearRegex(), inputString);
        if (hasDate) {
            hasYear = true;
        } else {
            // check for date in number format or text format
            hasDate = checkForRegexMatch(getDateRegex(), inputString)
                    || checkForRegexMatch(getDateTextRegex(), inputString);
        }

        // check for time with am/pm or check for word indicating time
        hasTime = checkForRegexMatch(getTimeRegex(), inputString) || checkForRegexMatch(REGEX_WORD_TIMED, inputString);
        hasTimeRange = checkForRegexMatch(getTimeRangeRegex(), inputString);

        // check for word indicating date
        if (checkForRegexMatch(REGEX_WORD_DATED, inputString)) {
            hasDate = true;
        }

        // check for preposition
        hasPreposition = checkForRegexMatch(REGEX_PREPOSITION_ALL, inputString);
        if (hasPreposition) {
            // check for time without am/pm
            hasTimeWithoutAmPm = checkForRegexMatch(getTimeWithoutAmPmRegex(), inputString);
            // check for start preposition only
            hasStartDate = checkForRegexMatch(REGEX_PREPOSITION_START, inputString);
        }

        inputString = correctUserInput(inputString);
        title = inputString;

        // check for priority
        hasPriority = checkForRegexMatch(REGEX_PRIORITY, inputString);
        if (hasPriority) {
            String priorityString = getPriorityString(inputString);
            priority = getPriority(priorityString);
            assert (priority > 0 && priority < 4);
            title = removeStringFromTitle(inputString, priorityString);
        }

        hasLabel = checkForLabel(inputString);
        if (hasLabel) {
            try {
                label = getLabel(inputString);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Label cannot be parsed by parser.");
                logger.log(Level.WARNING, "InvalidLabelFormat exception thrown.");
                throw new InvalidLabelFormat("Invalid label input detected.");
            }

            if (!checkValidLabel(label)) {
                label = null;
                hasLabel = false;
            }

            if (hasLabel) {
                String labelString = "#".concat(label);
                title = removeStringFromTitle(title, labelString);
            }
        }

        inputString = title;
        inputString = correctDateNumFormat(inputString, hasYear);

        if (hasDate && hasTime) {
            dates = parseDateTime(inputString);
        } else if (hasDate) {
            dates = parseDateOnly(inputString);
            isDatedOnly = true;
        } else if (hasDay && hasTime) {
            dates = parseDateTime(inputString);
        } else if (hasDay) {
            dates = parseDayOnly(inputString);
            isDatedOnly = true;
        } else if (hasTime) {
            dates = parseTimeOnly(inputString);
        }

        if (inputString.contains(STRING_NEXT_WEEK)) {
            if (!hasTime) {
                isDatedOnly = true;
            }

            dates = correctNextWeek(dates);
            regex = getNextWeekRegex();
            title = removeRegex(regex, title);
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

            if (hasDate) {
                regex = "\\b(the day after )" + REGEX_WORD_DATED;
                title = removeRegex(regex, title);
            }

            if (hasTime) {
                // remove timed word from title
                regex = REGEX_PREPOSITION_ALL + "?" + REGEX_WORD_TIMED;
                title = removeRegex(regex, title);

                if (hasTimeRange) {
                    // remove range from title
                    regex = "(" + REGEX_PREPOSITION_ALL + "?)(" + getTimeRangeRegex() + ")";
                    title = removeRegex(regex, title);
                }
            }

            if (hasDate) {
                if (hasYear) {
                    // remove date in num form with year from title
                    regex = getDateWithYearRegex();
                    title = removeRegex(regex, title);
                }

                // remove date in text form with year from title
                regex = getDateRegexTextWithYear();
                title = removeRegex(regex, title);

                // remove date in text form with corrected year due to shortform
                regex = "\\b ?'(\\d\\d)\\b";
                title = removeRegex(regex, title);

                // remove date in text form from title
                regex = getDateTextRegex();
                title = removeRegex(regex, title);
            }

            // remove remaining date information from title
            title = removeDateFromTitle(title, dates);
        }

        Task task = new Task(title, startDate, endDate, label);
        task.setPriority(priority);
        task.setIsDatedOnly(isDatedOnly);

        logger.log(Level.INFO, "Task object built.");

        if (title.length() == 0) {
            task.setTitle("<No title>");
            logger.log(Level.WARNING, "Title cannot be parsed by parser.");
            logger.log(Level.WARNING, "<No title> set for task's title.");
            logger.log(Level.WARNING, "InvalidTitle exception thrown.");
            throw new InvalidTitle("Invalid title detected.", task);
        }
        
        logger.log(Level.INFO, "Task object returned.");
        return task;
    }

    private boolean checkForRegexMatch(String regex, String inputString) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(inputString);
        return matcher.find();
    }

    private String getDayRegex() {
        return REGEX_PREPOSITION_ALL + "?" + REGEX_DAYS;
    }

    private String getDateWithYearRegex() {
        return REGEX_PREPOSITION_ALL + "?" + REGEX_DATE_NUM_YEAR;
    }

    private String getDateRegex() {
        return REGEX_PREPOSITION_ALL + "?" + REGEX_DATE_NUM;
    }

    private String getDateTextRegex() {
        return REGEX_PREPOSITION_ALL + "?" + REGEX_DATE_TEXT + REGEX_MONTH_TEXT;
    }

    private String getTimeRegex() {
        return "\\b" + REGEX_TIME_TWELVE + REGEX_TIME_PERIOD;
    }

    private String getTimeRangeRegex() {
        return "\\b" + REGEX_TIME_TWELVE + REGEX_TIME_PERIOD + "?" + "\\s?-\\s?" + "\\b" + REGEX_TIME_TWELVE
                + REGEX_TIME_PERIOD + "|" + "\\b" + REGEX_TIME_TWELVE + REGEX_TIME_PERIOD + "\\s?-\\s?" + "\\b"
                + REGEX_TIME_TWELVE + REGEX_TIME_PERIOD + "?\\b";
    }

    private String getTimeWithoutAmPmRegex() {
        return REGEX_PREPOSITION_ALL + "\\b " + REGEX_TIME_TWELVE + "\\b$";
    }

    private String correctUserInput(String inputString) {
        assert (inputString != null);
        boolean hasTimeRange = checkForRegexMatch(getTimeRangeRegex(), inputString);

        inputString = correctDateText(inputString);
        inputString = correctDateTextYear(inputString);
        inputString = correctDotTime(inputString);
        inputString = correctShorthand(inputString);

        if (hasTimeRange) {
            inputString = correctRangeTime(inputString);
        }

        inputString = removeExtraSpaces(inputString);
        return inputString;
    }

    /**
     * This method corrects date text in user input without spaces for date
     * parsing.
     * Eg: 1apr -> 1 apr
     * 
     * @param inputString
     *            {@code String} input to be corrected
     * @return {@code  String} with date text corrected
     */
    private String correctDateText(String inputString) {
        assert (inputString != null);
        String regex = REGEX_DATE_TEXT + REGEX_MONTH_TEXT;
        inputString = inputString.replaceAll(regex, "$1 $3");
        inputString = removeExtraSpaces(inputString);
        return inputString;
    }

    private String removeExtraSpaces(String inputString) {
        assert (inputString != null);
        return inputString.replaceAll("\\s+", " ").trim();
    }

    /**
     * This method corrects the date when the year is specified with only two
     * digits for parsing.
     * 
     * @param inputString
     *            {@code String} containing date with year
     * @return {@code String} with corrected date year
     */
    private String correctDateTextYear(String inputString) {
        assert (inputString != null);
        String regex = getDateTextRegex() + "\\b ?(\\d\\d)(?:$|\\s)";
        inputString = inputString.replaceAll(regex, "$2 $4 $5 '$29 ");
        return inputString;
    }

    /**
     * This method corrects time separated with a dot for date parsing.
     * Eg: 5.30pm
     * 
     * @param inputString
     *            {@code String} input to be corrected
     * @return {@code String} with time corrected
     */
    private String correctDotTime(String inputString) {
        assert (inputString != null);
        String regex = "\\b((1[012]|0?[1-9])(([:|.])([0-5][0-9])?))(?i)(am|pm)";
        inputString = inputString.replaceAll(regex, "$2:$5$6");
        return inputString;
    }

    /**
     * This method correct short forms used by the user for parsing.
     * 
     * @param inputString
     *            {@code String} containing short forms
     * @return {@code String} with short forms corrected
     */
    private String correctShorthand(String inputString) {
        assert (inputString != null);
        String regex = "\\b(tmr|tml|tmrw)\\b";
        inputString = inputString.replaceAll(regex, "tomorrow");

        regex = "\\b(tdy)\\b";
        inputString = inputString.replaceAll(regex, "today");
        return inputString;
    }

    /**
     * This method corrects ranged time for date parsing.
     * 
     * @param inputString
     *            {@code String} input to be corrected
     * @return {@code String} with time range corrected
     */
    private String correctRangeTime(String inputString) {
        assert (inputString != null);
        inputString = inputString.replaceAll("()-()", "$1 - $2");
        return inputString;
    }

    private String getPriorityString(String inputString) {
        assert (inputString != null);
        String regex = REGEX_PRIORITY;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(inputString);
        boolean checkMatch = matcher.find();
        if (checkMatch) {
            return matcher.group();
        }
        return null;
    }

    private int getPriority(String inputString) {
        assert (inputString != null);
        String priority = inputString.substring(PRIORITY_LENGTH).trim();
        int level = assignLevel(priority);
        return level;
    }
    
    private int assignLevel(String priority) {
        assert (priority != null);
        priority = priority.toLowerCase();
        int level = 0;
        
        switch (priority) {
            case "l" :
            case "low" :
                level = PRIORITY_LOW;
                break;
                
            case "m" :
            case "mid" :
            case "med" :
            case "medium" :
                level = PRIORITY_MID;
                break;

            case "h" :
            case "high" :
                level = PRIORITY_HIGH;
                break;
                
            default :
                break;
        }
        return level;
    }
    
    private String removeStringFromTitle(String title, String string) {
        assert (title != null && string != null);
        title = title.replace(string, "");
        title = removeExtraSpaces(title);
        return title;
    }

    /**
     * This method checks for indication of label through detection of '#'.
     * 
     * @param inputString
     *            {@code String} input to be checked
     * @return {@code boolean} true if found
     */
    private boolean checkForLabel(String inputString) {
        assert (inputString != null);
        if (inputString.contains("#")) {
            return true;
        } else {
            return false;
        }
    }

    private String getLabel(String inputString) throws InvalidLabelFormat {
        assert (inputString != null);
        int index = inputString.indexOf("#");
        index = index + INDEX_OFFSET;
        String substring = inputString.substring(index);
        String label = substring.trim();
        label = getFirstWord(label);
        return label;
    }

    private String getFirstWord(String inputString) throws InvalidLabelFormat {
        assert (inputString != null);
        String word = "";
        try {
            word = inputString.split(" ")[0];
        } catch (Exception e) {
            logger.log(Level.WARNING, "Label cannot be parsed by parser.");
            logger.log(Level.WARNING, "InvalidLabelFormat exception thrown.");
            throw new InvalidLabelFormat("Invalid label input detected.");
        }

        if (word.length() == 0) {
            logger.log(Level.WARNING, "Label cannot be parsed by parser.");
            logger.log(Level.WARNING, "InvalidLabelFormat exception thrown.");
            throw new InvalidLabelFormat("Invalid label input detected.");
        }

        return word;
    }

    private boolean checkValidLabel(String inputString) {
        assert (inputString != null);
        if (inputString.contains("-")) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * This method corrects dd/mm into mm/dd for date parsing.
     * 
     * @param inputString
     *            {@code String} input to be corrected
     * @return {@code String} with the date corrected
     */
    private String correctDateNumFormat(String inputString, boolean hasYear) {
        assert (inputString != null);
        boolean match = false;
        String swapped = "";

        // Preserve capitalization by not using toLowerCase
        List<String> words = new ArrayList<String>(Arrays.asList(inputString.split(" ")));

        for (int i = 0; i < words.size(); i++) {
            if (hasYear) {
                match = Pattern.matches(REGEX_DATE_NUM_YEAR, words.get(i));
            } else {
                match = Pattern.matches(REGEX_DATE_NUM, words.get(i));
            }

            if (match) {
                if (words.get(i).contains("/")) {
                    List<String> date = new ArrayList<String>(Arrays.asList(words.get(i).split("/")));
                    swapped = date.get(1).concat("/").concat(date.get(0));
                    if (date.size() == DATE_FIELD_FULL) {
                        swapped = swapped.concat("/").concat(date.get(2));
                    }
                } else if (words.get(i).contains("-")) {
                    List<String> date = new ArrayList<String>(Arrays.asList(words.get(i).split("-")));
                    swapped = date.get(1).concat("-").concat(date.get(0));
                    if (date.size() == DATE_FIELD_FULL) {
                        swapped = swapped.concat("-").concat(date.get(2));
                    }
                }
                words.set(i, swapped);
            }
        }

        return String.join(" ", words);
    }

    /**
     * This method uses PrettyTimeParser to generate dates from {@code String}
     * inputString.
     * 
     * @param inputString
     *            {@code String} input to be parsed
     * @return {@code List<Date>} of dates generated if possible
     */
    private List<Date> parseDateTime(String inputString) {
        assert (inputString != null);
        PrettyTimeParser parser = new PrettyTimeParser(TimeZone.getDefault());
        List<Date> dates = parser.parse(inputString);
        return dates;
    }

    /**
     * This method parses the date only.
     * Since time is not specified, it is set to 12am.
     * 
     * @param inputString
     *            {@code String} input to be parsed
     * @return {@code List<Date>} of dates
     */
    private List<Date> parseDateOnly(String inputString) {
        assert (inputString != null);
        List<Date> dates = parseDateTime(inputString);
        int size = dates.size();
        for (int i = 0; i < size; i++) {
            dates.add(setTimeToZero(dates.get(i)));
        }

        if (size != 0) {
            dates.remove(0);
        }

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
        assert (inputString != null);
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
     *            {@code List<Date>>} to be corrected
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
        assert (inputString != null);
        List<Date> dates = parseDateTime(inputString);
        dates = fixTimeToNearest(dates, false);
        dates = fixTimeForRange(dates);
        return dates;
    }

    /**
     * This method sets the time if it has past and if no date is specified.
     * It will always take the next nearest time.
     * 
     * Eg:
     * If now is 1pm, and if either 12am or 12pm is specified,
     * it will be 12am or 12pm of the next day.
     * 
     * @param dates
     *            {@code List<Date>} dates to be parsed
     * @param hasDate
     *            {@code boolean} indicating if date is specified
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
                    // date has past, get the next nearest
                    date.setTime(dates.get(i));
                    date.add(Calendar.DATE, 1);
                    dates.set(i, date.getTime());
                }

                if (dates.size() == 2) {
                    // update to start
                    now = dates.get(i);
                }
            }
        }
        return dates;
    }

    /**
     * This method ensures that range are sequential.
     * Corrects date parsed by PrettyTime.
     * The end will come after the start.
     * 
     * Eg: If now is 11pm, 10pm - 2am will be today 10pm to the next day 2am.
     * 
     * @param dates
     *            {@code List<Date>} to be corrected
     * @return {@code List<Date>} corrected dates
     */
    private List<Date> fixTimeForRange(List<Date> dates) {
        if (dates.size() == DATE_MAX_SIZE) {
            Date start = dates.get(DATE_START);
            Date end = dates.get(DATE_END);

            if (end.before(start)) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(end);
                cal.add(Calendar.DAY_OF_MONTH, 1);
                dates.set(DATE_END, cal.getTime());
            }
        }
        return dates;
    }

    /**
     * This method corrects the next week that is parsed by PrettyTime.
     * When next week is specified in the user input,
     * it will always be corrected to the Monday of next week.
     * The week starts on Monday.
     * 
     * @param dates
     *            {@code List<Date>} to be corrected
     * @return {@code List<Date>} corrected to Monday of next week
     */
    private List<Date> correctNextWeek(List<Date> dates) {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);

        if (dates.size() == 0) {
            //if there is no date parsed by PrettyTime, set to next Monday
            cal.setTime(new Date());
            int week = cal.get(Calendar.WEEK_OF_YEAR) + 1;
            cal.set(Calendar.WEEK_OF_YEAR, week);
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            cal.setTime(setTimeToZero(cal.getTime()));
            dates.add(cal.getTime());
        } else{
            //correct to next Monday for range time
            for (int i = 0; i < dates.size(); i++) {
                cal.setTime(dates.get(i));
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                dates.set(i, cal.getTime());
            }
        }
        return dates;
    }

    private String getNextWeekRegex() {
        return "\\b" + STRING_NEXT_WEEK + "\\b";
    }

    /**
     * This method sets the time for time specified without am/pm due to the
     * presence of preposition.
     * It will take the next possible time since am/pm is not specified.
     * 
     * Eg:
     * If now is 1pm, and 10 is specified,
     * it will be parsed as 10pm today.
     * 
     * If now is 10pm, and 1 is specified,
     * it will be parsed as 1am tomorrow.
     * 
     * @param dates
     *            {@code List<Date>} dates to be parsed
     * @param hasDate
     *            {@code boolean} indicating if date is specified
     * @return {@code List<Date>} of dates
     */
    private List<Date> fixTimeForWithoutAmPm(List<Date> dates) {
        Date now = new Date();
        Calendar currentDate = Calendar.getInstance();
        currentDate.setTime(now);

        Calendar date = Calendar.getInstance();

        for (int i = 0; i < dates.size(); i++) {
            if (dates.get(i).before(now)) {
                // time has past, need to check next nearest
                date.setTime(dates.get(i));
                date.add(Calendar.HOUR_OF_DAY, 12);

                // if time is after current, still within the day
                if (date.after(currentDate)) {
                    dates.set(i, date.getTime());
                } else {
                    // time is before current, time has past
                    // plus 12 hours to get to next nearest
                    date.add(Calendar.HOUR_OF_DAY, 12);
                    dates.set(i, date.getTime());
                }
            }

            if (dates.size() == 2) {
                // if time is ranged, check against the fixed start date
                now = dates.get(i);
            }
        }
        return dates;
    }

    /**
     * This method determines the start and end date for a task.
     * 
     * @param dates
     *            {@code List<Date} dates obtained from parsing
     * @param hasPreposition
     *            {@code boolean} indicate if preposition is detected
     * @param hasStartDate
     *            {@code boolean} indicate if start date is detected through
     *            detection of preposition
     * @return {@code List<Date>} of determined dates
     */
    private List<Date> assignDates(List<Date> dates, boolean hasPreposition, boolean hasStartDate) {
        List<Date> assigned = new ArrayList<Date>();
        int numberOfDate = dates.size();

        if (numberOfDate == DATE_MAX_SIZE) {
            assigned.add(DATE_START, getDate(dates, DATE_START));
            assigned.add(DATE_END, getDate(dates, DATE_END));
        } else {
            if (hasPreposition) {
                if (hasStartDate) {
                    assigned.add(DATE_START, getDate(dates, DATE_INDEX));
                    assigned.add(DATE_END, null);
                } else {
                    assigned.add(DATE_START, null);
                    assigned.add(DATE_END, getDate(dates, DATE_INDEX));
                }
            } else {
                // no preposition
                // one date/time only
                // assume start
                assigned.add(DATE_START, getDate(dates, DATE_INDEX));
                assigned.add(DATE_END, null);
            }
        }
        return assigned;
    }

    private Date getDate(List<Date> dates, int index) {
        return dates.get(index);
    }

    private String getDateRegexTextWithYear() {
        return REGEX_PREPOSITION_ALL + "?" + REGEX_DATE_TEXT + REGEX_MONTH_TEXT + REGEX_YEAR;
    }

    /**
     * This method removes date information from the {@code String} taken in.
     * 
     * @param title
     *            {@code String} containing date information
     * @param startDate
     *            {@code Date} start date
     * @param endDate
     *            {@code Date} end date
     * @return {@code String} without date information
     */
    private String removeDateFromTitle(String title, List<Date> datesList) {
        assert (title != null);
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
     * This method generates an {@code ArrayList<String} of possible date
     * formats from {@code LocalDateTime}.
     * 
     * @param dateTime
     *            {@code LocalDateTime} to generate different date formats
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
     * This method generates an {@code ArrayList<String} of possible month
     * formats from {@code LocalDateTime}.
     * 
     * @param dateTime
     *            {@code LocalDateTime} to generate different month formats
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
     * This method generates an {@code ArrayList<String} of possible day formats
     * from {@code LocalDateTime}.
     * 
     * @param dateTime
     *            {@code LocalDateTime} to generate different day formats
     * @return {@code ArrayList<String} of possible day formats
     */
    private ArrayList<String> getPossibleDays(LocalDateTime dateTime) {
        Locale locale = Locale.getDefault();
        DayOfWeek day = dateTime.getDayOfWeek();
        ArrayList<String> days = new ArrayList<String>();
        days.add(day.toString().toLowerCase());
        days.add(day.getDisplayName(TextStyle.SHORT, locale).toLowerCase());
        days = getDaysShorthand(days, day.toString().toLowerCase());

        int date = dateTime.getDayOfMonth();
        int month = dateTime.getMonthValue();
        LocalDateTime today = LocalDateTime.now();
        if (month == today.getMonthValue()) {
            if (date == today.getDayOfMonth()) {
                days.add("today");
                days.add("tdy");
                days.add("tonight");
            } else if (date == (today.getDayOfMonth() + 1)) {
                days.add("tomorrow");
                days.add("tmr");
                days.add("tml");
                days.add("tmrw");
            }
        }
        return days;
    }

    private ArrayList<String> getDaysShorthand(ArrayList<String> days, String day) {
        switch (day) {
            case "tuesday" :
                days.add("tues");
                break;
                
            case "wednesday" :
                days.add("weds");
                break;
                
            case "thursday" :
                days.add("thur");
                days.add("thurs");
                break;
                
            default :
                break;
        }
        return days;
    }

    /**
     * This method generates an {@code ArrayList<String} of possible time
     * formats from {@code LocalDateTime}.
     * 
     * @param dateTime
     *            {@code LocalDateTime} to generate different time formats
     * @return {@code ArrayList<String} of possible time formats
     */
    private ArrayList<String> getPossibleTimes(LocalDateTime dateTime) {
        ArrayList<String> timings = new ArrayList<String>();
        int hour = dateTime.getHour();
        int min = dateTime.getMinute();

        assert (hour >= 0);
        assert (min >= 0);

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
                timings.add(temp.concat(colon));
                timings.add(temp.concat(dot));
                timings.add(temp.concat(colon).concat(STRING_AM));
                timings.add(temp.concat(dot).concat(STRING_AM));
            } else {
                timings.add(Integer.toString(hour).concat(STRING_AM));
                timings.add(Integer.toString(hour).concat(colon));
                timings.add(Integer.toString(hour).concat(dot));
                timings.add(Integer.toString(hour).concat(colon).concat(STRING_AM));
                timings.add(Integer.toString(hour).concat(dot).concat(STRING_AM));
            }
        } else if (hour >= 12) {
            hour = hour - 12;
            if (hour == 0) {
                String temp = STRING_TWELVE;
                timings.add(temp.concat(STRING_PM));
                timings.add(temp.concat(colon));
                timings.add(temp.concat(dot));
                timings.add(temp.concat(colon).concat(STRING_PM));
                timings.add(temp.concat(dot).concat(STRING_PM));
            } else {
                timings.add(Integer.toString(hour));
                timings.add(Integer.toString(hour).concat(STRING_PM));
                timings.add(Integer.toString(hour).concat(colon));
                timings.add(Integer.toString(hour).concat(dot));
                timings.add(Integer.toString(hour).concat(colon).concat(STRING_PM));
                timings.add(Integer.toString(hour).concat(dot).concat(STRING_PM));
            }
        }
        return timings;
    }

    /**
     * This method checks for and removes {@code ArrayList<String>} of 
     * targeted word from {@code String}.
     * 
     * If word to be removed is found, 
     * it checks if the word before it is a preposition.
     * If preposition found, both are removed.
     * Else, only the matching word is removed.
     * 
     * @param title
     *            {@code String} to be checked
     * @param toBeRemoved
     *            {@code ArrayList<String>} of words to be removed
     * @return {@code String} with targeted words removed
     */
    private String checkAndRemove(String title, ArrayList<String> toBeRemoved) {
        assert (title != null);
        int index = 0;
        boolean isPreposition = false;

        for (int i = 0; i < toBeRemoved.size(); i++) {
            String toBeReplaced = "";
            List<String> words = new ArrayList<String>(Arrays.asList(title.toLowerCase().split(" ")));

            if (words.contains(toBeRemoved.get(i))) {
                toBeReplaced = toBeReplaced.concat(toBeRemoved.get(i));

                index = words.indexOf(toBeRemoved.get(i));
                String word = "";
                if (index != 0) {
                    index = index - INDEX_OFFSET;
                    word = getWord(title, index);
                }

                if (word.equals("this") || word.equals("next")) {
                    toBeReplaced = word.concat(" ").concat(toBeReplaced);
                    if (index != 0) {
                        index = index - INDEX_OFFSET;
                        word = getWord(title, index);
                    }
                }

                isPreposition = checkForRegexMatch(REGEX_PREPOSITION_ALL, word);
                if (isPreposition) {
                    toBeReplaced = word.concat(" ").concat(toBeReplaced);
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
     *            {@code String} input to obtain word from
     * @param index
     *            {@code int} index of word to be obtained
     * @return {@code String} word obtained
     */
    private String getWord(String title, int index) {
        assert (title != null);
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
     *            {@code String} input to be parsed
     * @return {@code Date} if date found, {@code null} if date is not found
     */
    public Date getDateForSearch(String inputString) {
        assert (inputString != null);
        if (isSpecialCase(inputString)) {
            return null;
        }

        inputString = correctShorthand(inputString);
        List<Date> dates = parseDateTime(inputString);

        if (dates.size() == 0) {
            return null;
        } else {
            if (inputString.length() == 1) {
                dates = setDate(dates, inputString);
            }
            return dates.get(DATE_INDEX);
        }
    }

    /**
     * This method escapes date parsing if it is a special string.
     * 
     * @param inputString
     *            {@code String} input string
     * @return {@code boolean} true if special case detected
     */
    private boolean isSpecialCase(String inputString) {
        assert (inputString != null);
        if (inputString.equalsIgnoreCase("this week") || inputString.equalsIgnoreCase("next week")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method uses the {@code String} to replace the date in {@code List<Date}
     * 
     * @param dates
     *          {@code List<Date>} date to be replaced
     * @param inputString
     *          {@code String} containing the date
     * @return {@code List<Date>} of corrected date.
     */
    private List<Date> setDate(List<Date> dates, String inputString) {
        assert (inputString != null);
        int date = Integer.parseInt(inputString);
        Calendar cal = Calendar.getInstance();
        cal.setTime(dates.get(DATE_INDEX));
        cal.set(Calendar.DATE, date);
        dates.add(DATE_INDEX, cal.getTime());
        return dates;
    }

    /**
     * This method detects if index is present in edit command.
     * 
     * @param inputString
     *            {@code String} input for index to be obtained
     * @return {@code int} index if found, {@code int} -1 if index is not found
     */
    public int getIndexForEdit(String inputString) {
        assert (inputString != null);
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
     *            {@code String} input for information to be removed
     * @return {@code String} without date and time information
     */
    private String removeDateTime(String inputString) {
        assert (inputString != null);
        boolean hasTime = false;
        boolean hasTimeRange = false;
        boolean hasDate = false;
        String regex = "";

        hasTime = checkForRegexMatch(getTimeRegex(), inputString);

        if (hasTime) {
            hasTimeRange = checkForRegexMatch(getTimeRangeRegex(), inputString);

            if (hasTimeRange) {
                regex = "(" + REGEX_PREPOSITION_ALL + "?)(" + getTimeRangeRegex() + ")";
                inputString = removeRegex(regex, inputString);
            }

            regex = getTimeRegex();
            inputString = removeRegex(regex, inputString);
        }

        hasDate = checkForRegexMatch(getDateRegex(), inputString);
        if (hasDate) {
            regex = getDateRegex();
            inputString = removeRegex(regex, inputString);
            hasDate = false;
        }

        hasDate = checkForRegexMatch(getDateTextRegex(), inputString);
        if (hasDate) {
            regex = getDateTextRegex();
            inputString = removeRegex(regex, inputString);
        }

        return inputString;
    }

    /**
     * This method takes in {@code String} regex and removes if from
     * {@code String} input.
     * 
     * @param regex
     *            {@code String} expressions to be removed
     * @param inputString
     *            {@code String} input for expression to be removed
     * @return {@code String} without expression
     */
    private String removeRegex(String regex, String inputString) {
        assert (regex != null && inputString != null);
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(inputString);

        while (matcher.find()) {
            String match = matcher.group();
            inputString = inputString.replaceAll(match, "");
        }

        inputString = removeExtraSpaces(inputString);
        return inputString;
    }

    /**
     * This method allows for editing of existing tasks.
     * 
     * @param oldTask
     *            {@code Task} to be edited
     * @param inputString
     *            {@code String} of new information
     * @return {@code Task} edited task
     * @throws InvalidLabelFormat
     */
    public Task parseEdit(Task oldTask, String inputString) throws InvalidLabelFormat {
        assert (inputString != null);
        logger.log(Level.INFO, "Parsing for EDIT command.");
        
        String newTitle = oldTask.getTitle();
        String newLabel = oldTask.getLabel();
        Date newStart = oldTask.getStartDate();
        Date newEnd = oldTask.getEndDate();
        Date createdDate = oldTask.getCreatedDate();
        int priority = oldTask.getPriority();
        boolean isDatedOnly = oldTask.getIsDatedOnly();

        Task editedTask;
        try {
            editedTask = parseAdd(inputString);
        } catch (InvalidTitle exception) {
            // exception doesn't matter if edit is using parseAdd
            editedTask = exception.getTask();
        }

        if (editedTask.getTitle().length() != 0 && !editedTask.getTitle().equals("<No title>")) {
            newTitle = editedTask.getTitle();
        }

        if (editedTask.getLabel() != null) {
            newLabel = editedTask.getLabel();
        }

        boolean hasPriority = checkForRegexMatch(REGEX_PRIORITY, inputString);
        if (hasPriority) {
            priority = editedTask.getPriority();
        }

        boolean hasTime = false;
        boolean hasDate = false;
        boolean hasPreposition = false;
        boolean hasTimeWithoutAmPm = false;
        List<Date> dates = new ArrayList<Date>();

        hasDate = checkForRegexMatch(getDateRegex(), inputString)
                || checkForRegexMatch(getDateTextRegex(), inputString);
        hasTime = checkForRegexMatch(getTimeRegex(), inputString) 
                || checkForRegexMatch(REGEX_WORD_TIMED, inputString);

        if (!hasTime) {
            hasPreposition = checkForRegexMatch(REGEX_PREPOSITION_ALL, inputString);
            if (hasPreposition) {
                hasTimeWithoutAmPm = checkForRegexMatch(getTimeWithoutAmPmRegex(), inputString);
                if (hasTimeWithoutAmPm) {
                    hasTime = true;
                }
            }
        }

        // check for word indicating date
        if (checkForRegexMatch(REGEX_WORD_DATED, inputString)) {
            hasDate = true;
        }

        if (editedTask.hasDate()) {
            if (hasDate && !hasTime) {
                // only date
                dates.add(editedTask.getStartDate());
                dates.add(editedTask.getEndDate());
                isDatedOnly = true;
            } else if (!hasDate && hasTime) {
                // only time, reuse date
                dates = reuseDate(editedTask, oldTask);
                isDatedOnly = false;
            } else {
                // have both
                // update by overwriting
                dates.add(editedTask.getStartDate());
                dates.add(editedTask.getEndDate());
                isDatedOnly = false;
            }

            newStart = dates.get(DATE_START);
            newEnd = dates.get(DATE_END);
        }

        Task newTask = new Task(newTitle, newStart, newEnd, newLabel);
        newTask.setCreatedDate(createdDate);
        newTask.setPriority(priority);
        newTask.setIsDatedOnly(isDatedOnly);
        logger.log(Level.INFO, "Edited task object returned.");
        return newTask;
    }

    /**
     * This method duplicates the date information in {@code Task} oldTask to
     * {@code Task} editedTask.
     * 
     * @param editedTask
     *            {@code Task} edited task that needs the old date information
     * @param oldTask
     *            {@code Task} with the old date information
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

            latest = setDayMonthYear(reuse, latest, startDate);
            oldStart = latest.getTime();
        }

        if (endDate != null) {
            if (oldEnd != null) {
                reuse.setTime(oldEnd);
            } else if (oldStart != null) {
                reuse.setTime(oldStart);
            }

            latest = setDayMonthYear(reuse, latest, endDate);
            oldEnd = latest.getTime();
        }

        if (!oldTask.hasDate()) {
            // floating task
            oldStart = startDate;
            oldEnd = endDate;
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
     * This method gets the day and month from {@code Calendar} reuse and sets
     * it in {@code Calendar} latest.
     * 
     * @param reuse
     *            {@code Calendar} for day and month to be obtained
     * @param latest
     *            {@code Calendar} for day and month to be set
     * @param date
     *            {@code Date} date to be set for {@code Calendar} latest
     * @return {@code Calendar} with updated day and month
     */
    private Calendar setDayMonthYear(Calendar reuse, Calendar latest, Date date) {
        int day = reuse.get(Calendar.DAY_OF_MONTH);
        int month = reuse.get(Calendar.MONTH);
        int year = reuse.get(Calendar.YEAR);

        latest.setTime(date);
        latest.set(Calendar.DAY_OF_MONTH, day);
        latest.set(Calendar.MONTH, month);
        latest.set(Calendar.YEAR, year);

        return latest;
    }

    // =============================
    // Parsing Indexes
    // =============================

    /**
     * This method detects the types of indexes and processes them.
     * 
     * @param inputString
     *            {@code String} input to be processed
     * @return {@code ArrayList<Integer>} of index(es)
     * @throws InvalidTaskIndexFormat
     *             if format is invalid
     */
    public ParseIndexResult parseIndexes(String inputString, int maxSize) throws InvalidTaskIndexFormat {
        assert (maxSize > 0);
        logger.log(Level.INFO, "Parsing indexes.");
        
        try {
            String indexString = getStringWithoutCommand(inputString);
            indexString = removeWhiteSpace(indexString);
            ArrayList<Integer> indexes = new ArrayList<Integer>();
            indexes = getIndex(indexString);
            ParseIndexResult indexResult = validateIndexes(indexes, maxSize);
            logger.log(Level.INFO, "Indexes retrieved.");
            return indexResult;
        } catch (Exception e) {
            logger.log(Level.WARNING, "NumberFormatException: Indexes cannot be parsed by parser.");
            logger.log(Level.WARNING, "InvalidTaskIndexFormat exception thrown.");
            throw new InvalidTaskIndexFormat("Invalid indexes input detected.");
        }
    }

    private String getStringWithoutCommand(String commandString) throws InvalidLabelFormat {
        int index = 0;
        String command = getFirstWord(commandString);
        index = command.length() + LENGTH_OFFSET;
        String indexString = commandString.substring(index, commandString.length());
        assert (!indexString.isEmpty());
        return indexString;
    }

    private String removeWhiteSpace(String string) {
        string = string.replaceAll("\\s", "");
        assert (!string.isEmpty());
        return string;
    }

    /**
     * This method obtains all numbers based on {@code String} taken in.
     * 
     * @param index
     *            {@code String} to be processed
     * @return {@code ArrayList<Integer>} of index(es)
     * @throws InvalidTaskIndexFormat
     */
    private ArrayList<Integer> getIndex(String index) throws InvalidTaskIndexFormat {
        assert (index != null);
        ArrayList<String> indexes = new ArrayList<String>();
        ArrayList<String> tempRangedIndexes = new ArrayList<String>();
        ArrayList<Integer> multipleIndexes = new ArrayList<Integer>();
        ArrayList<Integer> rangedIndexes = new ArrayList<Integer>();

        Collections.addAll(indexes, index.split(","));

        for (int i = 0; i < indexes.size(); i++) {
            if (indexes.get(i).contains("-")) {
                Collections.addAll(tempRangedIndexes, indexes.get(i).split("-"));

                // remove all empty after splitting
                // else will cause parseInt to fail
                tempRangedIndexes = removeEmpty(tempRangedIndexes);
                rangedIndexes = getRangedIndexes(tempRangedIndexes);

                if (rangedIndexes.size() == 1) {
                    logger.log(Level.WARNING, "NumberFormatException: Indexes cannot be parsed by parser.");
                    logger.log(Level.WARNING, "InvalidTaskIndexFormat exception thrown.");
                    throw new InvalidTaskIndexFormat("Invalid indexes input detected.");
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
        assert (arrayStrings != null);
        ArrayList<String> empty = new ArrayList<String>();
        empty.add("");
        arrayStrings.removeAll(empty);
        return arrayStrings;
    }

    /**
     * This method gets the range of indexes.
     * 
     * @param arrayStrings
     *            {@code ArrayList<String>} to be processed
     * @return {@code ArrayList<Integer>} of index(es) range
     */
    private ArrayList<Integer> getRangedIndexes(ArrayList<String> arrayStrings) {
        assert (arrayStrings != null);
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
     *            {@code ArrayList<Integer>} to be processed
     * @return {@code ArrayList<Integer>} of indexes
     */
    private ArrayList<Integer> getMultipleIndexes(ArrayList<Integer> arrayIntegers) {
        assert (arrayIntegers != null);
        ArrayList<Integer> multiple = new ArrayList<Integer>();
        int start, end;
        int possibleRange = arrayIntegers.size() - 1;

        for (int i = 0; i < possibleRange; i++) {
            // Check and fix range for descending cases
            if (arrayIntegers.get(i) < arrayIntegers.get(i + 1)) {
                start = arrayIntegers.get(i);
                end = arrayIntegers.get(i + 1);
            } else {
                start = arrayIntegers.get(i + 1);
                end = arrayIntegers.get(i);
            }

            // prevent duplicates from being added twice
            for (int j = start; j <= end; j++) {
                if (!multiple.contains(j)) {
                    multiple.add(j);
                }
            }
        }

        return multiple;
    }

    private ParseIndexResult validateIndexes(ArrayList<Integer> indexes, int maxSize) {
        assert (indexes != null);
        ArrayList<Integer> validIndexes = new ArrayList<Integer>();
        ArrayList<Integer> invalidIndexes = new ArrayList<Integer>();

        for (int i = 0; i < indexes.size(); i++) {
            int index = indexes.get(i);
            if (index > maxSize) {
                invalidIndexes.add(index);
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
}