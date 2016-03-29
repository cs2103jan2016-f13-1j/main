package main.logic;
import java.util.EmptyStackException;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/** 
 * @author Bevin Seetoh Jia Jin
 *
 */
public class Invoker {
	
    private static final Logger logger = Logger.getLogger(Invoker.class.getName());
    
	private Stack<Command> undoHistory = new Stack<Command>();
	private Stack<Command> redoHistory = new Stack<Command>();
	
	/** Executes a Command and adds it to the undo history
	 * 
	 * @param command An instance of the Command interface
	 */
	public void execute(Command command) {
		command.execute();
		undoHistory.push(command);
		redoHistory.clear();
	}

	/** Returns true if there is at least one undoable Command
	 * available on the undo history.
	 * 
	 * @return   true if undo stack is not empty
	 */
	public boolean isUndoAvailable() {
		return !undoHistory.empty();
	}

	/** 
	 * Undo the next available command.
	 */
	public Command undo() throws EmptyStackException {
	    Command command;
	    try {
    		command = undoHistory.pop();
    		command.undo();
    		redoHistory.push(command);
	    } catch (Exception e) {
	        logger.log(Level.WARNING, "Stack is empty, check if undo is available before calling");
	        throw new EmptyStackException();
	    }
	    return command;
	}

	/** 
	 * Returns true if there is at least one redoable Command
	 * available on the redo history.
	 * 
	 * @return   true if redo stack is not empty
	 */
	public boolean isRedoAvailable() {
		return !redoHistory.empty();
	}

	/** 
	 * Redo the next available command.
	 */
	public void redo() throws EmptyStackException {
	    try {
    		Command command = redoHistory.pop();
    		command.execute();
    		undoHistory.push(command);
	    } catch (Exception e) {
	        logger.log(Level.WARNING, "Stack is empty, check if redo is available before calling");
	        throw new EmptyStackException();
	    }
	}
}
