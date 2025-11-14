package pagpa.cs380;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import pagpa.cs380.Models.Course;
import pagpa.cs380.Models.Criterion;
import pagpa.cs380.Models.Grade;
import pagpa.cs380.Models.Student;
import pagpa.cs380.Models.StudentGrade;
import pagpa.cs380.Utils.Registrar;
import pagpa.cs380.Utils.SuccessManager;
import pagpa.cs380.Views.CourseView;

public class StudentdashController extends PaController {
	
	private Stage stage;
	private Scene scene;
	private Parent root;
	
	private Student theStudent; //handle on student class
	private Registrar registrar = Registrar.getCurrent(); // Get Registrar instance
	private boolean showWeeklyView = false; // Default to semester view

	@FXML
	private Label studentName; //will display the students' name
	
	@FXML
	private Label liveGPA; // will display the students' live GPA
	
	@FXML
	private Label cumGPA; // display the students cumulative GPA
	
	@FXML
	private Label termGPA; // display the student's term GPA
	
	@FXML
	private Label standing;     // displays the current student's risk (aka successStanding)
	
	@FXML
	private VBox standingPane;  // holds the criteria for the current students success/risk level
	
	@FXML
	private TableView<Course> gradeTable;
	
	@FXML
	private TableColumn<Course, Course> gradeColumn;
    
    @FXML
    private TableColumn<Course, String> courseNameColumn;
    
    @FXML
    private LineChart<String, Number> gpaChart; // Chart for GPA over time
    
    @FXML
    private CategoryAxis xAxis; // X-axis for terms
    
    @FXML
    private NumberAxis yAxis; // Y-axis for GPA values
    
    @FXML
    private ToggleButton viewToggleButton; // Toggle between semester and weekly views
    
    private FilteredList<Course> filteredList;
    
    private static Student currViewedStudent; // handle on the current student that is being viewed in the student dashboard
	
	/**
	 * switch to course view from student dash
	 * when the button is clicked
	 * @param e
	 * @throws IOException
	 */
	@FXML
	private void switchToCourseDash() throws IOException {
		try {
			// Load the FXML file
			FXMLLoader loader = new FXMLLoader(App.class.getResource("coursedash.fxml"));
			Parent root = loader.load();
			
			// Get the controller
			CourseView controller = loader.getController();
			
			// Set the student
			CourseView.setCurrStudent(theStudent);
			
			// Refresh the course list for the student
			controller.refreshCourseList();
			
			// Switch to the scene
			App.scene.setRoot(root);
		} catch (Exception e) {
			// Fall back to standard approach if custom fails
			CourseView.setCurrStudent(theStudent);
		App.switchScene("coursedash");
		}
	}
	
	
	/**
	 * From the student dash scene, this function will
	 * allow us to navigate back to the all students view
	 * @throws IOException
	 */
    @FXML
    private void switchBackToAllStudents() throws IOException {
    	System.out.println("Switching BACK to All Students view");
        App.switchScene("allstudents");
    }
	
    /**
     * switch to the GPA forecast scene from the 
     * student dash page
     * @param e
     * @throws IOException
     */
	@FXML
	private void switchToGpaForecast() throws IOException {
		if (theStudent == null) {
			System.err.println("Error: No student selected");
			return;
		}
		App.switchScene("gpaforecast");
		GPAForecastController controller = (GPAForecastController) App.getController();
		if (controller != null) {
			controller.setStudent(theStudent);
		} else {
			System.err.println("Error: Could not get GPA Forecast controller");
		}
	}

    /**
     * Toggle between semester view and weekly view
     * @param e
     */
    @FXML
    private void toggleGpaView(ActionEvent e) {
        System.out.println("Toggle button clicked. Current state: " + showWeeklyView);
        showWeeklyView = viewToggleButton.isSelected();
        System.out.println("New state: " + showWeeklyView);
        
        if (showWeeklyView) {
            viewToggleButton.setText("Show Terms");
        } else {
            viewToggleButton.setText("Show Weeks");
        }
        
        updateGpaChart();
    }

	public void setStudent(Student stu) {
		this.theStudent = stu;
	}

