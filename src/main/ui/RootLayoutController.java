package main.ui;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXSnackbar.SnackbarEvent;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Callback;
import main.data.Task;
import main.logic.AddCommand;
import main.logic.Command;
import main.logic.DeleteCommand;
import main.logic.EditCommand;
import main.logic.Invoker;
import main.logic.Receiver;
import main.logic.SearchCommand;
import main.parser.CommandParser;
import main.parser.CommandParser.InvalidLabelFormat;
import main.parser.CommandParser.InvalidTaskIndexFormat;

@SuppressWarnings("restriction")
public class RootLayoutController implements Observer {
    private static final String STRING_TODAY = "Today";
    private static final String COMMAND_EDIT = "edit";
    private static final String COMMAND_DELETE = "delete";
    private static final String COMMAND_DELETE_SHORTHAND = "del";
    private static final String COMMAND_SEARCH = "search";
    private static final String WHITESPACE = " ";
    private static final String EMPTY_STRING = "";
    private static final String STRING_TAB_TASK_SIZE = "(%1$s)";
    private static final String MESSAGE_LABEL_MODE_EDIT = "Edit mode";
    private static final String MESSAGE_LISTVIEW_TODO_EMPTY = "You have no task!";
    private static final String MESSAGE_LISTVIEW_COMPLETED_EMPTY = "You have no completed task!";
    private static final String MESSAGE_FEEDBACK_ACTION_ADD = "Adding: ";
    private static final String MESSAGE_FEEDBACK_ACTION_EDIT = "Editing: ";
    private static final String MESSAGE_FEEDBACK_ACTION_DELETE = "Deleting: ";
    private static final String MESSAGE_FEEDBACK_TOTAL_TASK = "(%1$s tasks)";
    private static final String MESSAGE_FEEDBACK_ACTION_SEARCH = "Searching:";
    private static final String MESSAGE_ERROR_NOT_FOUND = "Task -%1$s- not found.";

    // Ctrl+Tab hotkey
    private static final KeyCombination HOTKEY_CTRL_TAB = new KeyCodeCombination(KeyCode.TAB,
            KeyCombination.CONTROL_DOWN);

    @FXML // fx:id="rootLayout"
    private AnchorPane rootLayout; // Value injected by FXMLLoader

    @FXML // fx:id="tabPane"
    private TabPane tabPane; // Value injected by FXMLLoader

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

    @FXML // fx:id="labelUserAction"
    private Label labelUserAction; // Value injected by FXMLLoader

    @FXML // fx:id="labelUserParsedInput"
    private Label labelUserParsedInput; // Value injected by FXMLLoader

    @FXML // fx:id="labelUserParsedInput"
    private Label labelUserResult; // Value injected by FXMLLoader

    @FXML // fx:id="groupUndo"
    private Group groupUndo; // Value injected by FXMLLoader

    @FXML // fx:id="labelUndo"
    private Label labelUndo; // Value injected by FXMLLoader

    @FXML // fx:id="btnUndo"
    private Button btnUndo; // Value injected by FXMLLoader

    @FXML // fx:id="groupRedo"
    private Group groupRedo; // Value injected by FXMLLoader

    @FXML // fx:id="labelRedo"
    private Label labelRedo; // Value injected by FXMLLoader

    @FXML // fx:id="btnRedo"
    private Button btnRedo; // Value injected by FXMLLoader
    
    @FXML // fxid="snackbar"
    private JFXSnackbar snackbar;

    private VirtualFlow<IndexedCell<String>> virtualFlow;
    private IndexedCell<String> firstVisibleIndexedCell;
    private IndexedCell<String> lastVisibleIndexedCell;

    private Invoker invoker;
    private Receiver receiver;
    private CommandParser commandParser;
    private Command commandToBeExecuted;
    private Task taskToBeExecuted;
    private ArrayList<Integer> taskIndexesToBeDeleted;

