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
    private HashMap<Integer, Integer> displayIndexToActualIndexMap;
    private HashMap<Integer, Integer> actualIndexToDisplayIndexMap;

    private boolean isOverdueHeaderAdded = false;
    private boolean isTodayHeaderAdded = false;
    private boolean isTomorrowHeaderAdded = false;
    private boolean isUpcomingHeaderAdded = false;
    private boolean isSomedayHeaderAdded = false;

    private boolean isArrowKeysPressed;

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
        initHashMapForListViewIndexes();
        observableTaskList.setAll(taskListWithHeaders);
        listView.setItems(observableTaskList);
    }

    private void initHashMapForListViewIndexes() {
        if (displayIndexToActualIndexMap == null) {
            displayIndexToActualIndexMap = new HashMap<>(taskListWithHeaders.size());
        }

        if (actualIndexToDisplayIndexMap == null) {
            actualIndexToDisplayIndexMap = new HashMap<>(taskListWithHeaders.size());
        }

        actualIndexToDisplayIndexMap.clear();
        displayIndexToActualIndexMap.clear();

        for (int i = 0; i < taskListWithHeaders.size(); i++) {
            Task task = taskListWithHeaders.get(i);
            int indexWithOffset = getIndexWithOffset(task, i);
            boolean isTaskObject = !(task instanceof TaskHeader);
            if (isTaskObject) {
                mapIndexWithOffsetToActualIndex(indexWithOffset, i);
                mapActualIndexToIndexWithOffset(i, indexWithOffset);
            }

        }

    }

    public void mapIndexWithOffsetToActualIndex(int displayIndex, int actualIndex) {
        displayIndexToActualIndexMap.put(displayIndex, actualIndex);

    }

    public void mapActualIndexToIndexWithOffset(int actualIndex, int displayIndex) {
        actualIndexToDisplayIndexMap.put(actualIndex, displayIndex);

    }

    public int getActualIndex(int displayIndex) {
        return displayIndexToActualIndexMap.get(displayIndex);
    }

    public int getDisplayIndex(int actualIndex) {
        return actualIndexToDisplayIndexMap.get(actualIndex);
    }

    private int getIndexWithOffset(Task task, int taskIndex) {

        int numberOfHeaders = 0;
        if (task.isToday() && !task.isOverdue()) {
            if (isOverdueHeaderAdded()) {
                numberOfHeaders++;
            }
        }

        if (task.isTomorrow()) {
            if (isOverdueHeaderAdded()) {
                numberOfHeaders++;
            }
            if (isTodayHeaderAdded()) {
                numberOfHeaders++;
            }
        }

        if (task.isUpcoming()) {
            if (isOverdueHeaderAdded()) {
                numberOfHeaders++;
            }
            if (isTodayHeaderAdded()) {
                numberOfHeaders++;
            }
            if (isTomorrowHeaderAdded()) {
                numberOfHeaders++;
            }
        }

        if (task.isSomeday()) {
            if (isOverdueHeaderAdded()) {
                numberOfHeaders++;
            }
            if (isTodayHeaderAdded()) {
                numberOfHeaders++;
            }
            if (isTomorrowHeaderAdded()) {
                numberOfHeaders++;
            }
            if (isUpcomingHeaderAdded()) {
                numberOfHeaders++;
            }

        }

        int indexWithOffset = taskIndex - numberOfHeaders;
        System.out.println("indexWithOffset: " + indexWithOffset + " original index: " + taskIndex);
        return indexWithOffset;
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
                    if (getSelectedIndex() < getPreviousSelectedIndex()) {
                        listView.getSelectionModel().clearAndSelect(getSelectedIndex() - 1);

                    } else if (getSelectedIndex() > getPreviousSelectedIndex()) {
                        listView.getSelectionModel().clearAndSelect(getSelectedIndex() + 1);
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
        if (virtualFlow.getFirstVisibleCellWithinViewPort() != null) {
            int firstVisibleIndex = virtualFlow.getFirstVisibleCellWithinViewPort().getIndex();
            int lastVisibleIndex = virtualFlow.getLastVisibleCellWithinViewPort().getIndex();
            int numberOfCellsInViewPort = lastVisibleIndex - firstVisibleIndex + 1;
    
            System.out.println("first visible cell: " + firstVisibleIndex);
            System.out.println("last visible cell: " + lastVisibleIndex);
            System.out.println("number of cells in a viewport:" + numberOfCellsInViewPort);
            
            if (isArrowKeysPressed) {
                System.out.println("arrowkeys true");
                System.out.println("adjustViewPort: previous index " + previousSelectedTaskIndex);
                if (getSelectedIndex() <= firstVisibleIndex) {
                    System.out.println("Scrolling up");
                    // if the item at current index -1 is a task header, adjust
                    // viewport to show header
                    if (taskListWithHeaders.get(getSelectedIndex() - 1) instanceof TaskHeader) {
                        listView.scrollTo(getSelectedIndex() - 1);
                    } else {
                        // viewport will scroll and show the current item at the
                        // top
                        listView.scrollTo(getSelectedIndex());
                    }
    
                } else if (getSelectedIndex() > lastVisibleIndex) {
                    System.out.println("Scrolling down");
                    // viewport will scroll and show the current item at the bottom
                    listView.scrollTo(firstVisibleIndex + 1);
                }
                isArrowKeysPressed = false;
                System.out.println("arrowkeys false");
                return;
            }
    
            Command lastExecutedCommand = rootLayoutController.getLastExecutedCommand();
            boolean isAddCommand = lastExecutedCommand instanceof AddCommand;
            boolean isEditCommand = lastExecutedCommand instanceof EditCommand;
            boolean isDeleteCommand = lastExecutedCommand instanceof DeleteCommand;
            boolean isDoneCommand = lastExecutedCommand instanceof DoneCommand;
            boolean isUndoneCommand = lastExecutedCommand instanceof UndoneCommand;
    
            if (isAddCommand || isEditCommand || isDeleteCommand || isDoneCommand || isUndoneCommand) {
                logger.log(Level.INFO, "Adjusting viewport for: " + lastExecutedCommand.getClass().getSimpleName());
                
                if (getSelectedIndex() <= firstVisibleIndex) {
                    int numberOfCellsDifference = firstVisibleIndex - getSelectedIndex();
                    if (taskListWithHeaders.get(getSelectedIndex() - 1) instanceof TaskHeader) {
                        listView.scrollTo(getSelectedIndex() - 1);
                    } else {
                        listView.scrollTo(getSelectedIndex());
                    }
    
                    System.out.println("Item index: " + getSelectedIndex());
                    System.out.println("Item index is less than viewport first visible index");
                    System.out.println("Cell differences: " + numberOfCellsDifference);
                } else if (getSelectedIndex() > lastVisibleIndex) {
                    int numberOfCellsDifference = getSelectedIndex() - lastVisibleIndex;
                    if (numberOfCellsDifference == 1) {
                        listView.scrollTo(firstVisibleIndex + 1);
                    } else {
                        listView.scrollTo(numberOfCellsDifference + 4);
                    }
    
                    System.out.println("Item index: " + getSelectedIndex());
                    System.out.println("Item index is more than viewport last visible index");
                    System.out.println("Cell differences: " + numberOfCellsDifference);
                }
                return;
            }
        }
    }

    /**
    *
    */
    public int getSelectedIndex() {
        if (listView.getSelectionModel().getSelectedIndex() < 0) {
            return 1;
        }
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
            listView.getSelectionModel().clearAndSelect(observableTaskList.size() - 1); // select the last index
            saveSelectedIndex();
            logger.log(Level.INFO, "Restore ListView selection to last item");
        } else {
            System.out.println(listView.getId());
            System.out.println(previousSelectedTaskIndex);
            listView.getSelectionModel().select(previousSelectedTaskIndex);
            saveSelectedIndex();
            logger.log(Level.INFO, "Restore ListView selection to previous to previous item");
        }
    }

    public void clearListViewSelection() {
        listView.getSelectionModel().clearSelection();
    }

    public void select(int index) {
        int actualIndex;
        try {
            actualIndex = displayIndexToActualIndexMap.get(index);
        } catch (NullPointerException e) {
            actualIndex = index - 1;
        }
        listView.getSelectionModel().select(actualIndex);
    }

    public void clearAndSelect(int index) {
        int actualIndex = displayIndexToActualIndexMap.get(index);
        listView.getSelectionModel().clearAndSelect(actualIndex);
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
            if (getPreviousSelectedIndex() > 1) {
                isArrowKeysPressed = true;
                listView.getSelectionModel().clearAndSelect(getPreviousSelectedIndex() - 1);
            }

        } else if (keyEvent.getCode() == KeyCode.DOWN) {
            if (getPreviousSelectedIndex() < observableTaskList.size() - 1) {
                isArrowKeysPressed = true;
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