	@Override
	public void finishInit() {
		SuccessManager smgr = new SuccessManager();
		
		// Set student name
		studentName.setText(theStudent.getFirstName() + " " + this.theStudent.getLastName());
		
		// Calculate GPAs using Registrar
		double liveGpaValue = registrar.calcGpaForStudent(theStudent, true);
		double cumGpaValue = registrar.calcGpaForStudent(theStudent, false);
		double summerGpaValue = registrar.calcSummerGpa(theStudent, false);
		
		// Format GPAs to 2 decimal places
		liveGPA.setText("Live Cumulative GPA: " + String.format("%.2f", liveGpaValue));
		cumGPA.setText("Cumulative GPA: " + String.format("%.2f", cumGpaValue));
		termGPA.setText("Term GPA: " + String.format("%.2f", summerGpaValue));
		
		standing.setText(String.valueOf(smgr.studentStanding(theStudent)));
		
		standingPane.getChildren().clear();
		
		// Build a list from the criteria for this student
		List<Criterion<Student>> criteria = smgr.standingCriteria(theStudent);
		List<CheckBox> boxes = new LinkedList<>();
		for (Criterion<Student> cs: criteria) {
			boolean sel = cs.test(theStudent);
			CheckBox cb = new CheckBox(cs.getTitle());
			cb.setSelected(sel);
			boxes.add(cb);
		}
			
		standingPane.getChildren().addAll(boxes);
				
		// Set up the toggle button initial state
		viewToggleButton.setText("Show Weeks");
		viewToggleButton.setSelected(false);
		
		// Update the GPA chart with term data
		updateGpaChart();
		
		
		/**
		 * These commented out methods is code to use to populate the
		 * table in the student dashboard. Need to go back and edit/fix in order
		 * to get the table populated with the course name and grade in each 
		 * course for each student.
		 */
//		courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
//		gradeColumn.setCellValueFactory(new PropertyValueFactory<>("grade"));
//		
//		if (currViewedStudent == null) {
//	        return;
//	    }
//	    
//	    // Get all courses the student is taking based on their grades
//	    List<Course> studentCourses = new ArrayList<>();
//	    Map<String, Course> uniqueCourses = new HashMap<>();
//	    
//	    // Extract unique courses from student's grades
//	    for (StudentGrade grade : currViewedStudent.getGrades()) {
//	        Course course = grade.getCourse();
//	        String courseName = course.getCourseName();
//	        if (!uniqueCourses.containsKey(courseName)) {
//	            uniqueCourses.put(courseName, course);
//	            studentCourses.add(course);
//	        }
//	    }
//	    
//	 // Set up grade column to show the student's grade for each course
//        gradeColumn.setCellValueFactory(param -> new javafx.beans.property.SimpleObjectProperty<>(param.getValue()));
//        gradeColumn.setCellFactory(column -> {
//            return new TableCell<Course, Course>() {
//                @Override
//                protected void updateItem(Course course, boolean empty) {
//                    super.updateItem(course, empty);
//                    
//                    if (empty || course == null || currViewedStudent == null) {
//                        setText(null);
//                        return;
//                    }
//                    
//                    // Find the student's grade for this course
//                    double courseGrade = calculateStudentCourseGrade(course);
//                    if (courseGrade > 0) {
//                        // show letter grade
//                        String letterGrade = registrar.getLetterGrade(courseGrade);
//                        setText(letterGrade);
//                    } else {
//                        setText("No Grade");
//                    }
//                }
//            };
//        });
//	    
//	    // Update table with the student's courses
//	    ObservableList<Course> courseList = FXCollections.observableArrayList(studentCourses);
//	    filteredList = new FilteredList<>(courseList, s -> true);
//	    gradeTable.setItems(filteredList);
	}
	
//	private double calculateStudentCourseGrade(Course course) {
//	    if (currViewedStudent == null) {
//	        return 0.0;
//	    }
//	    
//	    List<StudentGrade> grades = currViewedStudent.getGrades();
//	    if (grades == null || grades.isEmpty()) {
//	        return 0.0;
//	    }
//	    
//	    // Create a list of grades for this specific course
//	    List<StudentGrade> courseGrades = new ArrayList<>();
//	    for (StudentGrade grade : grades) {
//	        if (grade.getCourse().getCourseNum().equals(course.getCourseNum())) {
//	            courseGrades.add(grade);
//	        }
//	    }
//	    
//	    // If no grades for this course, return 0
//	    if (courseGrades.isEmpty()) {
//	        return 0.0;
//	    }
//	    
//	    // Calculate weighted average grade
//	    return registrar.getCourseNumGrade(courseGrades);
//	}
	
	/**
	 * Updates the GPA chart based on the selected view (semester or weekly)
	 */
	private void updateGpaChart() {
	    // Clear any existing data
	    gpaChart.getData().clear();
	    
	    // Set axis labels
	    yAxis.setLabel("GPA");
	    yAxis.setAutoRanging(false);
	    yAxis.setLowerBound(0.0);
	    yAxis.setUpperBound(4.0);
	    yAxis.setTickUnit(0.5);
	    
	    if (showWeeklyView) {
	        updateWeeklyGpaView();
	    } else {
	        updateSemesterGpaView();
	    }
	}
	
