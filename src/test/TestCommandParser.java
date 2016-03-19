package test;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import main.data.Command;
import main.data.Task;
import main.parser.CommandParser;

/**
 * @author Joleen
 *
 */

public class TestCommandParser {
    
    @Test
    public void testDetectFloating() {
        CommandParser parser = new CommandParser();
        
        Command command = parser.parse("Do assignment 1");
        assertEquals(false, command.getTask().hasDate());
        
        command = parser.parse("Undo task 3");
        assertEquals(false, command.getTask().hasDate());
        
        command = parser.parse("Fetch my brothers from school");
        assertEquals(false, command.getTask().hasDate());
        
        command = parser.parse("Send 100 emails from my computer");
        assertEquals(false, command.getTask().hasDate());
        
        command = parser.parse("Drive by the supermarket");
        assertEquals(false, command.getTask().hasDate());
        
        command = parser.parse("Attack enemy base on signal");
        assertEquals(false, command.getTask().hasDate());
        
        command = parser.parse("Send 100 email before I sleep");
        assertEquals(false, command.getTask().hasDate());
        
        command = parser.parse("Watch \"day after tomorrow\" movie");
        assertEquals(false, command.getTask().hasDate());
    }
    
    @Test
    public void testAddFloating() {
        CommandParser parser = new CommandParser();
        
        Command command = parser.parse("Cook dinner");
        assertEquals(Command.Type.ADD, command.getCommandType());
        assertEquals(false, command.getTask().hasDate());
        assertEquals("Cook dinner", command.getTask().getTitle());
        
        command = parser.parse("Attack enemy base on signal");
        assertEquals(Command.Type.ADD, command.getCommandType());
        assertEquals(false, command.getTask().hasDate());
        assertEquals("Attack enemy base on signal", command.getTask().getTitle());
    }

    @Test
    public void testAdd() throws ParseException {
        CommandParser parser = new CommandParser();
        
        Command command = parser.parse("Cook dinner on 4 Mar 7pm #home");
        SimpleDateFormat df = new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy");
        String startDate = "Thu Mar 4 19:00:00 SGT 2016";
        Date expectedStart = df.parse(startDate);
        String endDate = "Thu Mar 4 20:00:00 SGT 2016";
        Date expectedEnd = df.parse(endDate);
        
        assertEquals(true, command.getTask().hasDate());
        assertEquals(expectedStart, command.getTask().getStartDate());
        assertEquals(expectedEnd, command.getTask().getEndDate());
    }
    
    @Test
    public void testLabel() {
        CommandParser parser = new CommandParser();
        
        Command command = parser.parse("Cook dinner #home");
        assertEquals("Cook dinner", command.getTask().getTitle());
        assertEquals("home", command.getTask().getLabel());
        
        command = parser.parse("#home Cook dinner");
        assertEquals("Cook dinner", command.getTask().getTitle());
        assertEquals("home", command.getTask().getLabel());
    }
    
    @Test
    public void testDatedTaskTitle(){
    	CommandParser parser = new CommandParser();
    	Command command = parser.parse("Attend meeting from Monday to Wednesday");
    	assertEquals("Attend meeting", command.getTask().getTitle());
    	
    	command = parser.parse("Attend meeting from 4 to 6");
    	assertEquals("Attend meeting", command.getTask().getTitle());
    	
    	command = parser.parse("Attend meeting from 4 - 6");
    	assertEquals("Attend meeting", command.getTask().getTitle());
    	
    	command = parser.parse("Cook dinner at 7");
    	assertEquals("Cook dinner", command.getTask().getTitle());

    	command = parser.parse("Attend meeting on Wed");
    	assertEquals("Attend meeting", command.getTask().getTitle());

    	command = parser.parse("Do homework by Sunday");
    	assertEquals("Do homework", command.getTask().getTitle());

    	command = parser.parse("Send 100 email before 8pm");
    	assertEquals("Send 100 email", command.getTask().getTitle());
    	
    	command = parser.parse("Meet at \"Taco Tuesday\" on Wednesday 5pm");
    	assertEquals("Meet at \"Taco Tuesday\"", command.getTask().getTitle());
    	
    	command =  parser.parse("Chase \"2pm\" Korean band on Saturday 7pm");
    	assertEquals("Chase \"2pm\" Korean band", command.getTask().getTitle());
    	
    	command = parser.parse("Attend meeting from Monday to Wednesday 6pm");
    	assertEquals("Attend meeting", command.getTask().getTitle());
    	
    	command = parser.parse("Cook dinner at 7pm at home");
    	assertEquals("Cook dinner at home", command.getTask().getTitle());
    	
    	command = parser.parse("Cook dinner on 4 Mar 7pm");
    	assertEquals("Cook dinner", command.getTask().getTitle());
    	
    	command = parser.parse("Do assignment by Sunday 8pm");
    	assertEquals("Do assignment", command.getTask().getTitle());
    	
    	command = parser.parse("Send 100 email before sunday 7pm");
    	assertEquals("Send 100 email", command.getTask().getTitle());
    }
    
