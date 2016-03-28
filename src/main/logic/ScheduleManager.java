package main.logic;

import main.data.Task;

public class ScheduleManager {
    
    public ScheduleManager() {
    }
    
    public void updateCollision(Task prev, Task curr, Task next) {
        if (curr.hasDate()) {
            if (curr.hasSingleDate() && curr.hasStartDate()) {
                //current only have a start date
                if (prev != null && prev.hasDate()) {
                    if (prev.hasSingleDate() && prev.hasStartDate()) {
                        //previous only have a start date
                        if (curr.getSingleDate().equals(prev.getSingleDate())) {
                            curr.setCollideWithPrev(true);
                        } else {
                            curr.setCollideWithPrev(false);
                        }
                    } else {
                        //previous is event task
                        if (curr.getSingleDate().equals(prev.getStartDate())) {
                            //start date collides
                            curr.setCollideWithPrev(true);
                        } else if (curr.getSingleDate().after(prev.getStartDate()) && curr.getSingleDate().before(prev.getEndDate())) {
                            //current date falls between previous's date range
                            curr.setCollideWithPrev(true);
                        } else {
                            curr.setCollideWithPrev(false);
                        }
                    }
                }
                
                if (next != null && next.hasDate()) {
                    if (next.hasSingleDate() && next.hasStartDate()) {
                        //next only have a start date
                        if (curr.getSingleDate().equals(next.getSingleDate())) {
                            curr.setCollideWithNext(true);
                        } else {
                            curr.setCollideWithNext(false);
                        }
                    } else {
                        //next is event task
                        if (curr.getSingleDate().equals(next.getStartDate())) {
                            //start date collides
                            curr.setCollideWithNext(true);
                        } else if (curr.getSingleDate().after(next.getStartDate()) && curr.getSingleDate().before(next.getEndDate())) {
                            //current date falls between next's date range
                            curr.setCollideWithNext(true);
                        } else {
                            curr.setCollideWithNext(false);
                        }
                    }
                }
            } else { 
                //current is event task
                if (prev != null && prev.hasDate()) {
                    if (prev.hasSingleDate() && prev.hasStartDate()) {
                        //previous only have a start date
                        if (prev.getSingleDate().equals(curr.getStartDate())) {
                            //start date collides
                            curr.setCollideWithPrev(true);
                        } else if (prev.getSingleDate().after(curr.getStartDate()) && prev.getSingleDate().before(curr.getEndDate())) {
                            //previous date falls between current's date range
                            curr.setCollideWithPrev(true);
                        } else {
                            curr.setCollideWithNext(false);
                        }
                    } else {
                        //previous is event task
                        if (prev.getStartDate().after(curr.getStartDate()) && prev.getStartDate().before(curr.getEndDate())) {
                            //previous's start date falls between current's date range
                            curr.setCollideWithPrev(true);
                        } else if (prev.getEndDate().after(curr.getStartDate()) && prev.getEndDate().before(curr.getEndDate())) {
                            //previous's end date falls between current's date range
                            curr.setCollideWithPrev(true);
                        } else {
                            curr.setCollideWithPrev(false);
                        }
                    }
                }
                
                if (next != null && next.hasDate()) {
                    if (next.hasSingleDate() && next.hasStartDate()) {
                        //next only have a start date
                        if (next.getSingleDate().equals(curr.getStartDate())) {
                            //start date collides
                            curr.setCollideWithNext(true);
                        } else if (next.getSingleDate().after(curr.getStartDate()) && next.getSingleDate().before(curr.getEndDate())) {
                            //next date falls between current's date range
                            curr.setCollideWithNext(true);
                        } else {
                            curr.setCollideWithNext(false);
                        }
                    } else {
                        //next is event task
                        if (next.getStartDate().after(curr.getStartDate()) && next.getStartDate().before(curr.getEndDate())) {
                            //next's start date falls between current's date range
                            curr.setCollideWithNext(true);
                        } else if (next.getEndDate().after(curr.getStartDate()) && next.getEndDate().before(curr.getEndDate())) {
                            //next's end date falls between current's date range
                            curr.setCollideWithNext(true);
                        } else {
                            curr.setCollideWithNext(false);
                        }
                    }
                }
            }
        } else {
            curr.setCollideWithPrev(false);
            curr.setCollideWithNext(false);
        }
    }
}
