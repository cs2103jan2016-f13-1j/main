package main.data;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @author Joleen
 *
 */

public class Task {	   
    private String title = null;
    private Date startDate = null;
    private Date endDate = null;
    private String label = null;
    private boolean done = false;
    private int priority = 0;
    private Date createdDate = null;
    private Date completedDate = null;

    public Task(String title, Date startDate, Date endDate, String label, Date createdDate) {
    	this.title = title;
    	this.startDate = startDate;
    	this.endDate = endDate;
    	this.label = label;
    	this.createdDate = createdDate;
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
    
    public int togglePriority() {
    	int limit = 4;
    	priority++;
    	priority = priority % limit;
    	return priority;
    }
    
    public Date getCompletedDate() {
        return this.completedDate;
    }
    
    public void setIsCompleted() {
        this.done = true;
        this.completedDate = new Date();
    }
    
    public void setNotCompleted() {
        this.done = false;
        this.completedDate = null;
    }
    
    public Date getCreatedDate() {
        return createdDate;
    }
    
    public int compareTo(Task task) {
        if (!createdDate.equals(task.getCreatedDate())) {
            return -1;
        }
    	return 0;
    }

    public String toString() {
    	int indexTitle = 0;
    	int indexStartDate = 1;
    	int indexStartTime = 2;
    	int indexEndDate = 3;
    	int indexEndTime = 4;
    	int indexLabel = 5;
    	
    	ArrayList<String> fields = getTaskFields();
    	String title = fields.get(indexTitle);
    	String startDate = fields.get(indexStartDate);
    	String startTime = fields.get(indexStartTime);
    	String endDate = fields.get(indexEndDate);
    	String endTime = fields.get(indexEndTime);
    	String label = fields.get(indexLabel);
    	
    	StringBuilder stringBuilder = new StringBuilder(title);

    	if (startDate != null) {
    		stringBuilder.append(" from " + startDate);
    		stringBuilder.append(" " + startTime);

    		if (!startDate.equals(endDate)) {
    			stringBuilder.append(" to " + endDate);
    			stringBuilder.append(" " + endTime);
    		} else {
    			stringBuilder.append(" to " + endTime);
    		}   	  
    	}

    	if (label != null) {
    		stringBuilder.append(" #" + label);
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
     * 3 - End Date
     * 4 - End Time
     * 5 - Label
     * 
     * @return ArrayList<String> of size 6
     */
    private ArrayList<String> getTaskFields() {
        ArrayList<String> fields = new ArrayList<String>();

        fields.add(title);
        
        if (hasDate()) {
        	fields.add(convertDate(startDate));
        	fields.add(convertTime(startDate));
        	fields.add(convertDate(endDate));
        	fields.add(convertTime(endDate));
        } else {
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
        
        return fields;
    }
    
    private String convertDate(Date date) {
    	if (isToday()) {
    		return "today";
    	}
    	
    	if (dateIsThisWeek(date)) {
    		return "this ".concat(getDay(date));
    	} else if (dateIsNextWeek(date)){
    		return "next ".concat(getDay(date));
    	} else {
    		return getDate(date).concat(" ").concat(getMonth(date));
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
    
    private String getDay(Date date) {
    	Locale locale = Locale.getDefault();
    	LocalDateTime dateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    	DayOfWeek day = dateTime.getDayOfWeek();
		return day.getDisplayName(TextStyle.SHORT, locale);
    }
    
    private String getDate(Date date) {
    	LocalDateTime dateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		return Integer.toString(dateTime.getDayOfMonth());
    }
    
    private String getMonth(Date date) {
		Locale locale = Locale.getDefault();
    	LocalDateTime dateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    	Month month = dateTime.getMonth();
		return month.getDisplayName(TextStyle.SHORT, locale);
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
    
    public boolean hasStarted() {
        return (startDate.compareTo(new Date()) < 0);
    }
    
    public boolean isToday() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");
        String today = dateFormat.format(new Date());
        
        if (hasEndDate()) {
            String end = dateFormat.format(endDate);
            if (today.equals(end)) {
                return true;
            }
        } else if (hasStartDate()) {
            String start = dateFormat.format(startDate);
            if (today.equals(start)) {
                return true;
            }
        }
        return false;
    }
}