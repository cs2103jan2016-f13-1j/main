//@@author A0126400Y
package main.ui;

import java.io.IOException;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListCell;
import com.jfoenix.effects.JFXDepthManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import main.data.Task;
import main.data.TaskHeader;

public class ListCellController extends JFXListCell<Task> {

    @FXML // fx:id="horizontalBox"
    private HBox horizontalBox; // Value injected by FXMLLoader

    @FXML // fx:id="labelTaskTime"
    private JFXButton labelTaskTime; // Value injected by FXMLLoader

    @FXML // fx:id="topLine"
    private Line topLine; // Value injected by FXMLLoader

    @FXML // fx:id="bottomLine"
    private Line bottomLine; // Value injected by FXMLLoader

    @FXML // fx:id="circleIndex"
    private Circle circleIndex; // Value injected by FXMLLoader

    @FXML // fx:id="labelTaskIndex"
    private Label labelTaskIndex; // Value injected by FXMLLoader

    @FXML // fx:id="verticalBox"
    private VBox verticalBox; // Value injected by FXMLLoader

    @FXML // fx:id="labelTaskTitle"
    private Label labelTaskTitle; // Value injected by FXMLLoader

    @FXML // fx:id="labelTaskDate"
    private Label labelTaskDate; // Value injected by FXMLLoader

    @FXML // fx:id="labelTaskLabel"
    private Label labelTaskLabel; // Value injected by FXMLLoader

    @FXML // fx:id="rectangleTaskPriority"
    private Rectangle rectangleTaskPriority; // Value injected by FXMLLoader

    private ListViewController parentListViewController;

    @FXML // This method is called by the FXMLLoader when initialization is
          // complete
    void initialize() {
        assert horizontalBox != null : "fx:id=\"horizontalBox\" was not injected: check your FXML file 'CustomListCellLayout.fxml'.";
        assert labelTaskTime != null : "fx:id=\"labelTaskTime\" was not injected: check your FXML file 'CustomListCellLayout.fxml'.";
        assert topLine != null : "fx:id=\"topLine\" was not injected: check your FXML file 'CustomListCellLayout.fxml'.";
        assert bottomLine != null : "fx:id=\"bottomLine\" was not injected: check your FXML file 'CustomListCellLayout.fxml'.";
        assert circleIndex != null : "fx:id=\"circleIndex\" was not injected: check your FXML file 'CustomListCellLayout.fxml'.";
        assert labelTaskIndex != null : "fx:id=\"labelTaskIndex\" was not injected: check your FXML file 'CustomListCellLayout.fxml'.";
        assert verticalBox != null : "fx:id=\"verticalBox\" was not injected: check your FXML file 'CustomListCellLayout.fxml'.";
        assert labelTaskTitle != null : "fx:id=\"labelTaskTitle\" was not injected: check your FXML file 'CustomListCellLayout.fxml'.";
        assert labelTaskDate != null : "fx:id=\"labelTaskDate\" was not injected: check your FXML file 'CustomListCellLayout.fxml'.";
        assert labelTaskLabel != null : "fx:id=\"labelTaskLabel\" was not injected: check your FXML file 'CustomListCellLayout.fxml'.";
        assert rectangleTaskPriority != null : "fx:id=\"rectangleTaskPriority\" was not injected: check your FXML file 'CustomListCellLayout.fxml'.";

    }

    public ListCellController(ListViewController parentListViewController) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/layouts/ListCellLayout.fxml"));
        loader.setController(this);
        try {
            loader.load();
            this.parentListViewController = parentListViewController;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateItem(Task task, boolean empty) {
        super.updateItem(task, empty);
        if (empty || task == null) {
            setGraphic(null);
        } else {
            if (task instanceof TaskHeader) {
                ListCellHeaderController listCellHeader = new ListCellHeaderController();
                listCellHeader.setText(task.getTitle());
                setGraphic(listCellHeader);
            } else {
                showTaskIndex(getIndex());
                setLabelTaskTitle(task);
                showTaskTime(task);
                showTaskDate(task);
                showTaskLabel(task);
                showTaskPriority(task);
                showTaskCollisions(task);
                // HBox is the parent layout of all
                // the other UI components here.
                // Returning an instance of this
                // will include the other UI
                // components here as well
                setGraphic(getHorizontalBox());
            }

        }

    }

    /**
     * @param task
     */
    private void showTaskCollisions(Task task) {
        if (!task.getCollideWithPrev() && !task.getCollideWithNext()) {
            topLine.setStroke(Color.web(AppColor.LINE_STROKE, 0.12));
            bottomLine.setStroke(Color.web(AppColor.LINE_STROKE, 0.12));
            circleIndex.setStroke(Color.web(AppColor.CIRCLE_STROKE));
            return;
        }

        if (task.getCollideWithPrev()) {
            topLine.setStroke(Color.web(AppColor.PRIMARY_RED));
            circleIndex.setStroke(Color.web(AppColor.PRIMARY_RED));
        } else {
            topLine.setStroke(Color.web(AppColor.LINE_STROKE, 0.12));
        }
        if (task.getCollideWithNext()) {
            bottomLine.setStroke(Color.web(AppColor.PRIMARY_RED));
            circleIndex.setStroke(Color.web(AppColor.PRIMARY_RED));
        } else {
            bottomLine.setStroke(Color.web(AppColor.LINE_STROKE, 0.12));
        }
    }

    public HBox getHorizontalBox() {
        return horizontalBox;
    }

    public void showTaskIndex(int taskIndex) {
        this.labelTaskIndex.setText(parentListViewController.getDisplayIndex(taskIndex) + "");
    }


    public void showTaskTime(Task task) {
        if (task.getSimpleTime().isEmpty()) {
            System.out.println(task.getSimpleTime());
            this.labelTaskTime.setText("-");
            return;
        }
        this.labelTaskTime.setText(task.getSimpleTime());
    }

    public void setLabelTaskTitle(Task task) {
        this.labelTaskTitle.setText(task.getTitle());
    }

    public void showTaskDate(Task task) {
        if (!task.hasDate()) {
            verticalBox.getChildren().remove(this.labelTaskDate);
            return;
        }

        this.labelTaskDate.setText(task.getSimpleDate());

        if (!verticalBox.getChildren().contains(this.labelTaskDate)) {
            verticalBox.getChildren().add(this.labelTaskDate);
        }

    }

    public void showTaskLabel(Task task) {
        if (task.hasLabel()) {
            this.labelTaskLabel.setText(task.getLabel());
            this.labelTaskLabel.setVisible(true);
        } else {
            this.labelTaskLabel.setVisible(false);
        }
    }

    public void showTaskPriority(Task task) {
        if (task.getPriority() == 1) {
            rectangleTaskPriority.setFill(Color.web(AppColor.PRIORITY_LOW));
            return;
        }

        if (task.getPriority() == 2) {
            rectangleTaskPriority.setFill(Color.web(AppColor.PRIORITY_MED));
            return;
        }

        if (task.getPriority() == 3) {
            rectangleTaskPriority.setFill(Color.web(AppColor.PRIORITY_HIGH));
            return;
        }

        rectangleTaskPriority.setFill(Color.TRANSPARENT);
    }
}
