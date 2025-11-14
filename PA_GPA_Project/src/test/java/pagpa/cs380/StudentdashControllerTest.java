package pagpa.cs380;

import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

@ExtendWith(ApplicationExtension.class)
class StudentdashControllerTest {
    
    private StudentdashController controller;
    private Stage stage;

    @Start
    private void start(Stage stage) throws Exception {
        this.stage = stage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/pagpa/cs380/studentdash2.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @BeforeEach
    public void setUp() {
        // Reset any static state between tests
    }

    @Test
    void testAllButtonsPresent() {
        // Verify all buttons are present
        verifyThat("#gpaForecastButton", (Button button) -> button != null);
        verifyThat("#courseViewButton", (Button button) -> button != null);
        verifyThat("#studentEncounterButton", (Button button) -> button != null);
        verifyThat("#learningPlanButton", (Button button) -> button != null);
        verifyThat("#statusHistoryButton", (Button button) -> button != null);
    }

    @Test
    void testGpaForecastButtonNavigation(FxRobot robot) {
        // Click GPA Forecast button
        robot.clickOn("#gpaForecastButton");
        
        // Verify we navigated to GPA forecast view
        Scene currentScene = stage.getScene();
        assertNotNull(currentScene.lookup("#gpaChart"), "Should show GPA chart after clicking GPA Forecast");
    }

    @Test
    void testCourseViewButtonNavigation(FxRobot robot) {
        // Click Course View button
        robot.clickOn("#courseViewButton");
        
        // Verify we navigated to course view
        Scene currentScene = stage.getScene();
        assertNotNull(currentScene.lookup("#gradeTable"), "Should show grade table after clicking Course View");
    }

    @Test
    void testGpaChartIsVisible(FxRobot robot) {
        // Click GPA Forecast button to ensure chart is shown
        robot.clickOn("#gpaForecastButton");
        Scene currentScene = stage.getScene();
        javafx.scene.chart.LineChart<?,?> chart = (javafx.scene.chart.LineChart<?,?>) currentScene.lookup("#gpaChart");
        assertNotNull(chart, "GPA chart should be present in the scene");
        assertTrue(chart.isVisible(), "GPA chart should be visible");
    }
}
