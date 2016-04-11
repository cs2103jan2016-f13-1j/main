package main.ui;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;

public class HelpPage extends StackPane {

    public HelpPage(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/main/resources/layouts/HelpPage.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

}
