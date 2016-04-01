package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.junit.Ignore;
import org.junit.Test;

import main.data.ParseIndexResult;
import main.data.Task;
import main.parser.CommandParser;
import main.parser.CommandParser.InvalidLabelFormat;
import main.parser.CommandParser.InvalidTaskIndexFormat;
import main.parser.CommandParser.InvalidTitle;

/**
 * @author Joleen
 *
 */

public class TestCommandParser {
	/**
	 * Test detection of time in user input
	 * Method checkForTime has been updated to private.
	 * This is for reference only.
	 */
	/*
	public void testCheckTime() {
		CommandParser parser = new CommandParser();
		
		assertEquals(true, parser.checkForTime("1am"));
		assertEquals(true, parser.checkForTime("2PM"));
		assertEquals(true, parser.checkForTime("12:50pm"));
		
		assertEquals(false, parser.checkForTime("13am"));
		assertEquals(false, parser.checkForTime("112pm"));
		assertEquals(false, parser.checkForTime("12:60pm"));
		assertEquals(false, parser.checkForTime("12:592pm"));
	}
	*/
	
	/**
	 * Test detection of ranged time in user input.
	 * Method checkForRangeTime has been updated to private.
	 * This is for reference only.
	 * See testTaskWithTimeRange() for valid testing.
	 */
	/*
	public void testCheckRange() {
		CommandParser parser = new CommandParser();
		
		assertEquals(true, parser.checkForRangeTime("1am-4"));
		assertEquals(true, parser.checkForRangeTime("1-4am"));
		assertEquals(true, parser.checkForRangeTime("1am-4am"));
		
		assertEquals(true, parser.checkForRangeTime("10am-11"));
		assertEquals(true, parser.checkForRangeTime("10-11am"));
		assertEquals(true, parser.checkForRangeTime("10am-11am"));
		
		assertEquals(true, parser.checkForRangeTime("1:30am-4:30"));
		assertEquals(true, parser.checkForRangeTime("1:30-4:30am"));
		assertEquals(true, parser.checkForRangeTime("1:30am-4:30am"));
		
		assertEquals(true, parser.checkForRangeTime("10:30am-11:30"));
		assertEquals(true, parser.checkForRangeTime("10:30-11:30am"));
		assertEquals(true, parser.checkForRangeTime("10:30am-11:30am"));
	
		assertEquals(false, parser.checkForRangeTime("1-4"));
		
		assertEquals(false, parser.checkForRangeTime("1am-41"));
		assertEquals(false, parser.checkForRangeTime("1-41am"));
		assertEquals(false, parser.checkForRangeTime("1am-41am"));
		
		assertEquals(false, parser.checkForRangeTime("41am-1"));
		assertEquals(false, parser.checkForRangeTime("41-1am"));
		assertEquals(false, parser.checkForRangeTime("41am-1am"));
		
		assertEquals(false, parser.checkForRangeTime("1:60m-4:60"));
		assertEquals(false, parser.checkForRangeTime("1:60-4:60am"));
		assertEquals(false, parser.checkForRangeTime("1:60am-4:60am"));
		
		assertEquals(false, parser.checkForRangeTime("10:60am-11:60"));
		assertEquals(false, parser.checkForRangeTime("10:60-11:60am"));
		assertEquals(false, parser.checkForRangeTime("10:60am-11:60am"));		
	}
	*/
	
	/**
	 * Test detection of date in number format in user input.
	 * Method checkForDate has been updated to private.
	 * This is for reference only.
	 */
	/*
	public void testCheckDate() {
		CommandParser parser = new CommandParser();
		
		assertEquals(true, parser.checkForDate("1/5"));
		assertEquals(true, parser.checkForDate("1/12"));
		assertEquals(true, parser.checkForDate("30-5"));
		assertEquals(true, parser.checkForDate("31-5"));
		
		assertEquals(true, parser.checkForDate("from 1/5"));
		assertEquals(true, parser.checkForDate("after 1/12"));
		assertEquals(true, parser.checkForDate("at 30-5"));
		assertEquals(true, parser.checkForDate("on 31-5"));
		
		assertEquals(false, parser.checkForDate("1/13"));
		assertEquals(false, parser.checkForDate("33/5"));
		assertEquals(false, parser.checkForDate("33/50"));
	}
	 */
	
