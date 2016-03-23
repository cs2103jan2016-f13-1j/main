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
     * Test if cloning is prevented.
     * Singleton classes do not support cloning.
     */
	@Test
    public void receiverCloneTest() {
        try {
            receiver.clone();
        } catch (CloneNotSupportedException e) {
        }
    }
	
	/*
	 * This is a boundary case for the undo stack
	 * Undo until stack is empty.
	 * Undo when stack is empty.
	 */
	@Test
	public void emptyUndoStackTest() {
	    try {
	        while (invoker.isUndoAvailable()) {
    	        invoker.undo();
	        }
	        invoker.undo();
	    } catch (EmptyStackException e) {
        }
	}
	
	/*
     * This is a boundary case for the redo stack
     * Redo until stack is empty.
     * Redo when stack is empty.
     */
	@Test
    public void emptyRedoStackTest() {
	    try {
            while (invoker.isRedoAvailable()) {
                invoker.redo();
            }
            invoker.redo();
        } catch (EmptyStackException e) {
        }
    }
	
	/*
	 * Tests the basic commands with one task
	 */
	@Test
	public void singleTaskTest() {
	    Task exampleTask = new Task("example");
        Task editedTask = new Task("edited task");

        invoker.execute(new AddCommand(receiver, exampleTask));
        invoker.undo();
        invoker.redo();
        invoker.execute(new EditCommand(receiver, exampleTask, editedTask));
        invoker.undo();
        invoker.redo();
        invoker.execute(new DoneCommand(receiver, editedTask));
        invoker.undo();
        invoker.redo();
        invoker.execute(new UndoneCommand(receiver, editedTask));
        invoker.undo();
        invoker.redo();
        invoker.execute(new DeleteCommand(receiver, editedTask));
        invoker.undo();
        invoker.redo();    
	}
	
	/*
     * Tests the basic commands with multiple tasks
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
        invoker.execute(new DoneCommand(receiver, tasks));
        invoker.undo();
        invoker.redo();
        invoker.execute(new UndoneCommand(receiver, tasks));
        invoker.undo();
        invoker.redo();
        invoker.execute(new DeleteCommand(receiver, tasks));
        invoker.undo();
        invoker.redo();
	}
	
	/*
	 * Tests the search method.
	 * Search with one term.
	 * Search with multiple term.
	 */
	@Test
	public void searchTest() {
	    invoker.execute(new AddCommand(receiver, new Task("task with long title, this title so long")));
        invoker.execute(new AddCommand(receiver, new Task("task with short title")));
        invoker.execute(new SearchCommand(receiver, "title"));
        invoker.execute(new SearchCommand(receiver, "title long"));
        invoker.undo();
	}
	
	/*
	 * Adds a mixture of all possible cases to test comparator
	 */
	@Test
	public void comparatorTest() {
	    invoker.execute(new AddCommand(receiver, parser.parseAdd("a")));
	    invoker.execute(new DoneCommand(receiver, todo));
	    invoker.execute(new AddCommand(receiver, parser.parseAdd("b")));
	    invoker.execute(new AddCommand(receiver, parser.parseAdd("c by 1")));
	    invoker.execute(new AddCommand(receiver, parser.parseAdd("d")));
	    invoker.execute(new AddCommand(receiver, parser.parseAdd("e by 1")));
	    invoker.execute(new AddCommand(receiver, parser.parseAdd("f by 5")));
	    invoker.execute(new AddCommand(receiver, parser.parseAdd("g from 2-3")));
	    invoker.execute(new AddCommand(receiver, parser.parseAdd("h from 2-3")));
	    invoker.execute(new AddCommand(receiver, parser.parseAdd("i from 9pm-10pm")));
        invoker.execute(new AddCommand(receiver, parser.parseAdd("j from 9pm-11pm")));
	    invoker.execute(new AddCommand(receiver, parser.parseAdd("k from 1-3")));
        invoker.execute(new AddCommand(receiver, parser.parseAdd("l from 7pm-12am")));
	    invoker.execute(new AddCommand(receiver, parser.parseAdd("m at 4")));
	    invoker.execute(new AddCommand(receiver, parser.parseAdd("n at 4")));
	    invoker.execute(new AddCommand(receiver, parser.parseAdd("o at 3")));
	    invoker.execute(new DoneCommand(receiver, todo));
	    while (!todo.isEmpty()) {
	        invoker.execute(new DeleteCommand(receiver, todo.get(0)));
	    }
	    while (!completed.isEmpty()) {
            invoker.execute(new DeleteCommand(receiver, completed.get(0)));
        }
	}
	
	/*
	 * Tests set location method with undo and redo
	 */
	@Test
	public void setFilePathTest() {
	    Command setLocation = new SetFileLocationCommand(receiver, "test.txt");
	    invoker.execute(setLocation);
	    invoker.undo();
	    invoker.redo();
	    invoker.undo();
	    try {
	        File file = new File("test.txt");
	        file.delete();
	    } catch (Exception e) {
	        System.out.println("Failed to delete test.txt file");
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
        }
    }
}
