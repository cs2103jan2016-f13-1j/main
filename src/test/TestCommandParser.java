//@@author A0126297X
package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.junit.Test;

import main.data.ParseIndexResult;
import main.data.Task;
import main.parser.CommandParser;
import main.parser.exceptions.*;

public class TestCommandParser {
    /**
     * Test detection of time in user input
     * Method checkForTime has been updated to private.
     * This is for reference only.
     */
    /*
     * public void testCheckTime() {
     * CommandParser parser = new CommandParser();
     *
     * assertEquals(true, parser.checkForTime("1am"));
     * assertEquals(true, parser.checkForTime("2PM"));
     * assertEquals(true, parser.checkForTime("12:50pm"));
     *
     * assertEquals(false, parser.checkForTime("13am"));
     * assertEquals(false, parser.checkForTime("112pm"));
     * assertEquals(false, parser.checkForTime("12:60pm"));
     * assertEquals(false, parser.checkForTime("12:592pm"));
     * }
     */

    /**
     * Test detection of ranged time in user input.
     * Method checkForRangeTime has been updated to private.
     * This is for reference only.
     * See testTaskWithTimeRange() for valid testing.
     */
    /*
     * public void testCheckRange() {
     * CommandParser parser = new CommandParser();
     *
     * assertEquals(true, parser.checkForRangeTime("1am-4"));
     * assertEquals(true, parser.checkForRangeTime("1-4am"));
     * assertEquals(true, parser.checkForRangeTime("1am-4am"));
     *
     * assertEquals(true, parser.checkForRangeTime("10am-11"));
     * assertEquals(true, parser.checkForRangeTime("10-11am"));
     * assertEquals(true, parser.checkForRangeTime("10am-11am"));
     *
     * assertEquals(true, parser.checkForRangeTime("1:30am-4:30"));
     * assertEquals(true, parser.checkForRangeTime("1:30-4:30am"));
     * assertEquals(true, parser.checkForRangeTime("1:30am-4:30am"));
     *
     * assertEquals(true, parser.checkForRangeTime("10:30am-11:30"));
     * assertEquals(true, parser.checkForRangeTime("10:30-11:30am"));
     * assertEquals(true, parser.checkForRangeTime("10:30am-11:30am"));
     *
     * assertEquals(false, parser.checkForRangeTime("1-4"));
     *
     * assertEquals(false, parser.checkForRangeTime("1am-41"));
     * assertEquals(false, parser.checkForRangeTime("1-41am"));
     * assertEquals(false, parser.checkForRangeTime("1am-41am"));
     *
     * assertEquals(false, parser.checkForRangeTime("41am-1"));
     * assertEquals(false, parser.checkForRangeTime("41-1am"));
     * assertEquals(false, parser.checkForRangeTime("41am-1am"));
     *
     * assertEquals(false, parser.checkForRangeTime("1:60m-4:60"));
     * assertEquals(false, parser.checkForRangeTime("1:60-4:60am"));
     * assertEquals(false, parser.checkForRangeTime("1:60am-4:60am"));
     *
     * assertEquals(false, parser.checkForRangeTime("10:60am-11:60"));
     * assertEquals(false, parser.checkForRangeTime("10:60-11:60am"));
     * assertEquals(false, parser.checkForRangeTime("10:60am-11:60am"));
     * }
     */

    /**
     * Test detection of date in number format in user input.
     * Method checkForDate has been updated to private.
     * This is for reference only.
     */
    /*
     * public void testCheckDate() {
     * CommandParser parser = new CommandParser();
     *
     * assertEquals(true, parser.checkForDate("1/5"));
     * assertEquals(true, parser.checkForDate("1/12"));
     * assertEquals(true, parser.checkForDate("30-5"));
     * assertEquals(true, parser.checkForDate("31-5"));
     *
     * assertEquals(true, parser.checkForDate("from 1/5"));
     * assertEquals(true, parser.checkForDate("after 1/12"));
     * assertEquals(true, parser.checkForDate("at 30-5"));
     * assertEquals(true, parser.checkForDate("on 31-5"));
     *
     * assertEquals(false, parser.checkForDate("1/13"));
     * assertEquals(false, parser.checkForDate("33/5"));
     * assertEquals(false, parser.checkForDate("33/50"));
     * }
     */

    /**
     * Test detection of date in text format in user input.
     * Method checkForDateText has been updated to private.
     * This is for reference only.
     */
    /*
     * public void testCheckDateText() {
     * CommandParser parser = new CommandParser();
     *
     * assertEquals(true, parser.checkForDateText("1 march"));
     * assertEquals(true, parser.checkForDateText("31 april"));
     * assertEquals(true, parser.checkForDateText("20 may"));
     *
     * assertEquals(true, parser.checkForDateText("1 mar"));
     * assertEquals(true, parser.checkForDateText("31 apr"));
     * assertEquals(true, parser.checkForDateText("20 jun"));
     *
     * assertEquals(true, parser.checkForDateText("from 1 march"));
     * assertEquals(true, parser.checkForDateText("by 31 april"));
     * assertEquals(true, parser.checkForDateText("on 20 may"));
     *
     * assertEquals(true, parser.checkForDateText("from 1 mar"));
     * assertEquals(true, parser.checkForDateText("by 31 apr"));
     * assertEquals(true, parser.checkForDateText("on 20 jun"));
     *
     * assertEquals(false, parser.checkForDateText("32 march"));
     * assertEquals(false, parser.checkForDateText("55 june"));
     *
     * assertEquals(false, parser.checkForDateText("1 januaryy"));
     * assertEquals(false, parser.checkForDateText("1 janu"));
     * assertEquals(false, parser.checkForDateText("1 jjan"));
     * }
     */

