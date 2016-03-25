package main.data;

import java.text.DateFormatSymbols;
import java.text.ParseException;
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
    
    public Task(String title) {
        this.title = title;
        this.createdDate = new Date();
    }

    public Task(String title, Date startDate, Date endDate, String label) {
    	this.title = title;
    	this.startDate = startDate;
    	this.endDate = endDate;
    	this.label = label;
    	this.createdDate = new Date();
    }
    
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
        return this.completedDate;
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

    	if (hasDate()) {
    		if (hasDateRange()) {
    			stringBuilder.append(" from " + startDate);
    			stringBuilder.append(" " + startTime);
    			
    			if (!startDate.equals(endDate)) {
        			stringBuilder.append(" to " + endDate);
        			stringBuilder.append(" " + endTime);
        		} else {
        			stringBuilder.append(" to " + endTime);
        		}   	  
    		} else {
    			if (startDate != null) {
    				stringBuilder.append(" from " + startDate);
    				stringBuilder.append(" " + startTime);
    			}
    			
    			if (endDate != null) {
    				stringBuilder.append(" by " + endDate);
    				stringBuilder.append(" " + endTime);
    			}
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
        	if (hasStartDate()){
        		fields.add(convertDate(startDate));
            	fields.add(convertTime(startDate));
        	} else {
        		fields.add(null);
            	fields.add(null);
        	}
        	
        	if (hasEndDate()) {
	        	fields.add(convertDate(endDate));
	        	fields.add(convertTime(endDate));
        	} else {
        		fields.add(null);
            	fields.add(null);
        	}
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
    	if (dateIsToday()) {
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
    
    public boolean dateIsToday() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");
        String today = dateFormat.format(new Date());
        
       if (hasStartDate()) {
            String start = dateFormat.format(startDate);
            if (today.equals(start)) {
                return true;
            }
        } else if (hasEndDate()) {
            String end = dateFormat.format(endDate);
            if (today.equals(end)) {
                return true;
            }
        }
        return false;
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
    
    public String getSimpleDate() {
    	StringBuilder stringBuilder = new StringBuilder("");    	
    	String start, end;
    	if (hasDateRange()) {
    		start = getDate(startDate).concat(" ").concat(getMonth(startDate));
        	end = getDate(endDate).concat(" ").concat(getMonth(endDate));
        	if (start.equals(end)) {
        		stringBuilder.append(start);
        	} else {
        		stringBuilder.append(start + " - " + end);
        	}
    	} else if (hasStartDate()) {
    		start = getDate(startDate).concat(" ").concat(getMonth(startDate));
    		stringBuilder.append(start);
    	} else if (hasEndDate()) {
    		end = getDate(endDate).concat(" ").concat(getMonth(endDate));
    		stringBuilder.append(end);
    	}
    	
    	return stringBuilder.toString();
    }
    
    public String getSimpleTime() {
    	StringBuilder stringBuilder = new StringBuilder("");
    	int indexStartTime = 2;
    	int indexEndTime = 4;
    	
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
    		stringBuilder.append("-");
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
    
    public boolean isToday() {
    	if (hasDateRange()) {
    		if (hasStarted()) {
    			return true;
    		} else {
    			return false;
    		}
    	} else {
    		return dateIsToday();
    	}
    }
    
    public boolean isTomorrow() throws ParseException {   	
    	Calendar cal = Calendar.getInstance();
    	cal.setTime(new Date());
    	cal.add(Calendar.DATE, 1);
    	
    	SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");
    	String tomorrow = dateFormat.format(cal.getTime());
    	String starting = "";
    	String ending = "";
    	Date tml, start = null, end = null;
    	
    	tml = dateFormat.parse(tomorrow);
    	
    	if (hasDateRange()) {
    		if (hasStarted()) {
    			ending = dateFormat.format(endDate);  			
    			end = dateFormat.parse(ending);
    			if (end.after(tml)) {
    				return true;
    			}
    		} else {
    			starting = dateFormat.format(startDate);
    			start = dateFormat.parse(starting);
    			if (start.equals(tml)) {
    				return true;
    			}
    		}
    	} else {
    		if (hasStartDate()) {
    			starting = dateFormat.format(startDate);
    			start = dateFormat.parse(starting);
    			
    			if (start.equals(tml)) {
    				return true;
    			}
    		} else if (hasEndDate()) {
    			ending = dateFormat.format(endDate);
    			end = dateFormat.parse(ending);
    			if (end.equals(tml)) {
    				return true;
    			}
    		}
    	}
 
    	return false;	
    }

    public boolean isUpcoming() throws ParseException {
    	if (hasDate()) {
    		if (!dateIsToday() && !isTomorrow()) {
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