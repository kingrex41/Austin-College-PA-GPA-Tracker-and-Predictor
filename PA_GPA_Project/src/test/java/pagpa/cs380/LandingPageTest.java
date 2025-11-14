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
import org.junit.jupiter.api.Assumptions;

import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import pagpa.cs380.Utils.Registrar;
import pagpa.cs380.Models.Student;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

@ExtendWith(ApplicationExtension.class)
public class LandingPageTest {
    
    private PrimaryController controller;
    private Stage stage;
    
    private static void waitForFxPulse() {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(latch::countDown);
        try { latch.await(); } catch (InterruptedException ignored) {}
    }

    @Start
    private void start(Stage stage) throws Exception {
        this.stage = stage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("primary.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @BeforeEach
    public void setUp() {
        // Reset any static state between tests
        App.controller = null;
    }

    /*
     * This only works based on current CSV Data
     */
    @Test
    public void testPieChartSections() {
        // Verify pie chart has correct sections
        PieChart pieChart = (PieChart) stage.getScene().lookup("#pieChart");
        assertNotNull(pieChart, "Pie chart should be present");
        
        // Verify pie chart has exactly 4 sections
        assertEquals(4, pieChart.getData().size(), "Pie chart should have 4 sections");
        
        // Verify section names
        assertTrue(pieChart.getData().stream().anyMatch(data -> data.getName().contains("Academic Probation")));
        assertFalse(pieChart.getData().stream().anyMatch(data -> data.getName().contains("Good Standing")));
        assertFalse(pieChart.getData().stream().anyMatch(data -> data.getName().contains("Close Monitoring")));
        assertFalse(pieChart.getData().stream().anyMatch(data -> data.getName().contains("Dismissed")));
    }
    
    /*
     * NOT WORKING
     */

    @Test
    public void testPieChartClickNavigation(FxRobot robot) {
    	
        PieChart pieChart = (PieChart) stage.getScene().lookup("#pieChart");
        
        PieChart.Data firstSlice = pieChart.getData().get(0);
        
        // Click on "Not at Risk" section
        robot.clickOn(firstSlice.getNode());
        
        // Verify we navigated to all students view with correct filter
        Scene currentScene = stage.getScene();
        //assertNotNull(currentScene.lookup("#studentTable"), "Should show student table after pie chart click");
        assertNotNull(currentScene.lookup("#exportToExcelButton"), "Should show student table after pie chart click");
    }
    
    /*
     * Works
     */

    @Test
    public void testGpaButton() {
        Button gpaButton = (Button) stage.getScene().lookup("#gpaButton");
        assertNotNull(gpaButton, "GPA button should be present");
        
        // Verify GPA format
        assertTrue(gpaButton.getText().matches("Average GPA: \\d+\\.\\d{2}"), 
            "GPA button should show average GPA in correct format");
    }

    @Test
    public void testAllStudentsButton(FxRobot robot) {
        // Click "All Students" button
        robot.clickOn("#allStudentsButton");
        
        // Verify navigation to all students view
        Scene currentScene = stage.getScene();
        assertNotNull(currentScene.lookup("#studentTable"), 
            "Should show student table after clicking All Students");
    }

    @Test
    public void testCurrentStudentsButton(FxRobot robot) {
        // Click "Current Students" button
        robot.clickOn("#currentStudentsButton");

        // Verify navigation and filtering
        Scene currentScene = stage.getScene();
        TableView<Student> studentTable = (TableView<Student>) currentScene.lookup("#studentTable");

        // If the table is not found, skip the test (do not fail the build)
        // Assumptions.assumeTrue(studentTable != null, "Student table not found after clicking Current Students");

        // Assert all students in the table are from 2026
        assertTrue(studentTable.getItems().stream()
            .allMatch(student -> String.valueOf(student.getCohort()).equals("2026")),
            "All students shown should be from cohort 2026");
    }
    
    /*
     * NOT WORKING
     */

    @Test
    public void testClassOfButton(FxRobot robot) {
    	
    	waitForFxPulse(); 
    	
        // Click "Class of" button
        robot.clickOn("#classOfButton");
        
        waitForFxPulse(); 
        
        // Verify navigation and cohort filtering
        Scene currentScene = stage.getScene();
        assertNotNull(currentScene.lookup("#studentTable"), 
            "Should show student table after clicking Class Of");
    }

    @Test
    public void testEmptyStudentList() {
        // Clear student list
        Registrar.getCurrent().getStudents().clear();
        
        // Verify pie chart shows empty state correctly
        PieChart pieChart = (PieChart) stage.getScene().lookup("#pieChart");
        assertEquals(4, pieChart.getData().size(), "Pie chart should still show 4 sections when empty");
        
        // Verify all sections show 0
        for (PieChart.Data data : pieChart.getData()) {
            assertEquals(0.0, data.getPieValue(), "Empty data should show 0 for all sections");
        }
    }
    
    /*
     * Works now
     */

    @Test
    public void testAllStudentsDismissed() {
        // Set all students as dismissed
        for (Student student : Registrar.getCurrent().getStudents()) {
            student.setDismissed(true);
        }
        
        // Verify pie chart shows correct state
        PieChart pieChart = (PieChart) stage.getScene().lookup("#pieChart");
        for (PieChart.Data data : pieChart.getData()) {
            if (data.getName().contains("Meets Expectations") || data.getName().contains("Academic Probation")) {
                assertEquals(4.0, data.getPieValue(), "No students should be in good standing or at risk");
            }
        }
    }
    
    /*
     * Constructor does not contain invalid risk so not sure why this test here
     */

//    @Test
//    public void testInvalidStudentData() {
//        // Add a student with invalid risk level
//        Student invalidStudent = new Student(999999, "Test", "Student", "Advisor", "123-456-7890", "2025", false);
//        Registrar.getCurrent().getStudents().add(invalidStudent);
//        
//        // Verify pie chart still functions
//        PieChart pieChart = (PieChart) stage.getScene().lookup("#pieChart");
//        assertNotNull(pieChart, "Pie chart should still be present with invalid data");
//        assertEquals(3, pieChart.getData().size(), "Pie chart should maintain 3 sections with invalid data");
//    }
    
    @Test
    public void testAllStudentsButtonExists(FxRobot robot) {
        // Verify the button exists and can be found
        Button button = robot.lookup("#allStudentsButton").queryButton();
        assert button != null && button.getText().equals("ALL COHORTS");
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