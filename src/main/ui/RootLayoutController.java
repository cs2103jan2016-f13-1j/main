package main.ui;

import java.util.ArrayList;

import com.jfoenix.controls.JFXListView;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import main.data.Task;
import main.logic.Controller;
import main.logic.Controller.Tab;

public class RootLayoutController {
	private DoolehMainApp main;

	private ObservableList<String> taskList = FXCollections.observableArrayList();

	@FXML
	private Label labelFeedback;

	@FXML
	private TextField commandBar;

	@FXML // fx:id="allView"
	private ListView<String> allView; // Value injected by FXMLLoader
	
	private Controller controller;
	private String inputFeedback;
	public RootLayoutController() {
	}

	@FXML
	private void initialize() {

		Controller controller = new Controller();
		
		//ListView seems to only allow binding of a OberservableList of String type
		ListProperty<String> lp = new SimpleListProperty<String>();
		allView.itemsProperty().bind(lp);
		
		//retrieve all task and add into an ObservableList
		ArrayList<Task> tasks = controller.getAllTasks();
		for (Task task : tasks) {
			taskList.add(task.toString());
		}
		lp.set(taskList);
		
		commandBar.setOnKeyReleased(new EventHandler<Event>() {

			@Override
			public void handle(Event event) {
				// TODO Auto-generated method stub
				inputFeedback = controller.parseCommand(commandBar.getText(), Tab.NO_TAB);
				labelFeedback.setText(inputFeedback);
				System.out.println(inputFeedback);
				
			}
		});
		
		commandBar.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				// TODO Auto-generated method stub
				controller.executeCommand();
				
				//clear and retrieve all task and add into an ObservableList
				taskList.clear();
				ArrayList<Task> tasks = controller.getAllTasks();
				for (Task task : tasks) {
					taskList.add(task.toString());
				}
				
				//if we dont set the list again, the listview item may show a buggy arrangement
				//due to how the listview recycles it's cell items for effiency
				lp.set(taskList);
				commandBar.clear();
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