    // private ArrayList<Task> allTasks;
    private ArrayList<Task> todoTasks;
    private ArrayList<Task> completedTasks;
    private ObservableList<Task> observableTodoTasks = FXCollections.observableArrayList();
    private ObservableList<Task> observableCompletedTasks = FXCollections.observableArrayList();
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

    @Override
    public void update(Observable o, Object arg) {
        
        if (o instanceof Receiver) {
            if (commandToBeExecuted instanceof AddCommand) {
                logger.log(Level.INFO, "(ADD TASK) update() is called");
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        refreshListView();
                        listViewTodo.getSelectionModel().selectLast();
                        listViewTodo.scrollTo(todoTasks.size() - 1);
//                        snackbar.fireEvent(new SnackbarEvent("Task added! ","UNDO",5000,(me)->{}));
                        // showResult(true, "Task added!");
                    }
                });

            } else if (commandToBeExecuted instanceof DeleteCommand) {
                logger.log(Level.INFO, "(DELETE TASK) update() is called");
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        saveSelectedTaskIndex();
                        refreshListView();
                        restoreListViewPreviousSelection();
                        // showResult(true, "Task deleted!");

                    }
                });
            } else if (commandToBeExecuted instanceof EditCommand) {
                logger.log(Level.INFO, "(EDIT TASK) update() is called");
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        saveSelectedTaskIndex();
                        refreshListView();
                        restoreListViewPreviousSelection();
                        // showResult(true, "Task deleted!");

                    }
                });
            } else if (commandToBeExecuted instanceof SearchCommand) {
                saveSelectedTaskIndex();
                refreshListView();
                restoreListViewPreviousSelection();
                showFeedback(true, MESSAGE_FEEDBACK_ACTION_SEARCH,
                        " Found " + todoTasks.size() + " tasks for -" + userArguments + "-");
            }
        }

    }

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
        initLogicAndParser();
        initListView();
        initMouseListener();
        initKeyboardListener();
        initCommandBarListener();
