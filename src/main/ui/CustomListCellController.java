package main.ui;

import java.io.IOException;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListCell;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import main.data.Task;

public class CustomListCellController extends JFXListCell<Task> {

    @FXML // fx:id="horizontalBox"
    private HBox horizontalBox; // Value injected by FXMLLoader
    
    @FXML // fx:id="verticalBox"
    private VBox verticalBox; // Value injected by FXMLLoader

    @FXML // fx:id="labelTaskTime"
    private JFXButton labelTaskTime; // Value injected by FXMLLoader

    @FXML // fx:id="circleIndex"
    private Circle circleIndex; // Value injected by FXMLLoader

    @FXML // fx:id="labelTaskIndex"
    private Label labelTaskIndex; // Value injected by FXMLLoader

    @FXML // fx:id="flowPaneTitleAndDate"
    private FlowPane flowPaneTitleAndDate; // Value injected by FXMLLoader

    @FXML // fx:id="labelTaskTitle"
    private Label labelTaskTitle; // Value injected by FXMLLoader

    @FXML // fx:id="labelTaskDate"
    private Label labelTaskDate; // Value injected by FXMLLoader

    @FXML // fx:id="labelTaskTag"
    private Label labelTaskTag; // Value injected by FXMLLoader

    public CustomListCellController() {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/main/resources/layouts/CustomListCellLayout.fxml"));
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateItem(Task task, boolean empty) {
        super.updateItem(task, empty);

        if (empty) {
            setGraphic(null);
        } else {
            setLabelTaskIndex(getIndex() + 1);
            setLabelTaskTitle(task);
            setLabelTaskTime(task);
            setLabelTaskDate(task);
            setLabelTaskTag(task);
            setGraphic(getHorizontalBox()); // HBox is the parent layout of all
                                            // the other UI components here.
                                            // Returning an instance of this
                                            // will include the other UI
                                            // components here as well
        }
    }

    public HBox getHorizontalBox() {
        return horizontalBox;
    }

    public void setLabelTaskIndex(int index) {
        this.labelTaskIndex.setText(index + "");
    }

    public void setLabelTaskTime(Task task) {
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

    public void setLabelTaskDate(Task task) {
        System.out.println(task.getSimpleDate());
        if (task.getSimpleDate().isEmpty()) {
            verticalBox.getChildren().remove(this.labelTaskDate);
            this.labelTaskDate.setVisible(false);
            return;
        }
        if(!verticalBox.getChildren().contains(this.labelTaskDate)){
            verticalBox.getChildren().add(this.labelTaskDate);
        }
        this.labelTaskDate.setText(task.getSimpleDate());
    }

    public void setLabelTaskTag(Task task) {
        if (task.hasLabel()) {
            this.labelTaskTag.setText(task.getLabel());
            this.labelTaskTag.setVisible(true);
        } else {
            this.labelTaskTag.setVisible(false);
        }

    }

}
