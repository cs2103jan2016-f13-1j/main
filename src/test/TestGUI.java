package test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertEquals;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.service.query.impl.NodeQueryUtils.hasText;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import main.ui.DoolehMainApp;
import main.ui.RootLayoutController;

public class TestGUI extends ApplicationTest {
    private Stage primaryStage;
    private AnchorPane rootLayout;
    private RootLayoutController controller;

    // Ctrl+Tab hotkey
    private static final KeyCodeCombination HOTKEY_CTRL_TAB = new KeyCodeCombination(KeyCode.TAB,
            KeyCombination.CONTROL_DOWN);

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.primaryStage.setTitle("Dooleh");
        initRootLayout();

    }

    @Before
    public void initTaskListWithOneItem() {
        String json = "[[{\"title\": \"First task\",\"status\": false,\"priority\": 0}],[]]";
        ArrayList<String> lines = new ArrayList<String>();
        lines.add(json);

        try {
            Files.write(Paths.get("storage.txt"), lines);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }
    }

    @Test
    public void currentModeLabel_should_follow_current_tab_name() {
        clickOn("#tabToday", MouseButton.PRIMARY);
        verifyThat("#labelCurrentMode", hasText("Today"));
        clickOn("#tabWeek", MouseButton.PRIMARY);
        verifyThat("#labelCurrentMode", hasText("This week"));
        clickOn("#tabAll", MouseButton.PRIMARY);
        verifyThat("#labelCurrentMode", hasText("All"));
    }

    @Test
    public void user_can_cycle_through_tabs_using_ctrl_tab_hotkeys() {
        TabPane tabPane = (TabPane) primaryStage.getScene().lookup("#tabPane");
        int previousTabIndex = tabPane.getSelectionModel().getSelectedIndex();

        for (int i = 0; i < 12; i++) {
            push(HOTKEY_CTRL_TAB);
            assertNotEquals(previousTabIndex, tabPane.getSelectionModel().getSelectedIndex());
            previousTabIndex = tabPane.getSelectionModel().getSelectedIndex();
        }

    }

    @Test
    public void commandBar_should_always_be_focused() {
        TextField commandBar = (TextField) primaryStage.getScene().lookup("#commandBar");
        clickOn("#tabToday", MouseButton.PRIMARY);
        assertTrue(commandBar.isFocused());
        clickOn("#tabWeek", MouseButton.PRIMARY);
        assertTrue(commandBar.isFocused());
        clickOn("#tabAll", MouseButton.PRIMARY);
        assertTrue(commandBar.isFocused());
        clickOn("#labelCurrentMode", MouseButton.PRIMARY);
        assertTrue(commandBar.isFocused());
        clickOn("#listView", MouseButton.PRIMARY);
        assertTrue(commandBar.isFocused());
    }

    @Test
    public void listView_should_always_select_first_item_when_app_is_launched() {
        @SuppressWarnings("rawtypes")
        ListView listView = ((ListView) primaryStage.getScene().lookup("#listView"));
        assertEquals(0, listView.getSelectionModel().getSelectedIndex());
    }

    @Test
    public void userFeedback_should_be_shown_when_user_is_typing_something() {
        TextField commandBar = (TextField) primaryStage.getScene().lookup("#commandBar");

        write("I am testing the GUI woooooohoooo");
        verifyThat("#labelUserFeedback", hasText("I am testing the GUI woooooohoooo"));
        commandBar.clear();

        write("del 1");
        verifyThat("#labelUserFeedback", hasText("First task"));
        commandBar.clear();

        write("del 10");
        verifyThat("#labelResult", hasText("Task -10- not found."));
        commandBar.clear();
    }

    @Test
    public void user_can_add_task_with_only_a_title() {
        write("Test task 2").push(KeyCode.ENTER);
        write("Test task 3").push(KeyCode.ENTER);
        write("Test task 4").push(KeyCode.ENTER);

        assertEquals(4, controller.getTaskList().size());
    }

    @Test
    public void user_can_delete_task_using_delete_command() {
        write("Test task 2").push(KeyCode.ENTER);
        write("Test task 3").push(KeyCode.ENTER);
        write("Test task 4").push(KeyCode.ENTER);

        write("del 2").push(KeyCode.ENTER);
        write("del 2").push(KeyCode.ENTER);
        write("del 2").push(KeyCode.ENTER);

        assertEquals(1, controller.getTaskList().size());
    }

    @Test
    public void user_can_select_task_using_up_or_down_arrow_hotkeys() {
        @SuppressWarnings("rawtypes")
        ListView listView = ((ListView) primaryStage.getScene().lookup("#listView"));
        TextField commandBar = (TextField) primaryStage.getScene().lookup("#commandBar");
        
        commandBar.setText("Test task 2");
        push(KeyCode.ENTER);
        commandBar.setText("Test task 3");
        push(KeyCode.ENTER);
        commandBar.setText("Test task 4");
        push(KeyCode.ENTER);

        int previousTaskIndex = listView.getSelectionModel().getSelectedIndex();

        push(KeyCode.UP);
        assertEquals(previousTaskIndex - 1, listView.getSelectionModel().getSelectedIndex());
        previousTaskIndex = listView.getSelectionModel().getSelectedIndex();

        push(KeyCode.UP);
        assertEquals(previousTaskIndex - 1, listView.getSelectionModel().getSelectedIndex());
        previousTaskIndex = listView.getSelectionModel().getSelectedIndex();

        push(KeyCode.UP);
        assertEquals(previousTaskIndex - 1, listView.getSelectionModel().getSelectedIndex());
        previousTaskIndex = listView.getSelectionModel().getSelectedIndex();

        push(KeyCode.DOWN);
        assertEquals(previousTaskIndex + 1, listView.getSelectionModel().getSelectedIndex());
        previousTaskIndex = listView.getSelectionModel().getSelectedIndex();

        push(KeyCode.DOWN);
        assertEquals(previousTaskIndex + 1, listView.getSelectionModel().getSelectedIndex());
        previousTaskIndex = listView.getSelectionModel().getSelectedIndex();

        push(KeyCode.DOWN);
        assertEquals(previousTaskIndex + 1, listView.getSelectionModel().getSelectedIndex());
        previousTaskIndex = listView.getSelectionModel().getSelectedIndex();

    }

    /**
     * Initializes the root layout.
     */
    public void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(DoolehMainApp.class.getResource("/main/resources/layouts/RootLayout.fxml"));

            rootLayout = (AnchorPane) loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();

            // get a handle on the UI controller and set focus to the text field
            controller = (RootLayoutController) loader.getController();
            controller.requestFocusForCommandBar();
            controller.selectFirstItemFromListView();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
