/** 
 * How to use the logic component:
 * 1. get an instance of invoker and receiver
 *      Invoker invoker = new Invoker();
        Receiver receiver = Receiver.getReceiver();
 * 2. create command objects with receiver and task as parameters
 *      Command add = new AddCommand(receiver, task);
 * 3. use the invoker to execute the command object
 *      invoker.execute(add);
 *      
 * Short form: invoker.execute(new AddCommand(receiver, task));
 * 
 * Observer pattern: example of update() can be found at the last method
 * 
 * Available commands:
 * AddCommand(Task task);
 * DeleteCommand(Task task);
 * DeleteCommand(ArrayList<Task> tasks);
 * EditCommand(Task oldTask, Task newTask);
 * DoneCommand(Task task);
 * DoneCommand(ArrayList<Task> tasks);
 * UndoneCommand(Task task);
 * UndoneCommand(ArrayList<Task> tasks);
 * SetFileLocationCommand(String newLocation);
 * 
 * To get the tasks before the observer pattern is up,
 * getTodoTasks();
 * getCompletedTasks();
 * 
 */

/**
 * @author Bevin Seetoh Jia Jin
 *
 */

package test;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EmptyStackException;
import java.util.Observable;
import java.util.Observer;

import org.junit.Before;
import org.junit.Test;

import main.data.Task;
import main.logic.AddCommand;
import main.logic.Command;
import main.logic.DeleteCommand;
import main.logic.DoneCommand;
import main.logic.EditCommand;
import main.logic.Invoker;
import main.logic.PriorityCommand;
import main.logic.Receiver;
import main.logic.SearchCommand;
import main.logic.SetFileLocationCommand;
import main.logic.UndoneCommand;
import main.parser.CommandParser;



public class TestLogic implements Observer {
	CommandParser parser;
	Receiver receiver;
	Invoker invoker;
	Observer observer;
	ArrayList<Task> todo = new ArrayList<Task>();
	ArrayList<Task> completed = new ArrayList<Task>();
	
	/*
     * Tests if getter methods are assigned 
     * when constructor is called.
     */
	@Test
	public void getMethodsTest() {
	    assertNotNull(receiver.getAllTasks());
	    assertNotNull(receiver.getTodoTasks());
	    assertNotNull(receiver.getCompletedTasks());
	}
	
	/*
     * Test if CloneNotSupportedException is thrown.
     * Singleton classes do not support cloning.
     */
	@Test
    public void receiverCloneTest() {
        try {
            receiver.clone();
        } catch (CloneNotSupportedException e) {
            assertNotNull(e);
        }
    }
	
	/*
	 * This is a boundary case for the undo stack
	 * Undo until stack is empty, then undo when stack is empty.
	 * Tests if the EmptyStackException is thrown.
	 */
	@Test
	public void emptyUndoStackTest() {
	    invoker.execute(new AddCommand(receiver, new Task("test task")));
	    try {
	        while (invoker.isUndoAvailable()) {
    	        invoker.undo();
	        }
	        assertFalse(invoker.isUndoAvailable());
	        invoker.undo();
	    } catch (EmptyStackException e) {
	        assertNotNull(e);
        }
	}
	
	/*
     * This is a boundary case for the redo stack
     * Redo until stack is empty, then redo when stack is empty.
     * Tests if the EmptyStackException is thrown.
     */
	@Test
    public void emptyRedoStackTest() {
	    Task task = new Task("test task");
	    invoker.execute(new AddCommand(receiver, task));
	    invoker.undo();
	    try {
            while (invoker.isRedoAvailable()) {
                invoker.redo();
            }
            assertFalse(invoker.isRedoAvailable());
            invoker.redo();
        } catch (EmptyStackException e) {
            assertNotNull(e);
        }
	    invoker.execute(new DeleteCommand(receiver, task));
    }
	
