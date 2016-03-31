package main.data;

import java.util.ArrayList;

/**
 * @author Joleeen
 *
 */
public class ParseIndexResult {
	private boolean hasValid;
	private boolean hasInvalid;
	private ArrayList<Integer> validIndexes;
	private ArrayList<String> invalidIndexes;
	
	public ParseIndexResult() {
		hasValid = false;
		hasInvalid = false;
		validIndexes = null;
		invalidIndexes = null;
	}
	
	public boolean hasValidIndex() {
		return hasValid;
	}
	
	public void setHasValid(boolean hasValid) {
		this.hasValid = hasValid;
	}
	
	public boolean hasInvalidIndex(){
		return hasInvalid;
	}
	
	public void setHasInvalid(boolean hasInvalid) {
		this.hasInvalid = hasInvalid;
	}
	
	public ArrayList<Integer> getValidIndexes() {
		return validIndexes;
	}

	public void setValidIndexes(ArrayList<Integer> validIndexes) {
		this.validIndexes = validIndexes;
	}
	
	public ArrayList<String> getInvalidIndexes() {
		return invalidIndexes;
	}

	public void setInvalidIndexes(ArrayList<String> invalidIndexes) {
		this.invalidIndexes = invalidIndexes;
	}
	
}
