/**
 * 
 */
package main.logic;

/**
 * @author Bevin Seetoh Jia Jin
 *
 */
public abstract class Observer {
    protected Receiver receiver;
    public abstract void update();
}