	/*
	 * Tests the basic commands that handles single tasks
	 * Initial state: empty storage file.
	 * Post state: empty storage file.
	 */
	@Test
	public void singleTaskTest() {
	    Task exampleTask = new Task("example");
        Task editedTask = new Task("edited task");
        
        invoker.execute(new AddCommand(receiver, exampleTask));
        assertEquals(exampleTask, todo.get(0));
        invoker.undo();
        assertTrue(todo.isEmpty());
        invoker.redo();
        assertEquals(exampleTask, todo.get(0));
        invoker.execute(new EditCommand(receiver, exampleTask, editedTask));
        assertEquals(editedTask, todo.get(0));
        invoker.undo();
        assertEquals(exampleTask, todo.get(0));
        invoker.redo();
        assertEquals(editedTask, todo.get(0));
        invoker.execute(new DoneCommand(receiver, editedTask));
        assertEquals(editedTask, completed.get(0));
        invoker.undo();
        assertEquals(editedTask, todo.get(0));
        invoker.redo();
        assertEquals(editedTask, completed.get(0));
        invoker.execute(new UndoneCommand(receiver, editedTask));
        assertEquals(editedTask, todo.get(0));
        invoker.undo();
        assertEquals(editedTask, completed.get(0));
        invoker.redo();
        assertEquals(editedTask, todo.get(0));
        invoker.execute(new DeleteCommand(receiver, editedTask));
        assertTrue(todo.isEmpty());
        invoker.undo();
        assertEquals(editedTask, todo.get(0));
        invoker.redo();  
        assertTrue(todo.isEmpty());
	}
	
	/*
     * Tests the basic commands that handles multiple tasks
     * Initial state: empty storage file.
     * Post state: empty storage file.
     */
	@Test
	public void multipleTasksTest() {
	    Task exampleTask = new Task("example");
        Task editedTask = new Task("edited task");
        ArrayList<Task> tasks = new ArrayList<Task>();
        tasks.add(exampleTask);
        tasks.add(editedTask);
        
        invoker.execute(new AddCommand(receiver, exampleTask));
        invoker.execute(new AddCommand(receiver, editedTask));
        assertTrue(todo.size() == 2);
        invoker.execute(new DoneCommand(receiver, tasks));
        assertTrue(completed.size() == 2);
        invoker.undo();
        assertTrue(todo.size() == 2);
        invoker.redo();
        assertTrue(completed.size() == 2);
        invoker.execute(new UndoneCommand(receiver, tasks));
        assertTrue(todo.size() == 2);
        invoker.undo();
        assertTrue(completed.size() == 2);
        invoker.redo();
        assertTrue(todo.size() == 2);
        invoker.execute(new DeleteCommand(receiver, tasks));
        assertTrue(todo.isEmpty());
        invoker.undo();
        assertTrue(todo.size() == 2);
        invoker.redo();
        assertTrue(todo.isEmpty());
	}
	
