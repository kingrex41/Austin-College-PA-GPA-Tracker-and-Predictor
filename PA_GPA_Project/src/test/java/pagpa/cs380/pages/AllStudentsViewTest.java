/* src/test/java/pagpa/cs380/AllStudentsViewTest.java
 * ─────────────────────────────────────────────────────────────── */
package pagpa.cs380.pages;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import pagpa.cs380.Models.Student;
import pagpa.cs380.Utils.Registrar;
import pagpa.cs380.Views.AllStudentsView;

@ExtendWith(ApplicationExtension.class)
public class AllStudentsViewTest {

	/*
	 * Starting with making a list of fake students
	 */
    private static final List<Student> SAMPLE = List.of(
        new Student(1, "Amy",  "Adams",  "Dr. Brown", "555--444-0001", "2025", false),
        new Student(2, "Bob",  "Barker", "Dr. Chan",  "555--333-0002", "2026", false),
        new Student(3, "Cory", "Cooper","Dr. Brown",  "555--567-0003", "2026", false)
    );

    @BeforeEach
    void injectFakeRegistrar() throws Exception {
        Registrar stub = mock(Registrar.class, RETURNS_DEEP_STUBS);
        when(stub.getStudents()).thenReturn(SAMPLE);
        when(stub.calcGpaForStudent(any(), anyBoolean())).thenReturn(3.25);
        when(stub.calcTermGpa(any(), anyString(), anyBoolean())).thenReturn(3.10);
        when(stub.getCurrentTerm()).thenReturn("SPRING");

        var fld = Registrar.class.getDeclaredField("current");
        fld.setAccessible(true);
        fld.set(null, stub);
    }

    /*
     * Load the UI once
     */
    private Stage stage;

    @Start
    private void start(Stage s) throws Exception {
        stage = s;
        Parent root = new FXMLLoader(
                getClass().getResource("allstudents.fxml")).load();
        s.setScene(new Scene(root));
        s.show();
    }

    private TableView<Student> table()        { return lookup("#studentTable"); }
    private ComboBox<String> cohortBox()      { return lookup("#cohortComboBox"); }
    private ComboBox<String> riskBox()        { return lookup("#riskComboBox");  }
    private ComboBox<String> advisorBox()     { return lookup("#advisorComboBox"); }
    private TextField search()                { return lookup("#searchField");   }
    private <T> T lookup(String css)          { return (T) stage.getScene().lookup(css); }


    @Test
    void tableIsPopulatedOnLoad() {
        assertNotNull(table(),           "TableView should exist");
        assertEquals(3, table().getItems().size(),
                     "Table should start with all students");
        // first column added by the controller is “Select”
        assertEquals("Select", table().getColumns().get(0).getText());
    }

    @Test
    void cohortFilterWorks(FxRobot robot) {
        robot.clickOn(cohortBox()).clickOn("2024");
        assertEquals(2, table().getItems().size(),
                     "Only 2024 students should remain");
        // reset for the next test
        robot.clickOn(cohortBox()).clickOn("All Cohorts");
    }

    @Test
    void searchFieldFiltersByName(FxRobot robot) {
        robot.clickOn(search()).write("coo");  // matches Cory Cooper only
        assertEquals(1, table().getItems().size());
    }

    @Test
    void presetRiskAndCohortAreApplied() throws Exception {
        AllStudentsView.setPresetRiskLevel("Meets Expectations");
        AllStudentsView.setPresetCohortFilter("2025");

        // reload view to simulate navigation from landing page
        Parent root = new FXMLLoader(
                getClass().getResource("allstudents.fxml")).load();
        stage.getScene().setRoot(root);

        assertEquals("Meets Expectations", riskBox().getValue());
        assertEquals("2025",               cohortBox().getValue());
    }

    @Test
    void exportWithoutSelectionShowsAlert(FxRobot robot) {
        try (MockedConstruction<Alert> alerts =
                 mockConstruction(Alert.class,
                     (a,ctx) -> when(a.showAndWait())
                                .thenReturn(Optional.of(ButtonType.OK)))) {

            robot.clickOn("#exportToExcelButton");
            assertEquals(1, alerts.constructed().size(),
                         "One Alert should have been shown");
            assertEquals(Alert.AlertType.INFORMATION,
                         alerts.constructed().get(0).getAlertType());
        }
    }

    @Test
    void allButtonsExist(FxRobot robot) {
        assertNotNull(robot.lookup("#exportToExcelButton").queryButton());
        assertNotNull(robot.lookup("#cohortComboBox").queryComboBox());
//        assertNotNull(robot.lookup("#risk
    }
}
