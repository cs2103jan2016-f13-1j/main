package test;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import main.data.Command;
import main.parser.CommandParser;

/**
 * @author Joleen
 *
 */

public class TestCommandParser {
    
    @Test
    public void testAddFloating() {
        CommandParser parser = new CommandParser();
        Command command = parser.parse("Cook dinner");
        
        assertEquals("add", command.getCommandType());
        assertEquals("floating", command.getTab());
        assertEquals("Cook dinner", command.getTask().getTitle());
    }
    
    @Test
    public void testAdd() throws ParseException {
        CommandParser parser = new CommandParser();
        Command command = parser.parse("Cook dinner by 7pm");
        
        SimpleDateFormat df = new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy");
        String date = "Thu Feb 26 19:00:00 SGT 2016";
        Date expectedDate = df.parse(date);
        
        assertEquals("dated", command.getTab());
        assertEquals(expectedDate, command.getTask().getEndDate());
        
        //testing pretty on latest
        command = parser.parse("attend meeting from 14 - 16");
        System.out.println(command.getTask().getStartDate());
        System.out.println(command.getTask().getEndDate());
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
    public void testDelete() {
        CommandParser parser = new CommandParser();
        Command command = parser.parse("delete 1");
        
        ArrayList<Integer> indexesToDelete= new ArrayList<Integer>();
        indexesToDelete.add(1);
        assertEquals("delete", command.getCommandType());
        assertEquals(indexesToDelete, command.getIndexes());
        
        command = parser.parse("del 1,3,5,7,9");
        indexesToDelete.clear();
        indexesToDelete.add(1);
        indexesToDelete.add(3);
        indexesToDelete.add(5);
        indexesToDelete.add(7);
        indexesToDelete.add(9);
        assertEquals("delete", command.getCommandType());
        assertEquals(indexesToDelete, command.getIndexes());
        
        command = parser.parse("delete 1-10");
        indexesToDelete.clear();
        indexesToDelete.add(1);
        indexesToDelete.add(2);
        indexesToDelete.add(3);
        indexesToDelete.add(4);
        indexesToDelete.add(5);
        indexesToDelete.add(6);
        indexesToDelete.add(7);
        indexesToDelete.add(8);
        indexesToDelete.add(9);
        indexesToDelete.add(10);
        assertEquals(indexesToDelete, command.getIndexes());
        
        command = parser.parse("delete 1-3,4,5,6-9,10");
        assertEquals(indexesToDelete, command.getIndexes());
    }
}