	/*
	 * Tests the search command with strings.
	 * Search with one term, multiple terms, and with tag.
	 */
	@Test
	public void searchStringTest() {
	    try {
    	    Task task1 = parser.parseAdd("a b c #f");
    	    Task task2 = parser.parseAdd("a b c d");
    	    Task task3 = parser.parseAdd("a b c d #e");
    	    ArrayList<Task> tasks = new ArrayList<Task>();
    	    tasks.add(task1);
    	    tasks.add(task2);
    	    tasks.add(task3);
    	    
    	    invoker.execute(new AddCommand(receiver, task1));
            invoker.execute(new AddCommand(receiver, task2));
            invoker.execute(new AddCommand(receiver, task3));
            assertTrue(todo.size() == 3);
            invoker.execute(new SearchCommand(receiver, "a b c"));
            assertTrue(todo.size() == 3);
            invoker.execute(new SearchCommand(receiver, "d"));
            assertTrue(todo.size() == 2);
            invoker.execute(new SearchCommand(receiver, "d #e"));
            assertTrue(todo.size() == 1);
            assertEquals(task3, todo.get(0));
            invoker.undo();
            assertTrue(todo.size() == 3);
            invoker.redo();
            assertTrue(todo.size() == 1);
            assertEquals(task3, todo.get(0));
            invoker.undo();
            assertTrue(todo.size() == 3);
            invoker.execute(new DeleteCommand(receiver,tasks));
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	/*
     * Tests the search command with date.
     */
    @Test
    public void searchDateTest() {
        try {
            Date today = new Date();
            Calendar calendar = Calendar.getInstance(); 
            calendar.setTime(today); 
            calendar.add(Calendar.DATE, 1);
            Date tomorrow = calendar.getTime();
            
            Task task1 = parser.parseAdd("task1 by today");
            Task task2 = parser.parseAdd("task2 by 11.59pm");
            Task task3 = parser.parseAdd("task3 by tomorrow");
            ArrayList<Task> tasks = new ArrayList<Task>();
            tasks.add(task1);
            tasks.add(task2);
            tasks.add(task3);
            
            invoker.execute(new AddCommand(receiver, task1));
            invoker.execute(new AddCommand(receiver, task2));
            invoker.execute(new AddCommand(receiver, task3));
            assertTrue(todo.size() == 3);
            invoker.execute(new SearchCommand(receiver, today));
            assertTrue(todo.size() == 2);
            invoker.execute(new SearchCommand(receiver, tomorrow));
            assertTrue(todo.size() == 1);
            assertEquals(task3, todo.get(0));
            invoker.execute(new DeleteCommand(receiver,tasks));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	/*
	 * Tests the priority command
	 * Cycle through priority 0 to 3, then undo and redo
	 * Ensures boundary 0 and 3 not exceeded
	 */
	@Test
	public void priorityTest() {
	    Task task = new Task("test");
	    invoker.execute(new AddCommand(receiver, task));
	    assertEquals(0, task.getPriority());
	    invoker.execute(new PriorityCommand(receiver, task));
	    assertEquals(1, task.getPriority());
	    invoker.execute(new PriorityCommand(receiver, task));
	    assertEquals(2, task.getPriority());
        invoker.execute(new PriorityCommand(receiver, task));
        assertEquals(3, task.getPriority());
        invoker.execute(new PriorityCommand(receiver, task));
        assertEquals(0, task.getPriority());
        invoker.undo();
        assertEquals(3, task.getPriority());
        invoker.undo();
        assertEquals(2, task.getPriority());
        invoker.undo();
        assertEquals(1, task.getPriority());
        invoker.undo();
        assertEquals(0, task.getPriority());
        invoker.redo();
        assertEquals(1, task.getPriority());
        invoker.redo();
        assertEquals(2, task.getPriority());
        invoker.redo();
        assertEquals(3, task.getPriority());
        invoker.redo();
        assertEquals(0, task.getPriority());
        invoker.execute(new DeleteCommand(receiver, task));
	}
	
	/*
	 * Adds a mixture of all possible cases to test comparator
	 */
	@Test
	public void comparatorTest() {
	    try {
	        invoker.execute(new AddCommand(receiver, parser.parseAdd("floating")));
    	    invoker.execute(new DoneCommand(receiver, todo));
    	    invoker.execute(new AddCommand(receiver, parser.parseAdd("start date at 11am")));
    	    invoker.execute(new AddCommand(receiver, parser.parseAdd("end date by 1")));
    	    invoker.execute(new AddCommand(receiver, parser.parseAdd("floating")));
    	    invoker.execute(new AddCommand(receiver, parser.parseAdd("range date from 1am to 11pm today")));
    	    invoker.execute(new AddCommand(receiver, parser.parseAdd("range date from 11am to 10pm")));
    	    invoker.execute(new DoneCommand(receiver, todo.get(0)));
    	    while (!todo.isEmpty()) {
    	        invoker.execute(new DeleteCommand(receiver, todo.get(0)));
    	    }
    	    while (!completed.isEmpty()) {
                invoker.execute(new DeleteCommand(receiver, completed.get(0)));
            }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	/*
	 * Tests set location command with undo and redo
	 */
	@Test
	public void setFilePathTest() {
	    String originalPath = receiver.getFilePath();
	    String test = "test.txt";
	    File file = new File(test);
	    
	    Command setLocation = new SetFileLocationCommand(receiver, test);
	    invoker.execute(setLocation);
	    assertTrue(file.exists());
	    invoker.undo();
	    assertEquals(receiver.getFilePath(), originalPath);
	    invoker.redo();
	    assertEquals(receiver.getFilePath(), test);
	    invoker.undo();
	    assertEquals(receiver.getFilePath(), originalPath);
	    try {
	        file.delete();
	    } catch (Exception e) {
	        System.out.println("Failed to delete test.txt file");
	        e.printStackTrace();
	    }
	}
	
	@Before
	public void initialize() {
	    parser = new CommandParser();
	    invoker = new Invoker();
        receiver = Receiver.getInstance();
        receiver.addObserver(this);
	}
	
	/*
	 * Tests observer pattern update method
	 */
    @Override
    public void update(Observable o, Object arg) {
        if (o == receiver) {
            todo = receiver.getTodoTasks();
            completed = receiver.getCompletedTasks();
            assertNotNull(todo);
            assertNotNull(completed);
        }
    }
}
