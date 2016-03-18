package main.ui;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jfoenix.controls.JFXListView;
import com.sun.javafx.scene.control.skin.VirtualFlow;

import javafx.application.Platform;
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

    @FXML // fx:id="tabTodo"
    private Tab tabTodo; // Value injected by FXMLLoader

    @FXML // fx:id="tabCompleted"
    private Tab tabCompleted; // Value injected by FXMLLoader

    @FXML // fx:id="listViewTodo"
    private JFXListView<Task> listViewTodo; // Value injected by FXMLLoader

    @FXML // fx:id="listViewCompleted"
    private JFXListView<Task> listViewCompleted; // Value injected by FXMLLoader

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
    private boolean isUndo;
    private boolean isRedo;

    private static final Logger logger = Logger.getLogger(RootLayoutController.class.getName());

    public void requestFocusForCommandBar() {
        logger.log(Level.INFO, "Set focus to command bar");
        commandBar.requestFocus();
    }

    public void selectFirstItemFromListView() {
        logger.log(Level.INFO, "Set Select the first item on the ListView");
        listViewTodo.getSelectionModel().selectNext();
        initCustomViewportBehaviorForListView();

    }

    @FXML
    private void initialize() {
        logger.log(Level.INFO, "Initializing the UI...");
        populateListView();
        initMouseListener();
        initKeyboardListener();
        initCommandBarListener();
        initTabSelectionListener();
        logger.log(Level.INFO, "UI initialization complete");
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
                restoreCaretPosition();
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
                    // do nothing here to prevent the ui from changing focus
                    keyEvent.consume();
                }

                saveCaretPosition();
            }
        });
    }

    /**
     * 
     */
    private void populateListView() {
        if (logic == null) {
            logic = Logic.getLogic();
        }

        listViewTodo.setPlaceholder(new Label(MESSAGE_LISTVIEW_EMPTY));

        // retrieve all task and add into an ObservableList
        allTasks = logic.getAllTasks();
        assert allTasks != null;
        observableTaskList.setAll(allTasks);
        listViewTodo.setItems(observableTaskList);
        listViewTodo.setCellFactory(new Callback<ListView<Task>, ListCell<Task>>() {

            @Override
            public ListCell<Task> call(ListView<Task> param) {
                return new CustomListCellController();
            }
        });
        logger.log(Level.INFO, "Populated ListView: " + allTasks.size() + " task");
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
                    listViewTodo.getSelectionModel().selectPrevious();
                    adjustViewportForListView();
                } else if (keyEvent.getCode() == KeyCode.DOWN) {
                    listViewTodo.getSelectionModel().selectNext();
                    adjustViewportForListView();
                }

                // Set current task title to command bar when in Edit mode
                if (isEditMode) {
                    commandBar.setText(allTasks.get(getSelectedTaskIndex()).toString());
                    moveCaretPositionToLast();
                    logger.log(Level.INFO, "(EDIT MODE) Pressed " + keyEvent.getCode()
                            + " arrow key: currently selected index is " + getSelectedTaskIndex());
                    return;
                }

                logger.log(Level.INFO, "Pressed " + keyEvent.getCode() + " arrow key: currently selected index is "
                        + getSelectedTaskIndex());
            }
        });

    }

    /**
     * This method currently accesses the private API, the VirtualFlow class.
     * This method can only be called after the Stage has been set in the
     * MainApp class. Due to the lifecycle of the JavaFX framework, we can only
     * to grab an instance of the VirtualFlow class from our ListView after the
     * Stage has been set. This will allow us to adjust the viewport of the
     * ListView programmatically whenever user hits the up/down arrow key to
     * select items from the ListView. See adjustViewportForListView() method to
     * find out more about the viewport adjusting algorithm
     */
    @SuppressWarnings("unchecked")
    private void initCustomViewportBehaviorForListView() {
        for (Node node : listViewTodo.getChildrenUnmodifiable()) {
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
            listViewTodo.scrollTo(getSelectedTaskIndex());
        } else if (getSelectedTaskIndex() > lastVisibleIndexedCell.getIndex()) {

            // viewport will scroll and show the current item at the bottom
            listViewTodo.scrollTo(firstVisibleIndexedCell.getIndex() + 1);
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
                    logger.log(Level.INFO, "Pressed F1 key: Exit EDIT MODE");

                } else {
                    isEditMode = true;
                    saveCommandBarText();
                    saveCaretPosition();
                    showFeedback(false);
                    labelCurrentMode.setText(MESSAGE_LABEL_MODE_EDIT);
                    commandBar.setText(allTasks.get(getSelectedTaskIndex()).toString());
                    moveCaretPositionToLast();
                    logger.log(Level.INFO, "Pressed F1 key: Enter EDIT MODE");
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
                    logger.log(Level.INFO, "Pressed F2 key: REDO operation");
                } else if (isRedo) {
                    isUndo = true;
                    isRedo = false;
                    labelUndoRedo.setText("redo");
                    logic.undo();
                    logger.log(Level.INFO, "Pressed F2 key: UNDO operation");
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
                userInput = commandBar.getText();
                assert userInput != null;

                if (!isEditMode) {
                    logger.log(Level.INFO, "User is typing: " + userInput);

                    if (userInput.length() == 0) {
                        showFeedback(false);
                        showResult(false, EMPTY_STRING);
                        clearFeedback();
                        clearStoredUserInput();
                        return;
                    }

                    extractUserInput();
                    parseUserInput();
                } else {
                    logger.log(Level.INFO, "(EDIT MODE) User is typing: " + userInput);
                    logic.parseCommand(userInput, Logic.ListType.ALL);
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
                        logic.executeCommand();

                        // add operation
                        if (!userCommand.equals(COMMAND_DELETE) && !userCommand.equals(COMMAND_DELETE_SHORTHAND)) {
                            logger.log(Level.INFO, "(ADD TASK) Pressed ENTER key: " + commandBar.getText());
                            refreshListView();
                            listViewTodo.getSelectionModel().selectLast();
                            listViewTodo.scrollTo(allTasks.size() - 1);
                        } else {
                            logger.log(Level.INFO, "(DELETE TASK) Pressed ENTER key: " + commandBar.getText());
                            saveSelectedTaskIndex();
                            refreshListView();
                            restoreListViewPreviousSelection();
                        }

                    }

                    showFeedback(false);
                    clearFeedback();
                    clearStoredUserInput();
                    commandBar.clear();
                    showUndoRedoButton();

                } else {
                    logger.log(Level.INFO, "(EDIT MODE) Pressed ENTER key: " + commandBar.getText());
                    logic.editTask(getSelectedTaskIndex() + 1);
                    saveSelectedTaskIndex();
                    refreshListView();
                    restoreListViewPreviousSelection();

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
                logger.log(Level.INFO, "Pressed DELETE key: task index  " + getSelectedTaskIndex());
                logic.parseCommand(COMMAND_DELETE + WHITESPACE + (getSelectedTaskIndex() + 1), Logic.ListType.ALL);
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
                logger.log(Level.INFO,
                        "Pressed CTRL+TAB key: current selected Tab is " + "\"" + getSelectedTabName() + "\"");
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
                parseDelete();
                break;

            default :
                parseAdd();
        }
    }

    /**
     * 
     */
    private void parseAdd() {
        logger.log(Level.INFO, "Sending user input to logic: " + userInput);
        inputFeedback = logic.parseCommand(userInput, Logic.ListType.ALL);
        showFeedback(true, MESSAGE_FEEDBACK_ACTION_ADD, inputFeedback);
    }

    private void parseDelete() {
        if (userInputArray.length <= 1) {
            inputFeedback = EMPTY_STRING;
            return;
        }

        logger.log(Level.INFO, "Sending user input to logic: " + userInput);
        String parseResult = logic.parseCommand(userInput, Logic.ListType.ALL);
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

        showFeedback(true, MESSAGE_FEEDBACK_ACTION_DELETE,
                userArguments + WHITESPACE + String.format(MESSAGE_FEEDBACK_TOTAL_TASK, indexesToBeDeleted.length));

    }

    /**
     * 
     */
    private void parseSearch() {
        logger.log(Level.INFO, "Searching: " + userInput);
        if (userInputArray.length > 1) {
            inputFeedback = userArguments;
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
            logger.log(Level.INFO, "Showing user feedback");
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
            logger.log(Level.INFO, "Showing user feedback: " + userFeedback);
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
            logger.log(Level.INFO, "Showing result: " + resultString);
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
            listViewTodo.getSelectionModel().selectLast();
            listViewTodo.scrollTo(allTasks.size() - 1);
            logger.log(Level.INFO, "Restore ListView selection to last item");
        } else {
            listViewTodo.getSelectionModel().select(previousSelectedTaskIndex);
            listViewTodo.scrollTo(previousSelectedTaskIndex);
            logger.log(Level.INFO, "Restore ListView selection to previous to previous item");
        }
    }

    /**
     * 
     */
    private int getSelectedTaskIndex() {
        return listViewTodo.getSelectionModel().getSelectedIndex();
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
        logger.log(Level.INFO, "Save caret position to " + previousCaretPosition);
    }

    /**
     * 
     */
    private void restoreCaretPosition() {
        logger.log(Level.INFO, "Restore caret position to " + previousCaretPosition);
        commandBar.positionCaret(previousCaretPosition);
    }

    /**
     * 
     */
    private void moveCaretPositionToLast() {
        logger.log(Level.INFO, "Move caret position to " + commandBar.getText().length());
        commandBar.positionCaret(commandBar.getText().length());
    }

    private int getCaretCurrentPosition() {
        return commandBar.getCaretPosition();
    }

}
