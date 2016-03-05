/**
 * 
 */
package test;

/**
 * @author Bevin
 *
 */

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import main.data.Task;
import main.logic.Controller;



public class TestLogicController {
	
	Controller controller = null;
	
	//Edits task from different tabs
	@Test
	public void editTaskTest() {
		String title = "Floating task 0.1";
		String feedback = controller.parseCommand(title, Controller.NO_TAB);
		assertEquals(feedback,title);
		controller.editTask(Controller.FLOATING_TAB, 0);
		assertEquals(title,controller.getFloatingTasks().get(0).getTitle());
		
		//title = "Today task 1.1";
		//feedback = controller.parseCommand(title, Controller.Tab.NO_TAB);
		//assertEquals(feedback,title);
		//controller.editTask(Controller.TODAY, 1);
		//assertEquals(title,controller.getTodayTasks().get(0).getTitle());
	}
	
	//Retrieves tasks for next seven days
	@Test
	public void getNextSevenDaysTest() {
		for (Task t : controller.getNextSevenDays()) {
			System.out.println(t.getTitle());
		}
	}
	
	//Retrieves today's tasks
	@Test
	public void getTodayTasksTest() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");
		String today = dateFormat.format(new Date());
		
		for (Task t : controller.getTodayTasks()) {
			assertTrue(dateFormat.format(t.getEndDate()).equals(today));
		}
	}
	

	//Test parse add and delete. Adds 3 cook dinner tasks and deletes them
	@Test
	public void parseAddAndDeleteTaskTest() {
		String feedback = null;
		feedback = controller.parseCommand("cook dinner #home", Controller.NO_TAB);
		assertEquals(feedback,"cook dinner #home");
		controller.executeCommand();
		controller.executeCommand();
		
		feedback = controller.parseCommand("cook dinner", Controller.NO_TAB);
		assertEquals(feedback,"cook dinner");
		controller.executeCommand();
		
		feedback = controller.parseCommand("delete 5,6-7", Controller.FLOATING_TAB);
		assertEquals(feedback,"delete from all");
		controller.executeCommand();
		
		//feedback = controller.parseCommand("delete 1", Controller.Tab.TODAY);
		//assertEquals(feedback,"delete from today");
		//controller.executeCommand();
	}
	
	@Before
	public void initialize() {
		controller = new Controller();
	}
}
