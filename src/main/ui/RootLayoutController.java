//@@author A0126400Y
package main.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.EmptyStackException;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialog.DialogTransition;
import com.jfoenix.controls.JFXTabPane;
import com.jfoenix.controls.JFXTextField;

import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Duration;
import main.data.ParseIndexResult;
import main.data.Task;
import main.data.TaskHeader;
import main.logic.AddCommand;
import main.logic.Command;
import main.logic.DeleteCommand;
import main.logic.DoneCommand;
import main.logic.EditCommand;
import main.logic.Invoker;
import main.logic.PriorityCommand;
import main.logic.Receiver;
import main.logic.SearchCommand;
import main.logic.SetFileLocationCommand;
import main.logic.UndoneCommand;
import main.parser.CommandParser;
import main.parser.exceptions.InvalidLabelFormat;
import main.parser.exceptions.InvalidTaskIndexFormat;

public class RootLayoutController implements Observer {
    private static final String STRING_COMMAND_EDIT = "edit";
    private static final String STRING_COMMAND_DELETE = "delete";
    private static final String STRING_COMMAND_DELETE_SHORTHAND = "del";
    private static final String STRING_COMMAND_SEARCH = "search";
    private static final String STRING_COMMAND_DONE = "done";
    private static final String STRING_COMMAND_UNDONE = "undone";
    private static final String STRING_COMMAND_UNDO = "undo";
    private static final String STRING_COMMAND_REDO = "redo";
    private static final String STRING_COMMAND_SET_FILE_LOCATION = "set";
    private static final String STRING_COMMAND_EXIT = "exit";
    private static final String STRING_COMMAND_CLOSE = "close";
    private static final String STRING_COMMAND_QUIT = "quit";
    private static final String STRING_DOUBLE_QUOTATIONS_WITH_TEXT = "\"%1$s\"";
    private static final String STRING_TAB_TASK_SIZE = "(%1$s)";
    private static final String STRING_FEEDBACK_ACTION_ADD = "Adding:";
    private static final String STRING_FEEDBACK_ACTION_EDIT = "Editing:";
    private static final String STRING_FEEDBACK_ACTION_DELETE = "Deleting:";
    private static final String STRING_FEEDBACK_TOTAL_TASK = "(%1$s tasks)";
    private static final String STRING_FEEDBACK_ACTION_SEARCH = "Searching:";
    private static final String STRING_FEEDBACK_ACTION_DONE = "Mark as done:";
    private static final String STRING_FEEDBACK_ACTION_UNDONE = "Mark as undone:";
    private static final String STRING_FEEDBACK_ACTION_UNDO = "Undoing:";
    private static final String STRING_FEEDBACK_ACTION_REDO = "Redoing:";
    private static final String STRING_FEEDBACK_ACTION_EXIT = "Exit Dooleh";
    private static final String STRING_FEEDBACK_TOTAL_ACTION = "%1$s action(s)";
    private static final String STRING_FEEDBACK_ACTION_SET_FILE_LOCATION = "Set file location";
    private static final String STRING_ERROR_NOT_FOUND = "Task %1$s not found.";
    private static final String STRING_EMPTY = "";
    private static final String STRING_WHITESPACE = " ";

