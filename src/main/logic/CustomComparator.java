/** 
 * This class acts as a container to store custom comparators
 * used to sort tasks by different conditions
 */

/**
 * @author Bevin Seetoh Jia Jin
 *
 */

package main.logic;
import java.util.Comparator;

import main.data.Task;

/**
 * This comparator sorts tasks by a few rules:
 * 1. If both are floating tasks,
 *       - return earlier created first
 * 2. If only one has a date,
 *       - return the floating task
 * 3. If both have dates,
 *       - if both have single date, return earlier date first
 *       - if one have single date and the other is ranged, return earlier date first
 *       - if both are ranged,
 *          - if both started, return earlier created first
 *          - if only one started, return the task that already started
 *          - if both have not started, return earlier end date first
 */
class TodoTaskComparator implements Comparator<Task> {
    public int compare(Task t1, Task t2) {
        if (!t1.hasDate() && !t2.hasDate()) {
            //If both are floating tasks
            if (t1.getPriority() == t2.getPriority()) {
                //If equal priority, compare the created date
                return t1.getCreatedDate().compareTo(t2.getCreatedDate());
            } else {
                //Return higher priority first
                return t2.getPriority() - t1.getPriority();
            }
        } else if (!t1.hasDate() && t2.hasDate()) {
            //If one is floating and the other is dated, return the floating task
            return 1;
        } else if (t1.hasDate() && !t2.hasDate()) {
            //If one is dated and the other is floating, return the floating task
            return -1;
        } else {
            //Both tasks has date
            if (t1.hasSingleDate() && t2.hasSingleDate()) {
                //If both only has one date
                return t1.getSingleDate().compareTo(t2.getSingleDate());
            } else if (t1.hasSingleDate() && t2.hasDateRange()) {
                //If one has single date and the other is ranged
                if (t1.hasStarted()) {
                    //t1 have stared
                    return t1.getSingleDate().compareTo(t2.getEndDate());
                } else {
                    //t1 have not started
                    if (t1.getSingleDate().compareTo(t2.getStartDate()) == 0) {
                        return -1;
                    } else {
                        return t1.getSingleDate().compareTo(t2.getStartDate());
                    }
                }
            } else if (t1.hasDateRange() && t2.hasSingleDate()) {
                //If one is ranged and the other has single date
                if (t1.hasStarted()) {
                    //t1 have stared
                    return t1.getEndDate().compareTo(t2.getSingleDate());
                } else {
                    //t1 have not started
                    if (t1.getStartDate().compareTo(t2.getSingleDate()) == 0) {
                        return 1;
                    } else {
                        return t1.getStartDate().compareTo(t2.getSingleDate());
                    }
                }
            } else {
                //Both tasks are ranged dates
                if (t1.hasStarted() && t2.hasStarted()) {
                    //If both tasks already started
                    if (t1.getEndDate().compareTo(t2.getEndDate()) == 0) {
                        //If both tasks has same end date
                        if (t1.getPriority() == t2.getPriority()) {
                            //If equal priority, compare the created date
                            return t1.getCreatedDate().compareTo(t2.getCreatedDate());
                        } else {
                            //Return higher priority first
                            return t2.getPriority() - t1.getPriority();
                        }
                    } else {
                        //If both tasks has different end date, return earlier deadline first
                        return t1.getEndDate().compareTo(t2.getEndDate());
                    }
                } else if (t1.hasStarted() && !t2.hasStarted()) {
                    //t1 has started but not t2
                    return -1;
                } else if (!t1.hasStarted() && t2.hasStarted()) {
                    //t2 has started but not t1
                    return 1;
                } else {
                    //If both have not started yet
                    if (t1.getStartDate().compareTo(t2.getStartDate()) == 0) {
                        //If both have the same start date
                        if (t1.getPriority() == t2.getPriority()) {
                            //If equal priority, compare the created date
                            return t1.getCreatedDate().compareTo(t2.getCreatedDate());
                        } else {
                            //Return higher priority first
                            return t2.getPriority() - t1.getPriority();
                        }
                    } else {
                        //Return earlier start date first
                        return t1.getStartDate().compareTo(t2.getStartDate());
                    }
                }
            }
        }
    }
}

/**
 * This comparator sorts tasks by a few rules:
 * 1. If both tasks are completed at the same time, return the task that was created later
 * 2. If not completed at the same time, return the last completed task
 */
class CompletedTaskComparator implements Comparator<Task> {
    public int compare(Task t1, Task t2) {
        if (t2.getCompletedDate().compareTo(t1.getCompletedDate()) == 0) {
            //If both tasks are set completed at the same time, compare the created date, later first
            return t2.getCreatedDate().compareTo(t1.getCreatedDate());
        } else {
            //Return the tasks that was completed later first
            return t2.getCompletedDate().compareTo(t1.getCompletedDate());
        }
    }
}
