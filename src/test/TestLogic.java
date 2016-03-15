/**
 * 
 */
package test;

/**
 * @author Bevin
 *
 */
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

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
	    feedback = logic.parseCommand("first test task", Logic.ListType.ALL);
	    assertEquals(feedback, "first test task");
	    logic.executeCommand();
	    logic.undo();
	    logic.redo();
	    
	    //Edit task
	    feedback = logic.parseCommand("edited task", Logic.ListType.ALL);
        assertEquals(feedback, "edited task");
        logic.editTask(Logic.ListType.ALL, 1);
        logic.undo();
        logic.redo();
        
        //Delete a task
        feedback = logic.parseCommand("del 1", Logic.ListType.ALL);
        assertEquals(feedback, "edited task");
        logic.executeCommand();
        logic.undo();
        logic.redo();
        
        //Delete multiple task
        logic.parseCommand("c multiple task", Logic.ListType.ALL);
        logic.executeCommand();
        logic.parseCommand("b multiple task", Logic.ListType.ALL);
        logic.executeCommand();
        logic.parseCommand("a multiple task", Logic.ListType.ALL);
        logic.executeCommand();
        feedback = logic.parseCommand("del 1-2,3", Logic.ListType.ALL);
        assertEquals(feedback, "1-2,3 (3 tasks)");
        logic.executeCommand();
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
        assertEquals(feedback, "1-2,3 (3 tasks)");
        logic.executeCommand();
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
        assertEquals(feedback, "mark task");
        logic.executeCommand();
        logic.undo();
        logic.redo();
        feedback = logic.parseCommand("del 1", Logic.ListType.COMPLETED);
        assertEquals(feedback, "mark task");
        logic.executeCommand();
        
        //Mark multiple tasks
        logic.parseCommand("mark task 1", Logic.ListType.ALL);
        logic.executeCommand();
        logic.parseCommand("mark task 2", Logic.ListType.ALL);
        logic.executeCommand();
        logic.parseCommand("mark task 3", Logic.ListType.ALL);
        logic.executeCommand();
        feedback = logic.parseCommand("done 1-2,3", Logic.ListType.ALL);
        assertEquals(feedback, "1-2,3 (3 tasks)");
        logic.executeCommand();
        logic.undo();
        logic.redo();
        feedback = logic.parseCommand("delete 1-2,3", Logic.ListType.COMPLETED);
        assertEquals(feedback, "1-2,3 (3 tasks)");
        logic.executeCommand();
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
