/**
 * 
 */
package test;

/**
 * @author Bevin
 *
 */
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import main.data.Task;
import main.logic.Logic;



public class TestLogic {
	
	Logic logic = null;
	
	/*
	 * Tests add a task, undo, then redo
	 * Tests edit a task, undo, then redo
	 * Tests delete a task, undo, then redo
	 * Tests delete multiple tasks, undo, then redo
	 */
	@Test
	public void allFunctionsTest() {
	    String feedback = null;
	    
	    //Add task
	    feedback = logic.parseCommand("a", Logic.ListType.ALL);
	    assertEquals(feedback, "a");
	    logic.executeCommand();
	    logic.undo();
	    logic.redo();
	    
	    //Edit task
	    feedback = logic.parseCommand("b", Logic.ListType.ALL);
        assertEquals(feedback, "b");
        logic.editTask(logic.getAllTasks().get(0)); //edit to b
        logic.undo();  //edit to a
        logic.redo();  //edit to b
        
        //Delete a task
        feedback = logic.parseCommand("del 1", Logic.ListType.ALL);
        assertEquals(feedback, "1");
        logic.executeCommand(logic.getAllTasks().get(0)); //delete b
        logic.undo();
        logic.redo();
        
        ArrayList<Task> tasks = null;
        ArrayList<Task> list = null;
        
        //Delete multiple task
        logic.parseCommand("c multiple task", Logic.ListType.ALL);
        logic.executeCommand();
        logic.parseCommand("b multiple task", Logic.ListType.ALL);
        logic.executeCommand();
        logic.parseCommand("a multiple task", Logic.ListType.ALL);
        logic.executeCommand();
        feedback = logic.parseCommand("del 1-2,3", Logic.ListType.ALL);
        assertEquals(feedback, "1 2 3");
        tasks = new ArrayList<Task>();
        list = logic.getAllTasks();
        for (String s : feedback.split(" ")) {
            int i = Integer.parseInt(s);
            tasks.add(list.get(i - 1));
        }
        logic.executeCommand(tasks);
        logic.undo();
        logic.redo();
        
        //Sorting test
        feedback = logic.parseCommand("c multiple task by 8", Logic.ListType.ALL);
        logic.executeCommand();
        feedback = logic.parseCommand("a multiple task by 10", Logic.ListType.ALL);
        logic.executeCommand();
        feedback = logic.parseCommand("b multiple task by 8", Logic.ListType.ALL);
        logic.executeCommand();
        feedback = logic.parseCommand("del 1-2,3", Logic.ListType.ALL);
        assertEquals(feedback, "1 2 3");
        tasks = new ArrayList<Task>();
        list = logic.getAllTasks();
        for (String s : feedback.split(" ")) {
            int i = Integer.parseInt(s);
            tasks.add(list.get(i - 1));       
        }
        logic.executeCommand(tasks);
	}
	
	/*
     * Tests mark a task, undo, then redo
     * Tests mark multiple tasks, undo, then redo
     */
	@Test
	public void markTaskTest() {
	    String feedback = null;
	    
	    //Mark a task
	    logic.parseCommand("mark task", Logic.ListType.ALL);
        logic.executeCommand();
        feedback = logic.parseCommand("done 1", Logic.ListType.ALL);
        assertEquals(feedback, "1");
        logic.executeCommand(logic.getAllTasks().get(0));
        logic.undo();
        logic.redo();
        feedback = logic.parseCommand("del 1", Logic.ListType.COMPLETED);
        assertEquals(feedback, "1");
        logic.executeCommand(logic.getCompletedTasks().get(0));
        
        ArrayList<Task> tasks = null;
        ArrayList<Task> list = null;
        
        //Mark multiple tasks
        logic.parseCommand("mark task 1", Logic.ListType.ALL);
        logic.executeCommand();
        logic.parseCommand("mark task 2", Logic.ListType.ALL);
        logic.executeCommand();
        logic.parseCommand("mark task 3", Logic.ListType.ALL);
        logic.executeCommand();
        feedback = logic.parseCommand("done 1-2,3", Logic.ListType.ALL);
        assertEquals(feedback, "1 2 3");
        tasks = new ArrayList<Task>();
        list = logic.getAllTasks();
        for (String s : feedback.split(" ")) {
            int i = Integer.parseInt(s);
            tasks.add(list.get(i - 1));       
        }
        logic.executeCommand(tasks);
        logic.undo();
        logic.redo();
        feedback = logic.parseCommand("delete 1-2,3", Logic.ListType.COMPLETED);
        assertEquals(feedback, "1 2 3");
        tasks = new ArrayList<Task>();
        list = logic.getCompletedTasks();
        for (String s : feedback.split(" ")) {
            int i = Integer.parseInt(s);
            tasks.add(list.get(i - 1));       
        }
        logic.executeCommand(tasks);
	}
	
	//@Test
	public void setFilePathTest() {
	    logic.setFileLocation("invalid$path");
	}
	
	@Before
	public void initialize() {
		logic = Logic.getLogic();
	}
}
