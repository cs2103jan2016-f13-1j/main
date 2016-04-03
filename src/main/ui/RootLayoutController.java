package main.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.EmptyStackException;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTabPane;
import com.jfoenix.controls.JFXTextField;
import com.sun.javafx.scene.control.skin.VirtualFlow;

import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Callback;
import javafx.util.Duration;
import main.data.ParseIndexResult;
import main.data.Task;
import main.logic.AddCommand;
import main.logic.Command;
import main.logic.DeleteCommand;
import main.logic.DoneCommand;
import main.logic.EditCommand;
import main.logic.Invoker;
import main.logic.PriorityCommand;
import main.logic.Receiver;
import main.logic.SearchCommand;
import main.logic.UndoneCommand;
import main.parser.CommandParser;
import main.parser.CommandParser.InvalidLabelFormat;
import main.parser.CommandParser.InvalidTaskIndexFormat;
import main.parser.CommandParser.InvalidTitle;

@SuppressWarnings("restriction")
public class RootLayoutController implements Observer {
    private static final String STRING_COMMAND_EDIT = "edit";
    private static final String STRING_COMMAND_DELETE = "delete";
    private static final String STRING_COMMAND_DELETE_SHORTHAND = "del";
    private static final String STRING_COMMAND_SEARCH = "search";
    private static final String STRING_DOUBLE_QUOTATIONS_WITH_TEXT = "\"%1$s\"";
    private static final String STRING_TAB_TASK_SIZE = "(%1$s)";
    private static final String STRING_LISTVIEW_TODO_EMPTY = "You have no task!";
    private static final String STRING_LISTVIEW_COMPLETED_EMPTY = "You have no completed task!";
    private static final String STRING_FEEDBACK_ACTION_ADD = "Adding: ";
    private static final String STRING_FEEDBACK_ACTION_EDIT = "Editing: ";
    private static final String STRING_FEEDBACK_ACTION_DELETE = "Deleting: ";
    private static final String STRING_FEEDBACK_TOTAL_TASK = "(%1$s tasks)";
    private static final String STRING_FEEDBACK_ACTION_SEARCH = "Searching:";
    private static final String STRING_ERROR_NOT_FOUND = "Task -%1$s- not found.";
    private static final String STRING_EMPTY = "";
    private static final String STRING_WHITESPACE = " ";

