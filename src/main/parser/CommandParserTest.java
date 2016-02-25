import static org.junit.Assert.*;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;


/**
 * @author Joleeen
 *
 */

public class CommandParserTest {
    
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
        Command command = parser.parse("Cook dinner at 7pm");
        
        SimpleDateFormat df = new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy");
        String date = "Thu Feb 25 19:00:00 SGT 2016";
        Date expectedDate = df.parse(date);
        
        assertEquals("dated", command.getTab());
        assertEquals(expectedDate, command.getTask().getStartDate());
    }
    
    @Test
    public void testLabels() {
        CommandParser parser = new CommandParser();
        Command command = parser.parse("Cook dinner #home, personal");
        
        ArrayList<String> labels = new ArrayList<String>();
        labels.add("home");
        labels.add("personal");
        
        assertEquals("Cook dinner", command.getTask().getTitle());
        assertEquals(labels, command.getTask().getLabels());
    }
    
    @Test
    public void testDelete() {
        CommandParser parser = new CommandParser();
        Command command = parser.parse("delete 1");
        
        ArrayList<Integer> deleteIndexes = new ArrayList<Integer>();
        deleteIndexes.add(1);
        assertEquals("delete", command.getCommandType());
        assertEquals(deleteIndexes, command.getIndexes());
        
        command = parser.parse("del 1,3,5,7,9");
        deleteIndexes.clear();
        deleteIndexes.add(1);
        deleteIndexes.add(3);
        deleteIndexes.add(5);
        deleteIndexes.add(7);
        deleteIndexes.add(9);
        assertEquals("delete", command.getCommandType());
        assertEquals(deleteIndexes, command.getIndexes());
        
        command = parser.parse("delete 1-5");
        deleteIndexes.clear();
        deleteIndexes.add(1);
        deleteIndexes.add(2);
        deleteIndexes.add(3);
        deleteIndexes.add(4);
        deleteIndexes.add(5);
        assertEquals(deleteIndexes, command.getIndexes());
    }
}