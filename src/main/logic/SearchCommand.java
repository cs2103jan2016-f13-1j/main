package main.logic;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import main.data.Task;

/**
 * 
 */

/**
 * @author Bevin Seetoh Jia Jin
 *
 */
public class SearchCommand implements Command {
    Receiver receiver;
    String searchTerm;
    Date searchDate;
    
    public SearchCommand(Receiver receiver, String searchTerm) {
        this.receiver = receiver;
        this.searchTerm = searchTerm;
    }
    
    public SearchCommand(Receiver receiver, Date searchDate) {
        this.receiver = receiver;
        this.searchDate = searchDate;
    }
    
    public void execute() {
        ArrayList<Task> searchResults = new ArrayList<Task>();
        if (searchTerm != null) {
            searchResults = search(searchTerm);
        } else if (searchDate != null) {
            searchResults = search(searchDate);
        }
        updateReceiverTasks(searchResults);
        
    }
    
    public void undo() {
    }
    
    /**
     * This method allows you to search for tasks that has description
     * or label found in the given {@code searchTerm}.
     * 
     * @param   searchTerm
     *          The {@code String} of terms to search which are separated by spaces.
     * @return  The list of results
     */
    private ArrayList<Task> search(String searchTerm) {
        ArrayList<Task> allTasks = receiver.getAllTasks();
        String[] searchList = searchTerm.split(" ");
        ArrayList<Task> searchResults = new ArrayList<Task>();
        
        for (Task t : allTasks) {
            String title = t.getTitle().toLowerCase();
            boolean found = true;
            int prevIndex = Integer.MIN_VALUE;
            
            for (String s : searchList) {
                String term = s.toLowerCase();
                
                if (term.contains("#")) {
                    if (t.getLabel() == null) {
                        found = false;
                    } else if (!("#" + t.getLabel()).toLowerCase().contains(term)) {
                        found = false;
                    }
                } else if (term.length() == 1) {
                    if (title.contains(term)) {
                        int currIndex = title.indexOf(term);
                        if (currIndex < prevIndex) {
                            found = false;
                        } else {
                            prevIndex = currIndex;
                        }
                    } else {
                        found = false;
                    }
                } else if (!title.contains(term)) {
                    found = false;
                }
            }
            
            if (found) {
                searchResults.add(t);
            }
        }
        return searchResults;
    }
    
    /**
     * This method allows you to search for tasks by date which have the
     * same date as the given {@code searchDate}. Not time specific.
     * 
     * @param   searchDate
     *          The {@code Date} search.
     * @return  The list of results
     */
    private ArrayList<Task> search(Date searchDate) {
        ArrayList<Task> allTasks = receiver.getAllTasks();
        ArrayList<Task> searchResults = new ArrayList<Task>();
        Calendar dateToSearch = removeTimeFromDate(searchDate);
        
        for (Task t : allTasks) {
            if (t.hasDateRange()) {
                Calendar startDate = removeTimeFromDate(t.getStartDate());
                Calendar endDate = removeTimeFromDate(t.getEndDate());
                if (startDate.equals(dateToSearch) || endDate.equals(dateToSearch)) {
                    searchResults.add(t);
                }
            } else if (t.hasSingleDate()) {
                Calendar singleDate = removeTimeFromDate(t.getSingleDate());
                if (singleDate.equals(dateToSearch)) {
                    searchResults.add(t);
                }
            }
        }
        return searchResults;
    }
    
    //Removes all time details from the given Date
    private Calendar removeTimeFromDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }
    
    private void updateReceiverTasks(ArrayList<Task> searchResults) {
        ArrayList<Task> todoTasks = new ArrayList<Task>();
        ArrayList<Task> completedTasks = new ArrayList<Task>();
        
        for (Task task : searchResults) {
            if (task.isDone()) {
                completedTasks.add(task);
            } else {
                todoTasks.add(task);
            }
        }
        Collections.sort(todoTasks, new TodoTaskComparator());
        Collections.sort(completedTasks, new CompletedTaskComparator());
        
        receiver.setTodoTasks(todoTasks);
        receiver.setCompletedTasks(completedTasks);
        receiver.updateObservers();
    }
}