	/**
	 * Test detection of date in text format in user input.
	 * Method checkForDateText has been updated to private.
	 * This is for reference only.
	 */
	/*
	public void testCheckDateText() {
		CommandParser parser = new CommandParser();

		assertEquals(true, parser.checkForDateText("1 march"));
		assertEquals(true, parser.checkForDateText("31 april"));
		assertEquals(true, parser.checkForDateText("20 may"));
		
		assertEquals(true, parser.checkForDateText("1 mar"));
		assertEquals(true, parser.checkForDateText("31 apr"));
		assertEquals(true, parser.checkForDateText("20 jun"));
		
		assertEquals(true, parser.checkForDateText("from 1 march"));
		assertEquals(true, parser.checkForDateText("by 31 april"));
		assertEquals(true, parser.checkForDateText("on 20 may"));
		
		assertEquals(true, parser.checkForDateText("from 1 mar"));
		assertEquals(true, parser.checkForDateText("by 31 apr"));
		assertEquals(true, parser.checkForDateText("on 20 jun"));
		
		assertEquals(false, parser.checkForDateText("32 march"));
		assertEquals(false, parser.checkForDateText("55 june"));
		
		assertEquals(false, parser.checkForDateText("1 januaryy"));
		assertEquals(false, parser.checkForDateText("1 janu"));
		assertEquals(false, parser.checkForDateText("1 jjan"));
	}
	*/
	
