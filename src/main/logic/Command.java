package main.logic;
/** This interface represents a Command that the program logic can
 * execute or undo.
 * 
 * @author Bevin Seetoh Jia Jin
 *
 */
public interface Command {
	public void execute();
	public void undo();
}