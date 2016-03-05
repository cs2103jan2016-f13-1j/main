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
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import main.data.Task;
import main.logic.Controller;

public class RootLayoutController {
    private static final String COMMAND_DELETE = "delete";
    private static final String COMMAND_DELETE_SHORTHAND = "del";
    private static final String COMMAND_SEARCH = "search";
    private static final String WHITESPACE = " ";
    private static final String MESSAGE_LABEL_MODE_EDIT = "Edit mode";
    private static final String MESSAGE_FEEDBACK_ACTION_ADD = "Adding:";
    private static final String MESSAGE_FEEDBACK_ACTION_DELETE = "Deleting:";
    private static final String MESSAGE_FEEDBACK_ACTION_SEARCH = "Searching:";
    private static final String MESSAGE_ERROR_RESULT_DELETE = "Task %1$s not found.";

    // Ctrl+Tab hotket
    private static final KeyCombination HOTKEY_CTRL_TAB = new KeyCodeCombination(KeyCode.TAB,
            KeyCombination.CONTROL_DOWN);

    @FXML // fx:id="rootLayout"
    private AnchorPane rootLayout; // Value injected by FXMLLoader

    @FXML // fx:id="tabPane"
    private TabPane tabPane; // Value injected by FXMLLoader

    @FXML // fx:id="tabAll"
    private Tab tabAll; // Value injected by FXMLLoader

    @FXML // fx:id="tabToday"
    private Tab tabToday; // Value injected by FXMLLoader

    @FXML // fx:id="tabWeek"
    private Tab tabWeek; // Value injected by FXMLLoader

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

    @FXML // fx:id="labelResult"
    private Label labelResult; // Value injected by FXMLLoader

    private Controller controller;
    private ArrayList<Task> allTasks;
    private ListProperty<String> listProperty;
    private ObservableList<String> observableTaskList = FXCollections.observableArrayList();
    private String inputFeedback;
    private String userInput;
    private String[] userInputArray;
    private String userCommand;
    private String userArguments;
    private int previousSelectedTaskIndex;
    private int previousCaretPosition;
    private boolean isEditMode;

    public RootLayoutController() {

    }

    public void requestFocusForCommandBar() {
        commandBar.requestFocus();
        restoreCaretPosition();
    }

    public void selectFirstItemFromListView() {
        listView.getSelectionModel().selectNext();

    }

    @FXML
    private void initialize() {
        populateListView();
        initMouseListener();
        initKeyboardListener();
        initCommandBarListener();
        initTabSelectionListener();
    }

    /**
     * 
     */
    private void initTabSelectionListener() {
        ObservableList<Tab> tabList = tabPane.getTabs();
        for (int i = 0; i < tabList.size(); i++) {
            tabList.get(i).setOnSelectionChanged(new EventHandler<Event>() {
                @Override
                public void handle(Event event) {
                    // TODO Auto-generated method stub
                    String currentTabName = getSelectedTabName();
                    labelCurrentMode.setText(currentTabName);

                }
            });
        }
    }