    @Test
    public void testDetectStartTime() {
    	CommandParser parser = new CommandParser();
    	Command command = parser.parse("Attempt quiz from 5pm 14 MARCH");
    	assertEquals("Attempt quiz", command.getTask().getTitle());  	
    	assertEquals("Attempt quiz from this Mon 5pm to 6pm", command.getTask().toString());
    	
    	command = parser.parse("Watch webcast after 3am on 15 MAR");
    	assertEquals("Watch webcast", command.getTask().getTitle());  	
    	assertEquals("Watch webcast from this Tue 3am to 4am", command.getTask().toString());
    	
    	command = parser.parse("Watch movie at 7pm");
    	assertEquals("Watch movie", command.getTask().getTitle());
    	assertEquals("Watch movie from today 7pm to 8pm", command.getTask().toString());
    	
    	command = parser.parse("Watch movie at 7:15pm");
    	assertEquals("Watch movie", command.getTask().getTitle());
    	assertEquals("Watch movie from today 7:15pm to 8:15pm", command.getTask().toString());
    }
    
    @Test
    public void testTaskToString() {
    	CommandParser parser = new CommandParser();

    	Command command = parser.parse("Cook dinner");
    	assertEquals("Cook dinner", command.getTask().toString());
    	
    	command = parser.parse("Cook dinner #home");
    	assertEquals("Cook dinner #home", command.getTask().toString());

    	command = parser.parse("Cook dinner 14/3 at 7pm #home");
    	assertEquals("Cook dinner from this Mon 7pm to 8pm #home", command.getTask().toString());

    	command = parser.parse("Cook dinner on 15/3 7pm");
    	assertEquals("Cook dinner from this Tue 7pm to 8pm", command.getTask().toString());
    	
    	command = parser.parse("Cook dinner on 24/3 7:15pm");
    	assertEquals("Cook dinner from next Thu 7:15pm to 8:15pm", command.getTask().toString());
    	
    	command = parser.parse("Attend meeting on 26-3 7pm");
    	assertEquals("Attend meeting from next Sat 7pm to 8pm", command.getTask().toString());

    	command = parser.parse("Attend meeting from 4 to 6pm on 25 Mar");
    	assertEquals("Attend meeting from next Fri 4pm to 6pm",command.getTask().toString());
    	
    	command = parser.parse("Attend meeting 4 to 6pm on 25 Mar");
    	assertEquals("Attend meeting from next Fri 4pm to 6pm",command.getTask().toString()); 
    	
    	command = parser.parse("Attend meeting on 1 April 9am");
    	assertEquals("Attend meeting from 1 Apr 9am to 10am", command.getTask().toString());
    	
    	command = parser.parse("Go camp from 1/3 8am to 3/3 9pm");
    	assertEquals("Go camp from 1 Mar 8am to 3 Mar 9pm", command.getTask().toString());
    }
    
    @Test
    public void testCompareTo() throws InterruptedException {
    	CommandParser parser = new CommandParser();
    	Task task1, task2;
    	
    	Command command = parser.parse("Cook dinner at 7pm");
    	task1 = command.getTask();
    	Thread.sleep(2000);
    	command = parser.parse("Cook dinner at 7pm");
    	task2 = command.getTask();
    	assertEquals(-1, task1.compareTo(task2));
    	
    	command = parser.parse("Cook dinner at 8pm");
    	task1 = command.getTask();
    	Thread.sleep(2000);
    	command = parser.parse("Cook dinner at 9pm");
    	task2 = command.getTask();
    	assertEquals(-1, task1.compareTo(task2));
    	
    	command = parser.parse("Attend meeting");
    	task1 = command.getTask();
    	Thread.sleep(2000);
    	command = parser.parse("Attend meeting #important");
    	task2 = command.getTask();
    	assertEquals(-1, task1.compareTo(task2));
    }
    
