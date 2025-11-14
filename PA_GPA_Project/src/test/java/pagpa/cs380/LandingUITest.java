package pagpa.cs380;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

/**
 * Verifies that all five left-side buttons exist and carry the
 * expected label text.  Uses the JUnit-5 TestFX extension.
 * 
 * This one works
 */
@ExtendWith(ApplicationExtension.class)
class LandingUITest {


	/*
	 * Launch the app once to do the tests on it after
	 */
	
    @Start
    private void start(Stage stage) throws Exception {
        Parent root = new FXMLLoader(
                getClass().getResource("/pagpa/cs380/primary.fxml")
        ).load();
        stage.setScene(new Scene(root));
        stage.show();
    }
    
	/*
	 * Now we test all FXML left side yellow buttons
	 * If they FXID and test matches and test passes, then we know 
	 * the right FXID is being assigned #onActions, setTexts, etc. 
	 */

    @Test
    void allStudentsButtonExists(FxRobot robot) {
        Button btn = robot.lookup("#allStudentsButton").queryButton();
        assertNotNull(btn);
        assertEquals("ALL COHORTS", btn.getText());
    }

    @Test
    void currentStudentsButtonExists(FxRobot robot) {
        Button btn = robot.lookup("#currentStudentsButton").queryButton();
        assertNotNull(btn);
        assertEquals("CURRENT STUDENTS", btn.getText());
    }

    @Test
    void generateReportButtonExists(FxRobot robot) {
        Button btn = robot.lookup("#generateReportButton").queryButton();
        assertNotNull(btn);
        assertEquals("GENERAL REPORT", btn.getText());
    }

    @Test
    void manageCurriculumButtonExists(FxRobot robot) {
        Button btn = robot.lookup("#manageCurrButton").queryButton();
        assertNotNull(btn);
        assertEquals("MANAGE CURRICULUM", btn.getText());
    }

    @Test
    void manageCoachesButtonExists(FxRobot robot) {
        Button btn = robot.lookup("#manageCoachesButton").queryButton();
        assertNotNull(btn);
        assertEquals("MANAGE COACHES", btn.getText());
    }
}
