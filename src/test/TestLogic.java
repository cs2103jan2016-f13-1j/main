/**
 * 
 */
package test;

/**
 * @author Bevin
 *
 */
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import main.data.Task;
import main.logic.Logic;



public class TestLogic {
	
	Logic logic = null;
	
	//Edits task from different tabs
	//@Test
	public void editTaskTest() {
		String title = "jUnit edit task";
		String feedback = logic.parseCommand(title, Logic.List.ALL);
		logic.executeCommand();
		assertEquals(feedback,title);
		logic.editTask(Logic.List.ALL, 0);
		assertEquals(title,logic.getFloatingTasks().get(0).getTitle());
		logic.parseCommand("delete 1", Logic.List.ALL);
		logic.executeCommand();
	}
	
	//Retrieves tasks for next seven days
	@Test
	public void getNextSevenDaysTest() {
		for (Task t : logic.getThisWeek()) {
			System.out.println(t.getTitle());
		}
	}
	
	//Retrieves today's tasks
	@Test
	public void getTodayTasksTest() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");
		String today = dateFormat.format(new Date());
		
		for (Task t : logic.getTodayTasks()) {
			assertTrue(dateFormat.format(t.getEndDate()).equals(today));
		}
	}
	

	//Test parse add and delete. Adds 3 cook dinner tasks and deletes them
	@Test
	public void parseAddAndDeleteTaskTest() {
		String feedback = null;
		int lastIndex = -1;
		
		feedback = logic.parseCommand("cook dinner #home", Logic.List.ALL);
		assertEquals(feedback,"cook dinner #home");
		logic.executeCommand();
		logic.executeCommand();
		
		feedback = logic.parseCommand("cook dinner", Logic.List.ALL);
		assertEquals(feedback,"cook dinner");
		logic.executeCommand();
		
		//delete last 3 floating tasks
		lastIndex = logic.getAllTasks().size();
		feedback = logic.parseCommand("delete " + lastIndex, Logic.List.ALL);
        logic.executeCommand();
		
        lastIndex = logic.getAllTasks().size();
		feedback = logic.parseCommand("delete " + (lastIndex-1) + "-" + lastIndex, Logic.List.ALL);
		logic.executeCommand();
		
		//feedback = logic.parseCommand("delete 1", logic.Tab.TODAY);
		//assertEquals(feedback,"delete from today");
		//logic.executeCommand();
	}
	
	@Test
	public void undoAndRedoTest() {
	    int lastIndex = -1;
	    
	    //undo and redo an add operation
	    logic.parseCommand("undo task", Logic.List.ALL);
        logic.executeCommand();
        logic.undo();
        logic.redo();
        
        
        //undo a delete operation of one task
        lastIndex = logic.getFloatingTasks().size();
        logic.parseCommand("delete " + lastIndex, Logic.List.ALL);
        logic.executeCommand();
        logic.undo();
        logic.redo();
        
        //undo a delete operation of multiple task  
        logic.parseCommand("undo task", Logic.List.ALL);
        logic.executeCommand();
        logic.executeCommand();
        
        int secondLastIndex = logic.getFloatingTasks().size() - 1;
        lastIndex = logic.getFloatingTasks().size();
        
        logic.parseCommand("delete " + secondLastIndex + "-" + lastIndex, Logic.List.ALL);
        logic.executeCommand();
        logic.undo();
        logic.redo();
        
        //undo and redo an edit task
        String title = "Floating task 0.1";
        logic.parseCommand(title, Logic.List.ALL);
        logic.executeCommand();
        logic.editTask(Logic.List.ALL, 0);
        logic.undo();
        logic.redo();
        logic.parseCommand("delete 1", Logic.List.ALL);
        logic.executeCommand();
	}
	
	@Test
	public void markTaskTest() {
        logic.parseCommand("Floating task 0.1", Logic.List.ALL);
        logic.executeCommand();
        logic.parseCommand("done 1", Logic.List.ALL);
        logic.executeCommand();
        logic.undo();
        logic.redo();
        logic.undo();
        logic.parseCommand("delete 1", Logic.List.ALL);
        logic.executeCommand();
	}
	
	@Test
	public void setFilePathTest() {
	    logic.setFileLocation("invalid\\path");
	}
	
	@Before
	public void initialize() {
		logic = Logic.getLogic();
	}
}
