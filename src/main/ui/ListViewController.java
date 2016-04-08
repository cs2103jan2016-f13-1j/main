//@@author A0126400Y
package main.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXListView;
import com.sun.javafx.scene.control.skin.VirtualFlow;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import main.data.Task;
import main.data.TaskHeader;
import main.logic.AddCommand;
import main.logic.Command;
import main.logic.DeleteCommand;
import main.logic.DoneCommand;
import main.logic.EditCommand;
import main.logic.UndoneCommand;

public class ListViewController extends AnchorPane {
    private static final Logger logger = Logger.getLogger(ListViewController.class.getName());
    private static final String STRING_LISTVIEW_TODO_EMPTY = "You have no task!";

    @FXML
    private JFXListView<Task> listView;
    // ListView UI related
    private VirtualFlow<IndexedCell<String>> virtualFlow;
    private RootLayoutController rootLayoutController;
    private ObservableList<Task> observableTaskList = FXCollections.observableArrayList();
    private ArrayList<Task> taskListWithHeaders;
    private int previousSelectedTaskIndex;
    private HashMap<Integer, Integer> displayedIndexToOriginalIndexMap;

    private boolean isOverdueHeaderAdded = false;
    private boolean isTodayHeaderAdded = false;
    private boolean isTomorrowHeaderAdded = false;
    private boolean isUpcomingHeaderAdded = false;
    private boolean isSomedayHeaderAdded = false;

    public ListViewController() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/main/resources/layouts/TaskListView.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @FXML
    void initialize() {
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listView.setPlaceholder(new Label(STRING_LISTVIEW_TODO_EMPTY));
        listView.setCellFactory(new Callback<ListView<Task>, ListCell<Task>>() {

            @Override
            public JFXListCell<Task> call(ListView<Task> param) {
                return new ListCellController(ListViewController.this);
            }
        });
    }

    public ArrayList<Task> getTaskList() {
        return taskListWithHeaders;
    }

    public void refreshListView(ArrayList<Task> taskList) {
        saveSelectedIndex();
        observableTaskList.clear();
        populateListView(taskList);
        restoreListViewPreviousSelection();
    }

    public void populateListView(ArrayList<Task> taskList) {
        taskListWithHeaders = createListWithHeaders(taskList);
        observableTaskList.setAll(taskListWithHeaders);
        listView.setItems(observableTaskList);

        initHashMapForListViewIndexes();
    }

    private void initHashMapForListViewIndexes() {
        if (displayedIndexToOriginalIndexMap == null) {
            displayedIndexToOriginalIndexMap = new HashMap<>(taskListWithHeaders.size());
        }
//        displayedIndexToOriginalIndexMap.clear();
    }

    public void mapIndexToActualIndex(int displayedIndex, int actualIndex) {
        displayedIndexToOriginalIndexMap.put(displayedIndex, actualIndex);
    }

    public int getActualIndex(int displayedIndex){
        return displayedIndexToOriginalIndexMap.get(displayedIndex);
    }

    private ArrayList<Task> createListWithHeaders(ArrayList<Task> taskList) {
        // +4 to the size due to the additional header task objects
        ArrayList<Task> taskListWithHeaders = new ArrayList<>(taskList.size() + 4);
        taskListWithHeaders.addAll(taskList);

        resetHeadersState();

        for (int i = 0; i < taskListWithHeaders.size(); i++) {
            Task task = taskListWithHeaders.get(i);

            if (task.isOverdue()) {
                if (!isOverdueHeaderAdded) {
                    taskListWithHeaders.add(i, new TaskHeader("Overdue"));
                    isOverdueHeaderAdded = true;
                    System.out.println("Overdue header is added");
                }
            }

            if (task.isToday() && !task.isOverdue()) { // cases where task is
                                                       // today and not overdue
                if (!isTodayHeaderAdded) {
                    taskListWithHeaders.add(i, new TaskHeader("Today"));
                    isTodayHeaderAdded = true;
                    System.out.println("Today header is added");
                }
            }

            if (task.isTomorrow()) {
                if (!isTomorrowHeaderAdded) {
                    taskListWithHeaders.add(i, new TaskHeader("Tomorrow"));
                    isTomorrowHeaderAdded = true;
                    System.out.println("Tomorrow header is added");
                }
            }
            if (task.isUpcoming()) {
                if (!isUpcomingHeaderAdded) {
                    taskListWithHeaders.add(i, new TaskHeader("Upcoming"));
                    isUpcomingHeaderAdded = true;
                    System.out.println("Upcoming header is added");
                }
            }
            if (task.isSomeday()) {
                if (!isSomedayHeaderAdded) {
                    taskListWithHeaders.add(i, new TaskHeader("Someday"));
                    isSomedayHeaderAdded = true;
                    System.out.println("Someday header is added");
                }
            }

        }

        return taskListWithHeaders;
    }