	/**
	 * Test detection of days in text format in user input.
	 * Method checkForDay has been updated to private.
	 * This is for reference only.
	 */
	/*
	public void testCheckDayText() {
		CommandParser parser = new CommandParser();

		assertEquals(true, parser.checkForDay("monday"));
		assertEquals(true, parser.checkForDay("thur"));
		assertEquals(true, parser.checkForDay("thurs"));
		
		assertEquals(true, parser.checkForDay("from monday"));
		assertEquals(true, parser.checkForDay("on thur"));
		assertEquals(true, parser.checkForDay("at thurs"));
		
		
		assertEquals(false, parser.checkForDay("mondayy"));
		assertEquals(false, parser.checkForDay("mmon"));
		assertEquals(false, parser.checkForDay("monn"));
	}
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
	public void testDetectFloating() throws InvalidLabelFormat, InvalidTitle {
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
	public void testAddFloating() throws InvalidLabelFormat, InvalidTitle {
		CommandParser parser = new CommandParser();

		Task task = parser.parseAdd("Cook dinner");
		assertEquals(false, task.hasDate());
		assertEquals("Cook dinner", task.getTitle());

		task = parser.parseAdd("Attack enemy base on signal");
		assertEquals(false, task.hasDate());
		assertEquals("Attack enemy base on signal", task.getTitle());
	}
	
	/**
	 * Test for label extraction.
	 * 
	 * @throws InvalidLabelFormat 
	 * @throws InvalidTitle 
	 */
	@Test
	public void testLabel() throws InvalidLabelFormat, InvalidTitle{
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
	public void testTogglePriority() throws InvalidLabelFormat, InvalidTitle {
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
	public void testToggleDone() throws InvalidLabelFormat, InvalidTitle {
		CommandParser parser = new CommandParser();
		Task task = parser.parseAdd("Do assignment");
		assertEquals(false, task.isDone());

		task.setIsCompleted();
		assertEquals(true, task.isDone());

		task.setNotCompleted();
		assertEquals(false, task.isDone());

		task.toggleDone();
		assertEquals(true, task.isDone());

		task.toggleDone();
		assertEquals(false, task.isDone());
	}

	/**
	 * Test for invalid title detection.
	 * This is mainly for dated task.
	 * Because if not dated, title is whole user input.
	 * 
	 * @throws InvalidLabelFormat
	 * @throws InvalidTitle
	 */
	@Test
	public void testInvalidTitle() throws InvalidLabelFormat, InvalidTitle {
		CommandParser parser = new CommandParser();
		Task task;
		boolean thrown;
		
		thrown = false;
		try {
			task = parser.parseAdd("29 mar");
		} catch (InvalidTitle e) {
			thrown = true;
		}
		assertEquals(true, thrown);  
		
		thrown = false;
		try {
			task = parser.parseAdd("3pm");
		} catch (InvalidTitle e) {
			thrown = true;
		}
		assertEquals(true, thrown);  
		
		thrown = false;
		try {
			task = parser.parseAdd("29 mar 3pm");
		} catch (InvalidTitle e) {
			thrown = true;
		}
		assertEquals(true, thrown);  
		
		thrown = false;
		try {
			task = parser.parseAdd("29 mar 3pm-5pm");
		} catch (InvalidTitle e) {
			thrown = true;
		}
		assertEquals(true, thrown);
		
		thrown = false;
		try {
			task = parser.parseAdd("3");
		} catch (InvalidTitle e) {
			thrown = true;
		}
		assertEquals(false, thrown);  
		
		thrown = false;
		try {
			task = parser.parseAdd("29 march 3");
		} catch (InvalidTitle e) {
			thrown = true;
		}
		assertEquals(false, thrown);
	}
	
	/**
	 * Test for adding a task without preposition.
	 * 
	 * @throws InvalidLabelFormat
	 * @throws InvalidTitle 
	 */
	@Test
	public void testNoPreposition() throws InvalidLabelFormat, InvalidTitle {
		CommandParser parser = new CommandParser();
		Task task;
		
		task = parser.parseAdd("Buy apple 1 may 3pm");
		assertEquals("Buy apple from 1 May 3pm", task.toString());
		
		//date only = 12am
		task = parser.parseAdd("Buy apple 1 may");
		assertEquals("Buy apple from 1 May 12am", task.toString());
		
		//1pm has past, nearest is 1am so take 1pm
		task = parser.parseAdd("Buy apple 1pm");
		assertEquals("Buy apple from today 1pm", task.toString());
		
		//1am has past, nearest is 1am so still matching
		task = parser.parseAdd("Buy apple 5am");
		assertEquals("Buy apple from today 5am", task.toString());
		
		task = parser.parseAdd("Buy apple 1-3pm");
		assertEquals("Buy apple from today 1pm to 3pm", task.toString());
		
		task = parser.parseAdd("Buy apple 1pm-3pm");
		assertEquals("Buy apple from today 1pm to 3pm", task.toString());
		
		//if ending not specified, am
		task = parser.parseAdd("Buy apple 1pm-3");
		assertEquals("Buy apple from today 1pm to this Sun 3am", task.toString());	
	}
	
	/**
	 * Test for time detection without preposition if time is explicitly specified.
	 * 
	 * @throws InvalidLabelFormat
	 * @throws InvalidTitle 
	 */
	@Test
	public void testHasTime() throws InvalidLabelFormat, InvalidTitle {
		CommandParser parser = new CommandParser();
		Task task;

		task = parser.parseAdd("Dinner 7pm");
		assertEquals("Dinner from today 7pm", task.toString());

		task = parser.parseAdd("Dinner 7PM today");
		assertEquals("Dinner from today 7pm", task.toString());

		task = parser.parseAdd("Homework 5.15pm");
		assertEquals("Homework from today 5:15pm", task.toString());
	}
	
	/**
	 * Date should be relative to current time when being parsed.
	 * 
	 * @throws InvalidLabelFormat
	 * @throws InvalidTitle 
	 */
	@Test
	public void testSmartDetectionOfTime() throws InvalidLabelFormat, InvalidTitle {
		CommandParser parser = new CommandParser();
		Task task;

		task = parser.parseAdd("Do homework by 2");
		assertEquals("Do homework by today 2pm", task.toString());
		
		task = parser.parseAdd("Do homework by 10");
		assertEquals("Do homework by today 10am", task.toString());

		task = parser.parseAdd("Do homework by 10am");
		assertEquals("Do homework by today 10am", task.toString());

		task = parser.parseAdd("Do homework by 10pm");
		assertEquals("Do homework by today 10pm", task.toString());  

		task = parser.parseAdd("Do homework by 10 mar 2pm");
		assertEquals("Do homework by 10 Mar 2pm", task.toString());
	}
	
	/**
	 * Test for start time detection when parsing.
	 * Feedback shown is relative to the current period.
	 * 
	 * @throws InvalidLabelFormat 
	 * @throws InvalidTitle 
	 */
	@Test
	public void testDetectStartTime() throws InvalidLabelFormat, InvalidTitle {
		CommandParser parser = new CommandParser();
		Task task = parser.parseAdd("Attempt quiz from 5pm 20 apr");
		assertEquals("Attempt quiz", task.getTitle());
		assertEquals("Attempt quiz from 20 Apr 5pm", task.toString());

		task = parser.parseAdd("Watch webcast after 3am on 20 APR");
		assertEquals("Watch webcast", task.getTitle());  	
		assertEquals("Watch webcast from 20 Apr 3am", task.toString());

		task = parser.parseAdd("Watch movie at 2pm");
		assertEquals("Watch movie", task.getTitle());
		assertEquals("Watch movie from today 2pm", task.toString());

		task = parser.parseAdd("Watch movie on 20 Apr 7pm");
		assertEquals("Watch movie", task.getTitle());
		assertEquals("Watch movie from 20 Apr 7pm", task.toString());
	}	
	
	/**
	 * Test for correct parsing of ranged time in user input.
	 * 
	 * @throws InvalidLabelFormat
	 * @throws InvalidTitle 
	 */
	@Test
	public void testTaskWithTimeRange() throws InvalidLabelFormat, InvalidTitle {
		CommandParser parser = new CommandParser();
		Task task = parser.parseAdd("Do homework on 20apr 2-4pm");
		assertEquals("Do homework from 20 Apr 2pm to 4pm", task.toString());

		task = parser.parseAdd("Do homework from 2pm-4 on 20apr");
		assertEquals("Do homework from 20 Apr 2pm to 4am", task.toString());

		task = parser.parseAdd("Do homework on 20apr 2pm-4pm");
		assertEquals("Do homework from 20 Apr 2pm to 4pm", task.toString());

		task = parser.parseAdd("Do homework on 21st apr 2pm-4pm");
		assertEquals("Do homework from 21 Apr 2pm to 4pm", task.toString());
		
		task = parser.parseAdd("Do homework on 20 apr 10-11pm");
		assertEquals("Do homework from 20 Apr 10pm to 11pm", task.toString());

		task = parser.parseAdd("Do homework from 10pm-11 on 20 apr");
		assertEquals("Do homework from 20 Apr 10pm to 11am", task.toString());

		task = parser.parseAdd("Do homework on 20 apr 10pm-11pm");
		assertEquals("Do homework from 20 Apr 10pm to 11pm", task.toString());

		task = parser.parseAdd("Do homework on 21st apr 10pm-11pm");
		assertEquals("Do homework from 21 Apr 10pm to 11pm", task.toString());
	}
	
	/**
	 * Test feedback shown when parsing tasks 
	 * Feedback shown is relative to the current period.
	 * 
	 * @throws InvalidLabelFormat 
	 * @throws InvalidTitle 
	 */
	@Test
	public void testTaskToString() throws InvalidLabelFormat, InvalidTitle {
		CommandParser parser = new CommandParser();

		Task task = parser.parseAdd("Cook dinner");
		assertEquals("Cook dinner", task.toString());

		task = parser.parseAdd("Cook dinner #home");
		assertEquals("Cook dinner #home", task.toString());
		
		task = parser.parseAdd("Cook dinner on 14/3 at 7pm #home");
		assertEquals("Cook dinner from 14 Mar 7pm #home",task.toString());

		task = parser.parseAdd("Cook dinner on 15/3 7.15pm");
		assertEquals("Cook dinner from 15 Mar 7:15pm",task.toString());

		task = parser.parseAdd("Cook dinner on 30/4 7:15pm");
		assertEquals("Cook dinner from 30 Apr 7:15pm", task.toString());

		task = parser.parseAdd("Attend meeting on 26-4 7pm");
		assertEquals("Attend meeting from 26 Apr 7pm", task.toString());

		task = parser.parseAdd("Attend meeting from 4 to 6pm on 25 Apr");
		assertEquals("Attend meeting from 25 Apr 4pm to 6pm", task.toString());

		task = parser.parseAdd("Attend meeting 4 to 6pm on 25 Apr");
		assertEquals("Attend meeting from 25 Apr 4pm to 6pm", task.toString());

		task = parser.parseAdd("Attend meeting on 1 Mar 9am");
		assertEquals("Attend meeting from 1 Mar 9am", task.toString());

		task = parser.parseAdd("Go camp from 1-3 8am to 3-3 9pm");
		assertEquals("Go camp from 1 Mar 8am to 3 Mar 9pm", task.toString());
	}
	
	/**
	 * Test parsing of days 
	 * 
	 * @throws InvalidLabelFormat
	 * @throws InvalidTitle
	 */
	@Test
	public void testDays() throws InvalidLabelFormat, InvalidTitle {
		CommandParser parser = new CommandParser();
		//now is tues
		Task task = parser.parseAdd("Attend meeting on thurs");
		assertEquals("Attend meeting from next Thu 12am", task.toString());
		
		//now is tues
		task = parser.parseAdd("Attend meeting from mon to weds");
		assertEquals("Attend meeting from next Mon 12am to next Wed 12am", task.toString());
		
		task = parser.parseAdd("Do homework from 1/2 to 2/2");
		assertEquals("Do homework from 1 Feb 12am to 2 Feb 12am", task.toString());
	}

	/**
	 * Test adding of dated task.
	 * Asserts title and time has been parsed correctly.
	 * 
	 * @throws ParseException for dateFormat.parse()
	 * @throws InvalidLabelFormat 
	 * @throws InvalidTitle 
	 */
	@Test
	public void testAdd() throws ParseException, InvalidLabelFormat, InvalidTitle {
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
	@Test //left quoted //pending
	public void testDatedTaskTitle() throws InvalidLabelFormat, InvalidTitle{
		CommandParser parser = new CommandParser();
		Task task = parser.parseAdd("Attend meeting from Monday to Wednesday");
		assertEquals("Attend meeting", task.getTitle());

		task = parser.parseAdd("Attend meeting from 4 to 6");
		assertEquals("Attend meeting", task.getTitle());

		task = parser.parseAdd("Cook dinner at 7");
		assertEquals("Cook dinner", task.getTitle());

		task = parser.parseAdd("Attend meeting on Wed");
		
		assertEquals("Attend meeting", task.getTitle());

		task = parser.parseAdd("Do homework by Sunday");
		assertEquals("Do homework", task.getTitle());

		task = parser.parseAdd("Send 100 email before 8pm");
		assertEquals("Send 100 email", task.getTitle());

		task = parser.parseAdd("Meet at \"Taco Tuesday\" on Wednesday 5pm");
		//assertEquals("Meet at \"Taco Tuesday\"", task.getTitle());

		task =  parser.parseAdd("Chase \"2pm\" Korean band on Saturday 7pm");
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
	 * Test method for comparing task.
	 * Tasks are compared by their creation date.
	 * Task with same title would not be equal.
	 * 
	 * @throws InterruptedException for Thread.sleep().
	 * Ensures that there are differences in time when creating task.
	 * @throws InvalidLabelFormat 
	 * @throws InvalidTitle 
	 */

	public void testCompareTo() throws InterruptedException, InvalidLabelFormat, InvalidTitle {
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
	public void testEditBasic() throws InvalidLabelFormat, InvalidTitle {
		CommandParser parser = new CommandParser();
		Task task, task2;
		
    	task = parser.parseAdd("Buy milk");
    	task2 = parser.parseEdit(task, "Buy chocolate");
    	assertEquals("Buy chocolate", task2.toString());

    	task = parser.parseAdd("Buy milk");
    	task2 = parser.parseEdit(task, "Buy chocolate #party");
    	assertEquals("Buy chocolate #party", task2.toString());
    	
    	task = parser.parseAdd("Buy milk");
    	task2 = parser.parseEdit(task, "Buy chocolate by 10pm");
    	assertEquals("Buy chocolate by today 10pm", task2.toString());

     	task = parser.parseAdd("Buy milk");
    	task2 = parser.parseEdit(task, "Buy chocolate from 10pm");
    	assertEquals("Buy chocolate from today 10pm", task2.toString());
    	
    	task = parser.parseAdd("Buy milk");
    	task2 = parser.parseEdit(task, "Buy milk at discount from 10pm to 11pm");
    	assertEquals("Buy milk at discount from today 10pm to 11pm", task2.toString());
	}
	
	/**
	 * Test editing time of existing task.
	 * Old information should be retained.
	 * 
	 * @throws InvalidLabelFormat
	 * @throws InvalidTitle 
	 */
	@Test
	public void testEditTimeOnly() throws InvalidLabelFormat, InvalidTitle {
		CommandParser parser = new CommandParser();
		Task task, task2;
    	
     	task = parser.parseAdd("Buy milk");
    	task2 = parser.parseEdit(task, "by 10");
    	assertEquals("Buy milk by today 10am", task2.toString());
    	
    	task = parser.parseAdd("Buy milk");
    	task2 = parser.parseEdit(task, "10pm");
    	assertEquals("Buy milk from today 10pm", task2.toString());

    	task = parser.parseAdd("Buy milk");
    	task2 = parser.parseEdit(task, "10pm to 11pm");
    	assertEquals("Buy milk from today 10pm to 11pm", task2.toString());
    	
    	task = parser.parseAdd("Buy milk at 2pm");
    	task2 = parser.parseEdit(task, "10pm to 11pm");
    	assertEquals("Buy milk from today 10pm to 11pm", task2.toString());
    	
    	task = parser.parseAdd("Buy milk from 6 to 7pm");
    	task2 = parser.parseEdit(task, "by 11:51pm");
    	assertEquals("Buy milk by today 11:51pm", task2.toString());
    	
    	task = parser.parseAdd("Buy milk from 6 to 7pm");
    	task2 = parser.parseEdit(task, "from 9 to 10pm");
    	assertEquals("Buy milk from today 9pm to 10pm", task2.toString());
	}
	
	@Test
	public void testEditTimeOnly2() throws InvalidLabelFormat, InvalidTitle {
		CommandParser parser = new CommandParser();
		Task task, task2;
		
		task = parser.parseAdd("Buy milk on 1 may at 2pm #party");

		task2 = parser.parseEdit(task, "3am");
		assertEquals("Buy milk from 1 May 3am #party", task2.toString());

		task2 = parser.parseEdit(task, "3pm");
		assertEquals("Buy milk from 1 May 3pm #party", task2.toString());

		task2 = parser.parseEdit(task, "at 4");
		assertEquals("Buy milk from 1 May 4am #party", task2.toString());

		task2 = parser.parseEdit(task, "at 4am");
		assertEquals("Buy milk from 1 May 4am #party", task2.toString());

		task2 = parser.parseEdit(task, "at 4pm");
		assertEquals("Buy milk from 1 May 4pm #party", task2.toString());

		task2 = parser.parseEdit(task, "3-4pm");
		assertEquals("Buy milk from 1 May 3pm to 4pm #party", task2.toString());

		task2 = parser.parseEdit(task, "3pm-4pm");
		assertEquals("Buy milk from 1 May 3pm to 4pm #party", task2.toString());

		task2 = parser.parseEdit(task, "3pm-4");
		assertEquals("Buy milk from 1 May 3pm to 4am #party", task2.toString());

		task2 = parser.parseEdit(task, "from 3-4pm");
		assertEquals("Buy milk from 1 May 3pm to 4pm #party", task2.toString());

		task2 = parser.parseEdit(task, "from 3pm-4pm");
		assertEquals("Buy milk from 1 May 3pm to 4pm #party", task2.toString());

		task2 = parser.parseEdit(task, "from 3pm-4");
		assertEquals("Buy milk from 1 May 3pm to 4am #party", task2.toString());
	}
	
	/**
	 * Test editing date of existing task.
	 * Old information should be retained.
	 * 
	 * @throws InvalidLabelFormat
	 * @throws InvalidTitle 
	 */
	@Test
	public void testEditDateOnly() throws InvalidLabelFormat, InvalidTitle {
		CommandParser parser = new CommandParser();
		Task task, task2;
    	
     	task = parser.parseAdd("Buy milk 1 may 2pm");
    	task2 = parser.parseEdit(task, "1 june");
    	assertEquals("Buy milk from 1 Jun 2pm", task2.toString());
    	
    	task = parser.parseAdd("Buy milk 1 may 2pm");
    	task2 = parser.parseEdit(task, "by 1 june");
    	assertEquals("Buy milk by 1 Jun 2pm", task2.toString());

    	task = parser.parseAdd("Buy milk 1 may 2pm");
    	task2 = parser.parseEdit(task, "2/5");
    	assertEquals("Buy milk from 2 May 2pm", task2.toString());
    	
    	task = parser.parseAdd("Buy milk 1 may 2pm");
    	task2 = parser.parseEdit(task, "by 2/5");
    	assertEquals("Buy milk by 2 May 2pm", task2.toString());  
    	
    	task = parser.parseAdd("Buy milk 1 may 2pm");
    	task2 = parser.parseEdit(task, "from 1 june to 3 june");
    	assertEquals("Buy milk from 1 Jun 2pm to 3 Jun 2pm", task2.toString());
    	
    	task = parser.parseAdd("Buy milk 1 may 2pm");
    	task2 = parser.parseEdit(task, "from 1/6 to 2/6");
    	assertEquals("Buy milk from 1 Jun 2pm to 2 Jun 2pm", task2.toString());
	}
	
	/**
	 * Test editing of task.
	 * If information is not specified, use the old task information.
	 * This allows updating of only the date or time.
	 * 
	 * @throws InvalidLabelFormat
	 * @throws InvalidTitle 
	 */
	@Test
	public void testOverallEdit() throws InvalidLabelFormat, InvalidTitle {
		CommandParser parser = new CommandParser();
		Task task, task2;
		
		task = parser.parseAdd("Buy milk by 30 april 7pm");
		task2 = parser.parseEdit(task, "by 4pm");
		assertEquals("Buy milk by 30 Apr 4pm", task2.toString());
		assertEquals(0, task2.getPriority());
		
		task = parser.parseAdd("Buy milk by 30 april 7pm");
		task.togglePriority(true);
		task2 = parser.parseEdit(task, "from 1 to 3pm");
		assertEquals("Buy milk from 30 Apr 1pm to 3pm P:1", task2.toString());
		assertEquals(1, task2.getPriority());
		
		task = parser.parseAdd("Buy milk by 10 apr");
		task.togglePriority(true);
		task.togglePriority(true);
    	task2 = parser.parseEdit(task, "by 20 april 8pm");
    	assertEquals("Buy milk by 20 Apr 8pm P:2", task2.toString());
    	assertEquals(2, task2.getPriority());		
    	
		task = parser.parseAdd("Buy kitkat by 10 april 7pm");
		task.togglePriority(true);
		task.togglePriority(true);
		task.togglePriority(true);
		task.setPriority(3);
		task2 = parser.parseEdit(task, "by 11 april");
		assertEquals("Buy kitkat by 11 Apr 7pm P:3", task2.toString());
		assertEquals(3, task2.getPriority());
		
		task = parser.parseAdd("Buy kitkat by 10 april 7pm");
		task2 = parser.parseEdit(task, "FROM 11 APR TO 12 APRIL");
		assertEquals("Buy kitkat from 11 Apr 7pm to 12 Apr 7pm", task2.toString());	
	}
	
	@Test
	public void testOverallEdit2() throws InvalidLabelFormat, InvalidTitle {
		CommandParser parser = new CommandParser();
		Task task, task2;
		
		task = parser.parseAdd("Drink coffee from 1/6 to 3/6 8pm #overdose");
		task2 = parser.parseEdit(task, "1/7 to 3/7");
		assertEquals("Drink coffee from 1 Jul 8pm to 3 Jul 8pm #overdose", task2.toString());
		
		task2 = parser.parseEdit(task, "Drink hot chocolate");
		assertEquals("Drink hot chocolate from 1 Jun 8pm to 3 Jun 8pm #overdose", task2.toString());
		
		task2 = parser.parseEdit(task, "#beedohbeedoh");
		assertEquals("Drink coffee from 1 Jun 8pm to 3 Jun 8pm #beedohbeedoh", task2.toString());
		
		task2 = parser.parseEdit(task, "1 july");
		assertEquals("Drink coffee from 1 Jul 8pm #overdose", task2.toString());
		
		task2 = parser.parseEdit(task, "from 9 to 10pm");
		assertEquals("Drink coffee from 1 Jun 9pm to 3 Jun 10pm #overdose", task2.toString());
		
		task2 = parser.parseEdit(task, "by 1 may 2pm");
		assertEquals("Drink coffee by 1 May 2pm #overdose", task2.toString());
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
	
	@Test
	public void testIndexesString() throws InvalidTaskIndexFormat {
		CommandParser parser = new CommandParser();	
		ParseIndexResult indexes;
		
		indexes = parser.parseIndexes("del 1-10,12,13,14,15", 10);
		assertEquals("1-10", indexes.getValidIndexesString());
		assertEquals("12-15", indexes.getInvalidIndexesString());
		
		indexes = parser.parseIndexes("del 1-5,10-20, 31, 32, 33, 35", 10);
		assertEquals("1-5,10", indexes.getValidIndexesString());
		assertEquals("11-20,31-33,35", indexes.getInvalidIndexesString());	
	}

	// =============================
	// Latest stuff
	// =============================
	@Test
	public void testBuggyTheClown() throws InvalidLabelFormat, InvalidTitle {
		CommandParser parser = new CommandParser();	
		Task task;
		task = parser.parseAdd("eat brownie p1");
		assertEquals(1, task.getPriority());
		assertEquals("eat brownie P:1", task.toString());
		
		task = parser.parseAdd("eat brownie p 2");
		assertEquals(2, task.getPriority());
		assertEquals("eat brownie P:2", task.toString());
		
		task = parser.parseAdd("eat brownie priority3");
		assertEquals(3, task.getPriority());
		assertEquals("eat brownie P:3", task.toString());
		
		task = parser.parseAdd("eat brownie priority 3");
		assertEquals(3, task.getPriority());
		assertEquals("eat brownie P:3", task.toString());
		
		task = parser.parseAdd("eat brownie from 1/5 10pm to 11pm priority 3");
		System.out.println(task.toString());
		assertEquals(3, task.getPriority());
		assertEquals("eat brownie from 1 May 10pm to 11pm P:3", task.toString());		
	}
}