package test;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import main.data.Task;
import main.parser.CommandParser;
import main.parser.CommandParser.InvalidTaskIndexFormat;

/**
 * @author Joleen
 *
 */

public class TestCommandParser {
    /**
     * Test for the detection of floating task.
     * Even with prepositions, it should not be dated.
     */
    @Test    
    public void testDetectFloating() {
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
     */
    @Test
    public void testAddFloating() {
        CommandParser parser = new CommandParser();
        
        Task task = parser.parseAdd("Cook dinner");
        assertEquals(false, task.hasDate());
        assertEquals("Cook dinner", task.getTitle());
        
        task = parser.parseAdd("Attack enemy base on signal");
        assertEquals(false, task.hasDate());
        assertEquals("Attack enemy base on signal", task.getTitle());
    }

    /**
     * Test adding of dated task.
     * Asserts title and time has been parsed correctly.
     * 
     * @throws ParseException for dateFormat.parse()
     */
    @Test
    public void testAdd() throws ParseException {
        CommandParser parser = new CommandParser();
        Task task = parser.parseAdd("Cook dinner on 4 Mar 7pm #home");
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy");
        String startDate = "Thu Mar 4 19:00:00 SGT 2016";
        Date expectedStart = dateFormat.parse(startDate); 
        String endDate = "Thu Mar 4 20:00:00 SGT 2016";
        Date expectedEnd = dateFormat.parse(endDate);
        
        assertEquals(true, task.hasDate());
        assertEquals(expectedStart, task.getStartDate());
        assertEquals(expectedEnd, task.getEndDate());
        assertEquals("Cook dinner", task.getTitle());
    }
    
    /**
     * Test for label extraction.
     */
    @Test
    public void testLabel() {
        CommandParser parser = new CommandParser();
        
        Task task = parser.parseAdd("Cook dinner #home");
        assertEquals("Cook dinner", task.getTitle());
        assertEquals("home", task.getLabel());
        
        task = parser.parseAdd("#home Cook dinner");
        assertEquals("Cook dinner", task.getTitle());
        assertEquals("home", task.getLabel());
    }
    
    /**
     * Test for extraction of date information in title.
     */
    @Test
    public void testDatedTaskTitle(){
    	CommandParser parser = new CommandParser();
    	Task task = parser.parseAdd("Attend meeting from Monday to Wednesday");
    	assertEquals("Attend meeting", task.getTitle());
    	
    	task = parser.parseAdd("Attend meeting from 4 to 6");
    	assertEquals("Attend meeting", task.getTitle());
    	
    	task = parser.parseAdd("Attend meeting from 4 - 6");
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
    	assertEquals("Meet at \"Taco Tuesday\"", task.getTitle());
    	
    	task =  parser.parseAdd("Chase \"2pm\" Korean band on Saturday 7pm");
    	assertEquals("Chase \"2pm\" Korean band", task.getTitle());
    	
    	task = parser.parseAdd("Attend meeting from Monday to Wednesday 6pm");
    	assertEquals("Attend meeting", task.getTitle());
    	
    	task = parser.parseAdd("Cook dinner at 7pm at home");
    	assertEquals("Cook dinner at home", task.getTitle());
    	
    	task = parser.parseAdd("Cook dinner on 4 Mar 7pm");
    	assertEquals("Cook dinner", task.getTitle());
    	
    	task = parser.parseAdd("Do assignment by Sunday 8pm");
    	assertEquals("Do assignment", task.getTitle());
    	
    	task = parser.parseAdd("Send 100 email before sunday 7pm");
    	assertEquals("Send 100 email", task.getTitle());
    }
    
    /**
     * Test for start time detection when parsing.
     * If only start time specified, auto assigned one hour task.
     * 
     * Test failing because of feedback.
     * It works relative to the current period.
     */
    @Ignore @Test
    public void testDetectStartTime() {
    	CommandParser parser = new CommandParser();
    	Task task = parser.parseAdd("Attempt quiz from 5pm 14 MARCH");
    	assertEquals("Attempt quiz", task.getTitle());  	
    	assertEquals("Attempt quiz from this Mon 5pm to 6pm", task.toString());
    	
    	task = parser.parseAdd("Watch webcast after 3am on 15 MAR");
    	assertEquals("Watch webcast", task.getTitle());  	
    	assertEquals("Watch webcast from this Tue 3am to 4am", task.toString());
    	
    	task = parser.parseAdd("Watch movie at 7pm");
    	assertEquals("Watch movie", task.getTitle());
    	assertEquals("Watch movie from today 7pm to 8pm", task.toString());
    	
    	task = parser.parseAdd("Watch movie at 7:15pm");
    	assertEquals("Watch movie", task.getTitle());
    	assertEquals("Watch movie from today 7:15pm to 8:15pm", task.toString());
    }
    
    /**
     * Test feedback shown when parsing tasks.
     * 
     * Test failing because of feedback.
     * It works relative to the current period.
     */
    @Ignore @Test
    public void testTaskToString() {
    	CommandParser parser = new CommandParser();

    	Task task = parser.parseAdd("Cook dinner");
    	assertEquals("Cook dinner", task.toString());
    	
    	task = parser.parseAdd("Cook dinner #home");
    	assertEquals("Cook dinner #home", task.toString());

    	task = parser.parseAdd("Cook dinner 14/3 at 7pm #home");
    	assertEquals("Cook dinner from this Mon 7pm to 8pm #home", task.toString());

    	task = parser.parseAdd("Cook dinner on 15/3 7pm");
    	assertEquals("Cook dinner from this Tue 7pm to 8pm", task.toString());
    	
    	task = parser.parseAdd("Cook dinner on 24/3 7:15pm");
    	assertEquals("Cook dinner from next Thu 7:15pm to 8:15pm", task.toString());
    	
    	task = parser.parseAdd("Attend meeting on 26-3 7pm");
    	assertEquals("Attend meeting from next Sat 7pm to 8pm", task.toString());

    	task = parser.parseAdd("Attend meeting from 4 to 6pm on 25 Mar");
    	assertEquals("Attend meeting from next Fri 4pm to 6pm",task.toString());
    	
    	task = parser.parseAdd("Attend meeting 4 to 6pm on 25 Mar");
    	assertEquals("Attend meeting from next Fri 4pm to 6pm",task.toString()); 
    	
    	task = parser.parseAdd("Attend meeting on 1 April 9am");
    	assertEquals("Attend meeting from 1 Apr 9am to 10am", task.toString());
    	
    	task = parser.parseAdd("Go camp from 1-3 8am to 3-3 9pm");
    	assertEquals("Go camp from 1 Mar 8am to 3 Mar 9pm", task.toString());
    }
    
    /**
     * Test method for comparing task.
     * Tasks are compared by their creation date.
     * Task with same title would not be equal.
     * 
     * @throws InterruptedException for Thread.sleep().
     * Ensures that there are differences in time when creating task.
     */
    @Ignore @Test
    public void testCompareTo() throws InterruptedException {
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
    
    
    /**
     * Test priority toggling.
     * There are only four levels of priority.
     * It cycles between the four.
     */
    @Test
    public void testTogglePriority() {
    	CommandParser parser = new CommandParser();
        Task task = parser.parseAdd("Cook dinner #home");
        assertEquals(0, task.getPriority());
        assertEquals(1, task.togglePriority());
        assertEquals(2, task.togglePriority());
        assertEquals(3, task.togglePriority());
        assertEquals(0, task.togglePriority());
    }
    
    /**
     * Test parsing of indexes
     * 
     * @throws InvalidTaskIndexFormat
     */
    @Test
    public void testIndexes() throws InvalidTaskIndexFormat {
        CommandParser parser = new CommandParser();
        ArrayList<Integer> indexes = parser.parseIndexes("delete 1");
        
        ArrayList<Integer> expectedIndexes = new ArrayList<Integer>();
        expectedIndexes.add(1);
        assertEquals(expectedIndexes, indexes);
        
        indexes = parser.parseIndexes("del 1,3,5,7,9");
        expectedIndexes.clear();
        expectedIndexes.add(1);
        expectedIndexes.add(3);
        expectedIndexes.add(5);
        expectedIndexes.add(7);
        expectedIndexes.add(9);
        assertEquals(expectedIndexes, indexes);
        
        indexes = parser.parseIndexes("done 1-10");
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
        assertEquals(expectedIndexes, indexes);
        
        indexes = parser.parseIndexes("undone 1-3,4,5,6-9,10");
        assertEquals(expectedIndexes, indexes);
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
        ArrayList<Integer> indexes = new ArrayList<Integer>();
        
        thrown = false;
        try {
        	indexes = parser.parseIndexes("del -1,-2");
        } catch (InvalidTaskIndexFormat e) {
        	thrown = true;
        }
        assertEquals(true, thrown);
        
        thrown = false;
        try {
        	indexes = parser.parseIndexes("del 1--10");
        } catch (InvalidTaskIndexFormat e) {
        	thrown = true;
        }
        assertEquals(true, thrown);
        
        thrown = false;
        try {
        	indexes = parser.parseIndexes("del 1-,10");
        } catch (InvalidTaskIndexFormat e) {
        	thrown = true;
        }
        assertEquals(true, thrown);
        
        thrown = false;
        try {
        	indexes = parser.parseIndexes("del abc,def");
        } catch (InvalidTaskIndexFormat e) {
        	thrown = true;
        }
        assertEquals(true, thrown);
    }
    
    @Test
    public void testToggleDone() {
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
    
    @Test
    public void testCasesToNote() {
    	CommandParser parser = new CommandParser();
    	Task task;
    	
    	task = parser.parseAdd("Dinner 7pm");
    	System.out.println(task.toString());
    	
    	task = parser.parseAdd("Dinner 7pm tomorrow");
    	System.out.println(task.toString());
    	
    	task = parser.parseAdd("Dinner 7pm today");
    	System.out.println(task.toString());
    	
    	//pending
    	task = parser.parseAdd("Meet boss tomorrow");
    	System.out.println(task.toString());
    }
}