    /**
     * Test detection of days in text format in user input.
     * Method checkForDay has been updated to private.
     * This is for reference only.
     */
    /*
     * public void testCheckDayText() {
     * CommandParser parser = new CommandParser();
     *
     * assertEquals(true, parser.checkForDay("monday"));
     * assertEquals(true, parser.checkForDay("thur"));
     * assertEquals(true, parser.checkForDay("thurs"));
     *
     * assertEquals(true, parser.checkForDay("from monday"));
     * assertEquals(true, parser.checkForDay("on thur"));
     * assertEquals(true, parser.checkForDay("at thurs"));
     *
     *
     * assertEquals(false, parser.checkForDay("mondayy"));
     * assertEquals(false, parser.checkForDay("mmon"));
     * assertEquals(false, parser.checkForDay("monn"));
     * }
     */

    // =============================
    // Add's stuff
    // =============================

    /**
     * Test for the detection of floating task.
     * Even with prepositions, it should not be dated.
     *
     * @throws InvalidLabelFormat
     * @throws InvalidTitle
     */
    @Test
    public void testDetectFloating() throws InvalidLabelFormat {
        CommandParser parser = new CommandParser();

        Task task = parser.parseAdd("Do assignment 1");
        assertEquals(false, task.hasDate());

        task = parser.parseAdd("Undo task 3");
        assertEquals(false, task.hasDate());

        task = parser.parseAdd("Fetch my brothers from school");
        assertEquals(false, task.hasDate());

        task = parser.parseAdd("Send 100 emails from my computer");
        assertEquals(false, task.hasDate());

        task = parser.parseAdd("Drive by the supermarket");
        assertEquals(false, task.hasDate());

        task = parser.parseAdd("Attack enemy base on signal");
        assertEquals(false, task.hasDate());

        task = parser.parseAdd("Send 100 email before I sleep");
        assertEquals(false, task.hasDate());

        task = parser.parseAdd("Watch \"day after tomorrow\" movie");
        assertEquals(false, task.hasDate());
    }

    /**
     * Test to ensure floating task are added correctly.
     * It should not be dated.
     * The title should be the whole user input.
     *
     * @throws InvalidLabelFormat
     * @throws InvalidTitle
     */
    @Test
    public void testAddFloating() throws InvalidLabelFormat {
        CommandParser parser = new CommandParser();

        Task task = parser.parseAdd("Cook dinner");
        assertEquals(false, task.hasDate());
        assertEquals("Cook dinner", task.getTitle());

        task = parser.parseAdd("Attack enemy base on signal");
        assertEquals(false, task.hasDate());
        assertEquals("Attack enemy base on signal", task.getTitle());
    }

    /**
     * Test for valid label extraction.
     *
     * @throws InvalidLabelFormat
     * @throws InvalidTitle
     */
    @Test
    public void testLabel() throws InvalidLabelFormat {
        CommandParser parser = new CommandParser();

        Task task = parser.parseAdd("Cook dinner #home");
        assertEquals("Cook dinner", task.getTitle());
        assertEquals("home", task.getLabel());

        task = parser.parseAdd("#home Cook dinner");
        assertEquals("Cook dinner", task.getTitle());
        assertEquals("home", task.getLabel());

        boolean thrown = false;
        try {
            task = parser.parseAdd("Cook dinner #");
        } catch (InvalidLabelFormat e) {
            thrown = true;
        }
        assertEquals(true, thrown);

        thrown = false;
        try {
            task = parser.parseAdd("  #  ");
        } catch (InvalidLabelFormat e) {
            thrown = true;
        }
        assertEquals(true, thrown);
    }

    /**
     * Test priority toggling.
     * There are only four levels of priority.
     *
     * @throws InvalidLabelFormat
     * @throws InvalidTitle
     */
    @Test
    public void testTogglePriority() throws InvalidLabelFormat {
        CommandParser parser = new CommandParser();
        Task task = parser.parseAdd("Cook dinner #home");
        assertEquals(0, task.getPriority());
        assertEquals(1, task.togglePriority(true));
        assertEquals(2, task.togglePriority(true));
        assertEquals(3, task.togglePriority(true));
        assertEquals(0, task.togglePriority(true));
    }

    /**
     * Test completed status toggling.
     * A status can either be done or undone.
     *
     * @throws InvalidLabelFormat
     * @throws InvalidTitle
     */
    @Test
    public void testToggleDone() throws InvalidLabelFormat {
        CommandParser parser = new CommandParser();
        Task task = parser.parseAdd("Do assignment");
        assertEquals(false, task.isDone());

        task.setIsCompleted();
        assertEquals(true, task.isDone());

        task.setNotCompleted();
        assertEquals(false, task.isDone());
    }

    /**
     * Test for adding a task without preposition.
     *
     * @throws InvalidLabelFormat
     * @throws InvalidTitle
     */
    @Test
    public void testNoPreposition() throws InvalidLabelFormat {
        CommandParser parser = new CommandParser();
        Task task;

        task = parser.parseAdd("Buy apple 1 Aug 3pm");
        assertEquals("Buy apple on 1 Aug at 3pm", task.toString());

        task = parser.parseAdd("Buy apple 1 Aug");
        assertEquals("Buy apple on 1 Aug", task.toString());

        task = parser.parseAdd("Buy apple 11.50pm");
        assertEquals("Buy apple today at 11:50pm", task.toString());

        task = parser.parseAdd("Buy apple 11.50pm");
        assertEquals("Buy apple today at 11:50pm", task.toString());

        task = parser.parseAdd("Buy apple 11:49-11:50pm");
        assertEquals("Buy apple today at 11:49pm - 11:50pm", task.toString());

        task = parser.parseAdd("Buy apple 11:49pm-11:50pm");
        assertEquals("Buy apple today at 11:49pm - 11:50pm", task.toString());

        // if ending not specified, am
        // task = parser.parseAdd("Buy apple 10pm-3");
        //assertEquals("Buy apple from today 10pm - this Sun 3am", task.toString());
    }

