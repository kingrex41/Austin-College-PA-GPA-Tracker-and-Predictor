package pagpa.cs380;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import pagpa.cs380.Utils.Registrar;
import pagpa.cs380.Views.AllStudentsView;

import java.util.Map;

public class PrimaryController extends PaController implements Initializable {
	
//	AllStudentsView asv = new AllStudentsView();
	
	/**
	 * A registrar instance manages our session data and provides
	 * critical computational methods needed by our app controllers.
	 */
	Registrar registrar;
    
    @FXML
    private Label currentDateLabel;
    
    @FXML
    private Label currentTermLabel;
    
    @FXML
    private void switchToCurrentCohort() throws IOException {
        System.out.println("DEBUG: Switching to Current Cohort view");
        System.out.println("DEBUG: Setting preset cohort filter to 2026");
        AllStudentsView.setPresetCohortFilter("2026");
        System.out.println("DEBUG: About to switch to allstudents scene");
        App.switchScene("allstudents");
    }
    
    @FXML
	private PieChart pieChart; // Landing page pie chart

    @FXML
    private Button gpaButton; // Average GPA button

    /**
     * This function initializes the categories and corresponding values for the landing page pie chart.
     * This function can be used in the future for initialization of other objects when needed
     */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		registrar = Registrar.getCurrent();
		
		// Set current date and term
        updateDateAndTermLabels();
        
        try {
            // Get the actual risk level distribution from CSV data
            Map<String, Integer> riskDistribution = registrar.calculateRiskLevelDistribution();
            
            // Create pie chart data based on actual distribution
            PieChart.Data meetsExpectations = new PieChart.Data("Not at Risk", 
                riskDistribution.getOrDefault("Meets Expectations", 0));
            
            PieChart.Data closeMonitoring = new PieChart.Data("Close Monitoring", 
                riskDistribution.getOrDefault("Close Monitoring", 0));
            
            PieChart.Data academicProbation = new PieChart.Data("Academic Probation", 
                riskDistribution.getOrDefault("Academic Probation", 0));
            
            PieChart.Data dismissed = new PieChart.Data("Dismissed", 
                    riskDistribution.getOrDefault("Dismissed", 0));
            
            pieChart.getData().addAll(meetsExpectations, closeMonitoring, academicProbation, dismissed);
            
            // Remove the legend
            pieChart.setLegendVisible(false);
            
            // Set colors for each segment
            meetsExpectations.getNode().setStyle("-fx-pie-color:rgb(0, 116, 0);"); // Green for good standing
            closeMonitoring.getNode().setStyle("-fx-pie-color:rgb(232, 170, 0);"); // Yellow for at risk
            academicProbation.getNode().setStyle("-fx-pie-color:rgb(200, 0, 0);"); // Red for on probation
            dismissed.getNode().setStyle("-fx-pie-color:rgb(104, 104, 104);"); // Gray for dismissed
            
            // This loop displays the value of each slice of the pie chart next to the title of the slice
            for (PieChart.Data pieData : pieChart.getData()) {
                pieData.nameProperty().bind(
                        Bindings.concat(
                                pieData.getName()," (",pieData.pieValueProperty().asString("%.0f"), ")"
                        )
                );
            }
            
            // Allows user to click on a slice
            meetsExpectations.getNode().setOnMouseClicked(event -> sliceClick("Not at Risk", event));
            closeMonitoring.getNode().setOnMouseClicked(event -> sliceClick("At Risk", event));
            academicProbation.getNode().setOnMouseClicked(event -> sliceClick("On Probation", event));
            dismissed.getNode().setOnMouseClicked(event -> sliceClick("Dismissed", event));
            
            // Hover effect when hovering over a slice, slice will change color
            meetsExpectations.getNode().setOnMouseEntered(event -> meetsExpectations.getNode().setStyle("-fx-pie-color:rgb(0, 170, 0);"));
            meetsExpectations.getNode().setOnMouseExited(event -> meetsExpectations.getNode().setStyle("-fx-pie-color:rgb(0, 116, 0);"));
            
            closeMonitoring.getNode().setOnMouseEntered(event -> closeMonitoring.getNode().setStyle("-fx-pie-color:rgb(255, 200, 0);"));
            closeMonitoring.getNode().setOnMouseExited(event -> closeMonitoring.getNode().setStyle("-fx-pie-color:rgb(232, 170, 0);"));
            
            academicProbation.getNode().setOnMouseEntered(event -> academicProbation.getNode().setStyle("-fx-pie-color:rgb(255, 0, 0);"));
            academicProbation.getNode().setOnMouseExited(event -> academicProbation.getNode().setStyle("-fx-pie-color:rgb(200, 0, 0);"));

            dismissed.getNode().setOnMouseEntered(event -> dismissed.getNode().setStyle("-fx-pie-color:rgb(119, 119, 119);"));
            dismissed.getNode().setOnMouseExited(event -> dismissed.getNode().setStyle("-fx-pie-color:rgb(104, 104, 104);"));

            // Update GPA button with actual average GPA
            if (gpaButton != null) {
                double averageGPA = registrar.calculateAverageGPA();
                gpaButton.setText(String.format("Average GPA: %.2f", averageGPA));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error initializing PrimaryController: " + e.getMessage());
        }
	}
	
    private void updateDateAndTermLabels() {
        // Get current date
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
        String formattedDate = today.format(formatter);
        
        // Get current term from Registrar
        String currentTerm = registrar.getCurrentTerm();
        
        // Set the labels
        currentDateLabel.setText("Current Date: " + formattedDate);
        currentTermLabel.setText("Current Term: " + currentTerm);
    }

	/**
	 * This function switches the scene to allstudents.fxml and sets the filter to dispaly on the students
	 * that are in the slice of the pieChart that was clicked
	 * 
	 * @param sliceName
	 * @param event
	 */
	private void sliceClick(String sliceName, MouseEvent event) {
		try {
			switch(sliceName) {
				case "Not at Risk":
	                System.out.println("Switching to allstudents: Not at Risk");
	                AllStudentsView.setPresetRiskLevel("Meets Expectations");
	                App.switchScene("allstudents");
					break;
				case "At Risk":
					System.out.println("Switching to allstudents: Close Monitoring");
					AllStudentsView.setPresetRiskLevel("Close Monitoring");
					App.switchScene("allstudents");
					break;
				case "On Probation":
					System.out.println("Switching to allstudents: Close Monitoring");
					AllStudentsView.setPresetRiskLevel("Academic Probation");
					App.switchScene("allstudents");
					break;					
				case "Dismissed":
					System.out.println("Switching to allstudents: Dismissed");
					AllStudentsView.setPresetRiskLevel("Dismissed");
					App.switchScene("allstudents");
					break;
			}
		} catch (IOException e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
	
	
	
	/**
	 * These two functions are the switch methods, they move you to All students or secondary when clicked
	 * @throws IOException
	 */
	
    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }
    
    @FXML
    private void switchToAllStudents() throws IOException {
        System.out.println("Switching to All Students view");
        App.switchScene("allstudents");
    }

	@Override
	public void finishInit() {
		// TODO put any additional GUI initialization here 
		System.err.println("finishing initialization on page");
		
	}
	
	/**
	 * This method is a universal method that creates an alert if an unimplemented button is clicked
	 * This is so user/ tester knows a button is meant to be dead and is not dead by mistake
	 * @param event
	 */
	
	@FXML
	private void manageUnimplementedButtonClicks (ActionEvent event) {
		new Alert(Alert.AlertType.INFORMATION,
				"This button does not yet have implementation.")
		.showAndWait();
	}

}
