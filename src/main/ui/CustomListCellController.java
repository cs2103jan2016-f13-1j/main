package main.ui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import main.data.Task;

public class CustomListCellController extends ListCell<Task> {

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="horizontalBox"
    private HBox horizontalBox; // Value injected by FXMLLoader

    @FXML // fx:id="labelTaskIndex"
    private Label labelTaskIndex; // Value injected by FXMLLoader

    @FXML // fx:id="labelTaskTime"
    private Label labelTaskTime; // Value injected by FXMLLoader

    @FXML // fx:id="labelTaskTitle"
    private Label labelTaskTitle; // Value injected by FXMLLoader

    @FXML // fx:id="labelTaskTag"
    private Label labelTaskTag; // Value injected by FXMLLoader

    public CustomListCellController() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/layouts/CustomListCellLayout.fxml"));
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void updateItem(Task item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setGraphic(null);
        } else {
            setLabelTaskIndex(getIndex()+1);
            setLabelTaskTitle(item);
            setLabelTaskTime(item);
            setLabelTaskTag(item);
            setGraphic(getHorizontalBox());
        }
    }

    public HBox getHorizontalBox() {
        return horizontalBox;
    }

    public void setLabelTaskIndex(int index) {
        this.labelTaskIndex.setText(index+"");
    }

    public void setLabelTaskTime(Task task) {
        this.labelTaskTime.setText(task.getStartDate() + " - " + task.getEndDate());
    }

    public void setLabelTaskTitle(Task task) {
        this.labelTaskTitle.setText(task.getTitle());
    }

    public void setLabelTaskTag(Task task) {
        if(task.hasLabel()){
            this.labelTaskTag.setText(task.getLabel());   
            this.labelTaskTag.setVisible(true);
        }
        else{
            this.labelTaskTag.setVisible(false);
        }

        

    }

}