	/**
	 * Updates the chart to show semester GPA progression
	 */
	private void updateSemesterGpaView() {
	    // data series for the student's GPA progression
	    XYChart.Series<String, Number> series = new XYChart.Series<>();
	    series.setName(theStudent.getFirstName() + "'s GPA");
	    
	    // Get GPA values for each term
	    double summerGpa = registrar.calcSummerGpa(theStudent, false);
	    double fallGpa = registrar.calcFallGpa(theStudent, false);
	    double springGpa = registrar.calcSpringGpa(theStudent, false);
	    
	    // Add data points 
	    series.getData().add(new XYChart.Data<>("Summer 2024", summerGpa));
	    series.getData().add(new XYChart.Data<>("Fall 2024", fallGpa));
	    series.getData().add(new XYChart.Data<>("Spring 2025", springGpa));
	    
	    // plot the data
	    gpaChart.getData().add(series);
	    
	    // Add style class
	    series.getNode().getStyleClass().add("series-gpa");
	}
	
	/**
	 * Updates the chart to show weekly GPA progression
	 */
	private void updateWeeklyGpaView() {
	    System.out.println("Entering updateWeeklyGpaView method");
	    // Clear any existing data
	    gpaChart.getData().clear();
	    
	    // Create a new series
	    XYChart.Series<String, Number> series = new XYChart.Series<>();
	    series.setName(theStudent.getFirstName() + "'s Cumulative GPA Through Week");
	    
	    // Get the GPA values for each week
	    List<Double> weeklyGpas = registrar.getWeeklyGpas(theStudent, false);
	    System.out.println("Fetched weekly GPAs: " + weeklyGpas);
	    
	    // Get mapping of weeks to terms
	    Map<Integer, String> weekToTermMap = registrar.getWeekToTermMapping();
	    
	    // Create formatted week labels for x-axis
	    ArrayList<String> categoryLabels = new ArrayList<>();
	    
	    // Add data points for all weeks
	    for (int i = 0; i < weeklyGpas.size(); i++) {
	        int weekNum = i + 1;
	        Double gpa = weeklyGpas.get(i);
	        
	        // Create a formatted label for this week
	        String weekLabel;
	        if (weekNum == 1) {
	            weekLabel = "W1\nSummer";
	        } else if (weekNum == 5) {
	            weekLabel = "W5\nFall";
	        } else if (weekNum == 9) {
	            weekLabel = "W9\nSpring";
	        } else {
	            weekLabel = "W" + weekNum;
	        }
	        
	        // Add to our labels list
	        categoryLabels.add(weekLabel);
	        
	        // Add data point to series
	        XYChart.Data<String, Number> dataPoint = new XYChart.Data<>(weekLabel, gpa);
	        series.getData().add(dataPoint);
	        System.out.println("Added data point: Week " + weekNum + ", GPA: " + gpa);
	    }
	    
	    // Set categories for x-axis
	    xAxis.getCategories().setAll(categoryLabels);
	    
	    // Add the series to the chart
	    gpaChart.getData().add(series);
	    System.out.println("Added series to chart with " + series.getData().size() + " data points");
	    
	    // Style the series
	    if (series.getNode() != null) {
	        series.getNode().getStyleClass().add("series-gpa");
	    } else {
	        System.out.println("Series node is null, couldn't apply style");
	    }
	    
	    // Apply tooltips to all data points
	    for (int i = 0; i < series.getData().size(); i++) {
	        final int weekNum = i + 1;
	        final XYChart.Data<String, Number> data = series.getData().get(i);
	        final double gpaValue = weeklyGpas.get(i);
	        
	        // Add tooltips after nodes are created using Platform.runLater
	        Platform.runLater(() -> {
	            if (data.getNode() != null) {
	                Tooltip tooltip = new Tooltip(
	                    String.format("Cumulative GPA through Week %d: %.2f", weekNum, gpaValue)
	                );
	                Tooltip.install(data.getNode(), tooltip);
	                System.out.println("Installed tooltip for week " + weekNum);
	            } else {
	                System.out.println("Data node is null for week " + weekNum + ", couldn't install tooltip");
	            }
	        });
	    }
	}
	
    @FXML
    private void switchToMainMenu(ActionEvent event) throws IOException {
        App.switchScene("primary");
    }
    
	@FXML
	private void manageUnimplementedButtonClicks (ActionEvent event) {
		new Alert(Alert.AlertType.INFORMATION,
				"This button does not yet have implementation.")
		.showAndWait();
	}

	
//	@FXML
//	public void switchToCurrentStat(ActionEvent e) throws IOException {
//		App.switchScene("currentstatus");
//	}
	
}
