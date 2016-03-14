package main.data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Joleen
 *
 */

public class Task {
    private String title;
    private boolean done;
    private int priority;
    private String label;
    private Date startDate;
    private Date endDate;
    
    public String getTitle() {
        return title;
    }
    
    public boolean getDone() {
        return done;
    }
    
    public void setDone(boolean done) {
        this.done = done;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public String getLabel() {
        return label;
    }
    
    public Date getStartDate() {
        return startDate;
    }
    
    public Date getEndDate() {
        return endDate;
    }
    
    public String toString() {
        String feedback = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("d/M (EEE) HH:mm");
        
        if (hasDate()) {
        	 String startDateTime = dateFormat.format(startDate);
             String endDateTime = dateFormat.format(endDate);
             feedback = title + " from " + startDateTime + " to " + endDateTime;
        } else {
        	feedback = title;
        }
        
        if (hasLabel()) {
            feedback += " #" + label;
        }
        
        return feedback;
    }
    
    public ArrayList<String> getParameters() {
        ArrayList<String> feedback = new ArrayList<String>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("d/M (EEE) HH:mm");

        feedback.add(title);
        
        if (hasDate()) {
        	 feedback.add(dateFormat.format(startDate));
        	 feedback.add(dateFormat.format(endDate));
        } else {
        	feedback.add(null);
        	feedback.add(null);
        }
        
        if (hasLabel()) {
            feedback.add(label);
        } else {
        	feedback.add(null);
        }
        
        return feedback;
    }
    
    public boolean isThisWeek() {
        Date tomorrow = getTomorrow();
        Date eighthDay = getEigthDay();
        
        if (hasEndDate()) {
            if (endDate.compareTo(tomorrow) >= 0 && endDate.compareTo(eighthDay) < 0) {
                return true;
            }
        } else if (hasStartDate()) {
            if (startDate.compareTo(tomorrow) >= 0 && startDate.compareTo(eighthDay) < 0) {
                return true;
            }
        }
        return false;
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
    
    private Date getTomorrow() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE,1);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        return calendar.getTime();
    }
    
    private Date getEigthDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE,8);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        return calendar.getTime();
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
    
    public boolean hasLabel() {
        if (label == null) {
            return false;
        } else {
            return true;
        }
    }
    
    private Task(TaskBuilder builder) {
        this.title = builder.title;
        this.done = builder.done;
        this.priority = builder.priority;
        this.label = builder.label;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
    }
    
    public static class TaskBuilder {
        private String title = null;
        private boolean done = false;
        private int priority = 0;
        private String label = null;
        private Date startDate = null;
        private Date endDate = null;
        
        public TaskBuilder (String title) {
            this.title = title;
        }
        
        public TaskBuilder setDone(boolean done) {
            this.done = done;
            return this;
        }
        
        public TaskBuilder setPriority(int priority) {
            this.priority = priority;
            return this;
        }
        
        public TaskBuilder setLabel(String label) {
            this.label = label;
            return this;
        }
        
        public TaskBuilder setStartDate(Date startDate) {
            this.startDate = startDate;
            return this;
        }
        
        public TaskBuilder setEndDate(Date endDate) {
            this.endDate = endDate;
            return this;
        }
        
        public Task build() {
            return new Task(this);
        }
    }
}