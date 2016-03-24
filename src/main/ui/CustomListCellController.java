package main.ui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXListCell;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import main.data.Task;

public class CustomListCellController extends JFXListCell<Task> {

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
    
    @FXML // fx:id="labelTaskDate"
    private Label labelTaskDate; // Value injected by FXMLLoader

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
    public void updateItem(Task item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setGraphic(null);
        } else {
            setLabelTaskIndex(getIndex() + 1);
            setLabelTaskTitle(item);
            setLabelTaskTime(item);
            setLabelTaskTag(item);
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
//        if (task.getStartDate() == null && task.getStartDate() == null) {
//            this.labelTaskTime.setText("");
//            return;
//        }
//
//        if (task.getStartDate() == null) {
//            String hours = task.getEndDate().getHours() + "";
//            String minutes = task.getEndDate().getMinutes() + "";
//            if (hours.length() == 1) {
//                hours = "0" + hours;
//            }
//            if (minutes.length() == 1) {
//                minutes = "0" + minutes;
//            }
//
//            this.labelTaskTime.setText(hours + ":" + minutes);
//            return;
//        }
//        if (task.getEndDate() == null) {
//            String hours = task.getStartDate().getHours() + "";
//            String minutes = task.getStartDate().getMinutes() + "";
//            if (hours.length() == 1) {
//                hours = "0" + hours;
//            }
//            if (minutes.length() == 1) {
//                minutes = "0" + minutes;
//            }
//
//            this.labelTaskTime.setText(hours + ":" + minutes);
//
//            return;
//        }
//
//        String startHours = task.getStartDate().getHours() + "";
//        String startMinutes = task.getStartDate().getMinutes() + "";
//
//        String endHours = task.getEndDate().getHours() + "";
//        String endMinutes = task.getEndDate().getMinutes() + "";
//
//        if (startHours.length() == 1) {
//            startHours = "0" + startHours;
//        }
//        if (startMinutes.length() == 1) {
//            startMinutes = "0" + startMinutes;
//        }
//
//        if (endHours.length() == 1) {
//            endHours = "0" + endHours;
//        }
//        if (endMinutes.length() == 1) {
//            endMinutes = "0" + endMinutes;
//        }
//
//        this.labelTaskTime.setText(startHours + ":" + startMinutes + " - " + endHours + ":" + endMinutes);
        // this.labelTaskTime.setText(task.getStartDate().getHours()+":"+task.getStartDate().getMinutes()
        // + " - " +
        // task.getEndDate().getHours()+":"+task.getEndDate().getMinutes());
        this.labelTaskTime.setText(task.getSimpleTime());
    }

    public void setLabelTaskTitle(Task task) {
        this.labelTaskTitle.setText(task.getTitle());
    }
    
    public void setLabelTaskDate(Task task){
        System.out.println(task.getSimpleDate());
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
