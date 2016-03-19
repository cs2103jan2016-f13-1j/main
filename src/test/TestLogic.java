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



public class TestLogic {
	CommandParser parser;
	Receiver receiver;
	Invoker invoker;
	
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
	    invoker.execute(new DoneCommand(receiver, receiver.getAllTasks().get(0)));
	    invoker.execute(new AddCommand(receiver, parser.parseAdd("b")));
	    invoker.execute(new AddCommand(receiver, parser.parseAdd("c by 1")));
	    invoker.execute(new AddCommand(receiver, parser.parseAdd("a by 1")));
	    invoker.execute(new AddCommand(receiver, parser.parseAdd("d by 5")));
	    invoker.execute(new AddCommand(receiver, parser.parseAdd("e from 2-3")));
	    invoker.execute(new AddCommand(receiver, parser.parseAdd("a from 2-3")));
	    invoker.execute(new AddCommand(receiver, parser.parseAdd("b from 1-3")));
	    invoker.execute(new AddCommand(receiver, parser.parseAdd("q at 4")));
	    invoker.execute(new AddCommand(receiver, parser.parseAdd("g at 4")));
	    invoker.execute(new AddCommand(receiver, parser.parseAdd("a at 3")));
	    invoker.execute(new DoneCommand(receiver, receiver.getAllTasks().get(0)));
	    receiver.getAllTasks().clear();
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
	    invoker = new Invoker();
        receiver = Receiver.getReceiver();
        parser = new CommandParser();
	}
}
