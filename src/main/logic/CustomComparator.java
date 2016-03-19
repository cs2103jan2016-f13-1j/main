package main.logic;
import java.util.Comparator;

import main.data.Task;

class lastAddedFirst implements Comparator<Task> {
    public int compare(Task t1, Task t2) {
        if (!t1.hasDate() && !t2.hasDate()) {
            //If both are floating tasks, compare the created date
            return t2.getCreatedDate().compareTo(t1.getCreatedDate());
        } else if (!t1.hasDate() && t2.hasDate()) {
            //If one is floating and the other is dated, return the floating task
            return -1;
        } else if (t1.hasDate() && !t2.hasDate()) {
            //If one is floating and the other is dated, return the floating task
            return 1;
        } else {
            //Both tasks has date
            if (t1.hasStarted() && t2.hasStarted()) {
                if (t1.getEndDate().compareTo(t2.getEndDate()) == 0) {
                    return t1.getTitle().compareTo(t2.getTitle());
                } else {
                    //If both tasks already started, return earlier deadline first
                    return t1.getEndDate().compareTo(t2.getEndDate());
                }
            } else if (!t1.hasStarted() && !t2.hasStarted()) {
                //If both have not started yet
                return t1.getEndDate().compareTo(t2.getEndDate());
            } else if (t1.hasStarted()) {
                return -1;
            } else if (t2.hasStarted()) {
                return 1;
            } else {
                System.out.println("FAIL TO COMPARE");
                return 0;
            }
        }
    }
}

class LastCompletedFirst implements Comparator<Task> {
    public int compare(Task t1, Task t2) {
        System.out.println("SORTING COMPLETED");
        if (t2.getCompletedDate().compareTo(t1.getCompletedDate()) == 0) {
            //If both tasks are set completed at the same time, compare the titles
            return t1.getTitle().compareTo(t2.getTitle());
        } else {
            //Return the tasks that was completed later first
            return t2.getCompletedDate().compareTo(t1.getCompletedDate());
        }
    }
}
