/**
 * 
 */
package main.data;

import java.util.Date;

/**
 * @author Joleen
 *
 */
public class Task {

	private String title;
	private Date startDate;
	private Date endDate;
	private String label;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	//returns different strings based on fields that tasks has
	public String toString() {
		return title;
	}
}