    /**
     * Test for time detection without preposition if time is explicitly
     * specified.
     *
     * @throws InvalidLabelFormat
     * @throws InvalidTitle
     */
    @Test
    public void testHasTime() throws InvalidLabelFormat {
        CommandParser parser = new CommandParser();
        Task task;

        task = parser.parseAdd("Dinner 11.50pm");
        assertEquals("Dinner today at 11:50pm", task.toString());

        task = parser.parseAdd("Dinner 11.50PM today");
        assertEquals("Dinner today at 11:50pm", task.toString());

        task = parser.parseAdd("Homework 11.50pm");
        assertEquals("Homework today at 11:50pm", task.toString());
        /*
        task = parser.parseAdd("Do tutorial by morning");
        assertEquals("Do tutorial by this Sun 8am", task.toString());

        task = parser.parseAdd("Do tutorial by afternoon");
        assertEquals("Do tutorial by this Sun 12pm", task.toString());

        task = parser.parseAdd("Do tutorial by evening");
        assertEquals("Do tutorial by this Sun 7pm", task.toString());

        task = parser.parseAdd("Do tutorial midnight");
        assertEquals("Do tutorial on this Sun at 12am", task.toString());

        task = parser.parseAdd("Do tutorial afternoon 5pm");
        assertEquals("Do tutorial on this Sun at 5pm", task.toString());
        */
    }

    /**
     * Date should be relative to current time when being parsed.
     *
     * @throws InvalidLabelFormat
     * @throws InvalidTitle
     */
    @Test
    public void testSmartDetectionOfTime() throws InvalidLabelFormat {
        CommandParser parser = new CommandParser();
        Task task;

        /*
        task = parser.parseAdd("Do homework by 11.50");
        assertEquals("Do homework by today 11:50pm", task.toString());
        */

        task = parser.parseAdd("Do homework by 11:50pm");
        assertEquals("Do homework by today 11:50pm", task.toString());

        task = parser.parseAdd("Do homework by 1 Aug 2pm");
        assertEquals("Do homework by 1 Aug 2pm", task.toString());
    }

    /**
     * Test for start time detection when parsing.
     * Feedback shown is relative to the current period.
     *
     * @throws InvalidLabelFormat
     * @throws InvalidTitle
     */
    @Test
    public void testDetectStartTime() throws InvalidLabelFormat {
        CommandParser parser = new CommandParser();
        Task task = parser.parseAdd("Attempt quiz from 5pm 1 Aug");
        assertEquals("Attempt quiz", task.getTitle());
        assertEquals("Attempt quiz on 1 Aug at 5pm", task.toString());

        task = parser.parseAdd("Watch webcast after 3am on 1 AUG");
        assertEquals("Watch webcast", task.getTitle());
        assertEquals("Watch webcast on 1 Aug at 3am", task.toString());

        task = parser.parseAdd("Watch movie at 11:50pm");
        assertEquals("Watch movie", task.getTitle());
        assertEquals("Watch movie today at 11:50pm", task.toString());

        task = parser.parseAdd("Watch movie on 1 Aug 7pm");
        assertEquals("Watch movie", task.getTitle());
        assertEquals("Watch movie on 1 Aug at 7pm", task.toString());
    }

    /**
     * Test for correct parsing of ranged time in user input.
     *
     * @throws InvalidLabelFormat
     * @throws InvalidTitle
     */
    @Test
    public void testTaskWithTimeRange() throws InvalidLabelFormat {
        CommandParser parser = new CommandParser();
        Task task = parser.parseAdd("Do homework on 1Aug 2-4pm");
        assertEquals("Do homework 1 Aug from 2pm - 4pm", task.toString());

        task = parser.parseAdd("Do homework from 2pm-4 on 1aug");
        assertEquals("Do homework 1 Aug from 2pm - 4am", task.toString());

        task = parser.parseAdd("Do homework on 1aug 2pm-4pm");
        assertEquals("Do homework 1 Aug from 2pm - 4pm", task.toString());

        task = parser.parseAdd("Do homework on 1st aug 2pm-4pm");
        assertEquals("Do homework 1 Aug from 2pm - 4pm", task.toString());

        task = parser.parseAdd("Do homework on 1 aug 10-11pm");
        assertEquals("Do homework 1 Aug from 10pm - 11pm", task.toString());

        task = parser.parseAdd("Do homework from 10pm-11 on 1 Aug");
        assertEquals("Do homework 1 Aug from 10pm - 11am", task.toString());

        task = parser.parseAdd("Do homework on 1 Aug 10pm-11pm");
        assertEquals("Do homework 1 Aug from 10pm - 11pm", task.toString());

        task = parser.parseAdd("Do homework on 1st aug 10pm-11pm");
        assertEquals("Do homework 1 Aug from 10pm - 11pm", task.toString());
    }

