/**
 * 
 */
package test;

import org.junit.Before;

import main.logic.Invoker;
import main.logic.Receiver;



public class TestLogic {
	
	Receiver receiver;
	Invoker invoker;

	
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
