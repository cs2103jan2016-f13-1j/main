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
	
	@Test
	public void getMethodsTest() {
	    assertNotNull(receiver.getAllTasks());
	    assertNotNull(receiver.getTodoTasks());
	    assertNotNull(receiver.getCompletedTasks());
	}
	
	@Test
    public void receiverCloneTest() {
        try {
            receiver.clone();
        } catch (CloneNotSupportedException e) {
        }
    }
	
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
	
	@Test
	public void allFunctionsTest() {
	    Task task = new Task("example");
        Task task1 = new Task("new task");
        ArrayList<Task> tasks = new ArrayList<Task>();
        tasks.add(task);
        tasks.add(task1);
        
        Command add = new AddCommand(receiver, task);
        Command add1 = new AddCommand(receiver, task1);
        Command edit = new EditCommand(receiver, task, task1);
        Command delete = new DeleteCommand(receiver, task1);
        Command deleteMultiple = new DeleteCommand(receiver, tasks);
        Command done = new DoneCommand(receiver, task1);
        Command doneMultiple = new DoneCommand(receiver, tasks);
        Command undone = new UndoneCommand(receiver, task1);
        Command undoneMultiple = new UndoneCommand(receiver, tasks);

        invoker.execute(add);
        invoker.undo();
        invoker.redo();
        invoker.execute(edit);
        invoker.undo();
        invoker.redo();
        invoker.execute(done);
        invoker.undo();
        invoker.redo();
        invoker.execute(undone);
        invoker.undo();
        invoker.redo();
        invoker.execute(delete);
        invoker.undo();
        invoker.redo();
        invoker.execute(add);
        invoker.execute(add1);
        invoker.execute(doneMultiple);
        invoker.undo();
        invoker.redo();
        invoker.execute(undoneMultiple);
        invoker.undo();
        invoker.redo();
        invoker.execute(deleteMultiple);
        invoker.undo();
        invoker.redo();
	}
	
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
	    //invoker.execute(new AddCommand(receiver, parser.parseAdd("a from 1-12")));
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
	    try {
            File file = new File("storage.txt");
            file.delete();
        } catch (Exception e) {
            System.out.println("Failed to delete storage.txt file");
        }
	    Command setCorrupted = new SetFileLocationCommand(receiver, "?");
        invoker.execute(setCorrupted);
	}
	
	@Before
	public void initialize() {
	    parser = new CommandParser();
	    invoker = new Invoker();
        receiver = Receiver.getReceiver();
        receiver.addObserver(this);
	}

    @Override
    public void update(Observable o, Object arg) {
        if (o == receiver) {
            todo = receiver.getTodoTasks();
            completed = receiver.getCompletedTasks();
        }
    }
}
