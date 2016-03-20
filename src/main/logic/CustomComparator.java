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
 *       - if same title, return latest created first
 * 2. If only one has a date,
 *       - return the other that does not have a date
 * 3. If both have dates,
 *       - if both already started,
 *          - if same end date, return latest created first
 *          - if different end date, return earlier deadline first
 *       - if only one started, return the task that started
 */
class lastAddedFirst implements Comparator<Task> {
    public int compare(Task t1, Task t2) {
        if (!t1.hasDate() && !t2.hasDate()) {
            //If both are floating tasks, compare the created date
            return t2.getCreatedDate().compareTo(t1.getCreatedDate());
        } else if (!t1.hasDate() && t2.hasDate()) {
            //If one is floating and the other is dated, return the floating task
            return -1;
        } else if (t1.hasDate() && !t2.hasDate()) {
            //If one is dated and the other is floating, return the floating task
            return 1;
        } else {
            //Both tasks has date
            if (t1.hasStarted() && t2.hasStarted()) {
                if (t1.getEndDate().compareTo(t2.getEndDate()) == 0) {
                    //If both tasks already started and has same end date, compare the created date
                    return t2.getCreatedDate().compareTo(t1.getCreatedDate());
                } else {
                    //If both tasks already started, return earlier deadline first
                    return t1.getEndDate().compareTo(t2.getEndDate());
                }
            } else if (!t1.hasStarted() && !t2.hasStarted()) {
                //If both have not started yet
                return t1.getEndDate().compareTo(t2.getEndDate());
            } else if (t1.hasStarted()) {
                //t1 has started but not t2
                return -1;
            } else {
                //t2 has started but not t1
                return 1;
            }
        }
    }
}

/**
 * This comparator sorts tasks by a few rules:
 * 1. If both tasks are completed at the same time, return the task that was created later
 * 2. If not completed at the same time, return the last completed task
 */
class LastCompletedFirst implements Comparator<Task> {
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
