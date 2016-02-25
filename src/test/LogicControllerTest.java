package test;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import main.data.Task;
import main.logic.Controller;

public class LogicControllerTest {
	
	Controller controller = null;
	
	//Adds a floating task
	@Test
	public void addTaskTest() {
		Task task = new Task();
		task.setTitle("Floating task 6");
		controller.addTask(Controller.FLOATING,task);
		assertEquals(controller.getFloatingTasks().size(),controller.getStorage().readTasks().get(0).size());	
	}

	//Deletes last task
	@Test
	public void deleteTaskTest() {
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		indexes.add(controller.getFloatingTasks().size()-1);
		controller.deleteTask("floating",indexes);
		assertEquals(controller.getFloatingTasks().size(),controller.getStorage().readTasks().get(0).size());
	}
	
	//Edits the first floating task
	@Test
	public void editTaskTest() {
		Task task = new Task();
		task.setTitle("Floating task 1.1");
		controller.editTask(Controller.FLOATING,1,task);
		assertEquals("Floating task 1.1", controller.getFloatingTasks().get(0).getTitle());
		task.setTitle("Floating task 1");
		controller.editTask(Controller.FLOATING,1,task);
		assertEquals("Floating task 1", controller.getFloatingTasks().get(0).getTitle());
	}
	
	//Retrieves tasks for next seven days
	//@Test
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
	
	@Before
	public void initialize() {
		controller = new Controller();
	}
}