    private static final KeyCombination HOTKEY_CTRL_TAB = new KeyCodeCombination(KeyCode.TAB,
            KeyCombination.CONTROL_DOWN);
    private static final KeyCombination HOTKEY_CTRL_P = new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN);
    private static final KeyCombination HOTKEY_CTRL_D = new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN);

    @FXML // fx:id="ListViewController"
    private ListViewController todoListViewController;

    @FXML // fx:id="ListViewController"
    private ListViewController completedListViewController;

    @FXML // fx:id="rootLayout"
    private AnchorPane rootLayout;

    @FXML // fx:id="buttonUndo"
    private JFXButton buttonUndo;

    @FXML // fx:id="buttonRedo"
    private JFXButton buttonRedo;

    @FXML // fx:id="buttonHelp"
    private JFXButton buttonHelp;

    @FXML // fx:id="iconUndo"
    private SVGPath iconUndo;

    @FXML // fx:id="iconRedo"
    private SVGPath iconRedo;

    @FXML // fx:id="dialogHelp"
    private JFXDialog dialogHelp;

    @FXML // fx:id="dialogContainer"
    private StackPane dialogContainer;

    @FXML // fx:id="totalTasksOverdue"
    private Text totalTasksOverdue;

    @FXML // fx:id="totalTasksToday"
    private Text totalTasksToday;

    @FXML // fx:id="totalTasksTomorrow"
    private Text totalTasksTomorrow;

    @FXML // fx:id="totalTastotalTasksUpcomingksToday"
    private Text totalTasksUpcoming;

    @FXML // fx:id="totalTasksSomeday"
    private Text totalTasksSomeday;

    @FXML // fx:id="tabPane"
    private JFXTabPane tabPane;

    @FXML // fx:id="tabTodo"
    private Tab tabTodo;

    @FXML // fx:id="tabCompleted"
    private Tab tabCompleted;

    @FXML // fx:id="commandBar"
    private JFXTextField commandBar;

    @FXML // fx:id="chipSearchMode"
    private JFXButton chipSearchMode;

    @FXML // fx:id="btnFeedback"
    private JFXButton btnFeedback;

    @FXML // fx:id="groupFeedback"
    private Group groupFeedback;

    @FXML // fx:id="textFlowFeedback"
    private TextFlow textFlowFeedback;

    @FXML // fx:id="textUserAction"
    private Text textUserAction;

    @FXML // fx:id="textUserParsedResult"
    private Text textUserParsedResult;

    @FXML // fx:id="anchorPaneExecutionResult"
    private AnchorPane anchorPaneExecutionResult;

    @FXML // fx:id="labelExecutedCommand"
    private Label labelExecutedCommand;

    @FXML // fx:id="labelExecutionDetails"
    private Label labelExecutionDetails;

    @FXML // fx:id="labelSuggestedAction"
    private Label labelSuggestedAction;

    private MainApp mainApp;

    private Invoker invoker;
    private Receiver receiver;
    private CommandParser commandParser;
    private Command commandToBeExecuted;
    private SearchCommand searchCommand;

    private Task taskToBeExecuted;
    private ArrayList<Task> listOfTaskToBeExecuted = new ArrayList<>();
    private ArrayList<Integer> taskIndexesToBeExecuted = new ArrayList<>();

    private ArrayList<Task> todoTasks;
    private ArrayList<Task> completedTasks;
    private ArrayList<Task> todoTasksWithHeaders;
    private ArrayList<Task> completedTasksWithHeaders;
    private String inputFeedback;
    private String userInput;
    private String[] userInputArray;
    private String userCommand;
    private String userArguments;
    private int previousCaretPosition;
    private boolean isSearchMode;
    private boolean isUndoRedo;
    private int numberOfActions;

    private ArrayList<Task> currentList;

    private ListViewController currentListViewController;

    private static final Logger logger = Logger.getLogger(RootLayoutController.class.getName());

    /**
     * Use to set {@code allTasks} with a new {@code ArrayList}
     *
     * @param observable
     * @param observable
     */
    @Override
    public void update(Observable observable, Object arg) {
        if (observable instanceof Receiver) {
            if (commandToBeExecuted != null) {
                logger.log(Level.INFO, "(" + commandToBeExecuted.getClass().getSimpleName() + ") update() is called");

                refreshListView();
                refreshUndoRedoState();
                // TODO do something about this
                if (isUndoRedo) {
                    isUndoRedo = false;
//                    System.out.println("IS UNDO, selected index: " + getCurrentListViewController().getSelectedIndex());
//                    return;
                }

                boolean isAddCommand = commandToBeExecuted instanceof AddCommand;
                boolean isDeleteCommand = commandToBeExecuted instanceof DeleteCommand;
                boolean isEditCommand = commandToBeExecuted instanceof EditCommand;
                boolean isDoneCommand = commandToBeExecuted instanceof DoneCommand;
                boolean isUndoneCommand = commandToBeExecuted instanceof UndoneCommand;
                boolean isSearchCommand = commandToBeExecuted instanceof SearchCommand;

                if (isAddCommand || isEditCommand) {
                    int displayIndex = getCurrentListViewController().getDisplayIndex(getIndexFromLastExecutedTask());
                    getCurrentListViewController().clearListViewSelection();
                    getCurrentListViewController().select(displayIndex);
                } else if (isDeleteCommand || isDoneCommand || isUndoneCommand) {
                    getCurrentListViewController().clearListViewSelection();
                    if (getCurrentListViewController().getPreviousSelectedIndex() >= getCurrentList().size() - 1) {
                        getCurrentListViewController().selectLast();
                    } else {
                        // select back the previous first index that was in the
                        // range
                        if (getCurrentList().size() > 0) {
                            getCurrentListViewController().select(taskIndexesToBeExecuted.get(0));
                        }
                    }
                } else if (isSearchCommand) {
                    if (userArguments.equals(" ")) {
                        showFeedback(true, STRING_FEEDBACK_ACTION_SEARCH, "");
                    } else {
                        int numberOfTasks = 0;
                        for (Task task : currentList) {
                            if (!(task instanceof TaskHeader)) {
                                numberOfTasks++;
                            }
                        }

                        showFeedback(true, STRING_FEEDBACK_ACTION_SEARCH,
                                " Found " + numberOfTasks + " task(s) for \"" + userArguments + "\"");
                    }
                }
            } else {
                if (isUndoRedo) {
                    refreshListView();
                }
            }
        }
    }

    /**
     * @return void
     */
    private void refreshUndoRedoState() {
        if (invoker.isUndoAvailable()) {
            iconUndo.setFill(Color.web(AppColor.PRIMARY_WHITE));
        } else {
            iconUndo.setFill(Color.web(AppColor.PRIMARY_WHITE, 0.4));
        }
        if (invoker.isRedoAvailable()) {
            iconRedo.setFill(Color.web(AppColor.PRIMARY_WHITE));
        } else {
            iconRedo.setFill(Color.web(AppColor.PRIMARY_WHITE, 0.4));
        }
    }

    /**
     *
     * @return
     */
    public int getIndexFromLastExecutedTask() {
        return getCurrentList().indexOf(taskToBeExecuted);
    }

    public void requestFocusForCommandBar() {
        logger.log(Level.INFO, "Set focus to command bar");
        commandBar.requestFocus();
    }

    public void initListViewBehavior() {
        todoListViewController.initListViewBehavior();
        completedListViewController.initListViewBehavior();
    }

    public void selectFirstItemFromListView() {
        logger.log(Level.INFO, "Set Select the first item on the ListView");
        todoListViewController.selectListViewFirstItem();
        completedListViewController.selectListViewFirstItem();
        setCurrentListViewController(tabTodo.getText());
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void initialize() {
        logger.log(Level.INFO, "Initializing the UI...");

        assertDependencyInjection();
        initLogicAndParser();
        initListView();
        initTabSelectionListener();
        initKeyboardListener();
        initCommandBarListener();
        initSearchModeChipsLayoutListener();
        initToolBarButtons();

        logger.log(Level.INFO, "UI initialization complete");
    }

    private void initToolBarButtons() {
        buttonUndo.setOnMouseClicked(new EventHandler<Event>() {

            @Override
            public void handle(Event event) {
                handleUndo();

            }
        });
        buttonRedo.setOnMouseClicked(new EventHandler<Event>() {

            @Override
            public void handle(Event event) {
                handleRedo();

            }
        });

        dialogHelp.setContent(new HelpPage());
        dialogHelp.setTransitionType(DialogTransition.CENTER);
        buttonHelp.setOnMouseClicked(new EventHandler<Event>() {

            @Override
            public void handle(Event event) {
                toggleHelpDialog();

            }
        });
    }

    private void assertDependencyInjection() {
        assert rootLayout != null : "fx:id=\"rootLayout\" was not injected: check your FXML file 'RootLayout.fxml'.";
        assert buttonUndo != null : "fx:id=\"buttonUndo\" was not injected: check your FXML file 'RootLayout.fxml'.";
        assert buttonRedo != null : "fx:id=\"buttonRedo\" was not injected: check your FXML file 'RootLayout.fxml'.";
        assert buttonHelp != null : "fx:id=\"buttonHelp\" was not injected: check your FXML file 'RootLayout.fxml'.";
        assert iconRedo != null : "fx:id=\"iconRedo\" was not injected: check your FXML file 'RootLayout.fxml'.";
        assert iconUndo != null : "fx:id=\"iconUndo\" was not injected: check your FXML file 'RootLayout.fxml'.";
        assert dialogContainer != null : "fx:id=\"dialogContainer\" was not injected: check your FXML file 'RootLayout.fxml'.";
        assert dialogHelp != null : "fx:id=\"dialogHelp\" was not injected: check your FXML file 'RootLayout.fxml'.";
        assert totalTasksOverdue != null : "fx:id=\"totalTasksOverdue\" was not injected: check your FXML file 'RootLayout.fxml'.";
        assert totalTasksToday != null : "fx:id=\"totalTasksToday\" was not injected: check your FXML file 'RootLayout.fxml'.";
        assert totalTasksTomorrow != null : "fx:id=\"totalTasksTomorrow\" was not injected: check your FXML file 'RootLayout.fxml'.";
        assert totalTasksUpcoming != null : "fx:id=\"totalTasksUpcoming\" was not injected: check your FXML file 'RootLayout.fxml'.";
        assert totalTasksSomeday != null : "fx:id=\"totalTasksSomeday\" was not injected: check your FXML file 'RootLayout.fxml'.";
        assert tabPane != null : "fx:id=\"tabPane\" was not injected: check your FXML file 'RootLayout.fxml'.";
        assert tabTodo != null : "fx:id=\"tabTodo\" was not injected: check your FXML file 'RootLayout.fxml'.";
        assert tabCompleted != null : "fx:id=\"tabCompleted\" was not injected: check your FXML file 'RootLayout.fxml'.";
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
                setCurrentListViewController(newValue.getText());
                setCurrentList(newValue.getText());
                updateTotalTasksForEveryCategory();
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
                handleKeyStrokes(newValue);

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
                } else if (keyEvent.getCode() == KeyCode.ESCAPE) {
                    handleEscKey();
                } else if (keyEvent.getCode() == KeyCode.F1) {
                    handleUndo();
                    keyEvent.consume();
                } else if (keyEvent.getCode() == KeyCode.F2) {
                    handleRedo();
                    keyEvent.consume();
                } else if (keyEvent.getCode() == KeyCode.F3) {
                    toggleHelpDialog();
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

            private void handleEscKey() {
                dialogHelp.close();
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
     * In ListController now
     */
    private void initListView() {
        populateListView();
        todoListViewController.setRootLayoutController(this);
        completedListViewController.setRootLayoutController(this);
    }

    /**
     * In ListController now
     *
     */
    private void populateListView() {
        getTasksFromReceiver();
        todoListViewController.populateListView(todoTasks);
        completedListViewController.populateListView(completedTasks);
        updateListWithHeaders();
        updateTabAndLabelWithTotalTasks();
        setCurrentList(getSelectedTabName());
        setCurrentListViewController(getSelectedTabName());
        updateTotalTasksForEveryCategory();

        logger.log(Level.INFO, "Populated Todo List: " + todoTasks.size() + " task");
        logger.log(Level.INFO, "Populated Completed List: " + completedTasks.size() + " task");
    }

    /**
     * In ListController now
     *
     */
    private void refreshListView() {
        getTasksFromReceiver();
        todoListViewController.refreshListView(todoTasks);
        completedListViewController.refreshListView(completedTasks);
        updateListWithHeaders();
        updateTabAndLabelWithTotalTasks();
        setCurrentList(getSelectedTabName());
        setCurrentListViewController(getSelectedTabName());
        updateTotalTasksForEveryCategory();
    }

    private void updateTotalTasksForEveryCategory() {
        totalTasksOverdue.setText(String.valueOf(getCurrentListViewController().getTotalOverdueTasks()));
        totalTasksToday.setText(String.valueOf(getCurrentListViewController().getTotalTodayTasks()));
        totalTasksTomorrow.setText(String.valueOf(getCurrentListViewController().getTotalTomorrowTasks()));
        totalTasksUpcoming.setText(String.valueOf(getCurrentListViewController().getTotalUpcomingTasks()));
        totalTasksSomeday.setText(String.valueOf(getCurrentListViewController().getTotalSomedayTasks()));
    }

    private void updateListWithHeaders() {
        todoTasksWithHeaders = todoListViewController.getTaskList();
        completedTasksWithHeaders = completedListViewController.getTaskList();
    }

    private void getTasksFromReceiver() {
        todoTasks = receiver.getTodoTasks();
        assert todoTasks != null;
        completedTasks = receiver.getCompletedTasks();
        assert completedTasks != null;

    }

    private void updateTabAndLabelWithTotalTasks() {
        tabTodo.setText("To-do" + STRING_WHITESPACE + String.format(STRING_TAB_TASK_SIZE, todoTasks.size()));
        tabCompleted
                .setText("Completed" + STRING_WHITESPACE + String.format(STRING_TAB_TASK_SIZE, completedTasks.size()));

    }

    /**
     *
     */
    private void handleUndo() {
        if (invoker.isUndoAvailable()) {
            try {
                isUndoRedo = true;
                Command previousCommand = invoker.undo();
                logger.log(Level.INFO, "Pressed F1 key: UNDO operation");

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
    private void handleRedo() {
        if (invoker.isRedoAvailable()) {
            try {
                isUndoRedo = true;
                Command previousCommand = invoker.redo();
                logger.log(Level.INFO, "Pressed F2 key: REDO operation");

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
    private void handleKeyStrokes(String input) {
        if (commandParser == null) {
            commandParser = new CommandParser();
        }
        userInput = input;
        userInput = userInput.trim();
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
        getCurrentListViewController().handleArrowKeys(keyEvent);
    }

    /**
     *
     */
    private void handleEnterKey() {
        if (commandBar.getText().trim().length() > 0) {
            if (userCommand.equals(STRING_COMMAND_EXIT) || userCommand.equals(STRING_COMMAND_QUIT)
                    || userCommand.equals(STRING_COMMAND_CLOSE)) {
                System.exit(0);
            }
            if (userCommand.equals(STRING_COMMAND_UNDO)) {
                if (numberOfActions < 0) {
                    handleUndo();
                } else {
                    for (int i = 0; i < numberOfActions; i++) {
                        handleUndo();
                    }
                }
                resetStateAfterExecution();
                return;

            }

            if (userCommand.equals(STRING_COMMAND_REDO)) {
                if (numberOfActions < 0) {
                    handleRedo();
                } else {
                    for (int i = 0; i < numberOfActions; i++) {
                        handleRedo();
                    }
                }
                resetStateAfterExecution();
                return;
            }

            if (commandToBeExecuted == null) {
                return;
            }

            logger.log(Level.INFO, "(" + commandToBeExecuted.getClass().getSimpleName() + ") Pressed ENTER key: "
                    + commandBar.getText());

            boolean isSearchCommand = commandToBeExecuted instanceof SearchCommand;
            boolean isSetFileLocationCommand = commandToBeExecuted instanceof SetFileLocationCommand;

            if (isSetFileLocationCommand) {
                if (userInputArray.length <= 1) {
                    File selectedFile = showFileChooserDialog();
                    if (selectedFile == null) {
                        return;
                    }
                    String selectedFilePath = getFilePath(selectedFile);
                    commandToBeExecuted = new SetFileLocationCommand(receiver, selectedFilePath);
                }
                invoker.execute(commandToBeExecuted);
            }

            if (!isSearchCommand) {
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

        resetStateAfterExecution();

    }

    private void resetStateAfterExecution() {
        btnFeedback.setVisible(false);
        clearStoredUserInput();
        commandBar.clear();
    }

    /**
     *
     */
    private void handleBackspaceKey() {
        if (isSearchMode && commandBar.getLength() == 0) {
            // invoker.undo();
            commandToBeExecuted = new SearchCommand(receiver, "");
            invoker.execute(commandToBeExecuted);
            isSearchMode = false;
            showSearchChipInCommandBar(isSearchMode);
        }
    }

    /**
     *
     */
    private void showSearchChipInCommandBar(boolean isVisible) {
        if (isVisible) {
            chipSearchMode.setVisible(isVisible);
            chipSearchMode.setText(STRING_COMMAND_SEARCH + STRING_WHITESPACE
                    + String.format(STRING_DOUBLE_QUOTATIONS_WITH_TEXT, userArguments));

        } else {
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
        logger.log(Level.INFO, "Pressed DELETE key: task index  " + getCurrentListViewController().getSelectedIndex());
        int taskIndex = getCurrentListViewController().getSelectedIndex();
        int actualIndex = getCurrentListViewController().getDisplayIndex(taskIndex);

        taskIndexesToBeExecuted = new ArrayList<>(1);
        taskIndexesToBeExecuted.add(actualIndex);
        listOfTaskToBeExecuted = getTasksToBeExecuted(taskIndexesToBeExecuted);
        taskToBeExecuted = getCurrentList().get(getCurrentListViewController().getSelectedIndex());
        commandToBeExecuted = new DeleteCommand(receiver, getTasksToBeExecuted(taskIndexesToBeExecuted));
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

        logger.log(Level.INFO, "Pressed CTRL+TAB key: current selected Tab is " + "\"" + getSelectedTabName() + "\"");
    }

    /**
     *
     */
    private void handleCtrlP() {
        Task oldTask = currentList.get(getCurrentListViewController().getSelectedIndex());
        commandToBeExecuted = new PriorityCommand(receiver, oldTask);
        invoker.execute(commandToBeExecuted);
        logger.log(Level.INFO,
                "Pressed CTRL+P key: Task " + getCurrentListViewController().getSelectedIndex() + 1 + " Priority");
    }

    /**
     *
     */
    private void handleCtrlD() {
        taskToBeExecuted = getCurrentList().get(getCurrentListViewController().getSelectedIndex());

        if (getSelectedTabName().equals(tabTodo.getText())) {
            logger.log(Level.INFO,
                    "Pressed CTRL+D key: Task " + (getCurrentListViewController().getSelectedIndex() + 1) + " done");
            taskIndexesToBeExecuted.clear();
            taskIndexesToBeExecuted.add(getCurrentListViewController().getSelectedIndex());
            // taskToBeExecuted =
            // getCurrentTaskList().get(getCurrentListViewController().getPreviousSelectedIndex());
            listOfTaskToBeExecuted.clear();
            listOfTaskToBeExecuted.add(taskToBeExecuted);
            commandToBeExecuted = new DoneCommand(receiver, listOfTaskToBeExecuted);

        } else if (getSelectedTabName().equals(tabCompleted.getText())) {
            logger.log(Level.INFO,
                    "Pressed CTRL+D key: Task " + (getCurrentListViewController().getSelectedIndex() + 1) + " undone");
            taskIndexesToBeExecuted.clear();
            taskIndexesToBeExecuted.add(getCurrentListViewController().getSelectedIndex());
            // taskToBeExecuted =
            // getCurrentTaskList().get(previousSelectedTaskIndex);
            listOfTaskToBeExecuted.clear();
            listOfTaskToBeExecuted.add(taskToBeExecuted);
            commandToBeExecuted = new UndoneCommand(receiver, listOfTaskToBeExecuted);
        }

        invoker.execute(commandToBeExecuted);

        showExecutionResult(commandToBeExecuted, null);
        if (!chipSearchMode.getText().equals("")) {
            invoker.execute(searchCommand);
        }
        logger.log(Level.INFO,
                "Pressed CTRL+D key: Task " + (getCurrentListViewController().getSelectedIndex() + 1) + " done");

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
            case STRING_COMMAND_DONE :
                parseDone();
                break;
            case STRING_COMMAND_UNDONE :
                parseUndone();
                break;
            case STRING_COMMAND_UNDO :
                parseUndo();
                break;
            case STRING_COMMAND_REDO :
                parseRedo();
                break;
            case STRING_COMMAND_SET_FILE_LOCATION :
                parseSetFileLocation();
                break;
            case STRING_COMMAND_EXIT :
            case STRING_COMMAND_CLOSE :
            case STRING_COMMAND_QUIT :
                parseExit();
                break;
            default:
                parseAdd();
        }
    }

    private void parseExit() {
        showFeedback(true, STRING_FEEDBACK_ACTION_EXIT, "");
    }

    /**
     *
     */
    private void parseAdd() {
        logger.log(Level.INFO, "Sending user input to commandParser: " + userInput);

        try {
            taskToBeExecuted = commandParser.parseAdd(userInput);
            commandToBeExecuted = new AddCommand(receiver, taskToBeExecuted);
            inputFeedback = taskToBeExecuted.toString();
            showFeedback(true, STRING_FEEDBACK_ACTION_ADD, inputFeedback);

        } catch (InvalidLabelFormat e) {
            logger.log(Level.INFO, "ADD command has invalid label.");
        }
    }

    private void parseDelete() {
        if (userInputArray.length <= 1) {
            logger.log(Level.INFO, "DELETE command has no index. Interpreting as ADD command instead");
            parseAdd(); // no index found. parse the input as an Add operation
                        // instead
            return;
        }

        if (userArguments.toLowerCase().equals("all")) {
            // previousSelectedTaskIndex = 0;
            ArrayList<Task> tasksToDelete = new ArrayList<Task>();
            for (Task task : getCurrentList()) {
                if (!(task instanceof TaskHeader)) {
                    tasksToDelete.add(task);
                }
            }
            listOfTaskToBeExecuted = tasksToDelete;
            commandToBeExecuted = new DeleteCommand(receiver, listOfTaskToBeExecuted);
            getCurrentListViewController().clearListViewSelection();
            getCurrentListViewController().selectAll();

            int numberOfTasks = 0;
            for (Task task : getCurrentList()) {
                if (!(task instanceof TaskHeader)) {
                    numberOfTasks++;
                }
            }

            showFeedback(true, STRING_FEEDBACK_ACTION_DELETE,
                    userArguments + STRING_WHITESPACE + String.format(STRING_FEEDBACK_TOTAL_TASK, numberOfTasks));
            return;

        }

        logger.log(Level.INFO, "Sending user input to commandParser: " + userInput);
        ParseIndexResult parseIndexResult;
        try {
            parseIndexResult = commandParser.parseIndexes(userInput, getCurrentList().size());

            if (parseIndexResult.hasInvalidIndex()) {
                throw new IndexOutOfBoundsException();
            }

            if (parseIndexResult.hasValidIndex()) {
                taskIndexesToBeExecuted = parseIndexResult.getValidIndexes();

                if (taskIndexesToBeExecuted.size() == 1) { // when there's only
                                                           // 1
                                                           // index
                    int taskIndex = taskIndexesToBeExecuted.get(0);
                    int actualIndex = getCurrentListViewController().getActualIndex(taskIndex);
                    inputFeedback = getCurrentList().get(actualIndex).toString();
                    taskToBeExecuted = getCurrentList().get(actualIndex);
                    listOfTaskToBeExecuted = getTasksToBeExecuted(taskIndexesToBeExecuted);
                    commandToBeExecuted = new DeleteCommand(receiver, listOfTaskToBeExecuted);
                    getCurrentListViewController().clearListViewSelection();
                    getCurrentListViewController().select(taskIndex);
                    showFeedback(true, STRING_FEEDBACK_ACTION_DELETE, inputFeedback);

                    // when there's a range of indexes
                } else if (taskIndexesToBeExecuted.size() > 1) {
                    listOfTaskToBeExecuted = getTasksToBeExecuted(taskIndexesToBeExecuted);
                    commandToBeExecuted = new DeleteCommand(receiver, listOfTaskToBeExecuted);
                    getCurrentListViewController()
                            .clearAndSelect(taskIndexesToBeExecuted.get(taskIndexesToBeExecuted.size() - 1));
                    for (int index : taskIndexesToBeExecuted) {
                        getCurrentListViewController().select(index);
                    }
                    showFeedback(true, STRING_FEEDBACK_ACTION_DELETE, userArguments + STRING_WHITESPACE
                            + String.format(STRING_FEEDBACK_TOTAL_TASK, listOfTaskToBeExecuted.size()));
                }
            } else {
                taskIndexesToBeExecuted.clear();
            }
        } catch (InvalidTaskIndexFormat invalidTaskIndexFormat) {
            logger.log(Level.INFO, "DELETE command index(es) invalid: " + userArguments);
            getCurrentListViewController().clearListViewSelection();
            showFeedback(true, STRING_FEEDBACK_ACTION_DELETE, String.format(STRING_ERROR_NOT_FOUND, userArguments));
            clearStoredUserInput();
            return;
        } catch (Exception e) {
            logger.log(Level.INFO, "DELETE command index(es) invalid: " + userArguments);
            getCurrentListViewController().clearListViewSelection();
            showFeedback(true, STRING_FEEDBACK_ACTION_DELETE, String.format(STRING_ERROR_NOT_FOUND, userArguments));
            clearStoredUserInput();
            return;
        }

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

        // parsing edit command with index
        try {
            logger.log(Level.INFO, "EDIT command index is " + taskIndex);
            int actualIndex = getCurrentListViewController().getActualIndex(taskIndex);
            Task taskToBeEdited = getCurrentList().get(actualIndex);
            showFeedback(true, STRING_FEEDBACK_ACTION_EDIT, taskToBeEdited.toString());
            userArguments = userInput.substring(userInputArray[0].length() + userInputArray[1].length() + 1).trim();
            logger.log(Level.INFO, "EDIT command arguments is: " + userArguments);
            taskToBeExecuted = commandParser.parseEdit(taskToBeEdited, userArguments);
            commandToBeExecuted = new EditCommand(receiver, taskToBeEdited, taskToBeExecuted);
            getCurrentListViewController().clearAndSelect(taskIndex);
            return;
        } catch (IndexOutOfBoundsException ioobe) {
            logger.log(Level.WARNING, "EDIT command index is out of range. index = " + taskIndex + " ArrayList size = "
                    + currentList.size());
            showFeedback(true, STRING_FEEDBACK_ACTION_EDIT, String.format(STRING_ERROR_NOT_FOUND, userInputArray[1]));
            clearStoredUserInput();
            return;
        } catch (InvalidLabelFormat e) {
            logger.log(Level.WARNING, "EDIT command has invalid label.");
        }

    }

    /**
     *
     */
    private void parseSearch() {
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

    private void parseDone() {
        // no index found. parse the input as an Add operation instead
        if (userInputArray.length <= 1) {
            logger.log(Level.INFO, "DONE command has no index. Interpreting as ADD command instead");
            parseAdd();
            return;
        }

        if (userArguments.toLowerCase().equals("all")) {
            ArrayList<Task> tasksToDone = new ArrayList<Task>();

            for (Task task : getCurrentList()) {
                if (!(task instanceof TaskHeader)) {
                    tasksToDone.add(task);
                }
            }

            listOfTaskToBeExecuted = tasksToDone;
            commandToBeExecuted = new DoneCommand(receiver, listOfTaskToBeExecuted);
            getCurrentListViewController().clearListViewSelection();
            getCurrentListViewController().selectAll();

            int numberOfTasks = listOfTaskToBeExecuted.size();

            showFeedback(true, STRING_FEEDBACK_ACTION_DONE,
                    userArguments + STRING_WHITESPACE + String.format(STRING_FEEDBACK_TOTAL_TASK, numberOfTasks));
            return;
        }

        logger.log(Level.INFO, "Sending user input to commandParser: " + userInput);
        ParseIndexResult parseIndexResult;
        try {
            parseIndexResult = commandParser.parseIndexes(userInput, getCurrentList().size());

            if (parseIndexResult.hasInvalidIndex()) {
                throw new IndexOutOfBoundsException();
            }
            if (parseIndexResult.hasValidIndex()) {
                taskIndexesToBeExecuted = parseIndexResult.getValidIndexes();

                if (taskIndexesToBeExecuted.size() == 1) { // when there's only
                                                           // 1
                                                           // index
                    int taskIndex = taskIndexesToBeExecuted.get(0);
                    int actualIndex = getCurrentListViewController().getActualIndex(taskIndex);
                    inputFeedback = getCurrentList().get(actualIndex).toString();
                    taskToBeExecuted = getCurrentList().get(actualIndex);
                    listOfTaskToBeExecuted = getTasksToBeExecuted(taskIndexesToBeExecuted);
                    commandToBeExecuted = new DoneCommand(receiver, listOfTaskToBeExecuted);
                    getCurrentListViewController().clearListViewSelection();
                    getCurrentListViewController().select(taskIndex);
                    showFeedback(true, STRING_FEEDBACK_ACTION_DONE, inputFeedback);

                } else if (taskIndexesToBeExecuted.size() > 1) { // when there's
                                                                 // a
                                                                 // range of
                                                                 // indexes
                    listOfTaskToBeExecuted = getTasksToBeExecuted(taskIndexesToBeExecuted);
                    commandToBeExecuted = new DoneCommand(receiver, listOfTaskToBeExecuted);
                    getCurrentListViewController()
                            .clearAndSelect(taskIndexesToBeExecuted.get(taskIndexesToBeExecuted.size() - 1));
                    for (int index : taskIndexesToBeExecuted) {
                        getCurrentListViewController().select(index);
                    }
                    showFeedback(true, STRING_FEEDBACK_ACTION_DONE, userArguments + STRING_WHITESPACE
                            + String.format(STRING_FEEDBACK_TOTAL_TASK, listOfTaskToBeExecuted.size()));
                }
            } else {
                taskIndexesToBeExecuted.clear();
            }
        } catch (InvalidTaskIndexFormat invalidTaskIndexFormat) {
            logger.log(Level.INFO, "DONE command index(es) invalid: " + userArguments);
            getCurrentListViewController().clearListViewSelection();
            showFeedback(true, STRING_FEEDBACK_ACTION_DONE, String.format(STRING_ERROR_NOT_FOUND, userArguments));
            clearStoredUserInput();
            return;
        } catch (Exception e) {
            logger.log(Level.INFO, "DONE command index(es) invalid: " + userArguments);
            getCurrentListViewController().clearListViewSelection();
            showFeedback(true, STRING_FEEDBACK_ACTION_DONE, String.format(STRING_ERROR_NOT_FOUND, userArguments));
            clearStoredUserInput();
            return;
        }
    }

    private void parseUndone() {
        if (userInputArray.length <= 1) {
            logger.log(Level.INFO, "UNDONE command has no index. Interpreting as ADD command instead");
            parseAdd(); // no index found. parse the input as an Add operation
                        // instead
            return;
        }

        if (userArguments.toLowerCase().equals("all")) {
            // previousSelectedTaskIndex = 0;
            ArrayList<Task> tasksToUndone = new ArrayList<Task>();
            for (Task task : getCurrentList()) {
                if (!(task instanceof TaskHeader)) {
                    tasksToUndone.add(task);
                }
            }
            listOfTaskToBeExecuted = tasksToUndone;
            commandToBeExecuted = new UndoneCommand(receiver, listOfTaskToBeExecuted);
            getCurrentListViewController().clearListViewSelection();
            getCurrentListViewController().selectAll();

            int numberOfTasks = 0;
            for (Task task : getCurrentList()) {
                if (!(task instanceof TaskHeader)) {
                    numberOfTasks++;
                }
            }

            showFeedback(true, STRING_FEEDBACK_ACTION_UNDONE,
                    userArguments + STRING_WHITESPACE + String.format(STRING_FEEDBACK_TOTAL_TASK, numberOfTasks));
            return;

        }

        logger.log(Level.INFO, "Sending user input to commandParser: " + userInput);
        ParseIndexResult parseIndexResult;
        try {
            parseIndexResult = commandParser.parseIndexes(userInput, getCurrentList().size());

            if (parseIndexResult.hasInvalidIndex()) {
                throw new IndexOutOfBoundsException();
            }
            if (parseIndexResult.hasValidIndex()) {
                taskIndexesToBeExecuted = parseIndexResult.getValidIndexes();

                if (taskIndexesToBeExecuted.size() == 1) { // when there's only
                                                           // 1
                                                           // index
                    int taskIndex = taskIndexesToBeExecuted.get(0);
                    int actualIndex = getCurrentListViewController().getActualIndex(taskIndex);
                    inputFeedback = getCurrentList().get(actualIndex).toString();
                    taskToBeExecuted = getCurrentList().get(actualIndex);
                    listOfTaskToBeExecuted = getTasksToBeExecuted(taskIndexesToBeExecuted);
                    commandToBeExecuted = new UndoneCommand(receiver, listOfTaskToBeExecuted);
                    getCurrentListViewController().clearListViewSelection();
                    getCurrentListViewController().select(taskIndex);
                    showFeedback(true, STRING_FEEDBACK_ACTION_UNDONE, inputFeedback);

                } else if (taskIndexesToBeExecuted.size() > 1) { // when there's
                                                                 // a
                                                                 // range of
                                                                 // indexes
                    listOfTaskToBeExecuted = getTasksToBeExecuted(taskIndexesToBeExecuted);
                    commandToBeExecuted = new UndoneCommand(receiver, listOfTaskToBeExecuted);
                    getCurrentListViewController()
                            .clearAndSelect(taskIndexesToBeExecuted.get(taskIndexesToBeExecuted.size() - 1));
                    for (int index : taskIndexesToBeExecuted) {
                        getCurrentListViewController().select(index);
                    }
                    showFeedback(true, STRING_FEEDBACK_ACTION_UNDONE, userArguments + STRING_WHITESPACE
                            + String.format(STRING_FEEDBACK_TOTAL_TASK, listOfTaskToBeExecuted.size()));
                }
            } else {
                taskIndexesToBeExecuted.clear();
            }
        } catch (InvalidTaskIndexFormat invalidTaskIndexFormat) {
            logger.log(Level.INFO, "UNDONE command index(es) invalid: " + userArguments);
            getCurrentListViewController().clearListViewSelection();
            showFeedback(true, STRING_FEEDBACK_ACTION_UNDONE, String.format(STRING_ERROR_NOT_FOUND, userArguments));
            clearStoredUserInput();
            return;
        } catch (Exception e) {
            logger.log(Level.INFO, "UNDONE command index(es) invalid: " + userArguments);
            getCurrentListViewController().clearListViewSelection();
            showFeedback(true, STRING_FEEDBACK_ACTION_UNDONE, String.format(STRING_ERROR_NOT_FOUND, userArguments));
            clearStoredUserInput();
            return;
        }
    }

    private void parseUndo() {
        numberOfActions = commandParser.getIndexForEdit(userInput);
        if (numberOfActions >= 0) {
            showFeedback(true, STRING_FEEDBACK_ACTION_UNDO,
                    String.format(STRING_FEEDBACK_TOTAL_ACTION, numberOfActions));
        } else {
            showFeedback(true, STRING_FEEDBACK_ACTION_UNDO, String.format(STRING_FEEDBACK_TOTAL_ACTION, 1));
        }
    }

    private void parseRedo() {
        numberOfActions = commandParser.getIndexForEdit(userInput);
        if (numberOfActions >= 0) {
            showFeedback(true, STRING_FEEDBACK_ACTION_REDO,
                    String.format(STRING_FEEDBACK_TOTAL_ACTION, numberOfActions));
        } else {
            showFeedback(true, STRING_FEEDBACK_ACTION_REDO, String.format(STRING_FEEDBACK_TOTAL_ACTION, 0));
        }

    }

    private void parseSetFileLocation() {
        showFeedback(true, STRING_FEEDBACK_ACTION_SET_FILE_LOCATION, userArguments);
        if (userInputArray.length <= 1) {
            logger.log(Level.INFO, "SET command has no arguments.");
            // no arguments found. parse the input as an Add operation instead
            commandToBeExecuted = new SetFileLocationCommand(receiver, "");
        } else {
            logger.log(Level.INFO, "SET command arguments: " + userArguments);
            commandToBeExecuted = new SetFileLocationCommand(receiver, userArguments);
        }

    }

    private File showFileChooserDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Set file location");
        fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Text Files", "*.txt"));
        fileChooser.setInitialFileName("tasks");

        fileChooser.setInitialDirectory(new File(receiver.getFileDir()));
        File selectedFile = fileChooser.showSaveDialog(mainApp.getPrimaryStage());

        return selectedFile;
    }

    private String getFilePath(File selectedFile) {
        String selectedFilePath = "";
        try {
            selectedFilePath = selectedFile.getCanonicalPath();
            return selectedFilePath;
        } catch (IOException e) {
            // TODO show some feedback about invalid filepath
            logger.log(Level.WARNING, "File path is invalid");
        }

        return selectedFilePath;
    }

    /**
    *
    */
    private ArrayList<Task> getTasksToBeExecuted(ArrayList<Integer> taskIndexes) {
        ArrayList<Task> tasksToBeDeleted = new ArrayList<>(taskIndexes.size());
        for (Integer index : taskIndexes) {
            int actualIndex = getCurrentListViewController().getActualIndex(index);
            tasksToBeDeleted.add(getCurrentList().get(actualIndex));
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

        textUserAction.setText(userAction + STRING_WHITESPACE);
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
                labelExecutionDetails.setTextFill(Color.web(AppColor.PRIMARY_WHITE, 0.9));
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
                labelExecutionDetails.setTextFill(Color.web(AppColor.PRIMARY_WHITE, 0.9));
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
                labelExecutionDetails.setTextFill(Color.web(AppColor.PRIMARY_WHITE, 0.9));

                if (listOfTaskToBeExecuted.size() == 1) {
                    labelExecutionDetails.setText(taskToBeExecuted.toString());
                } else if (listOfTaskToBeExecuted.size() > 1) {
                    labelExecutionDetails.setText(listOfTaskToBeExecuted.size() + " tasks");
                } else if (userArguments != null && userArguments.toLowerCase().equals("all")) {
                    // TODO very dirty. refactor for better checking
                    labelExecutionDetails.setText("all");
                }
            }

        }

        if (executedCommand instanceof DoneCommand) {
            if (undoOrRedo != null) {
                labelExecutedCommand.setText(undoOrRedo + STRING_WHITESPACE + "task completed.");
                labelExecutedCommand.setTextFill(Color.web(AppColor.PRIMARY_WHITE));
                labelExecutionDetails.setTextFill(Color.web(AppColor.PRIMARY_WHITE, 0));
            } else {
                labelExecutedCommand.setText(listOfTaskToBeExecuted.size() + STRING_WHITESPACE + "task completed.");
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
                labelExecutedCommand
                        .setText("Mark " + listOfTaskToBeExecuted.size() + STRING_WHITESPACE + "task as incomplete.");
                labelExecutedCommand.setTextFill(Color.web(AppColor.PRIMARY_LIME_LIGHT, 0.7));
                labelExecutionDetails.setTextFill(Color.web(AppColor.PRIMARY_WHITE, 0));
            }

        }

        if (executedCommand instanceof SetFileLocationCommand) {
            if (undoOrRedo != null) {
                labelExecutedCommand.setText(undoOrRedo + STRING_WHITESPACE + "new file location.");
                labelExecutedCommand.setTextFill(Color.web(AppColor.PRIMARY_WHITE));
                labelExecutionDetails.setTextFill(Color.web(AppColor.PRIMARY_WHITE, 0));
            } else {
                labelExecutedCommand.setText("Set:");
                labelExecutedCommand.setTextFill(Color.web(AppColor.PRIMARY_BLUE_LIGHT, 0.7));
                labelExecutionDetails.setText("New file location has been set.");
                labelExecutionDetails.setTextFill(Color.web(AppColor.PRIMARY_WHITE, 0.9));
            }

        }

        if (undoOrRedo != null) {
            if (undoOrRedo.equals("Undo")) {
                labelSuggestedAction.setText("REDO (F2)");
            } else if (undoOrRedo.equals("Redo")) {
                labelSuggestedAction.setText("UNDO (F1)");
            }
        } else {
            labelSuggestedAction.setText("UNDO (F1)");
        }

        FadeTransition appearVisible = new FadeTransition(Duration.seconds(2), anchorPaneExecutionResult);
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

    private ListViewController getCurrentListViewController() {
        return currentListViewController;
    }

    private ArrayList<Task> getCurrentList() {
        return currentList;
    }

    private void setCurrentListViewController(String tabName) {
        if (tabName.equals(tabTodo.getText())) {
            currentListViewController = todoListViewController; //
        } else if (tabName.equals(tabCompleted.getText())) {
            currentListViewController = completedListViewController; // completedController
        }
    }

    private void setCurrentList(String tabName) {
        if (tabName.equals(tabTodo.getText())) {
            currentList = todoTasksWithHeaders;
        } else if (tabName.equals(tabCompleted.getText())) {
            currentList = completedTasksWithHeaders;
        }
    }

    /**
     * @return String
     */
    private String getSelectedTabName() {
        return tabPane.getSelectionModel().getSelectedItem().getText();
    }

    /**
     *
     */
    private void saveCaretPosition() {
        previousCaretPosition = commandBar.getCaretPosition();
        logger.log(Level.INFO, "Save caret position to " + previousCaretPosition);
    }

    public Command getLastExecutedCommand() {
        return commandToBeExecuted;
    }

    public void restoreListViewPreviousSelection() {
        getCurrentListViewController().restoreListViewPreviousSelection();
        // TODO one more line for completedtaskcontroller
    }

    private void toggleHelpDialog() {
        if(!dialogHelp.isVisible()){
            dialogHelp.show(dialogContainer);
        }
        else{
            dialogHelp.close();
        }

    }
}