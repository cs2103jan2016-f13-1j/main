package main.ui;

import java.util.ArrayList;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import main.data.TaskBean;
import main.logic.ControllerStub;

public class RootLayoutController {
	private DoolehMainApp main;

	private ObservableList<String> taskList = FXCollections.observableArrayList();

	@FXML
	private Label labelFeedback;

	@FXML
	private TextField commandBar;

	@FXML // fx:id="allView"
	private ListView<String> allView; // Value injected by FXMLLoader
	
	ControllerStub controller;

	public RootLayoutController() {
	}

	@FXML
	private void initialize() {

		ControllerStub controller = new ControllerStub();
		ListProperty<String> lp = new SimpleListProperty<String>();
		allView.itemsProperty().bind(lp);
		ArrayList<TaskBean> tasks = controller.getTasks();
		for (TaskBean tb : tasks) {
			taskList.add(tb.getTitle());
		}
		lp.set(taskList);
		
		commandBar.setOnKeyPressed(new EventHandler<Event>() {

			@Override
			public void handle(Event event) {
				// TODO Auto-generated method stub
				labelFeedback.setText(commandBar.getText());
				
			}
		});

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