    @Test
    public void testTogglePriority() {
    	CommandParser parser = new CommandParser();
        Command command = parser.parse("Cook dinner #home");
        assertEquals(0, command.getTask().getPriority());
        assertEquals(1, command.getTask().togglePriority());
        assertEquals(2, command.getTask().togglePriority());
        assertEquals(3, command.getTask().togglePriority());
        assertEquals(0, command.getTask().togglePriority());
    }
    
    @Test
    public void testDelete() {
        CommandParser parser = new CommandParser();
        Command command = parser.parse("delete 1");
        
        ArrayList<Integer> indexes = new ArrayList<Integer>();
        indexes.add(1);
        assertEquals(Command.Type.DELETE, command.getCommandType());
        assertEquals(indexes, command.getIndexes());
        
        command = parser.parse("del 1,3,5,7,9");
        indexes.clear();
        indexes.add(1);
        indexes.add(3);
        indexes.add(5);
        indexes.add(7);
        indexes.add(9);
        assertEquals(Command.Type.DELETE, command.getCommandType());
        assertEquals(indexes, command.getIndexes());
        
        command = parser.parse("delete 1-10");
        indexes.clear();
        indexes.add(1);
        indexes.add(2);
        indexes.add(3);
        indexes.add(4);
        indexes.add(5);
        indexes.add(6);
        indexes.add(7);
        indexes.add(8);
        indexes.add(9);
        indexes.add(10);
        assertEquals(indexes, command.getIndexes());
        
        command = parser.parse("delete 1-3,4,5,6-9,10");
        assertEquals(indexes, command.getIndexes());
    }
    
    @Test
    public void testDone() {
        CommandParser parser = new CommandParser();
        Command command = parser.parse("done 1");
        
        ArrayList<Integer> indexes = new ArrayList<Integer>();
        indexes.add(1);
        assertEquals(Command.Type.DONE, command.getCommandType());
        assertEquals(indexes, command.getIndexes());
        
        command = parser.parse("done 1,3,5,7,9");
        indexes.clear();
        indexes.add(1);
        indexes.add(3);
        indexes.add(5);
        indexes.add(7);
        indexes.add(9);
        assertEquals(Command.Type.DONE, command.getCommandType());
        assertEquals(indexes, command.getIndexes());
        
        command = parser.parse("done 1-10");
        indexes.clear();
        indexes.add(1);
        indexes.add(2);
        indexes.add(3);
        indexes.add(4);
        indexes.add(5);
        indexes.add(6);
        indexes.add(7);
        indexes.add(8);
        indexes.add(9);
        indexes.add(10);
        assertEquals(indexes, command.getIndexes());
        
        command = parser.parse("done 1-3,4,5,6-9,10");
        assertEquals(indexes, command.getIndexes());
    }
    
    @Test
    public void testToggleDone() {
    	 CommandParser parser = new CommandParser();
         Command command = parser.parse("Do assignment");
         Task task = command.getTask();
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
    public void testUndone() {
        CommandParser parser = new CommandParser();
        Command command = parser.parse("undone 1");
        
        ArrayList<Integer> indexes = new ArrayList<Integer>();
        indexes.add(1);
        assertEquals(Command.Type.UNDONE, command.getCommandType());
        assertEquals(indexes, command.getIndexes());
        
        command = parser.parse("undone 1,3,5,7,9");
        indexes.clear();
        indexes.add(1);
        indexes.add(3);
        indexes.add(5);
        indexes.add(7);
        indexes.add(9);
        assertEquals(Command.Type.UNDONE, command.getCommandType());
        assertEquals(indexes, command.getIndexes());
        
        command = parser.parse("undone 1-10");
        indexes.clear();
        indexes.add(1);
        indexes.add(2);
        indexes.add(3);
        indexes.add(4);
        indexes.add(5);
        indexes.add(6);
        indexes.add(7);
        indexes.add(8);
        indexes.add(9);
        indexes.add(10);
        assertEquals(indexes, command.getIndexes());
        
        command = parser.parse("undone 1-3,4,5,6-9,10");
        assertEquals(indexes, command.getIndexes());
    }
}