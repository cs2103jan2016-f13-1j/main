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
	@Test
	public void editTaskTest() {
		String title = "jUnit edit task";
		String feedback = logic.parseCommand(title, Logic.NO_TAB);
		assertEquals(feedback,title);
		logic.editTask(Logic.FLOATING_TAB, 0);
		assertEquals(title,logic.getFloatingTasks().get(0).getTitle());
		
		//title = "Today task 1.1";
		//feedback = logic.parseCommand(title, logic.Tab.NO_TAB);
		//assertEquals(feedback,title);
		//logic.editTask(logic.TODAY, 1);
		//assertEquals(title,logic.getTodayTasks().get(0).getTitle());
	}
	
	//Retrieves tasks for next seven days
	@Test
	public void getNextSevenDaysTest() {
		for (Task t : logic.getNextSevenDays()) {
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
		
		feedback = logic.parseCommand("cook dinner #home", Logic.NO_TAB);
		assertEquals(feedback,"cook dinner #home");
		logic.executeCommand();
		logic.executeCommand();
		
		feedback = logic.parseCommand("cook dinner", Logic.NO_TAB);
		assertEquals(feedback,"cook dinner");
		logic.executeCommand();
		
		//delete last 3 floating tasks
		lastIndex = logic.getFloatingTasks().size() - 1;
		feedback = logic.parseCommand("delete " + lastIndex, Logic.FLOATING_TAB);
        logic.executeCommand();
		
        lastIndex = logic.getFloatingTasks().size() - 1;
		feedback = logic.parseCommand("delete " + (lastIndex-1) + "-" + lastIndex, Logic.FLOATING_TAB);
		logic.executeCommand();
		
		//feedback = logic.parseCommand("delete 1", logic.Tab.TODAY);
		//assertEquals(feedback,"delete from today");
		//logic.executeCommand();
	}
	
	@Test
	public void undoAndRedoTest() {
	    int lastIndex = -1;
	    
	    //undo and redo an add operation
	    logic.parseCommand("undo task", Logic.FLOATING_TAB);
        logic.executeCommand();
        logic.undo();
        logic.redo();
        
        
        //undo a delete operation of one task
        lastIndex = logic.getFloatingTasks().size() - 1;
        logic.parseCommand("delete " + lastIndex, Logic.FLOATING_TAB);
        logic.executeCommand();
        logic.undo();
        logic.redo();
        
        //undo a delete operation of multiple task  
        logic.parseCommand("undo task", Logic.FLOATING_TAB);
        logic.executeCommand();
        logic.executeCommand();
        
        int secondLastIndex = logic.getFloatingTasks().size() - 2;
        lastIndex = logic.getFloatingTasks().size() - 1;
        
        logic.parseCommand("delete " + secondLastIndex + "-" + lastIndex, Logic.FLOATING_TAB);
        logic.executeCommand();
        logic.undo();
        logic.redo();
        
        //undo and redo an edit task
        String title = "Floating task 0.1";
        logic.parseCommand(title, Logic.NO_TAB);
        logic.editTask(Logic.FLOATING_TAB, 0);
        logic.undo();
        logic.redo();
	}
	
	@Before
	public void initialize() {
		logic = Logic.getLogic();
	}
}
