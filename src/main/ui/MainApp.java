//@@author A0126400Y
package main.ui;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApp extends Application {
    private static final String WINDOW_TITLE = "Dooleh";
    private Stage primaryStage;
    private StackPane rootLayout;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle(WINDOW_TITLE);
        initRootLayout();
    }

    /**
     * Initializes the root layout.
     */
    public void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/layouts/RootLayout.fxml"));
//            rootLayout = FXMLLoader.load(getClass().getResource("/main/resources/layouts/RootLayout.fxml"));

            rootLayout = (StackPane) loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);

            primaryStage.setScene(scene);
            primaryStage.show();

            //get a handle on the UI controller and set focus to the text field
            RootLayoutController rootLayoutController = (RootLayoutController) loader.getController();
            rootLayoutController.requestFocusForCommandBar();
            rootLayoutController.selectFirstItemFromListView();
            rootLayoutController.initListViewBehavior();
            rootLayoutController.setMainApp(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the main stage.
     *
     * @return
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
