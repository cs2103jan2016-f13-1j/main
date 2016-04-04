package main.logic;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
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
    private ArrayList<Task> search(String searchString) {
        String searchTerm = searchString.toLowerCase();
        
        if (searchTerm.equals("this week") || searchTerm.equals("week")) {
            return searchForThisWeek();
        } else if (searchTerm.equals("next week")) {
            return searchForNextWeek();
        } else if (searchTerm.equals("upcoming")) {
            return searchForUpcoming();
        } else if (searchTerm.equals("someday")) {
            return searchForSomeday();
        } else if (searchTerm.equals("deadline")) {
            return searchForDeadline();
        } else {
            return searchForString(searchTerm);
        }
    }
    
    private ArrayList<Task> searchForThisWeek() {
        ArrayList<Task> allTasks = receiver.getAllTasks();
        ArrayList<Task> searchResults = new ArrayList<Task>();
        
        LocalDate now = LocalDate.now();
        LocalDate sundayLocalDate = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        Date today = Date.from(now.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date sunday = Date.from(sundayLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        
        for (Task task : allTasks) {
            if (task.hasDateRange()) {
                Date startDate = removeTimeFromDate(task.getStartDate());
                Date endDate = removeTimeFromDate(task.getEndDate());
                if (startDate.equals(today) || startDate.equals(sunday) || (startDate.after(today) && endDate.before(sunday))) {
                    searchResults.add(task);
                } else if (endDate.equals(today) || endDate.equals(sunday) || (endDate.after(today) && endDate.before(sunday))) {
                    searchResults.add(task);
                }
            } else if (task.hasSingleDate()) {
                Date singleDate = removeTimeFromDate(task.getSingleDate());
                if (singleDate.equals(today) || singleDate.equals(sunday) || (singleDate.after(today) && singleDate.before(sunday))) {
                    searchResults.add(task);
                }
            }
        }
        
        return searchResults;
    }
    
    private ArrayList<Task> searchForNextWeek() {
        ArrayList<Task> allTasks = receiver.getAllTasks();
        ArrayList<Task> searchResults = new ArrayList<Task>();
        
        LocalDate now = LocalDate.now();
        LocalDate sundayLocalDate = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        LocalDate nextSundayLocalDate = sundayLocalDate.with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        Date sunday = Date.from(sundayLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date nextSunday = Date.from(nextSundayLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        
        for (Task task : allTasks) {
            if (task.hasDateRange()) {
                Date startDate = removeTimeFromDate(task.getStartDate());
                Date endDate = removeTimeFromDate(task.getEndDate());
                if (startDate.equals(nextSunday) || (startDate.after(sunday) && endDate.before(nextSunday))) {
                    searchResults.add(task);
                } else if (endDate.equals(nextSunday) || (endDate.after(sunday) && endDate.before(nextSunday))) {
                    searchResults.add(task);
                }
            } else if (task.hasSingleDate()) {
                Date singleDate = removeTimeFromDate(task.getSingleDate());
                if (singleDate.equals(nextSunday) || (singleDate.after(sunday) && singleDate.before(nextSunday))) {
                    searchResults.add(task);
                }
            }
        }
        
        return searchResults;
    }
    
    private ArrayList<Task> searchForUpcoming() {
        ArrayList<Task> allTasks = receiver.getAllTasks();
        ArrayList<Task> searchResults = new ArrayList<Task>();
        
        LocalDate tmr = LocalDate.now().plusDays(1);
        Date tomorrow = Date.from(tmr.atStartOfDay(ZoneId.systemDefault()).toInstant());
        
        for (Task task : allTasks) {
            if (task.hasDateRange()) {
                Date startDate = removeTimeFromDate(task.getStartDate());
                Date endDate = removeTimeFromDate(task.getEndDate());
                if (startDate.after(tomorrow) || endDate.after(tomorrow)) {
                    searchResults.add(task);
                }
            } else if (task.hasSingleDate()) {
                Date singleDate = removeTimeFromDate(task.getSingleDate());
                if (singleDate.after(tomorrow)) {
                    searchResults.add(task);
                }
            }
        }
        
        return searchResults;
    }
    
    private ArrayList<Task> searchForSomeday() {
        ArrayList<Task> allTasks = receiver.getAllTasks();
        ArrayList<Task> searchResults = new ArrayList<Task>();
        
        for (Task task : allTasks) {
            if (!task.hasDate()) {
                searchResults.add(task);
            }
        }
        
        return searchResults;
    }
    
    private ArrayList<Task> searchForDeadline() {
        ArrayList<Task> allTasks = receiver.getAllTasks();
        ArrayList<Task> searchResults = new ArrayList<Task>();
        
        for (Task task : allTasks) {
            if (task.hasSingleDate() && task.hasEndDate()) {
                searchResults.add(task);
            }
        }
        
        return searchResults;
    }
    
    private ArrayList<Task> searchForString(String searchTerm) {
        ArrayList<Task> allTasks = receiver.getAllTasks();
        ArrayList<Task> searchResults = new ArrayList<Task>();
        String[] searchList = searchTerm.split(" ");
        
        for (Task task : allTasks) {
            String title = task.getTitle().toLowerCase();
            boolean found = true;
            int prevIndex = Integer.MIN_VALUE;
            
            for (String term : searchList) {         
                if (term.contains("#")) {
                    if (task.getLabel() == null) {
                        found = false;
                    } else if (!("#" + task.getLabel()).toLowerCase().contains(term)) {
                        found = false;
                    }
                } else {
                    String[] characters = term.split("");
                    boolean titleContainAllChars = true;
                    
                    for (String c : characters) {
                        if (!title.contains(c)) {
                            titleContainAllChars = false;
                        }
                    }
                    
                    if (titleContainAllChars) {
                        for (String character : characters) {
                            int currIndex;
                            if (prevIndex == Integer.MIN_VALUE) {
                                currIndex = title.indexOf(character);
                            } else {
                                currIndex = title.indexOf(character, prevIndex + 1);
                            }
                            if (currIndex < prevIndex) {
                                found = false;
                            } else {
                                prevIndex = currIndex;
                            }
                        }
                    } else {
                        found = false;
                    }
                }
            }
            
            if (found) {
                searchResults.add(task);
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
        Date dateToSearch = removeTimeFromDate(searchDate);
        
        for (Task task : allTasks) {
            if (task.hasDateRange()) {
                Date startDate = removeTimeFromDate(task.getStartDate());
                Date endDate = removeTimeFromDate(task.getEndDate());
                if (startDate.equals(dateToSearch) || endDate.equals(dateToSearch)) {
                    searchResults.add(task);
                }
            } else if (task.hasSingleDate()) {
                Date singleDate = removeTimeFromDate(task.getSingleDate());
                if (singleDate.equals(dateToSearch)) {
                    searchResults.add(task);
                }
            }
        }
        return searchResults;
    }
    
    //Removes all time details from the given Date
    private Date removeTimeFromDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
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
