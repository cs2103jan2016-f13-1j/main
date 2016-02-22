package main.data;

import java.util.ArrayList;
import java.util.Date;

/**
 * @author Joleeen
 *
 */

public class Task {
    //required
    private String title;
    
    //optional
    private int status; //01 undone done
    private int priority; //0123 no LMH
    private boolean isPostpone;
    private boolean isRecurring;
    private ArrayList<String> labels;
    private Date startDate;
    private Date endDate;
    
    public String getTitle() {
        return title;
    }
    
    public int getStatus() {
        return status;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public boolean getIsPostpone(){
        return isPostpone;
    }
    
    public boolean getIsRecurring() {
        return isRecurring;
    }
    
    public ArrayList<String> getLabels() {
        return labels;
    }
    
    public Date getStartDate() {
        return startDate;
    }
    
    public Date getEndDate() {
        return endDate;
    }
    
    @Override
    public String toString() {
        int startDateOnly = startDate.getDate();
        int startTime = startDate.getHours();
        int endDateOnly = endDate.getDate();
        int endTime = endDate.getHours();
        return title + " from " + startDateOnly + " " + startTime + " to " + endDateOnly + " " + endTime + " label: " + labels;
    }
    
    private Task(TaskBuilder builder) {
        this.title = builder.title;
        this.status = builder.status;
        this.priority = builder.priority;
        this.isPostpone = builder.isPostpone;
        this.isRecurring = builder.isRecurring;
        this.labels = builder.labels;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
    }
    
    public static class TaskBuilder {
        private String title;
        private int status;
        private int priority;
        private boolean isPostpone;
        private boolean isRecurring;
        private ArrayList<String> labels;
        private Date startDate;
        private Date endDate;
        
        public TaskBuilder (String title) {
            this.title = title;
        }
        
        public TaskBuilder setStatus(int status) {
            this.status = status;
            return this;
        }
        
        public TaskBuilder setPriority(int priority) {
            this.priority = priority;
            return this;
        }
        
        public TaskBuilder setPostpone(boolean isPostpone) {
            this.isPostpone = isPostpone;
            return this;
        }
        
        public TaskBuilder setRecurring(boolean isRecurring) {
            this.isRecurring = isRecurring;
            return this;
        }
        
        public TaskBuilder setLabels(ArrayList<String> labels) {
            this.labels = labels;
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