    /**
     * Test feedback shown when parsing tasks
     * Feedback shown is relative to the current period.
     *
     * @throws InvalidLabelFormat
     * @throws InvalidTitle
     */
    @Test
    public void testTaskToString() throws InvalidLabelFormat {
        CommandParser parser = new CommandParser();

        Task task = parser.parseAdd("Cook dinner");
        assertEquals("Cook dinner", task.toString());

        task = parser.parseAdd("Cook dinner #home");
        assertEquals("Cook dinner #home", task.toString());

        task = parser.parseAdd("Cook dinner on 14/3 at 7pm #home");
        assertEquals("Cook dinner on 14 Mar at 7pm #home", task.toString());

        task = parser.parseAdd("Cook dinner on 15/3 7.15pm");
        assertEquals("Cook dinner on 15 Mar at 7:15pm", task.toString());

        task = parser.parseAdd("Cook dinner on 30/3 7:15pm");
        assertEquals("Cook dinner on 30 Mar at 7:15pm", task.toString());

        task = parser.parseAdd("Attend meeting on 26-3 7pm");
        assertEquals("Attend meeting on 26 Mar at 7pm", task.toString());

        task = parser.parseAdd("Attend meeting from 4 to 6pm on 25 Mar");
        assertEquals("Attend meeting 25 Mar from 4pm - 6pm", task.toString());

        task = parser.parseAdd("Attend meeting 4 to 6pm on 25 Mar");
        assertEquals("Attend meeting 25 Mar from 4pm - 6pm", task.toString());

        task = parser.parseAdd("Attend meeting on 1 Mar 9am");
        assertEquals("Attend meeting on 1 Mar at 9am", task.toString());

        task = parser.parseAdd("Go camp from 1-3 8am to 3-3 9pm");
        assertEquals("Go camp from 1 Mar 8am - 3 Mar 9pm", task.toString());
    }

    /**
     * Test parsing of days.
     *
     * @throws InvalidLabelFormat
     * @throws InvalidTitle
     */
    @Test
    public void testDays() throws InvalidLabelFormat {
        CommandParser parser = new CommandParser();
        Task task;
        /*
        // now is tues
        task = parser.parseAdd("Attend meeting on thurs");
        assertEquals("Attend meeting on next Thu", task.toString());

        // now is tues
        task = parser.parseAdd("Attend meeting from mon to weds");
        assertEquals("Attend meeting from next Mon - next Wed", task.toString());
        */

        task = parser.parseAdd("Do homework from 1/2 to 2/2");
        assertEquals("Do homework from 1 Feb - 2 Feb", task.toString());
    }

    /**
     * Test adding of dated task.
     * Asserts title and time has been parsed correctly.
     *
     * @throws ParseException
     *             for dateFormat.parse()
     * @throws InvalidLabelFormat
     * @throws InvalidTitle
     */
    @Test
    public void testAdd() throws ParseException, InvalidLabelFormat {
        CommandParser parser = new CommandParser();
        Task task = parser.parseAdd("Cook dinner on 24 Mar 7pm #home");
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy");
        String startDate = "Thu Mar 24 19:00:00 SGT 2016";
        Date expectedStart = dateFormat.parse(startDate);

        assertEquals(true, task.hasDate());
        assertEquals(expectedStart, task.getStartDate());
        assertEquals("Cook dinner", task.getTitle());
    }

    /**
     * Test for extraction of date information in title.
     *
     * @throws InvalidLabelFormat
     * @throws InvalidTitle
     */
    @Test
    public void testDatedTaskTitle() throws InvalidLabelFormat {
        CommandParser parser = new CommandParser();
        Task task = parser.parseAdd("Attend meeting from Monday to Wednesday");
        assertEquals("Attend meeting", task.getTitle());

        task = parser.parseAdd("Attend meeting from 4 to 6");
        assertEquals("Attend meeting", task.getTitle());

        task = parser.parseAdd("Cook dinner at 7");
        assertEquals("Cook dinner", task.getTitle());

        task = parser.parseAdd("Attend meeting on this Wed");
        assertEquals("Attend meeting", task.getTitle());

        task = parser.parseAdd("Do homework by next Sunday");
        assertEquals("Do homework", task.getTitle());

        task = parser.parseAdd("Send 100 email before 8pm");
        assertEquals("Send 100 email", task.getTitle());

        task = parser.parseAdd("Meet at \"Taco Tuesday\" on Wednesday 5pm");
        assertEquals("Meet at \"Taco Tuesday\"", task.getTitle());

        task = parser.parseAdd("Chase \"2pm\" Korean band on Saturday 7pm");
        assertEquals("Chase \"2pm\" Korean band", task.getTitle());

        task = parser.parseAdd("Attend meeting from Monday to Wednesday 6pm");
        assertEquals("Attend meeting", task.getTitle());

        task = parser.parseAdd("Cook dinner at 7pm at home");
        assertEquals("Cook dinner at home", task.getTitle());

        task = parser.parseAdd("Cook dinner on 24 Mar 7pm");
        assertEquals("Cook dinner", task.getTitle());

        task = parser.parseAdd("Do assignment by Sunday 8pm");
        assertEquals("Do assignment", task.getTitle());

        task = parser.parseAdd("Send 100 email before sunday 7pm");
        assertEquals("Send 100 email", task.getTitle());
    }

    /**
     * Test for specifying priority level in command for adding or editing.
     *
     * @throws InvalidLabelFormat
     * @throws InvalidTitle
     */
    @Test
    public void testPriorityInCommand() throws InvalidLabelFormat {
        CommandParser parser = new CommandParser();
        Task task, task2;

        task = parser.parseAdd("eat brownie priority low");
        assertEquals(1, task.getPriority());
        assertEquals("eat brownie P:low", task.toString());

        task = parser.parseAdd("eat brownie priority mid");
        assertEquals(2, task.getPriority());
        assertEquals("eat brownie P:mid", task.toString());

        task = parser.parseAdd("eat brownie priority high");
        assertEquals(3, task.getPriority());
        assertEquals("eat brownie P:high", task.toString());

        task = parser.parseAdd("eat brownie priority HIGH");
        assertEquals(3, task.getPriority());
        assertEquals("eat brownie P:high", task.toString());

        task = parser.parseAdd("eat brownie from 1/8 10pm to 11pm priority h");
        assertEquals(3, task.getPriority());
        assertEquals("eat brownie 1 Aug from 10pm - 11pm P:high", task.toString());

        // for editing
        task2 = parser.parseEdit(task, "priority m");
        assertEquals("eat brownie 1 Aug from 10pm - 11pm P:mid", task2.toString());
    }

