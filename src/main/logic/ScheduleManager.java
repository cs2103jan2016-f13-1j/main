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
                    } else if (prev.hasDateRange()) {
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
                    } else if (next.hasDateRange()) {
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
            } else if (curr.hasDateRange()) { 
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
                    } else if (prev.hasDateRange()) {
                        //previous is event task
                        if (curr.getStartDate().equals(prev.getStartDate()) || curr.getEndDate().equals(prev.getEndDate())) {
                            curr.setCollideWithPrev(true);
                        } else if (curr.getStartDate().after(prev.getStartDate()) && curr.getStartDate().before(prev.getEndDate())) {
                            //previous's start date falls between current's date range
                            curr.setCollideWithPrev(true);
                        } else if (curr.getEndDate().after(prev.getStartDate()) && curr.getEndDate().before(prev.getEndDate())) {
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
                    } else if (next.hasDateRange()) {
                        //next is event task
                        if (curr.getStartDate().equals(next.getStartDate()) || curr.getEndDate().equals(next.getEndDate())) {
                            curr.setCollideWithNext(true);
                        } else if (curr.getStartDate().after(next.getStartDate()) && curr.getStartDate().before(next.getEndDate())) {
                            //next's start date falls between current's date range
                            curr.setCollideWithNext(true);
                        } else if (curr.getEndDate().after(next.getStartDate()) && curr.getEndDate().before(next.getEndDate())) {
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
