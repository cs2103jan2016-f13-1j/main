/**
 *
 */
package test;

/**
 * @author Joleen
 *
 */

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import main.data.Command;
import main.parser.CommandParser;

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
        
        command = parser.parse("Watch day after tomorrow movie");
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
        String date = "Thu Mar 4 19:00:00 SGT 2016";
        Date expectedDate = df.parse(date);
        
        assertEquals(true, command.getTask().hasDate());
        assertEquals(expectedDate, command.getTask().getEndDate());
    }
    
    @Test
    public void testLabel() {
        CommandParser parser = new CommandParser();
        Command command = parser.parse("Cook dinner #home");
        
        assertEquals("Cook dinner", command.getTask().getTitle());
        assertEquals("home", command.getTask().getLabel());
        
        parser.parse("#home Cook dinner");
        assertEquals("Cook dinner", command.getTask().getTitle());
        assertEquals("home", command.getTask().getLabel());
    }
    
    @Test
    public void testTaskToString() {
    	CommandParser parser = new CommandParser();

    	Command command = parser.parse("Cook dinner");
    	assertEquals("Cook dinner", command.getTask().toString());

    	command = parser.parse("Cook dinner #home");
    	assertEquals("Cook dinner #home", command.getTask().toString());

    	command = parser.parse("Cook dinner on 24 Mar 7pm #home");
    	assertEquals("Cook dinner by 24/3 (Thu) 19:00 #home", command.getTask().toString());
    	
    	command = parser.parse("Cook dinner on 3/24 7pm");
    	assertEquals("Cook dinner by 24/3 (Thu) 19:00", command.getTask().toString());
    	
    	command = parser.parse("Cook dinner on 3/24 7:15");
    	assertEquals("Cook dinner by 24/3 (Thu) 07:15", command.getTask().toString());
    	
    	command = parser.parse("Cook dinner on 3/24 7:15pm");
    	assertEquals("Cook dinner by 24/3 (Thu) 19:15", command.getTask().toString());
    	
    	command = parser.parse("Attend meeting on 3-26 7pm");
    	assertEquals("Attend meeting by 26/3 (Sat) 19:00", command.getTask().toString());

    	command = parser.parse("Attend meeting from 4 to 6pm on 25 Mar");
    	assertEquals("Attend meeting from 25/3 (Fri) 16:00 to 25/3 (Fri) 18:00",command.getTask().toString()); 	
    }
    
   @Ignore @Test
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
    }
    
    @Test
    public void testDatedTaskTitleAgain(){
    	CommandParser parser = new CommandParser();
    	Command command = parser.parse("Attend meeting from Monday to Wednesday 6pm");
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
    public void testDelete() {
        CommandParser parser = new CommandParser();
        Command command = parser.parse("delete 1");
        
        ArrayList<Integer> indexes = new ArrayList<Integer>();
        indexes.add(0);
        assertEquals(Command.Type.DELETE, command.getCommandType());
        assertEquals(indexes, command.getIndexes());
        
        command = parser.parse("del 1,3,5,7,9");
        indexes.clear();
        indexes.add(0);
        indexes.add(2);
        indexes.add(4);
        indexes.add(6);
        indexes.add(8);
        assertEquals(Command.Type.DELETE, command.getCommandType());
        assertEquals(indexes, command.getIndexes());
        
        command = parser.parse("delete 1-10");
        indexes.clear();
        indexes.add(0);
        indexes.add(1);
        indexes.add(2);
        indexes.add(3);
        indexes.add(4);
        indexes.add(5);
        indexes.add(6);
        indexes.add(7);
        indexes.add(8);
        indexes.add(9);
        assertEquals(indexes, command.getIndexes());
        
        command = parser.parse("delete 1-3,4,5,6-9,10");
        assertEquals(indexes, command.getIndexes());
    }
    
    @Test
    public void testDone() {
        CommandParser parser = new CommandParser();
        Command command = parser.parse("done 1");
        
        ArrayList<Integer> indexes = new ArrayList<Integer>();
        indexes.add(0);
        assertEquals(Command.Type.DONE, command.getCommandType());
        assertEquals(indexes, command.getIndexes());
        
        command = parser.parse("done 1,3,5,7,9");
        indexes.clear();
        indexes.add(0);
        indexes.add(2);
        indexes.add(4);
        indexes.add(6);
        indexes.add(8);
        assertEquals(Command.Type.DONE, command.getCommandType());
        assertEquals(indexes, command.getIndexes());
        
        command = parser.parse("done 1-10");
        indexes.clear();
        indexes.add(0);
        indexes.add(1);
        indexes.add(2);
        indexes.add(3);
        indexes.add(4);
        indexes.add(5);
        indexes.add(6);
        indexes.add(7);
        indexes.add(8);
        indexes.add(9);
        assertEquals(indexes, command.getIndexes());
        
        command = parser.parse("done 1-3,4,5,6-9,10");
        assertEquals(indexes, command.getIndexes());
    }
}