package main.ui;

import java.util.ArrayList;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import main.data.TaskBean;
import main.logic.ControllerStub;

public class RootLayoutController {
	private DoolehMainApp main;

	private ObservableList<String> taskList = FXCollections.observableArrayList();

	
	@FXML // fx:id="allView"
    private ListView<String> allView; // Value injected by FXMLLoader
	


	public RootLayoutController() {
	}

	@FXML
	private void initialize() {
	
		ControllerStub controller = new ControllerStub();
		ListProperty<String> lp =  new SimpleListProperty<String>();
		allView.itemsProperty().bind(lp);
		ArrayList<TaskBean> tasks = controller.getTasks();
		for(TaskBean tb : tasks){
			taskList.add(tb.getTitle());
		}
		lp.set(taskList);
		
	}

	/**
	 * Is called by the main application to give a reference back to itself.
	 * 
	 * @param mainApp
	 */
	public void setMainApp(DoolehMainApp mainApp) {
		this.main = mainApp;

	}
}
