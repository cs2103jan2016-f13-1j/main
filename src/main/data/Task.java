package main.data;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author Joleen
 *
 */

public class Task {
    private String title;
    private Date startDate;
    private Date endDate;
    private boolean isDatedOnly;
    private String label;
    private boolean done;
    private int priority;
    private Date createdDate;
    private Date completedDate;
    private boolean collideWithPrev;
    private boolean collideWithNext;

    public Task(String title) {
        this.title = title;
        startDate = null;
        endDate = null;
        isDatedOnly = false;
        label = null;
        done = false;
        priority = 0;
        createdDate = new Date();
        completedDate = null;
        collideWithPrev = false;
        collideWithNext = false;
    }

    public Task(String title, Date startDate, Date endDate, String label) {
    	this.title = title;
    	this.startDate = startDate;
    	this.endDate = endDate;
    	isDatedOnly = false;
    	this.label = label;
    	done = false;
    	priority = 0;
    	createdDate = new Date();
    	completedDate = null;
    	collideWithPrev = false;
    	collideWithNext = false;
    }

    public Task(){}

    public void setIsDatedOnly(boolean isDatedOnly) {
    	this.isDatedOnly = isDatedOnly;
    }

    public boolean getIsDatedOnly() {
    	return isDatedOnly;
    }

    public boolean getCollideWithPrev() {
        return collideWithPrev;
    }

    public void setCollideWithPrev(boolean collide) {
        collideWithPrev = collide;
    }

    public boolean getCollideWithNext() {
        return collideWithNext;
    }

    public void setCollideWithNext(boolean collide) {
        collideWithNext = collide;
    }

