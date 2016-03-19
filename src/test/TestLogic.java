/**
 * 
 */
package test;

import java.util.ArrayList;
import java.util.Date;

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
import main.logic.UndoneCommand;



public class TestLogic {
	
	Receiver receiver;
	Invoker invoker;
	
	@Test
	public void allFunctionsTest() {
	    Task task = new Task("example", null, null, null, new Date());
        Task task1 = new Task("new task", null, null, null, new Date());
        ArrayList<Task> tasks = new ArrayList<Task>();
        tasks.add(task);
        tasks.add(task1);
        
        Command add = new AddCommand(receiver, task);
        Command add1 = new AddCommand(receiver, task1);
        Command edit = new EditCommand(receiver, task, task1);
        Command delete = new DeleteCommand(receiver, task);
        Command delete1 = new DeleteCommand(receiver, task1);
        Command deleteMultiple = new DeleteCommand(receiver, tasks);
        Command done = new DoneCommand(receiver, task);
        Command done1 = new DoneCommand(receiver, task1);
        Command doneMultiple = new DoneCommand(receiver, tasks);
        Command undone = new UndoneCommand(receiver, task);
        Command undone1 = new UndoneCommand(receiver, task1);
        Command undoneMultiple = new UndoneCommand(receiver, tasks);

        invoker.execute(add);
        invoker.undo();
        invoker.redo();
        invoker.execute(edit);
        invoker.undo();
        invoker.redo();
        invoker.execute(done1);
        invoker.undo();
        invoker.redo();
        invoker.execute(undone1);
        invoker.undo();
        invoker.redo();
        invoker.execute(delete1);
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
	
	//@Test
	public void setFilePathTest() {
	    //logic.setFileLocation("invalid$path");
	}
	
	@Before
	public void initialize() {
	    invoker = new Invoker();
        receiver = Receiver.getReceiver();
	}
}
