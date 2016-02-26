/**
 * 
 */
package main.data;

import javafx.beans.property.SimpleStringProperty;

/**
 * @author Quek Yang Sheng
 *
 */
public class TaskBean {

	private SimpleStringProperty title;
	
	public TaskBean(){
		title = new SimpleStringProperty();
	};

	public String getTitle() {
		return title.get();
	}

	public void setTitle(String title) {
		this.title.set(title);
	}
	
	//returns different strings based on fields that tasks has
//	public String toString() {
//		return title + " from " + startDate + " " + startTime + " to "
//				+ endDate + " " + endTime + " label: " + label;
//	}
}