    /**
     * Test for parsing date with year (and time).
     *
     * @throws InvalidLabelFormat
     * @throws InvalidTitle
     */
    @Test
    public void testAddEditWithYear() throws InvalidLabelFormat {
        CommandParser parser = new CommandParser();
        Task task, task2;

        task = parser.parseAdd("Buy fountain pen on 30/8/2020");
        assertEquals("Buy fountain pen on 30 Aug 2020", task.toString());

        task = parser.parseAdd("Buy fountain pen on 30/8/20");
        assertEquals("Buy fountain pen on 30 Aug 2020", task.toString());

        task = parser.parseAdd("Buy fountain pen on 30/8/2020 8pm");
        assertEquals("Buy fountain pen on 30 Aug 2020 at 8pm", task.toString());

        task = parser.parseAdd("Buy fountain pen on 30/8/20 8pm");
        assertEquals("Buy fountain pen on 30 Aug 2020 at 8pm", task.toString());

        task = parser.parseAdd("Go Japan from 1/8/20 8am to 30/8/20 10pm");
        assertEquals("Go Japan from 1 Aug 2020 8am - 30 Aug 2020 10pm", task.toString());

        task = parser.parseAdd("Go Japan from 1/8/20 8am to 30/8/20 10pm");
        task2 = parser.parseEdit(task, "9-11pm");
        assertEquals("Go Japan from 1 Aug 2020 9pm - 30 Aug 2020 11pm", task2.toString());

        task = parser.parseAdd("Go Japan from 1/8/20 8am to 30/8/20 10pm");
        task2 = parser.parseEdit(task, "1/9");
        assertEquals("Go Japan on 1 Sep", task2.toString());

        task = parser.parseAdd("Go Japan from 1/8/20 8am to 30/8/20 10pm");
        task2 = parser.parseEdit(task, "1/9/21");
        assertEquals("Go Japan on 1 Sep 2021", task2.toString());

        task = parser.parseAdd("Buy fountain pen on 30 aug 2020");
        assertEquals("Buy fountain pen on 30 Aug 2020", task.toString());

        task = parser.parseAdd("Buy fountain pen on 30 aug 20");
        assertEquals("Buy fountain pen on 30 Aug 2020", task.toString());

        task = parser.parseAdd("Buy fountain pen by 30 august 2020 8pm");
        assertEquals("Buy fountain pen by 30 Aug 2020 8pm", task.toString());

        task = parser.parseAdd("Buy fountain pen on 30 august 20 9pm");
        assertEquals("Buy fountain pen on 30 Aug 2020 at 9pm", task.toString());

        task = parser.parseAdd("Go Japan from 1 Aug 2020 8am to 30 aug 2020 10pm");
        assertEquals("Go Japan from 1 Aug 2020 8am - 30 Aug 2020 10pm", task.toString());

        task = parser.parseAdd("Go Japan from 1 Aug 20 8am to 30 aug 20 10pm");
        assertEquals("Go Japan from 1 Aug 2020 8am - 30 Aug 2020 10pm", task.toString());

        task = parser.parseAdd("Go Japan from 1 Aug 20 8am to 30aug 20 10pm");
        task2 = parser.parseEdit(task, "9-11pm");
        assertEquals("Go Japan from 1 Aug 2020 9pm - 30 Aug 2020 11pm", task2.toString());

        task = parser.parseAdd("Go Japan from 1aug 20 8am to 30aug 20 10pm");
        task2 = parser.parseEdit(task, "1sep");
        assertEquals("Go Japan on 1 Sep", task2.toString());

        task = parser.parseAdd("Go Japan from 1aug 20 8am to 30aug 20 10pm");
        task2 = parser.parseEdit(task, "1sep 2021");
        assertEquals("Go Japan on 1 Sep 2021", task2.toString());
    }

    /**
     * Test method for comparing task.
     * Tasks are compared by their creation date.
     * Task with same title would not be equal.
     *
     * @throws InterruptedException
     *             for Thread.sleep().
     *             Ensures that there are differences in time when creating
     *             task.
     * @throws InvalidLabelFormat
     * @throws InvalidTitle
     */
    @Test
    public void testCompareTo() throws InterruptedException, InvalidLabelFormat {
        CommandParser parser = new CommandParser();
        Task task1, task2;

        task1 = parser.parseAdd("Cook dinner at 7pm");
        Thread.sleep(2000);
        task2 = parser.parseAdd("Cook dinner at 7pm");
        assertEquals(-1, task1.compareTo(task2));

        task1 = parser.parseAdd("Cook dinner at 8pm");
        Thread.sleep(2000);
        task2 = parser.parseAdd("Cook dinner at 9pm");
        assertEquals(-1, task1.compareTo(task2));

        task1 = parser.parseAdd("Attend meeting");
        Thread.sleep(2000);
        task2 = parser.parseAdd("Attend meeting #important");
        assertEquals(-1, task1.compareTo(task2));
    }

    // =============================
    // Edit's stuff
    // =============================

