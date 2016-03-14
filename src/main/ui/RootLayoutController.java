package main.ui;

import java.util.ArrayList;

import com.jfoenix.controls.JFXListView;
import com.sun.javafx.scene.control.skin.VirtualFlow;

import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
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
import javafx.util.Callback;
import main.data.Task;
import main.logic.Logic;

@SuppressWarnings("restriction")
public class RootLayoutController {
    private static final String STRING_TODAY = "Today";
    private static final String COMMAND_DELETE = "delete";
    private static final String COMMAND_DELETE_SHORTHAND = "del";
    private static final String COMMAND_SEARCH = "search";
    private static final String WHITESPACE = " ";
    private static final String EMPTY_STRING = "";
    private static final String MESSAGE_LABEL_MODE_EDIT = "Edit mode";
    private static final String MESSAGE_LISTVIEW_EMPTY = "You have no task!";
    private static final String MESSAGE_FEEDBACK_ACTION_ADD = "Adding:";
    private static final String MESSAGE_FEEDBACK_ACTION_DELETE = "Deleting:";
    private static final String MESSAGE_FEEDBACK_TOTAL_TASK = "(%1$s tasks)";
    private static final String MESSAGE_FEEDBACK_ACTION_SEARCH = "Searching:";
    private static final String MESSAGE_ERROR_RESULT_DELETE = "Task -%1$s- not found.";

    // Ctrl+Tab hotkey
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
    private JFXListView<Task> listViewAll; // Value injected by FXMLLoader

    @FXML // fx:id="listView"
    private JFXListView<String> listViewToday; // Value injected by FXMLLoader

    @FXML // fx:id="listView"
    private JFXListView<String> listViewWeek; // Value injected by FXMLLoader

    @FXML // fx:id="commandBar"
    private TextField commandBar; // Value injected by FXMLLoader

    @FXML // fx:id="labelCurrentMode"
    private Label labelCurrentMode; // Value injected by FXMLLoader

    @FXML // fx:id="labelDateToday"
    private Label labelDateToday; // Value injected by FXMLLoader

    @FXML // fx:id="labelUserAction"
    private Label labelUserAction; // Value injected by FXMLLoader

    @FXML // fx:id="labelUserFeedback"
    private Label labelUserFeedback; // Value injected by FXMLLoader

    @FXML // fx:id="labelResult"
    private Label labelResult; // Value injected by FXMLLoader

    @FXML // fx:id="btnDel"
    private Button btnDel; // Value injected by FXMLLoader

    @FXML // fx:id="btnEdit"
    private Button btnEdit; // Value injected by FXMLLoader

    @FXML // fx:id="groupUndoRedo"
    private Group groupUndoRedo; // Value injected by FXMLLoader

    @FXML // fx:id="labelUndoRedo"
    private Label labelUndoRedo; // Value injected by FXMLLoader

    @FXML // fx:id="btnUndoRedo"
    private Button btnUndoRedo; // Value injected by FXMLLoader

    private VirtualFlow<IndexedCell<String>> virtualFlow;
    private IndexedCell<String> firstVisibleIndexedCell;
    private IndexedCell<String> lastVisibleIndexedCell;

    private Logic logic;
    private ArrayList<Task> allTasks;
    private ListProperty<String> listProperty;
    private ObservableList<Task> observableTaskList = FXCollections.observableArrayList();
    private String inputFeedback;
    private String userInput;
    private String[] userInputArray;
    private String userCommand;
    private String userArguments;
    private String previousTextInCommandBar;
    private int previousSelectedTaskIndex;
    private int previousCaretPosition;
    private boolean isEditMode;
    private boolean isExecuteCommand;
    private boolean isUndo;
    private boolean isRedo;
    private boolean isUserRevertAction;

    public void requestFocusForCommandBar() {
        commandBar.requestFocus();
        restoreCaretPosition();
    }