//        snackbar.registerSnackbarContainer(rootLayout);
        // initTabSelectionListener();
        logger.log(Level.INFO, "UI initialization complete");
    }

    // /**
    // *
    // */
    // private void initTabSelectionListener() {
    // ObservableList<Tab> tabList = tabPane.getTabs();
    // for (int i = 0; i < tabList.size(); i++) {
    // tabList.get(i).setOnSelectionChanged(new EventHandler<Event>() {
    //
    // @Override
    // public void handle(Event event) {
    // if (getSelectedTabName().equals(STRING_TODAY)) {
    // labelDateToday.setVisible(true);
    // } else {
    // labelDateToday.setVisible(false);
    // }
    //
    // labelCurrentMode.setText(getSelectedTabName());
    // }
    // });
    // }
    // }

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
                } else if (keyEvent.getCode() == KeyCode.F3) {
                    handleFThreeKey();
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

        listViewTodo.setPlaceholder(new Label(MESSAGE_LISTVIEW_TODO_EMPTY));
        listViewTodo.setCellFactory(new Callback<ListView<Task>, ListCell<Task>>() {

            @Override
            public JFXListCell<Task> call(ListView<Task> param) {
                return new CustomListCellController();
            }
        });

        listViewCompleted.setPlaceholder(new Label(MESSAGE_LISTVIEW_COMPLETED_EMPTY));
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

    private void toggleUndoRedo() {
        groupUndo.setVisible(invoker.isUndoAvailable());
        groupRedo.setVisible(invoker.isRedoAvailable());

    }

    private void updateTabAndLabelWithTotalTasks() {
        // if(!isEditMode){
        // labelCurrentMode.setText(getSelectedTabName());
        // }
        tabTodo.setText("To-do" + WHITESPACE + String.format(STRING_TAB_TASK_SIZE, todoTasks.size()));
        tabCompleted.setText("Completed" + WHITESPACE + String.format(STRING_TAB_TASK_SIZE, completedTasks.size()));

    }

    /**
     * 
     */
    private void refreshListView() {
        observableTodoTasks.clear();
        populateListView();
        updateTabAndLabelWithTotalTasks();
        // toggleUndoRedo();
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
        // Platform.runLater(new Runnable() {
        //
        // @Override
        // public void run() {
        // if (isEditMode) { //edit mode is true. exit edit mode
        // isEditMode = false;
        // labelCurrentMode.setText(getSelectedTabName());
        // restoreCommandBarText();
        // restoreCaretPosition();
        // logger.log(Level.INFO, "Pressed F1 key: Exit EDIT MODE");
        //
        // } else { //edit mode is false. enter edit mode
        // isEditMode = true;
        // saveCommandBarText();
        // saveCaretPosition();
        // showFeedback(false);
        // labelCurrentMode.setText(MESSAGE_LABEL_MODE_EDIT);
        // commandBar.setText(todoTasks.get(getSelectedTaskIndex()).toString());
        // moveCaretPositionToLast();
        // logger.log(Level.INFO, "Pressed F1 key: Enter EDIT MODE");
        // }
        //
        // }
        // });

    }

    /**
     * 
     */
    private void handleFTwoKey() {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {

                if (invoker.isUndoAvailable()) {
                    saveSelectedTaskIndex();
                    try {
                        invoker.undo();
                        logger.log(Level.INFO, "Pressed F2 key: UNDO operation");
                    } catch (EmptyStackException emptyStackException) {
                        logger.log(Level.WARNING, emptyStackException.getMessage());
                    }

                    // showResult(true, "Undo!");
                }
            }
        });

    }

    /**
     * 
     */
    private void handleFThreeKey() {
        Platform.runLater(new Runnable() {

            @Override
            public void run() {

                if (invoker.isRedoAvailable()) {
                    saveSelectedTaskIndex();
                    try {
                        invoker.redo();
                        logger.log(Level.INFO, "Pressed F3 key: REDO operation");
                    } catch (EmptyStackException emptyStackException) {
                        logger.log(Level.WARNING, emptyStackException.getMessage());
                    }

                    // showResult(true, "Redo!");
                }
            }
        });

    }

    /**
     * 
     */
    private void handleKeyStrokes() {
        if (commandParser == null) {
            commandParser = new CommandParser();
        }

        userInput = commandBar.getText();
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
                    commandBar.setText(todoTasks.get(getSelectedTaskIndex()).toString());
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
     * 
     */
    private void handleEnterKey() {
        if (invoker == null) {
            invoker = new Invoker();
        }

        if (commandBar.getText().trim().length() > 0) {

            // add operation
            if (commandToBeExecuted == null) {
                return;
            }

            if (commandToBeExecuted instanceof AddCommand) {
                logger.log(Level.INFO, "(ADD TASK) Pressed ENTER key: " + commandBar.getText());
                invoker.execute(commandToBeExecuted);

            } else if (commandToBeExecuted instanceof DeleteCommand) {
                logger.log(Level.INFO, "(DELETE TASK) Pressed ENTER key: " + commandBar.getText());
                invoker.execute(commandToBeExecuted);
            } else if (commandToBeExecuted instanceof EditCommand) {
                logger.log(Level.INFO, "(EDIT TASK) Pressed ENTER key: " + commandBar.getText());
                invoker.execute(commandToBeExecuted);
            }

            // else if (commandToBeExecuted instanceof SearchCommand) {
            // logger.log(Level.INFO, "(SEARCH TASK) Pressed ENTER key: " +
            // commandBar.getText());
            // invoker.execute(commandToBeExecuted);
            // }

        }

        btnFeedback.setVisible(false);
        clearStoredUserInput();
        commandBar.clear();
        // showUndo();

    }

    /**
     * 
     */
    private void handleDeleteKey() {
        if (invoker == null) {
            invoker = new Invoker();
        }

        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                logger.log(Level.INFO, "Pressed DELETE key: task index  " + getSelectedTaskIndex());
                saveSelectedTaskIndex();
                commandToBeExecuted = new DeleteCommand(receiver, getTasksToBeDeleted(getSelectedTaskIndex()));
                invoker.execute(commandToBeExecuted);
                // refreshListView();
                // restoreListViewPreviousSelection();
                // showUndo();
                // showResult(true, "Task deleted!");
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
            case COMMAND_EDIT :
                parseEdit();
                break;
            case COMMAND_DELETE :
            case COMMAND_DELETE_SHORTHAND :
                parseDelete();
                break;
            case COMMAND_SEARCH :
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
        } catch (InvalidLabelFormat e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        commandToBeExecuted = new AddCommand(receiver, taskToBeExecuted);
        inputFeedback = taskToBeExecuted.toString();
        showFeedback(true, MESSAGE_FEEDBACK_ACTION_ADD, inputFeedback);
    }

    private void parseDelete() {
        if (userInputArray.length <= 1) {
            logger.log(Level.INFO, "DELETE command has no index. Interpreting as ADD command instead");
            parseAdd(); // no index found. parse the input as an Add operation
                        // instead
            return;
        }

        logger.log(Level.INFO, "Sending user input to commandParser: " + userInput);

        try {
            taskIndexesToBeDeleted = commandParser.parseIndexes(userInput);
        } catch (InvalidTaskIndexFormat invalidTaskIndexFormat) {
            logger.log(Level.INFO, "DELETE command index(es) invalid: " + userArguments);
            showFeedback(true, MESSAGE_FEEDBACK_ACTION_DELETE, String.format(MESSAGE_ERROR_NOT_FOUND, userArguments));
            clearStoredUserInput();
            return;
        }

        String parseResult = taskIndexesToBeDeleted.toString();
        System.out.println("user arguments: " + userArguments);
        System.out.println("parse result: " + parseResult);

        if (taskIndexesToBeDeleted.size() == 1) {
            int taskIndex = taskIndexesToBeDeleted.get(0); // get the the one
                                                           // and only
            // index

            // if selected index is out of bound
            if (taskIndex <= 0 || taskIndex > todoTasks.size()) {
                showFeedback(true, MESSAGE_FEEDBACK_ACTION_DELETE, String.format(MESSAGE_ERROR_NOT_FOUND, taskIndex));
                clearStoredUserInput();
            } else {
                inputFeedback = todoTasks.get(taskIndex - 1).toString();
                showFeedback(true, MESSAGE_FEEDBACK_ACTION_DELETE, inputFeedback);
            }

        } else {
            showFeedback(true, MESSAGE_FEEDBACK_ACTION_DELETE, userArguments + WHITESPACE
                    + String.format(MESSAGE_FEEDBACK_TOTAL_TASK, taskIndexesToBeDeleted.size()));

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

        // parsing edit command with index
        try {
            int taskIndex = Integer.parseInt(userInputArray[1]) - 1;
            logger.log(Level.INFO, "EDIT command index is " + taskIndex);
            Task taskToBeEdited = todoTasks.get(taskIndex);
            showFeedback(true, MESSAGE_FEEDBACK_ACTION_EDIT, taskToBeEdited.toString());
            taskToBeExecuted = commandParser.parseEdit(taskToBeEdited, userInput);
            commandToBeExecuted = new EditCommand(receiver, taskToBeEdited, taskToBeExecuted);
            return;
        } catch (NumberFormatException nfe) {
            // parse the edit command based on the currently selected item
            logger.log(Level.INFO, "EDIT command has no index. Editing current selected task");
            parseEditForSelectedTask();
        } catch (IndexOutOfBoundsException ioobe) {
            logger.log(Level.INFO, "EDIT command index is out of range. index = " + userInputArray[1]
                    + " ArrayList size = " + todoTasks.size());
            showFeedback(true, MESSAGE_FEEDBACK_ACTION_EDIT, String.format(MESSAGE_ERROR_NOT_FOUND, userInputArray[1]));
            clearStoredUserInput();
            return;
        } catch (InvalidLabelFormat e) {
            e.printStackTrace();
        }

    }

    /**
     * @param taskToBeEdited
     * @throws InvalidLabelFormat
     */
    private void parseEditForSelectedTask() {
        Task taskToBeEdited = todoTasks.get(getSelectedTaskIndex());
        showFeedback(true, MESSAGE_FEEDBACK_ACTION_EDIT, taskToBeEdited.toString());
        try {
            taskToBeExecuted = commandParser.parseEdit(taskToBeEdited, userInput);
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
        if (userInput.equals(COMMAND_SEARCH)) {
            logger.log(Level.INFO, "SEARCH command has no arguments. Interpreting as ADD command instead");
            // no arguments found. parse the input as an Add operation instead
            parseAdd();
            return;
        }

        //this allow a search without a search term
        if (userInput.equals(COMMAND_SEARCH + WHITESPACE)) {
            logger.log(Level.INFO, "Searching: " + userInput);
            commandToBeExecuted = new SearchCommand(receiver, WHITESPACE);
            userArguments = WHITESPACE;
            invoker.execute(commandToBeExecuted);
            return;
        }

        logger.log(Level.INFO, "Searching: " + userInput);
        commandToBeExecuted = new SearchCommand(receiver, userArguments);
        invoker.execute(commandToBeExecuted);
    }

    private ArrayList<Task> getTasksToBeDeleted(int taskIndex) {
        ArrayList<Task> tasksToBeDeleted = new ArrayList<>(1);
        tasksToBeDeleted.add(todoTasks.get(taskIndex));
        return tasksToBeDeleted;
    }

    private ArrayList<Task> getTasksToBeDeleted(ArrayList<Integer> taskIndexes) {
        ArrayList<Task> tasksToBeDeleted = new ArrayList<>(taskIndexes.size());
        for (Integer i : taskIndexes) {
            tasksToBeDeleted.add(todoTasks.get(i - 1));
        }
        return tasksToBeDeleted;
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
    private void showFeedback(boolean isVisible) {
        // if (isVisible) {
        // logger.log(Level.INFO, "Showing user feedback");
        // showResult(false, EMPTY_STRING);
        // }
        //
        // labelUserAction.setVisible(isVisible);
        // labelUserParsedInput.setVisible(isVisible);
    }

    /**
     * 
     */
    private void showFeedback(boolean isVisible, String userAction, String userFeedback) {
        if (isVisible) {
            logger.log(Level.INFO, "Showing user feedback: " + userFeedback);
            // showResult(!isVisible, EMPTY_STRING);
        }

        textUserAction.setText(userAction);
        textUserAction.setFont(new Font(20));
        textUserAction.setFill(Color.web("303F9F", 0.7));
        textUserParsedResult.setText(userFeedback);
        textUserParsedResult.setFont(new Font(20));
        textUserParsedResult.setFill(Color.web("#00111a", 0.7));

        // textFlowFeedback.getChildren().clear();
        // textFlowFeedback.getChildren().addAll(textUserAction,
        // textUserParsedResult);

        // labelUserAction.setVisible(isVisible);
        // labelUserParsedInput.setVisible(isVisible);
        // labelUserAction.setText(userAction);
        // labelUserParsedInput.setText(userFeedback);
    }

    // /**
    // * @param resultString
    // */
    // private void showResult(boolean isVisible, String resultString) {
    // if (isVisible) {
    // logger.log(Level.INFO, "Showing result: " + resultString);
    // showFeedback(!isVisible);
    // }
    //
    // labelUserResult.setText(resultString);
    // labelUserResult.setVisible(isVisible);
    // }
    // /**
    // *
    // */
    // private void showUndo() {
    // groupUndo.setVisible(true);
    // }
    //
    // /**
    // *
    // */
    // private void showRedo() {
    // groupRedo.setVisible(true);
    // }

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
        if (previousSelectedTaskIndex == todoTasks.size()) {
            listViewTodo.getSelectionModel().selectLast();
            listViewTodo.scrollTo(todoTasks.size() - 1);
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