    /**
     * Test extracting index for edit.
     */
    @Test
    public void testGetIndexForEdit() {
        CommandParser parser = new CommandParser();
        int index;

        index = parser.getIndexForEdit("edit 1 23 march");
        assertEquals(1, index);

        index = parser.getIndexForEdit("edit 2 1 april 7pm");
        assertEquals(2, index);

        index = parser.getIndexForEdit("edit 3 1 april 7pm-8pm");
        assertEquals(3, index);

        index = parser.getIndexForEdit("edit 4 buy chocolate #party on 1 april");
        assertEquals(4, index);

        index = parser.getIndexForEdit("edit 1 april");
        assertEquals(-1, index);
    }

    /**
     * Test if date is detected in string.
     * If detected, Date object is returned.
     * Else, null is returned.
     *
     * Note that there are some special cases in the method getDateForSearch.
     */
    @Test
    public void testGetDateForSearch() {
        CommandParser parser = new CommandParser();
        Date date;

        date = parser.getDateForSearch("1 march");
        assertNotNull(date);

        date = parser.getDateForSearch("1/3");
        assertNotNull(date);

        date = parser.getDateForSearch("today");
        assertNotNull(date);

        date = parser.getDateForSearch("tomorrow");
        assertNotNull(date);

        date = parser.getDateForSearch("meeting");
        assertNull(date);

        date = parser.getDateForSearch("finance proposal");
        assertNull(date);
    }

    /**
     * Test editing of an existing task.
     *
     * @throws InvalidLabelFormat
     * @throws InvalidTitle
     */
    @Test
    public void testEditBasic() throws InvalidLabelFormat {
        CommandParser parser = new CommandParser();
        Task task, task2;

        task = parser.parseAdd("Buy milk");
        task2 = parser.parseEdit(task, "Buy chocolate");
        assertEquals("Buy chocolate", task2.toString());

        task = parser.parseAdd("Buy milk");
        task2 = parser.parseEdit(task, "Buy chocolate #party");
        assertEquals("Buy chocolate #party", task2.toString());

        task = parser.parseAdd("Buy milk");
        task2 = parser.parseEdit(task, "Buy chocolate by 11.50pm");
        assertEquals("Buy chocolate by today 11:50pm", task2.toString());

        task = parser.parseAdd("Buy milk");
        task2 = parser.parseEdit(task, "Buy chocolate from 11:50pm");
        assertEquals("Buy chocolate today at 11:50pm", task2.toString());

        task = parser.parseAdd("Buy milk");
        task2 = parser.parseEdit(task, "Buy milk at discount from 11:49pm to 11:50pm");
        assertEquals("Buy milk at discount today at 11:49pm - 11:50pm", task2.toString());
    }

    /**
     * Test editing time of existing task.
     * Old information should be retained.
     *
     * @throws InvalidLabelFormat
     * @throws InvalidTitle
     */
    @Test
    public void testEditTimeOnly() throws InvalidLabelFormat {
        CommandParser parser = new CommandParser();
        Task task, task2;

        /*
        task = parser.parseAdd("Buy milk");
        task2 = parser.parseEdit(task, "by 11:50");
        assertEquals("Buy milk by today 11:50pm", task2.toString());
        */

        task = parser.parseAdd("Buy milk");
        task2 = parser.parseEdit(task, "11:50pm");
        assertEquals("Buy milk today at 11:50pm", task2.toString());

        task = parser.parseAdd("Buy milk");
        task2 = parser.parseEdit(task, "11:49pm to 11:50pm");
        assertEquals("Buy milk today at 11:49pm - 11:50pm", task2.toString());

        task = parser.parseAdd("Buy milk at 11:48pm");
        task2 = parser.parseEdit(task, "11:49pm to 11:50pm");
        assertEquals("Buy milk today at 11:49pm - 11:50pm", task2.toString());

        task = parser.parseAdd("Buy milk from 11:48 to 11:49pm");
        task2 = parser.parseEdit(task, "by 11:50pm");
        assertEquals("Buy milk by today 11:50pm", task2.toString());

        task = parser.parseAdd("Buy milk from 11:48 to 11:49pm");
        task2 = parser.parseEdit(task, "from 11:50 to 11:51pm");
        assertEquals("Buy milk today at 11:50pm - 11:51pm", task2.toString());
    }

    @Test
    public void testEditTimeOnly2() throws InvalidLabelFormat {
        CommandParser parser = new CommandParser();
        Task task, task2;

        task = parser.parseAdd("Buy milk on 1 Aug at 2pm #party");

        task2 = parser.parseEdit(task, "3am");
        assertEquals("Buy milk on 1 Aug at 3am #party", task2.toString());

        task2 = parser.parseEdit(task, "3pm");
        assertEquals("Buy milk on 1 Aug at 3pm #party", task2.toString());

        //task2 = parser.parseEdit(task, "at 4");
        //assertEquals("Buy milk on 1 Aug at 4am #party", task2.toString());

        task2 = parser.parseEdit(task, "at 4am");
        assertEquals("Buy milk on 1 Aug at 4am #party", task2.toString());

        task2 = parser.parseEdit(task, "at 4pm");
        assertEquals("Buy milk on 1 Aug at 4pm #party", task2.toString());

        task2 = parser.parseEdit(task, "3-4pm");
        assertEquals("Buy milk 1 Aug from 3pm - 4pm #party", task2.toString());

        task2 = parser.parseEdit(task, "3pm-4pm");
        assertEquals("Buy milk 1 Aug from 3pm - 4pm #party", task2.toString());

        task2 = parser.parseEdit(task, "3pm-4");
        assertEquals("Buy milk 1 Aug from 3pm - 4am #party", task2.toString());

        task2 = parser.parseEdit(task, "from 3-4pm");
        assertEquals("Buy milk 1 Aug from 3pm - 4pm #party", task2.toString());

        task2 = parser.parseEdit(task, "from 3pm-4pm");
        assertEquals("Buy milk 1 Aug from 3pm - 4pm #party", task2.toString());

        task2 = parser.parseEdit(task, "from 3pm-4");
        assertEquals("Buy milk 1 Aug from 3pm - 4am #party", task2.toString());
    }