    public String getTitle() {
        return title;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public boolean hasDate() {
        return (startDate != null || endDate != null);
    }

    public boolean hasDateRange() {
        return (startDate != null && endDate != null);
    }

    public boolean hasStartDate() {
        if (startDate == null) {
            return false;
        } else {
            return true;
        }
    }

    public boolean hasEndDate() {
        if (endDate == null) {
            return false;
        } else {
            return true;
        }
    }

    public boolean hasSingleDate() {
        if (startDate == null && endDate != null) {
            return true;
        } else if (startDate != null & endDate == null) {
            return true;
        } else {
            return false;
        }
    }

    public Date getSingleDate() {
        if (startDate == null && endDate != null) {
            return endDate;
        } else {
            return startDate;
        }
    }

    public String getLabel() {
        return label;
    }

    public boolean hasLabel() {
        if (label == null) {
            return false;
        } else {
            return true;
        }
    }

    public boolean isDone() {
        return done;
    }

    public void toggleDone() {
    	if (isDone()) {
    		setNotCompleted();
    	} else {
    		setIsCompleted();
    	}
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
    	this.priority = priority;
    }

    public int togglePriority(boolean increase) {
    	int limit = 3;
    	if (increase) {
    	    priority++;
            if (priority > limit) {
                priority = 0;
            }
    	} else {
    	    priority--;
    	    if (priority < 0) {
    	        priority = limit;
    	    }
    	}
    	return priority;
    }

    public Date getCompletedDate() {
        return completedDate;
    }

    public void setIsCompleted() {
        done = true;
        completedDate = new Date();
    }

    public void setNotCompleted() {
        done = false;
        completedDate = null;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
    	this.createdDate = createdDate;
    }

    public int compareTo(Task task) {
        if (!createdDate.equals(task.getCreatedDate())) {
            return -1;
        }
    	return 0;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     * This method generates the task feedback for UI to display to user.
     */
    public String toString() {
    	int indexTitle = 0;
    	int indexStartDate = 1;
    	int indexStartTime = 2;
    	int indexStartYear = 3;
    	int indexEndDate = 4;
    	int indexEndTime = 5;
    	int indexEndYear = 6;
    	int indexLabel = 7;
    	int indexPriority = 8;
    	int indexIsDatedOnly = 9;

    	ArrayList<String> fields = getTaskFields();
    	String title = fields.get(indexTitle);
    	String startDate = fields.get(indexStartDate);
    	String startTime = fields.get(indexStartTime);
    	String startYear = fields.get(indexStartYear);
    	String endDate = fields.get(indexEndDate);
    	String endTime = fields.get(indexEndTime);
    	String endYear = fields.get(indexEndYear);
    	String label = fields.get(indexLabel);
    	String priority = fields.get(indexPriority);
    	String isDatedOnly = fields.get(indexIsDatedOnly);

    	StringBuilder stringBuilder = new StringBuilder(title);

    	if (hasDate()) {
    		if (hasDateRange()) {
    			stringBuilder.append(" from " + startDate);

    			if (startYear != null) {
    				stringBuilder.append(" " + startYear);
    			}

    			if (!isDatedOnly.equals("true")) {
    				stringBuilder.append(" " + startTime);
    			}

    			if (startDate.equals(endDate) && startYear == null && endYear == null) {
    				//same day
    				stringBuilder.append(" to " + endTime);
    			} else {
    				stringBuilder.append(" to " + endDate);

        			if (endYear != null) {
        				stringBuilder.append(" " + endYear);
        			}

        			if (!isDatedOnly.equals("true")) {
        				stringBuilder.append(" " + endTime);
        			}
    			}


    			/*
    			if (!startDate.equals(endDate)) {
        			stringBuilder.append(" to " + endDate);

        			if (endYear != null) {
        				stringBuilder.append(" " + endYear);
        			}

        			if (!isDatedOnly.equals("true")) {
        				stringBuilder.append(" " + endTime);
        			}
        		} else {
        			stringBuilder.append(" to " + endTime);
        		}
        		*/
    		} else if (startDate != null) {
    				stringBuilder.append(" from " + startDate);

        			if (startYear != null) {
        				stringBuilder.append(" " + startYear);
        			}

    				if (!isDatedOnly.equals("true")) {
        				stringBuilder.append(" " + startTime);
        			}
    		} else if (endDate != null) {
    				stringBuilder.append(" by " + endDate);

    				if (endYear != null) {
        				stringBuilder.append(" " + endYear);
        			}

    				if (!isDatedOnly.equals("true")) {
        				stringBuilder.append(" " + endTime);
        			}
    		}
    	}

    	if (label != null) {
    		stringBuilder.append(" #" + label);
    	}

    	if (!priority.equals("0")) {
    		stringBuilder.append(" P:" + priority);
    	}

    	return stringBuilder.toString();
    }

    /**
     * Get attributes that is in task object to form feedback
     *
     * In order:
     * 0 - Title
     * 1 - Start Date
     * 2 - Start Time
     * 3 - Start Year
     * 4 - End Date
     * 5 - End Time
     * 6 - End Year
     * 7 - Label
     * 8 - Priority
     * 9 - isDatedOnly
     *
     * @return ArrayList<String> of size 10
     */
    private ArrayList<String> getTaskFields() {
        ArrayList<String> fields = new ArrayList<String>();

        fields.add(title);

        if (hasDate()) {
        	if (hasStartDate()){
        		fields.add(convertDate(startDate));
            	fields.add(convertTime(startDate));
            	fields.add(convertYear(startDate));
        	} else {
        		fields.add(null);
            	fields.add(null);
            	fields.add(null);
        	}

        	if (hasEndDate()) {
	        	fields.add(convertDate(endDate));
	        	fields.add(convertTime(endDate));
	        	fields.add(convertYear(endDate));
        	} else {
        		fields.add(null);
            	fields.add(null);
            	fields.add(null);
        	}
        } else {
        	fields.add(null);
        	fields.add(null);
        	fields.add(null);
        	fields.add(null);
        	fields.add(null);
        	fields.add(null);
        }

        if (hasLabel()) {
            fields.add(label);
        } else {
        	fields.add(null);
        }

        fields.add(Integer.toString(getPriority()));
        fields.add(String.valueOf(isDatedOnly));
        return fields;
    }

    private String convertDate(Date date) {
    	if (dateIsToday(date)) {
    		return "today";
    	}

    	DayOfWeek day = getDay(date);
    	String dayShort = getShortDay(day);

    	if (dateIsThisWeek(date)) {
    		return "this ".concat(dayShort);
    	} else if (dateIsNextWeek(date)){
    		return "next ".concat(dayShort);
    	} else {
    		Month month = getMonth(date);
    		String monthShort = getShortMonth(month);
    		return getDate(date).concat(" ").concat(monthShort);
    	}
    }

    public boolean dateIsToday(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");
        String today = dateFormat.format(new Date());

        String dateString = dateFormat.format(date);
        if (today.equals(dateString)) {
            return true;
        } else {
        	return false;
        }
    }

    private boolean dateIsThisWeek(Date date) {
    	Calendar calendar = Calendar.getInstance();
		calendar.setFirstDayOfWeek(Calendar.MONDAY);

		int currentWeek = calendar.get(Calendar.WEEK_OF_YEAR);

		calendar.setTime(date);
		int dateWeek = calendar.get(Calendar.WEEK_OF_YEAR);

		if (dateWeek == currentWeek) {
			return true;
		} else {
			return false;
		}
    }

    private boolean dateIsNextWeek(Date date) {
    	Calendar calendar = Calendar.getInstance();
		calendar.setFirstDayOfWeek(Calendar.MONDAY);

		int currentWeek = calendar.get(Calendar.WEEK_OF_YEAR);

		calendar.setTime(date);

		int dateWeek = calendar.get(Calendar.WEEK_OF_YEAR);
		int difference = dateWeek - currentWeek;

		if (difference == 1) {
			return true;
		} else {
			return false;
		}
    }

    private DayOfWeek getDay(Date date) {
    	LocalDateTime dateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    	DayOfWeek day = dateTime.getDayOfWeek();
	    return day;
    }

    private static String getShortDay(DayOfWeek day) {
		 DateFormatSymbols symbols = new DateFormatSymbols();
		 String[] days = symbols.getShortWeekdays();
		 List<String> correctedDays = new ArrayList<String>(Arrays.asList(days));
		 correctedDays.remove(0);
		 correctedDays.remove(0);
		 correctedDays.add("Sun");
		 int value = day.getValue() - 1;
		 return correctedDays.get(value);
	}

    private String getDate(Date date) {
    	LocalDateTime dateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		return Integer.toString(dateTime.getDayOfMonth());
    }

    private Month getMonth(Date date) {
    	LocalDateTime dateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    	Month month = dateTime.getMonth();
		return month;
    }

	private static String getShortMonth(Month month) {
		 DateFormatSymbols symbols = new DateFormatSymbols();
		 String[] months = symbols.getShortMonths();
		 int value = month.getValue() - 1;
		 return months[value];
	}

    private String convertTime(Date date) {
    	SimpleDateFormat timeFormat = new SimpleDateFormat("mm");
    	String minute = timeFormat.format(date);

    	if (minute.equals("00")){
    		timeFormat = new SimpleDateFormat("ha");
    	} else {
    		timeFormat = new SimpleDateFormat("h:mma");
    	}

	    DateFormatSymbols symbols = new DateFormatSymbols(Locale.getDefault());
        symbols.setAmPmStrings(new String[] {"am", "pm"});
        timeFormat.setDateFormatSymbols(symbols);
	    return timeFormat.format(date);
    }

    private String convertYear(Date date) {
    	LocalDateTime now = LocalDateTime.now();
    	String thisYear = Integer.toString(now.getYear());
    	String year = getYear(date);

    	if (thisYear.equals(year)) {
    		return null;
    	} else {
    		return year;
    	}
    }

    private String getYear(Date date) {
		LocalDateTime dateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		return Integer.toString(dateTime.getYear());
	}

    public String getSimpleDate() {
    	Locale locale = Locale.getDefault();
    	StringBuilder stringBuilder = new StringBuilder("");
    	String start, end, startMonthString, endMonthString;
    	start = end = startMonthString = endMonthString = "";

    	LocalDateTime now = LocalDateTime.now();
    	String thisYear = Integer.toString(now.getYear());

    	if (startDate != null) {
	    	Month startMonth = getMonth(startDate);
			startMonthString = startMonth.getDisplayName(TextStyle.FULL, locale);
			start = getDate(startDate).concat(" ").concat(startMonthString);

			if (!getYear(startDate).equals(thisYear)) {
	    		start = start.concat(" ").concat(getYear(startDate));
	    	}
    	}

    	if (endDate != null) {
			Month endMonth = getMonth(endDate);
			endMonthString = endMonth.getDisplayName(TextStyle.FULL, locale);
			end = getDate(endDate).concat(" ").concat(endMonthString);

			if (!getYear(endDate).equals(thisYear)) {
	    		end = end.concat(" ").concat(getYear(endDate));
	    	}
    	}

    	if (hasDateRange()) {
        	if (start.equals(end)) {
        		stringBuilder.append(start);
        	} else {
        		stringBuilder.append(start + " - " + end);
        	}
    	} else if (hasStartDate()) {
    		stringBuilder.append(start);
    	} else if (hasEndDate()) {
    		stringBuilder.append(end);
    	}

    	return stringBuilder.toString();
    }

    public String getSimpleTime() {
    	StringBuilder stringBuilder = new StringBuilder("");
    	int indexStartTime = 2;
    	int indexEndTime = 5;

    	ArrayList<String> fields = getTaskFields();
    	String startTime = fields.get(indexStartTime);
    	String endTime = fields.get(indexEndTime);

    	if (hasDateRange()) {
    		stringBuilder.append(startTime + " - " + endTime);
    	} else if (hasStartDate()) {
    		stringBuilder.append(startTime);
    	} else if (hasEndDate()) {
    		stringBuilder.append("by " + endTime);
    	} else {
    		//floating task
    		stringBuilder.append("-");
    	}

    	if (getIsDatedOnly()) {
    		stringBuilder = new StringBuilder("-");
    	}

    	return stringBuilder.toString();
    }

    public boolean hasStarted() {
    	if (startDate == null) {
    		return false;
    	} else {
    		return (startDate.compareTo(new Date()) < 0);
    	}
    }

    /**
     * This method checks if a task is today.
     * If the task has a ranged time, it will always be today until it ends.
     *
     * @return {@code Boolean} true if is today
     */
    public boolean isToday() {
    	if (hasDateRange()) {
    		if (hasStarted()) {
    			return true;
    		} else {
    			return false;
    		}
    	} else if (hasDate()) {
    		return dateIsToday(getSingleDate());
    	}

    	return false;
    }

    /**
     * This method checks if a task is tomorrow.
     *
     * If the task has a ranged time and it has started, check the end time.
     * If the end time is after tomorrow, then the task is tomorrow.
     * If the task has a ranged time and it has not started, check the start time.
     * If the start time is tomorrow, then the task is tomorrow.
     *
     * @return {@code Boolean} true if is tomorrow
     */
    public boolean isTomorrow() {
    	Calendar cal = Calendar.getInstance();
    	cal.setTime(new Date());
    	cal.add(Calendar.DATE, 1);

    	SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");
    	String tomorrow = dateFormat.format(cal.getTime());
    	String starting = "";
    	String ending = "";
    	String single = "";
    	Date tml= null, start = null, end = null, singleDate = null;

    	try {
    		tml = dateFormat.parse(tomorrow);
    	} catch (Exception e) {
    		return false;
    	}

    	if (hasDateRange()) {
    		if (hasStarted()) {
    			ending = dateFormat.format(endDate);

    			try {
    				end = dateFormat.parse(ending);
    			} catch (Exception e) {
    				return false;
    			}

    			if (end.after(tml)) {
    				return true;
    			}
    		} else {
    			starting = dateFormat.format(startDate);

    			try {
    				start = dateFormat.parse(starting);
    			} catch (Exception e) {
    				return false;
    			}

    			if (start.equals(tml)) {
    				return true;
    			}
    		}
    	} else if (hasDate()) {
    		single = dateFormat.format(getSingleDate());

    		try {
    			singleDate = dateFormat.parse(single);
    		} catch (Exception e) {
    			return true;
    		}

    		if (singleDate.equals(tml)) {
    			return true;
    		}
    	}
    	return false;
    }

    /**
     * This methods check if a task is upcoming.
     * An upcoming task must be after today and tomorrow.
     *
     * @return {@code Boolean} true if upcoming
     */
    public boolean isUpcoming() {
    	if (!isToday() && !isTomorrow()) {
    		Date today = new Date();

    		if (hasStartDate()) {
    			if (today.before(startDate)) {
    				return true;
    			}
    		} else if (hasEndDate()) {
    			if (today.before(endDate)) {
    				return true;
    			}
    		}
    	}
    	return false;
    }

    public boolean isSomeday() {
    	if (hasDate()) {
    		return false;
    	} else {
    		return true;
    	}
    }
}