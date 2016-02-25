package test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import main.data.Task;
import main.logic.Controller;

public class LogicControllerTest {
	
	Controller controller = null;
	
	@Before
	public void initialize() {
		controller = new Controller();
	}

	//Adds a floating task
	@Test
	public void addTaskTest() {
		Task task = new Task();
		task.setTitle("Floating task 6");
		controller.addTask("floating",task);
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
		controller.editTask("floating",1,task);
		assertEquals("Floating task 1.1", controller.getAllTasks().get(0).getTitle());
	}
	
	//Retrieves today's tasks
	@Test
	public void getTodayTest() {
		for (Task t : controller.getTodayTasks()) {
			System.out.println(t.getTitle());
		}
	}
	
	//Retrieves tasks for next seven days
	@Test
	public void getNextSevenDaysTest() {
		for (Task t : controller.getNextSevenDays()) {
			System.out.println(t.getTitle());
		}
	}
}
