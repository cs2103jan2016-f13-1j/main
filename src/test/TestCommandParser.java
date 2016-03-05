/**
 *
 */
package test;

/**
 * @author Joleen
 *
 */

import static org.junit.Assert.assertEquals;
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
        assertEquals("floating", command.getTab());
        
        command = parser.parse("Undo task 3");
        assertEquals("floating", command.getTab());
        
        command = parser.parse("Fetch my brothers from school");
        assertEquals("floating", command.getTab());
        
        command = parser.parse("Send 100 emails from my computer");
        assertEquals("floating", command.getTab());
        
        command = parser.parse("Drive by the supermarket");
        assertEquals("floating", command.getTab());
        
        command = parser.parse("Attack enemy base on signal");
        assertEquals("floating", command.getTab());
        
        command = parser.parse("Send 100 email before I sleep");
        assertEquals("floating", command.getTab());
    }
    
    @Test
    public void testAddFloating() {
        CommandParser parser = new CommandParser();
        Command command = parser.parse("Cook dinner");
        
        assertEquals("add", command.getCommandType());
        assertEquals("floating", command.getTab());
        assertEquals("Cook dinner", command.getTask().getTitle());
        
        command = parser.parse("Attack enemy base on signal");
        assertEquals("add", command.getCommandType());
        assertEquals("floating", command.getTab());
        assertEquals("Attack enemy base on signal", command.getTask().getTitle());
    }
    
    @Test
    public void testAdd() throws ParseException {
        CommandParser parser = new CommandParser();
        Command command = parser.parse("Cook dinner on 4 Mar 7pm");
        
        SimpleDateFormat df = new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy");
        String date = "Thu Mar 4 19:00:00 SGT 2016";
        Date expectedDate = df.parse(date);
        
        assertEquals("dated", command.getTab());
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

    	command = parser.parse("Attend meeting on Weds");
    	assertEquals("Attend meeting", command.getTask().getTitle());

    	command = parser.parse("Do homework by Sunday");
    	assertEquals("Do homework", command.getTask().getTitle());

    	command = parser.parse("Send 100 email before 8pm");
    	assertEquals("Send 100 email", command.getTask().getTitle());
    }
    
    @Test
    public void testDatedTaskTitleAgain(){
    	//more for residual date/time
    	CommandParser parser = new CommandParser();
    	Command command = parser.parse("Attend meeting from Monday to Wednesday 6pm");
    	assertEquals("Attend meeting", command.getTask().getTitle());
    	
    	command = parser.parse("Cook dinner at 7pm at home");
    	assertEquals("Cook dinner at home", command.getTask().getTitle());
    	
    	command = parser.parse("Cook dinner on 4 Mar 7pm");
    	assertEquals("Cook dinner", command.getTask().getTitle());
    	
    	command = parser.parse("Do assignment by Sunday midnight");
    	assertEquals("Do assignment", command.getTask().getTitle());

    	command = parser.parse("Send 100 email before sunday 7pm");
    	assertEquals("Send 100 email", command.getTask().getTitle());  	
    }
    
    @Test
    public void testDelete() {
        CommandParser parser = new CommandParser();
        Command command = parser.parse("delete 1");
        
        ArrayList<Integer> indexes = new ArrayList<Integer>();
        indexes.add(1);
        assertEquals("delete", command.getCommandType());
        assertEquals(indexes, command.getIndexes());
        
        command = parser.parse("del 1,3,5,7,9");
        indexes.clear();
        indexes.add(1);
        indexes.add(3);
        indexes.add(5);
        indexes.add(7);
        indexes.add(9);
        assertEquals("delete", command.getCommandType());
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
}