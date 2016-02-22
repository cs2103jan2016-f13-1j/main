package test;

import static org.junit.Assert.*;
import main.data.Task;
import main.logic.Controller;

import org.junit.Before;
import org.junit.Test;

public class LogicControllerTest {
	
	Controller controller = null;
	
	@Before
	public void initialize() {
		controller = new Controller();
	}
	
	@Test
	public void constructorTest() {
		assertNotEquals(0,controller.getTasks().size());
	}
	
	@Test
	public void addTaskTest() {
		Task task = new Task();
		task.setTitle("sweep the floor");
		controller.addTask(task);
		assertEquals(controller.getTasks().size(),controller.getStorage().readTasks().size());
	}
	
	@Test
	public void deleteTaskTest() {
		controller.deleteTask(controller.getTasks().size()-1);
		assertEquals(controller.getTasks().size(),controller.getStorage().readTasks().size());
	}
	
	@Test
	public void editTaskTest() {
		Task task = new Task();
		task.setTitle("mop the floor");
		controller.editTask(0,task);
		assertEquals("mop the floor", controller.getTasks().get(0).getTitle());
	}
	
}