    private void resetHeadersState() {
        isOverdueHeaderAdded = false;
        isTodayHeaderAdded = false;
        isTomorrowHeaderAdded = false;
        isUpcomingHeaderAdded = false;
        isSomedayHeaderAdded = false;
    }

    public void selectListViewFirstItem() {
        listView.getSelectionModel().select(1);
        saveSelectedIndex();
        initCustomViewportBehaviorForListView();

        listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Task>() {

            @Override
            public void changed(ObservableValue<? extends Task> observable, Task oldValue, Task newValue) {
                // TODO will encounter nullpointer on first run
                System.out.println("Listview selection changed");
                // saveSelectedIndex();

                if (newValue instanceof TaskHeader) {
                    int currentSelectedIndex = listView.getSelectionModel().getSelectedIndex();
                    System.out.println(currentSelectedIndex);

                    if (currentSelectedIndex < getPreviousSelectedIndex()) {

                        listView.getSelectionModel().clearAndSelect(currentSelectedIndex - 1);

                    } else if (currentSelectedIndex > getPreviousSelectedIndex()) {
                        listView.getSelectionModel().clearAndSelect(currentSelectedIndex + 1);
                    }
                }
                adjustViewportForListView();

            }
        });
    }

    public void initListViewBehavior() {
        initCustomViewportBehaviorForListView();
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
        for (Node node : listView.getChildrenUnmodifiable()) {
            if (node instanceof VirtualFlow) {
                // get an instance of VirtualFlow. this is essentially the
                // viewport for ListView
                virtualFlow = (VirtualFlow<IndexedCell<String>>) node;
                System.out.println("Found virtual flow");
            }
        }

    }

    /**
     * This method is used to emulate the original behavior of a ListView, i.e.
     * the automatic scrolling of focused ListView when a selected item is not
     * visible within the viewport.
     */
    @SuppressWarnings("restriction")
    private void adjustViewportForListView() {

        int firstVisibleIndex = virtualFlow.getFirstVisibleCellWithinViewPort().getIndex();
        int lastVisibleIndex = virtualFlow.getLastVisibleCellWithinViewPort().getIndex();
        int numberOfCellsInViewPort = lastVisibleIndex - firstVisibleIndex;

        System.out.println("first visible cell: " + firstVisibleIndex);
        System.out.println("last visible cell: " + lastVisibleIndex);
        System.out.println("number of cells in a viewport:" + numberOfCellsInViewPort);

        Command lastExecutedCommand = rootLayoutController.getLastExecutedCommand();
        boolean isAddCommand = lastExecutedCommand instanceof AddCommand;
        boolean isEditCommand = lastExecutedCommand instanceof EditCommand;
        boolean isDeleteCommand = lastExecutedCommand instanceof DeleteCommand;
        boolean isDoneCommand = lastExecutedCommand instanceof DoneCommand;
        boolean isUndoneCommand = lastExecutedCommand instanceof UndoneCommand;

        if (isAddCommand || isEditCommand || isDeleteCommand || isDoneCommand || isUndoneCommand) {
            logger.log(Level.INFO, "Adjusting viewport for: " + lastExecutedCommand.getClass().getSimpleName());

            int taskIndex = getSelectedIndex();
            System.out.println(taskIndex);

            if (isAddCommand) {
                logger.log(Level.INFO, "Adjusting viewport for: " + lastExecutedCommand.getClass().getSimpleName());
                taskIndex = rootLayoutController.getIndexFromLastExecutedTask();
            }

            if (taskIndex < firstVisibleIndex) {
                int numberOfCellsDifference = firstVisibleIndex - taskIndex;
                listView.scrollTo(taskIndex);
                logger.log(Level.INFO, "Item index: " + taskIndex);
                logger.log(Level.INFO, "Item index is less than viewport first visible index");
                logger.log(Level.INFO, "Cell differences: " + numberOfCellsDifference);
            } else if (taskIndex > lastVisibleIndex) {
                int numberOfCellsDifference = taskIndex - lastVisibleIndex;
                listView.scrollTo(numberOfCellsDifference + 1);
                logger.log(Level.INFO, "Item index: " + taskIndex);
                logger.log(Level.INFO, "Item index is more than viewport last visible index");
                logger.log(Level.INFO, "Cell differences: " + numberOfCellsDifference);
            }
            return;
        }

        if (previousSelectedTaskIndex < firstVisibleIndex) {
            System.out.println("Scrolling up");
            // viewport will scroll and show the current item at the top
            listView.scrollTo(previousSelectedTaskIndex);

        } else if (previousSelectedTaskIndex > lastVisibleIndex) {
            System.out.println("Scrolling down");
            // viewport will scroll and show the current item at the bottom
            listView.scrollTo(firstVisibleIndex + 1);

        }
    }

    /**
    *
    */
    public int getSelectedIndex() {
        return listView.getSelectionModel().getSelectedIndex();
    }

    /**
    *
    */
    public void saveSelectedIndex() {
        previousSelectedTaskIndex = getSelectedIndex();
        System.out.println("Saved index: " + previousSelectedTaskIndex);
    }

    /**
    *
    */
    public int getPreviousSelectedIndex() {
        return previousSelectedTaskIndex;
    }

    /**
    *
    */
    public void restoreListViewPreviousSelection() {
        // if previous selected index was the last index in the previous list
        if (previousSelectedTaskIndex == observableTaskList.size()) {
            listView.getSelectionModel().selectLast();
            saveSelectedIndex();
            // getCurrentListView().scrollTo(getCurrentTaskList().size() - 1);
            logger.log(Level.INFO, "Restore ListView selection to last item");
        } else {
            System.out.println(listView.getId());
            System.out.println(previousSelectedTaskIndex);
            listView.getSelectionModel().select(previousSelectedTaskIndex);
            saveSelectedIndex();
            // getCurrentListView().scrollTo(previousSelectedTaskIndex);
            logger.log(Level.INFO, "Restore ListView selection to previous to previous item");
        }
    }

    public void clearListViewSelection() {
        listView.getSelectionModel().clearSelection();
    }

    public void selectItem(int index) {
        int actualIndex = displayedIndexToOriginalIndexMap.get(index);
        System.out.println("index: " + index + " actualIndex: " + actualIndex);
        listView.getSelectionModel().select(actualIndex);
    }

    public void selectAll() {
        listView.getSelectionModel().selectAll();
    }

    public void selectLast() {
        listView.getSelectionModel().selectLast();
    }

    public void setRootLayoutController(RootLayoutController rootLayoutController) {
        this.rootLayoutController = rootLayoutController;
    }

    /**
    *
    */
    public void handleArrowKeys(KeyEvent keyEvent) {
        saveSelectedIndex();
        System.out.println("handleArrowKeys: " + previousSelectedTaskIndex);
        if (keyEvent.getCode() == KeyCode.UP) {
            if (previousSelectedTaskIndex > 1) {
                listView.getSelectionModel().clearAndSelect(getPreviousSelectedIndex() - 1);
            }

        } else if (keyEvent.getCode() == KeyCode.DOWN) {
            if (previousSelectedTaskIndex < observableTaskList.size() - 1) {
                listView.getSelectionModel().clearAndSelect(getPreviousSelectedIndex() + 1);
            }

        }

        logger.log(Level.INFO, "Pressed " + keyEvent.getCode() + " arrow key: currently selected index is "
                + getSelectedIndex() + " current listview: " + listView.getId());
        System.out.println("handleArrowKeys: " + previousSelectedTaskIndex);
    }

    public boolean isOverdueHeaderAdded() {
        return isOverdueHeaderAdded;
    }

    public boolean isTodayHeaderAdded() {
        return isTodayHeaderAdded;
    }

    public boolean isTomorrowHeaderAdded() {
        return isTomorrowHeaderAdded;
    }

    public boolean isUpcomingHeaderAdded() {
        return isUpcomingHeaderAdded;
    }

    public boolean isSomedayHeaderAdded() {
        return isSomedayHeaderAdded;
    }
}