    /**
     * Test editing of task.
     * If information is not specified, use the old task information.
     * This allows updating of only the date or time.
     * If only the date is specified, the time will not be retained.
     *
     * @throws InvalidLabelFormat
     * @throws InvalidTitle
     */
    @Test
    public void testOverallEdit() throws InvalidLabelFormat {
        CommandParser parser = new CommandParser();
        Task task, task2;

        task = parser.parseAdd("Buy milk by 30 august 7pm");
        task2 = parser.parseEdit(task, "by 4pm");
        assertEquals("Buy milk by 30 Aug 4pm", task2.toString());
        assertEquals(0, task2.getPriority());

        task = parser.parseAdd("Buy milk by 30 aug 7pm");
        task.togglePriority(true);
        task2 = parser.parseEdit(task, "from 1 to 3pm");
        assertEquals("Buy milk 30 Aug from 1pm - 3pm P:low", task2.toString());
        assertEquals(1, task2.getPriority());

        task = parser.parseAdd("Buy milk by 10 aug");
        task.togglePriority(true);
        task.togglePriority(true);
        task2 = parser.parseEdit(task, "by 20 aug 8pm");
        assertEquals("Buy milk by 20 Aug 8pm P:mid", task2.toString());
        assertEquals(2, task2.getPriority());

        task = parser.parseAdd("Buy kitkat by 10 aug 7pm");
        task.togglePriority(true);
        task.togglePriority(true);
        task.togglePriority(true);
        task.setPriority(3);
        task2 = parser.parseEdit(task, "by 30 aug");
        assertEquals("Buy kitkat by 30 Aug P:high", task2.toString());
        assertEquals(3, task2.getPriority());

        task = parser.parseAdd("Buy kitkat by 10 aug 7pm");
        task2 = parser.parseEdit(task, "FROM 25 Aug TO 26 AUGUST");
        assertEquals("Buy kitkat from 25 Aug - 26 Aug", task2.toString());
    }

    @Test
    public void testOverallEdit2() throws InvalidLabelFormat {
        CommandParser parser = new CommandParser();
        Task task, task2;

        task = parser.parseAdd("Drink coffee from 1/8 to 3/8 8pm #overdose");

        task2 = parser.parseEdit(task, "1/9 to 3/9");
        assertEquals("Drink coffee from 1 Sep - 3 Sep #overdose", task2.toString());

        task2 = parser.parseEdit(task, "Drink hot chocolate");
        assertEquals("Drink hot chocolate from 1 Aug 8pm - 3 Aug 8pm #overdose", task2.toString());

        task2 = parser.parseEdit(task, "#beedohbeedoh");
        assertEquals("Drink coffee from 1 Aug 8pm - 3 Aug 8pm #beedohbeedoh", task2.toString());

        task2 = parser.parseEdit(task, "1 Sep");
        assertEquals("Drink coffee on 1 Sep #overdose", task2.toString());

        task2 = parser.parseEdit(task, "from 9 to 10pm");
        assertEquals("Drink coffee from 1 Aug 9pm - 3 Aug 10pm #overdose", task2.toString());

        task2 = parser.parseEdit(task, "by 1 Aug 2pm");
        assertEquals("Drink coffee by 1 Aug 2pm #overdose", task2.toString());
    }

    /**
     * Tests for usage of common abbreviations of words.
     *
     * @throws InvalidLabelFormat
     * @throws InvalidTitle
     */
    @Test
    public void testShortcut() throws InvalidLabelFormat {
        CommandParser parser = new CommandParser();
        Task task, task2;
        /*
        task = parser.parseAdd("Supper tmr 11pm");
        assertEquals("Supper on this Sun at 11pm", task.toString());

        task = parser.parseAdd("Supper tml 11pm");
        assertEquals("Supper on this Sun at 11pm", task.toString());

        task = parser.parseAdd("Supper tmrw 11pm");
        assertEquals("Supper on this Sun at 11pm", task.toString());

        task = parser.parseAdd("Donate books");
        task2 = parser.parseEdit(task, "tmr 11pm");
        assertEquals("Donate books on this Sun at 11pm", task2.toString());
        */

        task = parser.parseAdd("Supper today 11:50pm");
        assertEquals("Supper today at 11:50pm", task.toString());

        task = parser.parseAdd("Supper tdy 11.50pm");
        assertEquals("Supper today at 11:50pm", task.toString());

        task = parser.parseAdd("Donate books");
        task2 = parser.parseEdit(task, "tdy 11:50pm");
        assertEquals("Donate books today at 11:50pm", task2.toString());
    }

    // =============================
    // Test parsing indexes
    // =============================

