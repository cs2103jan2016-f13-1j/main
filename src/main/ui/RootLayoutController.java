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
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import main.data.Task;
import main.logic.Controller;

public class RootLayoutController {
    private DoolehMainApp main;

    private ObservableList<String> taskList = FXCollections.observableArrayList();

    @FXML // fx:id="rootLayout"
    private AnchorPane rootLayout; // Value injected by FXMLLoader

    @FXML // fx:id="listView"
    private JFXListView<String> listView; // Value injected by FXMLLoader

    @FXML // fx:id="commandBar"
    private TextField commandBar; // Value injected by FXMLLoader

    @FXML // fx:id="labelCurrentMode"
    private Label labelCurrentMode; // Value injected by FXMLLoader

    @FXML // fx:id="labelUserAction"
    private Label labelUserAction; // Value injected by FXMLLoader

    @FXML // fx:id="labelUserFeedback"
    private Label labelUserFeedback; // Value injected by FXMLLoader

    private ArrayList<Task> allTasks;
    private Controller controller;
    private String inputFeedback;
    private String userInput;
    private String[] userInputArray;
    private String userCommand;
    private String userArguments;

    public RootLayoutController() {

    }

    @FXML
    private void initialize() {

        Controller controller = new Controller();

        // ListView seems to only allow binding of a OberservableList of String
        // type
        ListProperty<String> lp = new SimpleListProperty<String>();
        listView.itemsProperty().bind(lp);

        // retrieve all task and add into an ObservableList
        allTasks = controller.getAllTasks();
        
        for(int i=0; i<allTasks.size(); i++){
            taskList.add(i+1+". "+allTasks.get(i));
        }
        lp.set(taskList);

        rootLayout.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent keyEvent) {
                // TODO Auto-generated method stub

                // pass focus over to listview instead of tabs
                if (keyEvent.getCode() == KeyCode.UP) {
                    listView.getSelectionModel().selectPrevious();
                    keyEvent.consume();

                } else if (keyEvent.getCode() == KeyCode.DOWN) {
                    listView.getSelectionModel().selectNext();
                    keyEvent.consume();
                } else if (keyEvent.getCode() == KeyCode.F1) {
                    if (labelCurrentMode.getText().equals("Edit mode")) {
                        labelCurrentMode.setText("Today");

                    } else {
                        labelCurrentMode.setText("Edit mode");
                    }
                    keyEvent.consume();
                }

                System.out.println(keyEvent.getTarget());

            }
        });

        commandBar.setOnKeyReleased(new EventHandler<Event>() {

            @Override
            public void handle(Event event) {
                // TODO Auto-generated method stub
                userInput = commandBar.getCharacters().toString();

                if (userInput.length() == 0) {
                    labelUserFeedback.setVisible(false);
                    labelUserAction.setVisible(false);
                    userInput = "";
                    labelUserAction.setText("");
                    labelUserFeedback.setText("");
                    userArguments = "";
                    return;
                }

                userInputArray = userInput.split(" ");
                userCommand = userInputArray[0];
                if (userInputArray.length > 1) {
                    System.out.println(userCommand + " " +userInput.indexOf(userCommand) + " " +userInput.lastIndexOf(userCommand));
                    
                    userArguments = userInput.substring(userInput.indexOf(userInputArray[1]));
                }

                // stub method to grab command type from user input

                switch (userCommand) {
                    case "search" :
                    case "find" :
                        labelUserFeedback.setVisible(true);
                        labelUserAction.setText("Searching:");
                        labelUserAction.setVisible(true);
                        if (userInputArray.length > 1) {
                            inputFeedback = userArguments; // stub code
                        } else {
                            inputFeedback = "";
                        }
                        break;

                    case "delete" :
                    case "del" :
                        labelUserFeedback.setVisible(true);
                        labelUserAction.setText("Deleting:");
                        labelUserAction.setVisible(true);
                        if (userInputArray.length > 1) {
                            inputFeedback = allTasks.get(Integer.parseInt(userArguments) - 1).toString();
                            controller.parseCommand(userInput, Controller.Tab.FLOATING_TAB);
                        } else {
                            inputFeedback = "";
                        }
                        break;

                    default :
                        labelUserFeedback.setVisible(true);
                        labelUserAction.setText("Adding:");
                        labelUserAction.setVisible(true);
                        inputFeedback = controller.parseCommand(userInput, Controller.Tab.NO_TAB);

                }
                
                labelUserFeedback.setText(inputFeedback);
                labelUserAction.setVisible(true);
                labelUserFeedback.setVisible(true);
                System.out.println(inputFeedback);

            }
        });

        commandBar.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                // TODO Auto-generated method stub
                controller.executeCommand();

                //TODO refactor into a method
                // clear and retrieve all task and add into an ObservableList
                taskList.clear();
                allTasks = controller.getAllTasks();
                for(int i=0; i<allTasks.size(); i++){
                    taskList.add(i+1+". "+allTasks.get(i));
                }

                // if we dont set the list again, the listview item may show a
                // buggy arrangement
                // due to how the listview recycles it's cell items for effiency
                lp.set(taskList);
                commandBar.clear();

                labelUserFeedback.setVisible(false);
                labelUserAction.setVisible(false);
            }
        });

        // requestFocus will not work here since this class is run before
        // before the construction of the scene in DoolehMainApp class
        // commandBar.requestFocus();
    }

    public void requestFocusForCommandBar() {
        commandBar.requestFocus();
    }
}
