/**
 * 
 */
package main.data;

/**
 * @author Joleen
 *
 */
public class Task {

	private String title;
	private String startDate;
	private String startTime;
	private String endDate;
	private String endTime;
	private String label;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	//returns different strings based on fields that tasks has
	public String toString() {
		return title + " from " + startDate + " " + startTime + " to "
				+ endDate + " " + endTime + " label: " + label;
	}
}