    /**
     * 
     */
    private void initMouseListener() {
        rootLayout.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<Event>() {

            @Override
            public void handle(Event event) {
                // TODO Auto-generated method stub
                requestFocusForCommandBar();
            }
        });
    }

    /**
     * 
     */
    private void initCommandBarListener() {
        commandBar.setOnKeyReleased(new EventHandler<Event>() {

            @Override
            public void handle(Event event) {
                handleKeyStrokes();
                saveCaretPosition();

            }
        });

        commandBar.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                // TODO Auto-generated method stub

                // do nothing when there is no user input
                if (commandBar.getLength() == 0) {
                    event.consume();
                    return;
                }

                handleEnterKey();
                event.consume();
            }
        });
    }

    /**
     * 
     */
    private void initKeyboardListener() {
        rootLayout.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent keyEvent) {
                // TODO Auto-generated method stub

                // pass focus over to listview instead of tabs

                if (keyEvent.getCode() == KeyCode.UP) {
                    handleUpArrowKey();
                    keyEvent.consume();
                } else if (keyEvent.getCode() == KeyCode.DOWN) {
                    handleDownArrowKey();
                    keyEvent.consume();
                } else if (keyEvent.getCode() == KeyCode.F1) {
                    handleFOneKey();
                    keyEvent.consume();
                } else if (keyEvent.getCode() == KeyCode.DELETE) {
                    handleDeleteKey();
                    keyEvent.consume();
                } else if (HOTKEY_CTRL_TAB.match(keyEvent)) {
                    handleCtrlTab();
                    keyEvent.consume();
                }

                System.out.println(keyEvent.getTarget());

            }
        });
    }

    /**
     * 
     */
    private void populateListView() {

        if (controller == null) {
            controller = new Controller();
        }

        // ListView only allow binding of a OberservableList of String
        if (listProperty == null) {
            listProperty = new SimpleListProperty<String>();
        }

        listView.itemsProperty().bind(listProperty);

        // retrieve all task and add into an ObservableList
        allTasks = controller.getAllTasks();

        for (int i = 0; i < allTasks.size(); i++) {
            observableTaskList.add(i + 1 + ". " + allTasks.get(i));
        }

        listProperty.set(observableTaskList);
    }

    /**
     * 
     */
    private void refreshListView() {
        // TODO refactor into a method
        // clear and retrieve all task and add into an
        // ObservableList
        observableTaskList.clear();
        populateListView();
    }

    /**
     * 
     */
    private void handleUpArrowKey() {
        listView.getSelectionModel().selectPrevious();
        listView.scrollTo(getSelectedTaskIndex());
        System.out.println(getSelectedTaskIndex());

        // only set currently selected item to command bar when in
        // Edit mode
        if (isEditMode) {
            commandBar.setText(allTasks.get(getSelectedTaskIndex()).toString());
//            restoreCaretPosition();
            moveCaretPositionToLast();
        }

    }

    /**
     * 
     */
    private void handleDownArrowKey() {
        listView.getSelectionModel().selectNext();
        listView.scrollTo(getSelectedTaskIndex());
        System.out.println(getSelectedTaskIndex());

        // only set currently selected item to command bar when in
        // Edit mode
        if (isEditMode) {
            commandBar.setText(allTasks.get(getSelectedTaskIndex()).toString());
//            restoreCaretPosition();
            moveCaretPositionToLast();
        }
    }

    /**
     * 
     */
    private void handleFOneKey() {
        if (isEditMode) {
            isEditMode = false;
            labelCurrentMode.setText(getSelectedTabName());
            commandBar.clear();       

        } else {
            isEditMode = true;
            labelCurrentMode.setText(MESSAGE_LABEL_MODE_EDIT);
            commandBar.setText(allTasks.get(getSelectedTaskIndex()).toString());
            moveCaretPositionToLast();
        }
    }

    /**
     * 
     */
    private void moveCaretPositionToLast() {
        commandBar.positionCaret(commandBar.getText().length());
    }

    /**
     * 
     */
    private void handleDeleteKey() {
        controller.parseCommand(COMMAND_DELETE + WHITESPACE + (getSelectedTaskIndex() + 1),
                Controller.Tab.FLOATING_TAB);
        controller.executeCommand();
        saveSelectedTaskIndex();
        refreshListView();
        restoreListViewPreviousSelection();

    }

    /**
     * 
     */
    private void saveSelectedTaskIndex() {
        previousSelectedTaskIndex = getSelectedTaskIndex();
    }

    /**
     * 
     */
    private void restoreListViewPreviousSelection() {
        // if previous selected index was the last index in the previous list
        if (previousSelectedTaskIndex == allTasks.size()) {
            listView.getSelectionModel().selectLast();
            listView.scrollTo(allTasks.size() - 1);
        } else {
            listView.getSelectionModel().select(previousSelectedTaskIndex);
            listView.scrollTo(previousSelectedTaskIndex);
        }
    }

    /**
     * 
     */
    private void handleKeyStrokes() {
        if (!isEditMode) {

            // TODO Auto-generated method stub
            userInput = commandBar.getCharacters().toString();
            assert userInput != null;
            System.out.println(userInput);

            if (userInput.length() == 0) {
                showFeedback(false);
                clearFeedback();
                clearStoredUserInput();
                return;
            }

            extractUserInput();
            parseUserInput();
            System.out.println(inputFeedback);

        } else {
            controller.parseCommand(commandBar.getText(), Controller.Tab.FLOATING_TAB);
        }
    }

    /**
     * 
     */
    private void handleEnterKey() {

        if (!isEditMode) {
            controller.executeCommand();

            // add operation
            if (!userCommand.equals(COMMAND_DELETE) && !userCommand.equals(COMMAND_DELETE_SHORTHAND)) {
                refreshListView();
                listView.scrollTo(allTasks.size() - 1);
                listView.getSelectionModel().selectLast();
            } else {
                saveSelectedTaskIndex();
                refreshListView();
                restoreListViewPreviousSelection();
            }

        } else {
            // something is wrong with this controller.editTask API
            controller.editTask(Controller.FLOATING, listView.getSelectionModel().getSelectedIndex() + 1);
            refreshListView();
            saveSelectedTaskIndex();
            listView.getSelectionModel().select(previousSelectedTaskIndex);
            listView.scrollTo(getSelectedTaskIndex());
        }

        showFeedback(false);
        clearFeedback();
        clearStoredUserInput();
        commandBar.clear();
    }

    /**
     * 
     */
    private void extractUserInput() {
        userInputArray = userInput.split(" ");
        userCommand = userInputArray[0];
        if (userInputArray.length > 1) {
            System.out.println(
                    userCommand + " " + userInput.indexOf(userCommand) + " " + userInput.lastIndexOf(userCommand));

            userArguments = userInput.substring(userInput.indexOf(userInputArray[1]));
        }
    }

    /**
     * 
     */
    private void parseUserInput() {
        switch (userCommand) {
            case COMMAND_SEARCH :
                if (userInputArray.length > 1) {
                    inputFeedback = userArguments; // stub code
                } else {
                    inputFeedback = "";
                }

                showFeedback(true, MESSAGE_FEEDBACK_ACTION_SEARCH, inputFeedback);
                break;

            case COMMAND_DELETE :
            case COMMAND_DELETE_SHORTHAND :
                if (userInputArray.length > 1) {
                    int userIndex = Integer.parseInt(userArguments);
                    int actualIndex = Integer.parseInt(userArguments) - 1;

                    if (actualIndex >= allTasks.size()) {
                        showResult(true, String.format(MESSAGE_ERROR_RESULT_DELETE, userIndex));
                        showFeedback(false);
                    } else {
                        inputFeedback = allTasks.get(actualIndex).toString();
                        showResult(false, "");
                        showFeedback(true, MESSAGE_FEEDBACK_ACTION_DELETE, inputFeedback);
                        controller.parseCommand(userInput, Controller.Tab.FLOATING_TAB);
                        // listView.getSelectionModel().select(actualIndex);
                        listView.scrollTo(actualIndex);
                    }

                } else {
                    inputFeedback = "";
                }

                break;

            default :
                inputFeedback = controller.parseCommand(userInput, Controller.Tab.NO_TAB);
                showFeedback(true, MESSAGE_FEEDBACK_ACTION_ADD, inputFeedback);

        }
    }

    /**
     * 
     */
    private void clearStoredUserInput() {
        userInput = "";
        userInputArray = null;
        userCommand = "";
        userArguments = "";
    }

    /**
     * 
     */
    private void clearFeedback() {
        labelUserAction.setText("");
        labelUserFeedback.setText("");
    }

    /**
     * 
     */
    private void showFeedback(boolean isVisible) {
        labelUserAction.setVisible(isVisible);
        labelUserFeedback.setVisible(isVisible);
    }

    /**
     * 
     */
    private void showFeedback(boolean isVisible, String userAction, String userFeedback) {
        labelUserAction.setVisible(isVisible);
        labelUserFeedback.setVisible(isVisible);
        labelUserAction.setText(userAction);
        labelUserFeedback.setText(inputFeedback);
    }

    /**
     * @param resultString
     */
    private void showResult(boolean isVisible, String resultString) {
        labelResult.setText(resultString);
        labelResult.setVisible(isVisible);
    }

    /**
     * 
     */
    private int getSelectedTaskIndex() {
        return listView.getSelectionModel().getSelectedIndex();
    }

    /**
     * 
     */
    private void saveCaretPosition() {
        previousCaretPosition = commandBar.getCaretPosition();
    }

    /**
     * 
     */
    private void restoreCaretPosition() {
        commandBar.positionCaret(previousCaretPosition);
    }

    /**
     * @param tabList
     * @return
     */
    private String getSelectedTabName() {
        return tabPane.getTabs().get(tabPane.getSelectionModel().getSelectedIndex()).getText();
    }

    /**
     * 
     */
    private void handleCtrlTab() {
        SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
        
        //already at the last tab, lets bounce back to first tab
        if (selectionModel.getSelectedIndex() == tabPane.getTabs().size() - 1) {
            selectionModel.selectFirst();
        }
        else{
            selectionModel.selectNext();
        }
        
        requestFocusForCommandBar();
        restoreCaretPosition();
    }
}
