package main.ui;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;

public class ListCellHeaderController extends Label {
    public ListCellHeaderController() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/layouts/ListCellHeaderLayout.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