    /**
     * Test parsing of indexes.
     *
     * @throws InvalidTaskIndexFormat
     */
    @Test
    public void testIndexes() throws InvalidTaskIndexFormat {
        CommandParser parser = new CommandParser();
        ParseIndexResult indexes;
        indexes = parser.parseIndexes("delete 1", 2);

        ArrayList<Integer> expectedIndexes = new ArrayList<Integer>();
        expectedIndexes.add(1);
        assertEquals(expectedIndexes, indexes.getValidIndexes());

        indexes = parser.parseIndexes("del 1,3,5,7,9", 10);
        expectedIndexes.clear();
        expectedIndexes.add(1);
        expectedIndexes.add(3);
        expectedIndexes.add(5);
        expectedIndexes.add(7);
        expectedIndexes.add(9);
        assertEquals(expectedIndexes, indexes.getValidIndexes());

        indexes = parser.parseIndexes("done 1-10", 10);
        expectedIndexes.clear();
        expectedIndexes.add(1);
        expectedIndexes.add(2);
        expectedIndexes.add(3);
        expectedIndexes.add(4);
        expectedIndexes.add(5);
        expectedIndexes.add(6);
        expectedIndexes.add(7);
        expectedIndexes.add(8);
        expectedIndexes.add(9);
        expectedIndexes.add(10);
        assertEquals(expectedIndexes, indexes.getValidIndexes());

        indexes = parser.parseIndexes("undone 1-3,4,5,6-9,10", 10);
        assertEquals(expectedIndexes, indexes.getValidIndexes());

        indexes = parser.parseIndexes("done 1-10,5,6,7,8,9,10", 10);
        assertEquals(expectedIndexes, indexes.getValidIndexes());
    }

    /**
     * Test parsing of invalid indexes.
     *
     * @throws InvalidTaskIndexFormat
     */
    @Test
    public void testInvalidIndexes() throws InvalidTaskIndexFormat {
        CommandParser parser = new CommandParser();
        ArrayList<Integer> expectedIndexes = new ArrayList<Integer>();
        ParseIndexResult indexes;

        indexes = parser.parseIndexes("del 11", 10);
        expectedIndexes.add(11);
        assertEquals(true, indexes.hasInvalidIndex());
        assertEquals(expectedIndexes, indexes.getInvalidIndexes());

        indexes = parser.parseIndexes("del 11,12,13", 10);
        expectedIndexes.clear();
        expectedIndexes.add(11);
        expectedIndexes.add(12);
        expectedIndexes.add(13);
        assertEquals(true, indexes.hasInvalidIndex());
        assertEquals(expectedIndexes, indexes.getInvalidIndexes());

        indexes = parser.parseIndexes("del 11-13", 10);
        assertEquals(true, indexes.hasInvalidIndex());
        assertEquals(expectedIndexes, indexes.getInvalidIndexes());
    }

    /**
     * Test parsing of indexes when user input does not follow the "usual" way.
     *
     * @throws InvalidTaskIndexFormat
     */
    @Test
    public void testUnconventionalIndexes() throws InvalidTaskIndexFormat {
        CommandParser parser = new CommandParser();

        ArrayList<Integer> expectedIndexes = new ArrayList<Integer>();
        expectedIndexes.add(1);
        expectedIndexes.add(2);
        expectedIndexes.add(3);
        expectedIndexes.add(4);
        expectedIndexes.add(5);
        expectedIndexes.add(6);
        expectedIndexes.add(7);
        expectedIndexes.add(8);
        expectedIndexes.add(9);
        expectedIndexes.add(10);

        ParseIndexResult indexes;
        indexes = parser.parseIndexes("del 1--10", 10);
        assertEquals(expectedIndexes, indexes.getValidIndexes());

        indexes = parser.parseIndexes("del 1-----10", 10);
        assertEquals(expectedIndexes, indexes.getValidIndexes());

        indexes = parser.parseIndexes("del 1-3-5-7-9-10", 10);
        assertEquals(expectedIndexes, indexes.getValidIndexes());

        Collections.sort(expectedIndexes, Collections.reverseOrder());
        indexes = parser.parseIndexes("del 10-1", 10);
        assertEquals(expectedIndexes, indexes.getValidIndexes());

        indexes = parser.parseIndexes("del 10-9-7-5-3-1", 10);
        assertEquals(expectedIndexes, indexes.getValidIndexes());
    }

    /**
     * Test for invalid input for parsing indexes.
     * Exceptions should be thrown.
     *
     * @throws InvalidTaskIndexFormat
     */
    @Test
    public void testInvalidDelete() throws InvalidTaskIndexFormat {
        boolean thrown;
        CommandParser parser = new CommandParser();
        ParseIndexResult indexes;

        thrown = false;
        try {
            indexes = parser.parseIndexes("del -1,-2", 10);
        } catch (InvalidTaskIndexFormat e) {
            thrown = true;
        }

        assertEquals(true, thrown);

        thrown = false;
        try {
            indexes = parser.parseIndexes("del 1-,10", 10);
        } catch (InvalidTaskIndexFormat e) {
            thrown = true;
        }
        assertEquals(true, thrown);

        thrown = false;
        try {
            indexes = parser.parseIndexes("del abc,def", 10);
        } catch (InvalidTaskIndexFormat e) {
            thrown = true;
        }
        assertEquals(true, thrown);
    }

    /**
     * Test for getting valid and invalid indexes string.
     *
     * @throws InvalidTaskIndexFormat
     */
    @Test
    public void testIndexesString() throws InvalidTaskIndexFormat {
        CommandParser parser = new CommandParser();
        ParseIndexResult indexes;

        indexes = parser.parseIndexes("del 1-10,12,13,14,15", 10);
        assertEquals(true, indexes.hasValidIndex());
        assertEquals(true, indexes.hasInvalidIndex());
        assertEquals("1-10", indexes.getValidIndexesString());
        assertEquals("12-15", indexes.getInvalidIndexesString());

        indexes = parser.parseIndexes("del 1-5,10-20, 31, 32, 33, 35", 10);
        assertEquals(true, indexes.hasValidIndex());
        assertEquals(true, indexes.hasInvalidIndex());
        assertEquals("1-5,10", indexes.getValidIndexesString());
        assertEquals("11-20,31-33,35", indexes.getInvalidIndexesString());
    }
}