    private static final KeyCombination HOTKEY_CTRL_TAB = new KeyCodeCombination(KeyCode.TAB,
            KeyCombination.CONTROL_DOWN);
    private static final KeyCombination HOTKEY_CTRL_P = new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN);
    private static final KeyCombination HOTKEY_CTRL_D = new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN);

    @FXML // fx:id="rootLayout"
    private AnchorPane rootLayout; // Value injected by FXMLLoader

    @FXML // fx:id="tabPane"
    private JFXTabPane tabPane; // Value injected by FXMLLoader

    @FXML // fx:id="tabTodo"
    private Tab tabTodo; // Value injected by FXMLLoader

    @FXML // fx:id="listViewTodo"
    private JFXListView<Task> listViewTodo; // Value injected by FXMLLoader

    @FXML // fx:id="tabCompleted"
    private Tab tabCompleted; // Value injected by FXMLLoader

    @FXML // fx:id="listViewCompleted"
    private JFXListView<Task> listViewCompleted; // Value injected by FXMLLoader

    @FXML // fx:id="commandBar"
    private JFXTextField commandBar; // Value injected by FXMLLoader

    @FXML // fx:id="chipSearchMode"
    private JFXButton chipSearchMode; // Value injected by FXMLLoader

    @FXML // fx:id="btnFeedback"
    private JFXButton btnFeedback; // Value injected by FXMLLoader

    @FXML // fx:id="groupFeedback"
    private Group groupFeedback; // Value injected by FXMLLoader

    @FXML // fx:id="textFlowFeedback"
    private TextFlow textFlowFeedback; // Value injected by FXMLLoader

    @FXML // fx:id="textUserAction"
    private Text textUserAction; // Value injected by FXMLLoader

    @FXML // fx:id="textUserParsedResult"
    private Text textUserParsedResult; // Value injected by FXMLLoader

    @FXML // fx:id="anchorPaneExecutionResult"
    private AnchorPane anchorPaneExecutionResult; // Value injected by
                                                  // FXMLLoader
    @FXML // fx:id="labelExecutedCommand"
    private Label labelExecutedCommand; // Value injected by FXMLLoader

    @FXML // fx:id="labelExecutionDetails"
    private Label labelExecutionDetails; // Value injected by FXMLLoader

    @FXML // fx:id="labelSuggestedAction"
    private Label labelSuggestedAction; // Value injected by FXMLLoader

    private VirtualFlow<IndexedCell<String>> virtualFlowTodo;
    private VirtualFlow<IndexedCell<String>> virtualFlowCompleted;

    private Invoker invoker;
    private Receiver receiver;
    private CommandParser commandParser;
    private Command commandToBeExecuted;
    private SearchCommand searchCommand;
    private Task taskToBeExecuted;
    private ArrayList<Integer> taskIndexesToBeDeleted;

    private ArrayList<Task> todoTasks;
    private ArrayList<Task> completedTasks;
    private ObservableList<Task> observableTodoTasks = FXCollections.observableArrayList();
    private ObservableList<Task> observableCompletedTasks = FXCollections.observableArrayList();
    private String inputFeedback;
    private String userInput;
    private String[] userInputArray;
    private String userCommand;
    private String userArguments;
    private int previousSelectedTaskIndex;
    private int previousCaretPosition;
    private boolean isSearchMode;

    private JFXListView<Task> currentListView;
    private ArrayList<Task> currentTaskList;

    private static final Logger logger = Logger.getLogger(RootLayoutController.class.getName());

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof Receiver) {
            logger.log(Level.INFO, "(" + commandToBeExecuted.getClass().getSimpleName() + ") update() is called");

            boolean isAddCommand = commandToBeExecuted instanceof AddCommand;
            boolean isSearchCommand = commandToBeExecuted instanceof SearchCommand;

            refreshListView();

            if (isAddCommand) {
                // TODO
            } else if (isSearchCommand) {
                showFeedback(true, STRING_FEEDBACK_ACTION_SEARCH,
                        " Found " + currentTaskList.size() + " tasks for -" + userArguments + "-");
            }
        }

    }

    public void requestFocusForCommandBar() {
        logger.log(Level.INFO, "Set focus to command bar");
        commandBar.requestFocus();
    }

    public void selectFirstItemFromListView() {
        logger.log(Level.INFO, "Set Select the first item on the ListView");
        listViewTodo.getSelectionModel().selectFirst();
        listViewCompleted.getSelectionModel().selectFirst();
        initCustomViewportBehaviorForListView();
        setCurrentTaskListAndListView(tabTodo.getText());

    }

    @FXML
    private void initialize() {
        logger.log(Level.INFO, "Initializing the UI...");

        assert rootLayout != null : "fx:id=\"rootLayout\" was not injected: check your FXML file 'RootLayout.fxml'.";
        assert tabPane != null : "fx:id=\"tabPane\" was not injected: check your FXML file 'RootLayout.fxml'.";
        assert tabTodo != null : "fx:id=\"tabTodo\" was not injected: check your FXML file 'RootLayout.fxml'.";
        assert listViewTodo != null : "fx:id=\"listViewTodo\" was not injected: check your FXML file 'RootLayout.fxml'.";
        assert tabCompleted != null : "fx:id=\"tabCompleted\" was not injected: check your FXML file 'RootLayout.fxml'.";
        assert listViewCompleted != null : "fx:id=\"listViewCompleted\" was not injected: check your FXML file 'RootLayout.fxml'.";
        assert commandBar != null : "fx:id=\"commandBar\" was not injected: check your FXML file 'RootLayout.fxml'.";
        assert chipSearchMode != null : "fx:id=\"chipSearchMode\" was not injected: check your FXML file 'RootLayout.fxml'.";
        assert btnFeedback != null : "fx:id=\"btnFeedback\" was not injected: check your FXML file 'RootLayout.fxml'.";
        assert groupFeedback != null : "fx:id=\"groupFeedback\" was not injected: check your FXML file 'RootLayout.fxml'.";
        assert textFlowFeedback != null : "fx:id=\"textFlowFeedback\" was not injected: check your FXML file 'RootLayout.fxml'.";
        assert textUserAction != null : "fx:id=\"textUserAction\" was not injected: check your FXML file 'RootLayout.fxml'.";
        assert textUserParsedResult != null : "fx:id=\"textUserParsedResult\" was not injected: check your FXML file 'RootLayout.fxml'.";
        assert anchorPaneExecutionResult != null : "fx:id=\"anchorPaneExecutionResult\" was not injected: check your FXML file 'RootLayout.fxml'.";
        assert labelExecutedCommand != null : "fx:id=\"labelExecutedCommand\" was not injected: check your FXML file 'RootLayout.fxml'.";
        assert labelExecutionDetails != null : "fx:id=\"labelExecutionDetails\" was not injected: check your FXML file 'RootLayout.fxml'.";
        assert labelSuggestedAction != null : "fx:id=\"labelSuggestedAction\" was not injected: check your FXML file 'RootLayout.fxml'.";

        initLogicAndParser();
        initTabSelectionListener();
        initListView();
        initKeyboardListener();
        initCommandBarListener();
        initSearchModeChipsLayoutListener();

        logger.log(Level.INFO, "UI initialization complete");
    }

    /**
     * 
     */
    private void initSearchModeChipsLayoutListener() {
        chipSearchMode.widthProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                // inset : top right bottom left
                if (chipSearchMode.getText().isEmpty()) {
                    commandBar.setPadding(new Insets(8, 8, 8, 8));
                } else {
                    commandBar.setPadding(new Insets(8, 8, 8, newValue.doubleValue() + 20));
                }
            }
        });
        logger.log(Level.INFO, "Adding listener to search mode chip");
    }

    /**
    *
    */
    private void initTabSelectionListener() {
        tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {

            @Override
            public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
                setCurrentTaskListAndListView(newValue.getText());
            }
        });
    }

    /**
     * 
     */
    private void initCommandBarListener() {
        logger.log(Level.INFO, "Adding listener for command bar");
        commandBar.focusedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                logger.log(Level.INFO, "Command bar focus is: " + newValue);
                if (!newValue) {
                    commandBar.requestFocus();
                }

            }
        });
        commandBar.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                // TODO Auto-generated method stub
                handleKeyStrokes();

            }
        });
        commandBar.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                // do nothing when there is no user input
                if (commandBar.getText().isEmpty()) {
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
                } else if (keyEvent.getCode() == KeyCode.BACK_SPACE) {
                    // Do not consume the event here (i.e. event.consume())
                    handleBackspaceKey();
                } else if (HOTKEY_CTRL_TAB.match(keyEvent)) {
                    handleCtrlTab();
                    keyEvent.consume();
                } else if (HOTKEY_CTRL_P.match(keyEvent)) {
                    handleCtrlP();
                    keyEvent.consume();
                } else if (HOTKEY_CTRL_D.match(keyEvent)) {
                    handleCtrlD();
                    keyEvent.consume();
                } else if (keyEvent.getCode() == KeyCode.TAB) {
                    // do nothing here to prevent the ui from changing focus
                    keyEvent.consume();
                }

                saveCaretPosition();
            }
        });
    }

    private void initLogicAndParser() {
        if (invoker == null) {
            invoker = new Invoker();
        }

        if (receiver == null) {
            receiver = Receiver.getInstance();
            receiver.addObserver(this);
        }

        if (commandParser == null) {
            commandParser = new CommandParser();
        }

    }

    /**
     * 
     */
    private void initListView() {
        if (receiver == null) {
            receiver = Receiver.getInstance();
        }

        listViewTodo.setPlaceholder(new Label(STRING_LISTVIEW_TODO_EMPTY));
        listViewTodo.setCellFactory(new Callback<ListView<Task>, ListCell<Task>>() {

            @Override
            public JFXListCell<Task> call(ListView<Task> param) {
                return new CustomListCellController();
            }
        });
        listViewTodo.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Task>() {

            @Override
            public void changed(ObservableValue<? extends Task> observable, Task oldValue, Task newValue) {
                // TODO will encounter nullpointer on first run
                adjustViewportForListView();

            }
        });

        listViewCompleted.setPlaceholder(new Label(STRING_LISTVIEW_COMPLETED_EMPTY));
        listViewCompleted.setCellFactory(new Callback<ListView<Task>, ListCell<Task>>() {

            @Override
            public ListCell<Task> call(ListView<Task> param) {
                return new CustomListCellController();
            }
        });

        populateListView();

        logger.log(Level.INFO, "Populated Todo List: " + todoTasks.size() + " task");
        logger.log(Level.INFO, "Populated Completed List: " + completedTasks.size() + " task");
    }

    /**
     * 
     */
    private void populateListView() {
        // retrieve all task and add into an ObservableList
        todoTasks = receiver.getTodoTasks();
        assert todoTasks != null;

        observableTodoTasks.setAll(todoTasks);
        // listViewTodo.setDepthProperty(1);
        listViewTodo.setItems(observableTodoTasks);

        // retrieve all task and add into an ObservableList
        completedTasks = receiver.getCompletedTasks();
        assert completedTasks != null;

        observableCompletedTasks.setAll(completedTasks);
        listViewCompleted.setItems(observableCompletedTasks);

        updateTabAndLabelWithTotalTasks();
    }

    /**
     * 
     */
    private void refreshListView() {
        saveSelectedTaskIndex();
        observableTodoTasks.clear();
        observableCompletedTasks.clear();
        populateListView();
        setCurrentTaskListAndListView(getSelectedTabName());
        restoreListViewPreviousSelection();
        // toggleUndoRedo();
    }

    private void updateTabAndLabelWithTotalTasks() {
        // if(!isEditMode){
        // labelCurrentMode.setText(getSelectedTabName());
        // }
        tabTodo.setText("To-do" + STRING_WHITESPACE + String.format(STRING_TAB_TASK_SIZE, todoTasks.size()));
        tabCompleted
                .setText("Completed" + STRING_WHITESPACE + String.format(STRING_TAB_TASK_SIZE, completedTasks.size()));

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
                virtualFlowTodo = (VirtualFlow<IndexedCell<String>>) node;
            }
        }
        for (Node node : listViewCompleted.getChildrenUnmodifiable()) {
            if (node instanceof VirtualFlow) {
                // get an instance of VirtualFlow. this is essentially the
                // viewport for ListView
                virtualFlowCompleted = (VirtualFlow<IndexedCell<String>>) node;
            }
        }
    }

    /**
     * This method is used to emulate the original behavior of a ListView, i.e.
     * the automatic scrolling of focused ListView when a selected item is not
     * visible within the viewport
     */
    private void adjustViewportForListView() {

        // firstVisibleIndexedCell =
        // getCurrentVirtualFlow().getFirstVisibleCellWithinViewPort();
        // lastVisibleIndexedCell =
        // getCurrentVirtualFlow().getLastVisibleCellWithinViewPort();

        int firstVisibleIndex = getCurrentVirtualFlow().getFirstVisibleCellWithinViewPort().getIndex();
        int lastVisibleIndex = getCurrentVirtualFlow().getLastVisibleCellWithinViewPort().getIndex();
        System.out.println("first visible cell: " + firstVisibleIndex);
        System.out.println("last visible cell: " + lastVisibleIndex);

        if (getSelectedTaskIndex() < firstVisibleIndex) {
            // viewport will scroll and show the current item at the top
            getCurrentListView().scrollTo(getSelectedTaskIndex());
        } else if (getSelectedTaskIndex() > lastVisibleIndex) {
            // viewport will scroll and show the current item at the bottom
            getCurrentListView().scrollTo(firstVisibleIndex + 1);
        }
    }

    private VirtualFlow<IndexedCell<String>> getCurrentVirtualFlow() {
        if (getSelectedTabName().equals(tabCompleted.getText())) {
            return virtualFlowCompleted;
        }
        return virtualFlowTodo;
    }

    /**
     * 
     */
    private void handleFOneKey() {
        if (invoker.isUndoAvailable()) {
            try {
                Command previousCommand = invoker.undo();
                logger.log(Level.INFO, "Pressed F2 key: UNDO operation");

                if (!chipSearchMode.getText().equals("")) {
                    invoker.execute(searchCommand);
                }
                showExecutionResult(previousCommand, "Undo");
            } catch (EmptyStackException emptyStackException) {
                logger.log(Level.WARNING, emptyStackException.getMessage());
            }
        }
    }

    /**
     * 
     */
    private void handleFTwoKey() {
        if (invoker.isRedoAvailable()) {
            try {
                Command previousCommand = invoker.redo();
                logger.log(Level.INFO, "Pressed F3 key: REDO operation");

                if (!chipSearchMode.getText().equals("")) {
                    invoker.execute(searchCommand);
                }
                showExecutionResult(previousCommand, "Redo");
            } catch (EmptyStackException emptyStackException) {
                logger.log(Level.WARNING, emptyStackException.getMessage());
            }
        }
    }

    /**
     * 
     */
    private void handleKeyStrokes() {
        if (commandParser == null) {
            commandParser = new CommandParser();
        }

        userInput = commandBar.getText().trim();
        assert userInput != null;

        if (userInput.isEmpty()) {
            logger.log(Level.INFO, "Command bar is empty");
            clearStoredUserInput();
            btnFeedback.setVisible(false);
            return;
        }

        logger.log(Level.INFO, "User is typing: " + userInput);
        btnFeedback.setVisible(true);
        extractUserInput();
        parseUserInput();

    }

    /**
     * 
     */
    private void handleArrowKeys(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.UP) {
            getCurrentListView().getSelectionModel().selectPrevious();
        } else if (keyEvent.getCode() == KeyCode.DOWN) {
            getCurrentListView().getSelectionModel().selectNext();
        }

        logger.log(Level.INFO, "Pressed " + keyEvent.getCode() + " arrow key: currently selected index is "
                + getSelectedTaskIndex() + " current listview: " + currentListView.getId());
    }

    /**
     * 
     */
    private void handleEnterKey() {
        if (commandBar.getText().trim().length() > 0) {

            if (commandToBeExecuted == null) {
                return;
            }

            logger.log(Level.INFO, "(" + commandToBeExecuted.getClass().getSimpleName() + ") Pressed ENTER key: "
                    + commandBar.getText());

            boolean IsSearchCommand = commandToBeExecuted instanceof SearchCommand;

            if (!IsSearchCommand) {
                invoker.execute(commandToBeExecuted);
                showExecutionResult(commandToBeExecuted, null);
            } else {
                isSearchMode = true;
                showSearchChipInCommandBar(isSearchMode);
            }
        }

        if (isSearchMode) {
            invoker.execute(searchCommand);
        }

        btnFeedback.setVisible(false);
        clearStoredUserInput();
        commandBar.clear();
        // showUndo();

    }

    /**
     * 
     */
    private void handleBackspaceKey() {
        if (isSearchMode && commandBar.getLength() == 0) {
            // invoker.undo();
            invoker.execute(new SearchCommand(receiver, ""));
            isSearchMode = false;
            showSearchChipInCommandBar(isSearchMode);
        }
    }

    /**
     * 
     */
    private void showSearchChipInCommandBar(boolean isVisible) {
        System.out.println("showSearchChipInCommandBar");
        if (isVisible) {
            System.out.println("Show chips:true");
            chipSearchMode.setVisible(isVisible);
            chipSearchMode.setText(STRING_COMMAND_SEARCH + STRING_WHITESPACE
                    + String.format(STRING_DOUBLE_QUOTATIONS_WITH_TEXT, userArguments));

        } else {
            System.out.println("Show chips:false");
            System.out.println(isVisible);
            chipSearchMode.setVisible(isVisible);

            // must set it to a empty string so that the the chip will resize
            // and the chip listener will resize the command bar
            chipSearchMode.setText("");
        }

    }

    /**
     * 
     */
    private void handleDeleteKey() {
        if (invoker == null) {
            invoker = new Invoker();
        }

        logger.log(Level.INFO, "Pressed DELETE key: task index  " + getSelectedTaskIndex());
        taskIndexesToBeDeleted = new ArrayList<>(1);
        taskIndexesToBeDeleted.add(getSelectedTaskIndex());
        commandToBeExecuted = new DeleteCommand(receiver, getTasksToBeDeleted(getSelectedTaskIndex()));
        invoker.execute(commandToBeExecuted);
        showExecutionResult(commandToBeExecuted, null);
    }

    /**
     * 
     */
    private void handleCtrlTab() {
        SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();

        // already at the last tab, lets bounce back to first tab
        if (selectionModel.getSelectedIndex() == tabPane.getTabs().size() - 1) {
            selectionModel.selectFirst();
        } else {
            selectionModel.selectNext();
        }

        if (getSelectedTabName().equals("To-do")) {
            currentListView = listViewTodo;
            currentTaskList = todoTasks;
        } else if (getSelectedTabName().equals("Completed")) {
            currentListView = listViewCompleted;
            currentTaskList = completedTasks;
        }

        logger.log(Level.INFO, "Pressed CTRL+TAB key: current selected Tab is " + "\"" + getSelectedTabName() + "\"");
    }

    /**
     * 
     */
    private void handleCtrlP() {
        Task oldTask = currentTaskList.get(getSelectedTaskIndex());
        commandToBeExecuted = new PriorityCommand(receiver, oldTask);
        invoker.execute(commandToBeExecuted);
        logger.log(Level.INFO, "Pressed CTRL+P key: Task " + getSelectedTaskIndex() + 1 + " Priority");
    }

    /**
     * 
     */
    private void handleCtrlD() {
        taskToBeExecuted = currentTaskList.get(getSelectedTaskIndex());

        if (getSelectedTabName().equals(tabTodo.getText())) {
            logger.log(Level.INFO, "Pressed CTRL+D key: Task " + (getSelectedTaskIndex() + 1) + " done");
            commandToBeExecuted = new DoneCommand(receiver, taskToBeExecuted);

        } else if (getSelectedTabName().equals(tabCompleted.getText())) {
            logger.log(Level.INFO, "Pressed CTRL+D key: Task " + (getSelectedTaskIndex() + 1) + " undone");
            commandToBeExecuted = new UndoneCommand(receiver, taskToBeExecuted);
        }

        invoker.execute(commandToBeExecuted);
        showExecutionResult(commandToBeExecuted, null);
        if (!chipSearchMode.getText().equals("")) {
            invoker.execute(searchCommand);
        }
        logger.log(Level.INFO, "Pressed CTRL+D key: Task " + (getSelectedTaskIndex() + 1) + " done");

    }

    /**
     * 
     */
    private void extractUserInput() {
        userInputArray = userInput.split(" ");
        userCommand = userInputArray[0].toLowerCase();
        if (userInputArray.length > 1) {
            userArguments = userInput.substring(userCommand.length() + 1);
            logger.log(Level.INFO, "Extracted user arguments: " + userArguments);
        }
    }

    /**
     * 
     */
    private void parseUserInput() {
        commandToBeExecuted = null;
        switch (userCommand) {
            case STRING_COMMAND_EDIT :
                parseEdit();
                break;
            case STRING_COMMAND_DELETE :
            case STRING_COMMAND_DELETE_SHORTHAND :
                parseDelete();
                break;
            case STRING_COMMAND_SEARCH :
                parseSearch();
                break;

            default :
                parseAdd();
        }
    }

    /**
     * 
     */
    private void parseAdd() {
        logger.log(Level.INFO, "Sending user input to commandParser: " + userInput);

        try {
            taskToBeExecuted = commandParser.parseAdd(userInput);
        } catch (InvalidTitle e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidLabelFormat e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        commandToBeExecuted = new AddCommand(receiver, taskToBeExecuted);
        inputFeedback = taskToBeExecuted.toString();
        showFeedback(true, STRING_FEEDBACK_ACTION_ADD, inputFeedback);
    }

    private void parseDelete() {
        if (userInputArray.length <= 1) {
            logger.log(Level.INFO, "DELETE command has no index. Interpreting as ADD command instead");
            parseAdd(); // no index found. parse the input as an Add operation
                        // instead
            return;
        }

        logger.log(Level.INFO, "Sending user input to commandParser: " + userInput);
        ParseIndexResult parseIndexResult;
        try {
            parseIndexResult = commandParser.parseIndexes(userInput, getCurrentTaskList().size());
            if (parseIndexResult.hasValidIndex()) {
                taskIndexesToBeDeleted = parseIndexResult.getValidIndexes();
            }
            String parseResult = taskIndexesToBeDeleted.toString();
            System.out.println("user arguments: " + userArguments);
            System.out.println("parse result: " + parseResult);

            if (taskIndexesToBeDeleted.size() == 1) {
                int taskIndex = taskIndexesToBeDeleted.get(0) - 1;

                // if selected index is out of bound
                if (taskIndex < 0 || taskIndex > currentTaskList.size()) {
                    showFeedback(true, STRING_FEEDBACK_ACTION_DELETE,
                            String.format(STRING_ERROR_NOT_FOUND, userArguments));
                    clearStoredUserInput();
                } else {
                    System.out.println("CurrentList size: " + getCurrentTaskList().size());
                    inputFeedback = currentTaskList.get(taskIndex).toString();
                    taskToBeExecuted = getCurrentTaskList().get(taskIndex);
                    showFeedback(true, STRING_FEEDBACK_ACTION_DELETE, inputFeedback);
                }

            } else {
                showFeedback(true, STRING_FEEDBACK_ACTION_DELETE, userArguments + STRING_WHITESPACE
                        + String.format(STRING_FEEDBACK_TOTAL_TASK, taskIndexesToBeDeleted.size()));
            }
        } catch (InvalidTaskIndexFormat invalidTaskIndexFormat) {
            logger.log(Level.INFO, "DELETE command index(es) invalid: " + userArguments);
            showFeedback(true, STRING_FEEDBACK_ACTION_DELETE, String.format(STRING_ERROR_NOT_FOUND, userArguments));
            clearStoredUserInput();
            return;
        }

        commandToBeExecuted = new DeleteCommand(receiver, getTasksToBeDeleted(taskIndexesToBeDeleted));

    }

    private void parseEdit() {
        if (userInputArray.length <= 1) {
            logger.log(Level.INFO, "EDIT command has no arguments. Interpreting as ADD command instead");
            // no arguments found. parse the input as an Add operation instead
            parseAdd();
            return;
        }

        int taskIndex = commandParser.getIndexForEdit(userInput);
        logger.log(Level.INFO, "EDIT command index is " + taskIndex);

        // no index is found in user input
        if (taskIndex == -1) {
            logger.log(Level.INFO, "EDIT command has no index. Editing current selected task");
            parseEditForSelectedTask();
            return;
        }

        // parsing edit command with index
        try {
            logger.log(Level.INFO, "EDIT command index is " + taskIndex);
            taskIndex--; // decrement user input index to match array natural
                         // ordering
            Task taskToBeEdited = currentTaskList.get(taskIndex);
            showFeedback(true, STRING_FEEDBACK_ACTION_EDIT, taskToBeEdited.toString());
            userArguments = userInput.substring(userInputArray[0].length() + userInputArray[1].length() + 1).trim();
            logger.log(Level.INFO, "EDIT command arguments is: " + userArguments);
            taskToBeExecuted = commandParser.parseEdit(taskToBeEdited, userArguments);
            commandToBeExecuted = new EditCommand(receiver, taskToBeEdited, taskToBeExecuted);
            return;
        } catch (IndexOutOfBoundsException ioobe) {
            ioobe.printStackTrace();
            logger.log(Level.INFO, "EDIT command index is out of range. index = " + taskIndex + " ArrayList size = "
                    + currentTaskList.size());
            showFeedback(true, STRING_FEEDBACK_ACTION_EDIT, String.format(STRING_ERROR_NOT_FOUND, userInputArray[1]));
            clearStoredUserInput();
            return;
        } catch (InvalidLabelFormat e) {
            e.printStackTrace();
        }

    }

    /**
     * Parse the Edit command for the currently selected task item on the List
     * 
     * @param @throws
     */
    private void parseEditForSelectedTask() {
        Task taskToBeEdited = currentTaskList.get(getSelectedTaskIndex());
        showFeedback(true, STRING_FEEDBACK_ACTION_EDIT, taskToBeEdited.toString());
        try {
            logger.log(Level.INFO, "EDIT command arguments is: " + userArguments);
            taskToBeExecuted = commandParser.parseEdit(taskToBeEdited, userArguments);
            logger.log(Level.INFO, "EDIT command editedTaskObject is: " + taskToBeExecuted.toString());
            commandToBeExecuted = new EditCommand(receiver, taskToBeEdited, taskToBeExecuted);
            return;
        } catch (InvalidLabelFormat e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 
     */
    private void parseSearch() {
        // if (userInput.equals(STRING_COMMAND_SEARCH)) {
        // logger.log(Level.INFO, "SEARCH command has no arguments. Interpreting
        // as ADD command instead");
        // // no arguments found. parse the input as an Add operation instead
        // parseAdd();
        // return;
        // }
        //
        // // this allow a search without a search term
        // if (userInput.equals(STRING_COMMAND_SEARCH + STRING_WHITESPACE)) {
        // logger.log(Level.INFO, "Searching: " + userInput);
        // commandToBeExecuted = new SearchCommand(receiver, STRING_WHITESPACE);
        // userArguments = STRING_WHITESPACE;
        // invoker.execute(commandToBeExecuted);
        // return;
        // }

        if (userInput.equals(STRING_COMMAND_SEARCH)) {
            userArguments = STRING_WHITESPACE;
        }

        logger.log(Level.INFO, "Searching: " + userArguments);

        Date dateFromUserInput = commandParser.getDateForSearch(userArguments);

        // search input contains no date
        if (dateFromUserInput == null) {
            logger.log(Level.INFO, "SEARCH command has no date: " + userArguments);
            commandToBeExecuted = new SearchCommand(receiver, userArguments);
            searchCommand = new SearchCommand(receiver, userArguments.toString());
        } else {
            logger.log(Level.INFO, "SEARCH command has date: " + userArguments);
            commandToBeExecuted = new SearchCommand(receiver, dateFromUserInput);
            searchCommand = new SearchCommand(receiver, dateFromUserInput);
        }
        invoker.execute(commandToBeExecuted);
    }

    private ArrayList<Task> getTasksToBeDeleted(int taskIndex) {
        ArrayList<Task> tasksToBeDeleted = new ArrayList<>(1);
        tasksToBeDeleted.add(currentTaskList.get(taskIndex));
        return tasksToBeDeleted;
    }

    private ArrayList<Task> getTasksToBeDeleted(ArrayList<Integer> taskIndexes) {
        ArrayList<Task> tasksToBeDeleted = new ArrayList<>(taskIndexes.size());
        for (Integer i : taskIndexes) {
            tasksToBeDeleted.add(currentTaskList.get(i - 1));
        }
        return tasksToBeDeleted;
    }

    /**
     * 
     */
    private void clearStoredUserInput() {
        userInput = STRING_EMPTY;
        userInputArray = null;
        userCommand = STRING_EMPTY;
        userArguments = STRING_EMPTY;
    }

    /**
     * 
     */
    private void showFeedback(boolean isVisible, String userAction, String userFeedback) {
        if (isVisible) {
            logger.log(Level.INFO, "Showing user feedback: " + userFeedback);
        }

        textUserAction.setText(userAction);
        // textUserAction.setFont(new Font(20));
        textUserAction.setFill(Color.web("303F9F", 0.7));
        textUserParsedResult.setText(userFeedback);
        // textUserParsedResult.setFont(new Font(20));
        textUserParsedResult.setFill(Color.web("#00111a", 0.7));

        // textFlowFeedback.getChildren().clear();
        // textFlowFeedback.getChildren().addAll(textUserAction,
        // textUserParsedResult);

        // labelUserAction.setVisible(isVisible);
        // labelUserParsedInput.setVisible(isVisible);
        // labelUserAction.setText(userAction);
        // labelUserParsedInput.setText(userFeedback);
    }

    /**
     * 
     */
    private void showExecutionResult(Command executedCommand, String undoOrRedo) {
        logger.log(Level.INFO, "Showing user execution result: ");

        if (executedCommand instanceof AddCommand) {
            if (undoOrRedo != null) {
                labelExecutedCommand.setText(undoOrRedo + STRING_WHITESPACE + "add.");
                labelExecutedCommand.setTextFill(Color.web(AppColor.PRIMARY_WHITE));
                labelExecutionDetails.setTextFill(Color.web(AppColor.PRIMARY_WHITE, 0));

            } else {
                labelExecutedCommand.setText("Added:");
                labelExecutedCommand.setTextFill(Color.web(AppColor.PRIMARY_BLUE_LIGHT, 0.7));
                labelExecutionDetails.setText(taskToBeExecuted.toString());
            }

        }

        if (executedCommand instanceof EditCommand) {
            if (undoOrRedo != null) {
                labelExecutedCommand.setText(undoOrRedo + STRING_WHITESPACE + "edit.");
                labelExecutedCommand.setTextFill(Color.web(AppColor.PRIMARY_WHITE));
                labelExecutionDetails.setTextFill(Color.web(AppColor.PRIMARY_WHITE, 0));
            } else {
                labelExecutedCommand.setText("Edited:");
                labelExecutedCommand.setTextFill(Color.web(AppColor.PRIMARY_GREEN_LIGHT, 0.7));
                labelExecutionDetails.setText(taskToBeExecuted.toString());
            }

        }
        // TODO deleting something will not show the deleted item toString()
        if (executedCommand instanceof DeleteCommand) {
            if (undoOrRedo != null) {
                labelExecutedCommand.setText(undoOrRedo + STRING_WHITESPACE + "delete.");
                labelExecutedCommand.setTextFill(Color.web(AppColor.PRIMARY_WHITE));
                labelExecutionDetails.setTextFill(Color.web(AppColor.PRIMARY_WHITE, 0));
            } else {
                labelExecutedCommand.setText("Deleted:");
                labelExecutedCommand.setTextFill(Color.web(AppColor.PRIMARY_RED_LIGHT, 0.7));

                if (taskIndexesToBeDeleted.size() == 1) {
                    labelExecutionDetails.setText(taskToBeExecuted.toString());
                } else if (taskIndexesToBeDeleted.size() > 1) {
                    labelExecutionDetails.setText(taskIndexesToBeDeleted.size() + " tasks");
                }

            }

        }

        if (executedCommand instanceof SearchCommand) {
            // textCommandExecuted.setText("Searching:");
            // textCommandExecuted.setFill(Color.web("303F9F", 0.7));
            // textExecutionDetails.setText(userFeedback);

        }

        if (executedCommand instanceof DoneCommand) {
            if (undoOrRedo != null) {
                labelExecutedCommand.setText(undoOrRedo + STRING_WHITESPACE + "task completed.");
                labelExecutedCommand.setTextFill(Color.web(AppColor.PRIMARY_WHITE));
                labelExecutionDetails.setTextFill(Color.web(AppColor.PRIMARY_WHITE, 0));
            } else {
                labelExecutedCommand.setText("Task complete.");
                labelExecutedCommand.setTextFill(Color.web(AppColor.PRIMARY_LIME_LIGHT, 0.7));
                labelExecutionDetails.setTextFill(Color.web(AppColor.PRIMARY_WHITE, 0));
            }

        }

        if (executedCommand instanceof UndoneCommand) {
            if (undoOrRedo != null) {
                labelExecutedCommand.setText(undoOrRedo + STRING_WHITESPACE + "mark task as incomplete.");
                labelExecutedCommand.setTextFill(Color.web(AppColor.PRIMARY_WHITE));
                labelExecutionDetails.setTextFill(Color.web(AppColor.PRIMARY_WHITE, 0));
            } else {
                labelExecutedCommand.setText("Mark task as incomplete.");
                labelExecutedCommand.setTextFill(Color.web(AppColor.PRIMARY_LIME_LIGHT, 0.7));
                labelExecutionDetails.setTextFill(Color.web(AppColor.PRIMARY_WHITE, 0));
            }

        }

        // textFlowFeedback.getChildren().clear();
        // textFlowFeedback.getChildren().addAll(textUserAction,
        // textUserParsedResult);

        // labelUserAction.setVisible(isVisible);
        // labelUserParsedInput.setVisible(isVisible);
        // labelUserAction.setText(userAction);
        // labelUserParsedInput.setText(userFeedback);

        if (undoOrRedo != null) {
            if (undoOrRedo.equals("Undo")) {
                labelSuggestedAction.setText("REDO (F3)");
            } else if (undoOrRedo.equals("Redo")) {
                labelSuggestedAction.setText("UNDO (F2)");
            }

        }

        FadeTransition appearVisible = new FadeTransition(Duration.seconds(5), anchorPaneExecutionResult);
        appearVisible.setFromValue(1);
        appearVisible.setToValue(1);
        appearVisible.setCycleCount(1);
        FadeTransition fade = new FadeTransition(Duration.seconds(1), anchorPaneExecutionResult);
        fade.setFromValue(1);
        fade.setToValue(0);
        fade.setCycleCount(1);

        SequentialTransition st = new SequentialTransition();
        st.getChildren().clear();
        st.getChildren().addAll(appearVisible, fade);
        st.play();

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
        if (previousSelectedTaskIndex == getCurrentTaskList().size()) {
            getCurrentListView().getSelectionModel().selectLast();
            // getCurrentListView().scrollTo(getCurrentTaskList().size() - 1);
            logger.log(Level.INFO, "Restore ListView selection to last item");
        } else {
            System.out.println(getCurrentListView().getId());
            System.out.println(previousSelectedTaskIndex);
            getCurrentListView().getSelectionModel().select(previousSelectedTaskIndex);
            // getCurrentListView().scrollTo(previousSelectedTaskIndex);
            logger.log(Level.INFO, "Restore ListView selection to previous to previous item");
        }
    }

    private JFXListView<Task> getCurrentListView() {
        return currentListView;
    }

    private ArrayList<Task> getCurrentTaskList() {
        return currentTaskList;
    }

    private void setCurrentTaskListAndListView(String tabName) {
        if (tabName.equals(tabTodo.getText())) {
            currentListView = listViewTodo;
            currentTaskList = todoTasks;
        } else if (tabName.equals(tabCompleted.getText())) {
            currentListView = listViewCompleted;
            currentTaskList = completedTasks;
        } else {
            System.out.println("wtf: " + tabName);
        }
    }

    /**
     * 
     */
    private int getSelectedTaskIndex() {
        return getCurrentListView().getSelectionModel().getSelectedIndex();
    }

    /**
     * @return String
     */
    private String getSelectedTabName() {
        System.out.println(tabPane.getSelectionModel().getSelectedItem().getText());
        return tabPane.getSelectionModel().getSelectedItem().getText();
    }

    /**
     * 
     */
    private void saveCaretPosition() {
        previousCaretPosition = commandBar.getCaretPosition();
        logger.log(Level.INFO, "Save caret position to " + previousCaretPosition);
    }
}