    public void selectFirstItemFromListView() {
        listViewAll.getSelectionModel().selectNext();
        initCustomViewportBehaviorForListView();

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
                    if (getSelectedTabName().equals(STRING_TODAY)) {
                        labelDateToday.setVisible(true);
                    } else {
                        labelDateToday.setVisible(false);
                    }

                    labelCurrentMode.setText(getSelectedTabName());

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

            }
        });

        commandBar.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
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
                if (keyEvent.getCode() == KeyCode.UP || keyEvent.getCode() == KeyCode.DOWN) {
                    handleArrowKeys(keyEvent);
                    keyEvent.consume();
                } else if (keyEvent.getCode() == KeyCode.F1) {
                    handleFOneKey();
                    keyEvent.consume();
                } else if (keyEvent.getCode() == KeyCode.F2) {
                    handleFTwoKey();
                    keyEvent.consume();
                } else if (keyEvent.getCode() == KeyCode.DELETE) {
                    handleDeleteKey();
                    keyEvent.consume();
                } else if (HOTKEY_CTRL_TAB.match(keyEvent)) {
                    handleCtrlTab();
                    keyEvent.consume();
                } else if (keyEvent.getCode() == KeyCode.TAB) {
                    keyEvent.consume();
                }

                // System.out.println(keyEvent.getTarget());

            }
        });
    }

    // /**
    // *
    // */
    // private void populateListView() {
    // if (logic == null) {
    // logic = Logic.getLogic();
    // }
    //
    // // ListView only allow binding of a OberservableList of String
    // if (listProperty == null) {
    // listProperty = new SimpleListProperty<String>();
    // }
    //
    // listViewAll.itemsProperty().bind(listProperty);
    // listViewAll.setPlaceholder(new Label(MESSAGE_LISTVIEW_EMPTY));
    //
    // // retrieve all task and add into an ObservableList
    // allTasks = logic.getAllTasks();
    //
    // for (int i = 0; i < allTasks.size(); i++) {
    // // System.out.println(allTasks.get(i));
    // observableTaskList.add(i + 1 + ". " + allTasks.get(i));
    // }
    //
    // listProperty.set(observableTaskList);
    // }

    /**
     * 
     */
    private void populateListView() {
        if (logic == null) {
            logic = Logic.getLogic();
        }

        listViewAll.setPlaceholder(new Label(MESSAGE_LISTVIEW_EMPTY));

        // retrieve all task and add into an ObservableList
        allTasks = logic.getAllTasks();
        observableTaskList.setAll(logic.getAllTasks());
        listViewAll.setItems(observableTaskList);
        listViewAll.setCellFactory(new Callback<ListView<Task>, ListCell<Task>>() {

            @Override
            public ListCell<Task> call(ListView<Task> param) {
                // TODO Auto-generated method stub
                return new CustomListCellController();
            }
        });

    }

    /**
     * 
     */
    private void refreshListView() {
        observableTaskList.clear();
        populateListView();
    }

    /**
     * 
     */
    private void handleArrowKeys(KeyEvent keyEvent) {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                if (keyEvent.getCode() == KeyCode.UP) {
                    listViewAll.getSelectionModel().selectPrevious();
                    adjustViewportForListView();
                    System.out.println("current index " + getSelectedTaskIndex());

                } else if (keyEvent.getCode() == KeyCode.DOWN) {
                    listViewAll.getSelectionModel().selectNext();
                    adjustViewportForListView();
                    System.out.println("current index " + getSelectedTaskIndex());

                }

                // only set currently selected item to command bar when in
                // Edit mode
                if (isEditMode) {
                    commandBar.setText(allTasks.get(getSelectedTaskIndex()).toString());
                    moveCaretPositionToLast();
                }

            }
        });

    }

    /**
     * This method currently accesses the private API, the VirtualFlow class.
     * This method can only be called after the Stage has been set in the
     * DoolehMainApp class. Due to the lifecycle of the JavaFX framework, we can
     * only to grab an instance of the VirtualFlow class from our ListView after
     * the Stage has been set. This will allow us to adjust the viewport of the
     * ListView programmatically whenever user hits the up/down arrow key to
     * select items from the ListView. See adjustViewportForListView() method to
     * find out more about the viewport adjusting algorithm
     */
    @SuppressWarnings("unchecked")
    private void initCustomViewportBehaviorForListView() {
        for (Node node : listViewAll.getChildrenUnmodifiable()) {
            if (node instanceof VirtualFlow) {
                // get an instance of VirtualFlow. this is essentially the
                // viewport for ListView
                virtualFlow = (VirtualFlow<IndexedCell<String>>) node;
                System.out.println("found virtual flow");

            }
        }
    }

    /**
     * This method is used to emulate the original behavior of a ListView, i.e.
     * the automatic scrolling of focused ListView when a selected item is not
     * visible within the viewport
     */
    private void adjustViewportForListView() {
        firstVisibleIndexedCell = virtualFlow.getFirstVisibleCellWithinViewPort();
        lastVisibleIndexedCell = virtualFlow.getLastVisibleCellWithinViewPort();
        System.out.println("first visible cell: " + firstVisibleIndexedCell.getIndex());
        System.out.println("last visible cell: " + lastVisibleIndexedCell.getIndex());

        if (getSelectedTaskIndex() < firstVisibleIndexedCell.getIndex()) {

            // viewport will scroll and show the current item at the top
            listViewAll.scrollTo(getSelectedTaskIndex());
        } else if (getSelectedTaskIndex() > lastVisibleIndexedCell.getIndex()) {

            // viewport will scroll and show the current item at the bottom
            listViewAll.scrollTo(firstVisibleIndexedCell.getIndex() + 1);
        }
    }

    /**
     * 
     */
    private void handleFOneKey() {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                if (isEditMode) {
                    isEditMode = false;
                    labelCurrentMode.setText(getSelectedTabName());
                    restoreCommandBarText();
                    restoreCaretPosition();

                } else {
                    isEditMode = true;
                    saveCommandBarText();
                    saveCaretPosition();
                    showFeedback(false);
                    labelCurrentMode.setText(MESSAGE_LABEL_MODE_EDIT);
                    commandBar.setText(allTasks.get(getSelectedTaskIndex()).toString());
                    moveCaretPositionToLast();
                }

            }
        });

    }

    /**
     * 
     */
    private void handleFTwoKey() {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                saveSelectedTaskIndex();

                if (!groupUndoRedo.isVisible()) {
                    return;
                }

                if (isUndo) {
                    isUndo = false;
                    isRedo = true;
                    labelUndoRedo.setText("undo");
                    logic.redo();
                } else {
                    isUndo = true;
                    isRedo = false;
                    labelUndoRedo.setText("redo");
                    logic.undo();
                }

                refreshListView();
                restoreListViewPreviousSelection();

            }
        });

    }

    /**
     * 
     */
    private void handleKeyStrokes() {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (!isEditMode) {
                    userInput = commandBar.getText();
                    assert userInput != null;
                    System.out.println(userInput);

                    if (userInput.length() == 0) {
                        System.out.println("handleKeyStrokes: userInput length is 0");
                        showFeedback(false);
                        showResult(false, EMPTY_STRING);
                        clearFeedback();
                        clearStoredUserInput();
                        return;
                    }

                    extractUserInput();
                    parseUserInput();
                    System.out.println(inputFeedback);

                } else {
                    logic.parseCommand(commandBar.getText(), Logic.List.FLOATING);
                }

            }
        });

    }

    /**
     * 
     */
    private void handleEnterKey() {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                if (!isEditMode) {
                    if (commandBar.getText().trim().length() > 0) {
                        System.out.println("Command bar Text length: " + commandBar.getLength());
                        logic.executeCommand();

                        // add operation
                        if (!userCommand.equals(COMMAND_DELETE) && !userCommand.equals(COMMAND_DELETE_SHORTHAND)) {
                            refreshListView();
                            listViewAll.getSelectionModel().selectLast();
                            listViewAll.scrollTo(allTasks.size() - 1);
                        } else {
                            saveSelectedTaskIndex();
                            refreshListView();
                            restoreListViewPreviousSelection();
                        }

                    } else {
                        // something is wrong with this logic.editTask API
                        logic.editTask(Logic.List.FLOATING, getSelectedTaskIndex());
                        saveSelectedTaskIndex();
                        refreshListView();
                        restoreListViewPreviousSelection();
                    }

                    showFeedback(false);
                    clearFeedback();
                    clearStoredUserInput();
                    commandBar.clear();
                    showUndoRedoButton();
                }

            }
        });

    }

    /**
     * 
     */
    private void showUndoRedoButton() {
        groupUndoRedo.setVisible(true);
    }

    /**
     * 
     */
    private void handleDeleteKey() {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                logic.parseCommand(COMMAND_DELETE + WHITESPACE + (getSelectedTaskIndex() + 1), Logic.List.FLOATING);
                logic.executeCommand();
                saveSelectedTaskIndex();
                refreshListView();
                restoreListViewPreviousSelection();
                showUndoRedoButton();

            }
        });

    }

    /**
     * 
     */
    private void handleCtrlTab() {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();

                // already at the last tab, lets bounce back to first tab
                if (selectionModel.getSelectedIndex() == tabPane.getTabs().size() - 1) {
                    selectionModel.selectFirst();
                } else {
                    selectionModel.selectNext();
                }

                requestFocusForCommandBar();
                restoreCaretPosition();

            }
        });

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
                parseSearch();
                break;

            case COMMAND_DELETE :
            case COMMAND_DELETE_SHORTHAND :
                parseDeleteImproved();
                break;

            default :
                parseAdd();
        }
    }

    /**
     * 
     */
    private void parseAdd() {
        inputFeedback = logic.parseCommand(userInput, null);
        showFeedback(true, MESSAGE_FEEDBACK_ACTION_ADD, inputFeedback);
    }

    private void parseDeleteImproved() {
        if (userInputArray.length <= 1) {
            inputFeedback = EMPTY_STRING;
            return;
        }

        String parseResult = logic.parseCommand(userInput, Logic.List.DATED);

        System.out.println("user arguments: " + userArguments);
        System.out.println("parse result: " + parseResult);
        String[] indexesToBeDeleted = parseResult.split(" ");

        if (indexesToBeDeleted.length == 1) {
            int taskIndex = 0;
            try {
                taskIndex = Integer.parseInt(indexesToBeDeleted[0]);
            } catch (NumberFormatException nfe) {
                showResult(true, String.format(MESSAGE_ERROR_RESULT_DELETE, userArguments));
                return;
            }

            // if selected index is out of bound
            if (taskIndex >= allTasks.size() || taskIndex < 0) {
                showResult(true, String.format(MESSAGE_ERROR_RESULT_DELETE, taskIndex + 1));

            } else {
                inputFeedback = allTasks.get(taskIndex).toString();
                showFeedback(true, MESSAGE_FEEDBACK_ACTION_DELETE, inputFeedback);
            }

            return;
        }

        // int[] indexArray = new int[indexesToBeDeleted.length];
        // for (int i = 0; i < indexArray.length; i++) {
        //
        // }

        showFeedback(true, MESSAGE_FEEDBACK_ACTION_DELETE,
                userArguments + WHITESPACE + String.format(MESSAGE_FEEDBACK_TOTAL_TASK, indexesToBeDeleted.length));

    }

    /**
     * 
     */
    private void parseDelete() {
        // System.out.println("parseDelete");
        if (userInputArray.length <= 1) {
            inputFeedback = EMPTY_STRING;
            return;
        }

        String parseResult = logic.parseCommand("del 100", Logic.List.FLOATING);
        System.out.println(parseResult);

        int userIndex = 0;
        try {
            userIndex = Integer.parseInt(userArguments);
        } catch (NumberFormatException nfe) {
            showResult(true, String.format(MESSAGE_ERROR_RESULT_DELETE, userArguments));
            return;
        }

        int actualIndex = userIndex - 1;

        // if selected index is out of bound
        if (actualIndex >= allTasks.size() || actualIndex < 0) {
            showResult(true, String.format(MESSAGE_ERROR_RESULT_DELETE, userIndex));

        } else {
            inputFeedback = allTasks.get(actualIndex).toString();
            showFeedback(true, MESSAGE_FEEDBACK_ACTION_DELETE, inputFeedback);
            logic.parseCommand(COMMAND_DELETE + WHITESPACE + actualIndex, Logic.List.FLOATING);

            // saveSelectedTaskIndex();
            // listView.getFocusModel().focus(actualIndex);
            // listView.scrollTo(actualIndex);
        }

    }

    /**
     * 
     */
    private void parseSearch() {// TODO to be implemented
        if (userInputArray.length > 1) {
            inputFeedback = userArguments; // stub code
        } else {
            inputFeedback = EMPTY_STRING;
        }

        showFeedback(true, MESSAGE_FEEDBACK_ACTION_SEARCH, inputFeedback);
    }

    /**
     * 
     */
    private void clearStoredUserInput() {
        userInput = EMPTY_STRING;
        userInputArray = null;
        userCommand = EMPTY_STRING;
        userArguments = EMPTY_STRING;
    }

    /**
     * 
     */
    private void clearFeedback() {
        labelUserAction.setText(EMPTY_STRING);
        labelUserFeedback.setText(EMPTY_STRING);
    }

    /**
     * 
     */
    private void showFeedback(boolean isVisible) {
        if (isVisible) {
            showResult(false, EMPTY_STRING);
        }

        labelUserAction.setVisible(isVisible);
        labelUserFeedback.setVisible(isVisible);
    }

    /**
     * 
     */
    private void showFeedback(boolean isVisible, String userAction, String userFeedback) {
        if (isVisible) {
            showResult(false, EMPTY_STRING);
        }

        labelUserAction.setVisible(isVisible);
        labelUserFeedback.setVisible(isVisible);
        labelUserAction.setText(userAction);
        labelUserFeedback.setText(userFeedback);
    }

    /**
     * @param resultString
     */
    private void showResult(boolean isVisible, String resultString) {
        if (isVisible) {
            showFeedback(false);
        }

        labelResult.setText(resultString);
        labelResult.setVisible(isVisible);
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
            listViewAll.getSelectionModel().selectLast();
            listViewAll.scrollTo(allTasks.size() - 1);
        } else {
            listViewAll.getSelectionModel().select(previousSelectedTaskIndex);
            listViewAll.scrollTo(previousSelectedTaskIndex);
        }
    }

    /**
     * 
     */
    private int getSelectedTaskIndex() {
        return listViewAll.getSelectionModel().getSelectedIndex();
    }

    /**
     * @return String
     */
    private String getSelectedTabName() {
        return tabPane.getTabs().get(tabPane.getSelectionModel().getSelectedIndex()).getText();
    }

    private void saveCommandBarText() {
        previousTextInCommandBar = commandBar.getText();
    }

    private void restoreCommandBarText() {
        commandBar.setText(previousTextInCommandBar);
    }

    /**
     * 
     */
    private void saveCaretPosition() {
        previousCaretPosition = commandBar.getCaretPosition();
        System.out.println("Saving caret position: " + previousCaretPosition);
    }

    /**
     * 
     */
    private void restoreCaretPosition() {
        System.out.println("Restoring caret position: " + previousCaretPosition);
        commandBar.positionCaret(previousCaretPosition);
    }

    /**
     * 
     */
    private void moveCaretPositionToLast() {
        commandBar.positionCaret(commandBar.getText().length());
    }

    private int getCaretCurrentPosition() {
        return commandBar.getCaretPosition();
    }

}
