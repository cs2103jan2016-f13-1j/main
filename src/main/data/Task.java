package main.data;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Joleen
 *
 */

public class Task {
    private String title;
    
    boolean status;
    private int priority;
    private String label;
    private Date startDate;
    private Date endDate;
    
    public String getTitle() {
        return title;
    }
    
    public boolean getStatus() {
        return status;
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd HH:mm");
        
        if (startDate == null) {
            if (endDate == null) {
                //floating task
                if (label == null) {
                    feedback = title;
                } else {
                    feedback = title + " #" + label;
                }
            } else {
                //end date only
            }
        } else {
            String startDateTime = dateFormat.format(startDate);
            String endDateTime = dateFormat.format(endDate);
            feedback = title + " from " + startDateTime + " to " + endDateTime + " #" + label;
        }
        
        return feedback;
    }
    
    private Task(TaskBuilder builder) {
        this.title = builder.title;
        this.status = builder.status;
        this.priority = builder.priority;
        this.label = builder.label;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
    }
    
    public static class TaskBuilder {
        private String title = null;
        private boolean status = false;
        private int priority = 0;
        private String label = null;
        private Date startDate = null;
        private Date endDate = null;
        
        public TaskBuilder (String title) {
            this.title = title;
        }
        
        public TaskBuilder setStatus(boolean status) {
            this.status = status;
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