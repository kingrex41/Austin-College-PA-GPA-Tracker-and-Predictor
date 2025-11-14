package pagpa.cs380;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;
import pagpa.cs380.Models.Student;
import pagpa.cs380.Models.Course;
import pagpa.cs380.Models.Assignment;
import pagpa.cs380.Models.StudentGrade;
import pagpa.cs380.Utils.Registrar;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AssignmentForecastController {
    @FXML private Label studentNameLabel;
    @FXML private Label courseInfoLabel;
    @FXML private Label currentCourseGradeLabel;
    @FXML private Label forecastCourseGradeLabel;
    @FXML private Label forecastTermGPALabel;
    @FXML private Label forecastCumulativeGPALabel;
    @FXML private TableView<AssignmentGradeEntry> assignmentsTable;
    @FXML private TableColumn<AssignmentGradeEntry, String> assignmentNameColumn;
    @FXML private TableColumn<AssignmentGradeEntry, Double> weightColumn;
    @FXML private TableColumn<AssignmentGradeEntry, String> statusColumn;
    @FXML private TableColumn<AssignmentGradeEntry, String> gradeColumn;
    
    private Student currentStudent;
    private Course currentCourse;
    private Registrar registrar = Registrar.getCurrent();
    private ObservableList<AssignmentGradeEntry> assignmentGrades = FXCollections.observableArrayList();
    private Map<Assignment, StudentGrade> originalGrades = new HashMap<>();
    private Map<Assignment, StudentGrade> forecastGrades = new HashMap<>();
    
    // Class to represent an assignment with grade in the table
    public static class AssignmentGradeEntry {
        private Assignment assignment;
        private SimpleStringProperty nameProperty;
        private SimpleDoubleProperty weightProperty;
        private SimpleStringProperty statusProperty;
        private SimpleStringProperty gradeProperty;
        private boolean isCompleted;
        
        public AssignmentGradeEntry(Assignment assignment, double grade, boolean isCompleted) {
            this.assignment = assignment;
            this.nameProperty = new SimpleStringProperty(assignment.getAssignmentId());
            this.weightProperty = new SimpleDoubleProperty(assignment.getWeight());
            this.isCompleted = isCompleted;
            this.statusProperty = new SimpleStringProperty(isCompleted ? "Completed" : "In Progress");
            this.gradeProperty = new SimpleStringProperty(String.format("%.1f", grade));
        }
        
        public Assignment getAssignment() {
            return assignment;
        }
        
        public String getAssignmentName() {
            return nameProperty.get();
        }
        
        public double getWeight() {
            return weightProperty.get();
        }
        
        public String getStatus() {
            return statusProperty.get();
        }
        
        public String getGrade() {
            return gradeProperty.get();
        }
        
        public void setGrade(String grade) {
            if (!isCompleted) {
                gradeProperty.set(grade);
            }
        }
        
        public boolean isCompleted() {
            return isCompleted;
        }
        
        public SimpleStringProperty gradeProperty() {
            return gradeProperty;
        }
    }
    
    @FXML
    public void initialize() {
        // Setup table columns
        assignmentNameColumn.setCellValueFactory(new PropertyValueFactory<>("assignmentName"));
        weightColumn.setCellValueFactory(new PropertyValueFactory<>("weight"));
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty);
        
        // Setup editable grade column
        gradeColumn.setCellValueFactory(cellData -> cellData.getValue().gradeProperty());
        gradeColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        
        // Handle grade change events
        gradeColumn.setOnEditCommit(event -> {
            AssignmentGradeEntry entry = event.getRowValue();
            if (!entry.isCompleted()) {
                try {
                    // Validate that input is a valid number
                    double grade = Double.parseDouble(event.getNewValue());
                    if (grade < 0 || grade > 100) {
                        throw new NumberFormatException("Grade must be between 0 and 100");
                    }
                    entry.setGrade(String.format("%.1f", grade));
                    updateForecastGrade(entry);
                } catch (NumberFormatException e) {
                    // If input is not a valid number, revert to previous value
                    entry.setGrade(event.getOldValue());
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Invalid Grade");
                    alert.setHeaderText("Invalid Grade Value");
                    alert.setContentText("Please enter a valid numeric grade between 0 and 100.");
                    alert.showAndWait();
                }
            }
        });
        
        // Make the table editable
        assignmentsTable.setEditable(true);
        
        // Bind table to data
        assignmentsTable.setItems(assignmentGrades);
    }
    
    /**
     * Sets the student and course for this forecast view
     */
    public void setStudentAndCourse(Student student, Course course) {
        this.currentStudent = student;
        this.currentCourse = course;
        
        if (student != null && course != null) {
            studentNameLabel.setText("Student: " + student.getFirstName() + " " + student.getLastName());
            courseInfoLabel.setText("Course: " + course.getCourseNum() + " - " + course.getCourseName());
            
            // Load assignments for this course
            loadAssignments();
            
            // Update current course grade display
            updateCurrentCourseGrade();
        }
    }
    
    private void loadAssignments() {
        // Clear existing data
        assignmentGrades.clear();
        originalGrades.clear();
        forecastGrades.clear();
        
        if (currentStudent == null || currentCourse == null) {
            return;
        }
        
        // Get all assignments for this course
        List<Assignment> assignments = currentCourse.getAssignments();
        if (assignments.isEmpty()) {
            return;
        }
        
        // Get existing grades for this student and course
        Map<String, StudentGrade> studentGradeMap = new HashMap<>();
        for (StudentGrade grade : currentStudent.getGrades()) {
            if (grade.getCourse().equals(currentCourse) && grade.getAssignment() != null) {
                studentGradeMap.put(grade.getAssignment().getAssignmentId(), grade);
            }
        }
        
        // Add each assignment to the table
        for (Assignment assignment : assignments) {
            StudentGrade grade = studentGradeMap.get(assignment.getAssignmentId());
            boolean isCompleted = (grade != null && grade.isEarned());
            double gradeValue = (grade != null) ? grade.getGrade() : 0.0;
            
            // Add entry to table
            AssignmentGradeEntry entry = new AssignmentGradeEntry(assignment, gradeValue, isCompleted);
            assignmentGrades.add(entry);
            
            // Store original grade for reference
            if (grade != null) {
                originalGrades.put(assignment, grade);
            }
            
            // Initialize forecast grade with either the original earned grade or a default
            if (grade != null && grade.isEarned()) {
                forecastGrades.put(assignment, grade);
            } else {
                // Create a forecast grade with the default shown value
                StudentGrade forecastGrade = new StudentGrade(currentStudent, currentCourse, assignment);
                forecastGrade.setGrade(gradeValue);
                forecastGrade.setGradeDate(LocalDate.now());
                forecastGrade.setGradeStatus(StudentGrade.GRADE_FORECAST);
                forecastGrades.put(assignment, forecastGrade);
            }
        }
    }
    
    private void updateCurrentCourseGrade() {
        // Get all earned grades for this course
        List<StudentGrade> earnedGrades = currentStudent.getGrades().stream()
            .filter(g -> g.getCourse().equals(currentCourse) && g.isEarned())
            .collect(Collectors.toList());
        
        if (earnedGrades.isEmpty()) {
            currentCourseGradeLabel.setText("Current Course Grade: No grades yet");
        } else {
            double numericGrade = registrar.getCourseNumGrade(earnedGrades);
            String letterGrade = registrar.getLetterGrade(numericGrade);
            currentCourseGradeLabel.setText(String.format("Current Course Grade: %.1f (%s)", numericGrade, letterGrade));
        }
    }
    
    private void updateForecastGrade(AssignmentGradeEntry entry) {
        if (entry == null || entry.isCompleted()) {
            return;
        }
        
        try {
            // Parse the numeric grade
            double numericGrade = Double.parseDouble(entry.getGrade());
            
            // Update the forecast grade
            StudentGrade forecastGrade = forecastGrades.get(entry.getAssignment());
            if (forecastGrade == null) {
                forecastGrade = new StudentGrade(currentStudent, currentCourse, entry.getAssignment());
                forecastGrade.setGradeDate(LocalDate.now());
                forecastGrade.setGradeStatus(StudentGrade.GRADE_FORECAST);
                forecastGrades.put(entry.getAssignment(), forecastGrade);
            }
            forecastGrade.setGrade(numericGrade);
            
            // Calculate the new forecast results
            handleCalculate();
        } catch (NumberFormatException e) {
            // Invalid number, ignore the update
        }
    }
    
    @FXML
    private void handleCalculate() {
        // Create a temporary copy of student with forecast grades added
        Student studentWithForecasts = new Student(
            currentStudent.getAcid(),
            currentStudent.getFirstName(),
            currentStudent.getLastName(),
            currentStudent.getAdvisor(),
            currentStudent.getPhone(),
            currentStudent.getCohort(),
            currentStudent.isDismissed()
        );
        
        // Add all the student's original grades except for this course
        for (StudentGrade grade : currentStudent.getGrades()) {
            if (!grade.getCourse().equals(currentCourse)) {
                studentWithForecasts.getGrades().add(grade);
            }
        }
        
        // Add all the forecast and earned grades for this course
        for (StudentGrade grade : forecastGrades.values()) {
            studentWithForecasts.getGrades().add(grade);
        }
        
        // Calculate forecast course grade
        List<StudentGrade> courseGrades = studentWithForecasts.getGrades().stream()
            .filter(g -> g.getCourse().equals(currentCourse))
            .collect(Collectors.toList());
        
        if (!courseGrades.isEmpty()) {
            double numericGrade = registrar.getCourseNumGrade(courseGrades);
            String letterGrade = registrar.getLetterGrade(numericGrade);
            forecastCourseGradeLabel.setText(String.format("Forecast Course Grade: %.1f (%s)", numericGrade, letterGrade));
        } else {
            forecastCourseGradeLabel.setText("Forecast Course Grade: No data");
        }
        
        // Calculate forecast term GPA
        String currentTerm = currentCourse.getTerm();
        double forecastTermGPA = registrar.calcTermGpa(studentWithForecasts, currentTerm, true);
        forecastTermGPALabel.setText(String.format("Forecast %s GPA: %.3f", currentTerm, forecastTermGPA));
        
        // Calculate forecast cumulative GPA
        double forecastCumulativeGPA = registrar.calcGpaForStudent(studentWithForecasts, true);
        forecastCumulativeGPALabel.setText(String.format("Forecast Cumulative GPA: %.3f", forecastCumulativeGPA));
    }
    
    @FXML
    private void handleClear() {
        // Reset grades to default for in-progress assignments
        for (AssignmentGradeEntry entry : assignmentGrades) {
            if (!entry.isCompleted()) {
                // Calculate student's average grade as default
                double averageGrade = calculateStudentAverageGrade();
                entry.setGrade(String.format("%.1f", averageGrade));
                
                // Update the forecast grade
                StudentGrade forecastGrade = forecastGrades.get(entry.getAssignment());
                if (forecastGrade != null) {
                    forecastGrade.setGrade(averageGrade);
                }
            }
        }
        
        // Reset forecast displays
        handleCalculate();
    }
    
    /**
     * Calculates the student's average earned grade for use as a default
     */
    private double calculateStudentAverageGrade() {
        if (currentStudent == null || currentStudent.getGrades().isEmpty()) {
            return 80.0; // Default to B- if no grades
        }
        
        double totalGrade = 0.0;
        int gradeCount = 0;
        
        for (StudentGrade grade : currentStudent.getGrades()) {
            if (grade.isEarned()) {
                totalGrade += grade.getGrade();
                gradeCount++;
            }
        }
        
        if (gradeCount == 0) {
            return 80.0; // Default to B- if no earned grades
        }
        
        return totalGrade / gradeCount;
    }
    
    @FXML
    private void switchToMainMenu() throws IOException {
        App.switchScene("primary");
    }
    
    @FXML
    private void switchToAllStudents() throws IOException {
        App.switchScene("allstudents");
    }
    
    @FXML
    private void switchToStudentDashboard() throws IOException {
        App.switchScene("studentdash2");
        StudentdashController controller = (StudentdashController) App.getController();
        if (controller != null) {
            controller.setStudent(currentStudent);
            controller.finishInit();
        }
    }
    
    @FXML
    private void switchToGPAForecast() throws IOException {
        App.switchScene("gpaforecast");
        GPAForecastController controller = (GPAForecastController) App.getController();
        if (controller != null) {
            controller.setStudent(currentStudent);
        }
    }
} 