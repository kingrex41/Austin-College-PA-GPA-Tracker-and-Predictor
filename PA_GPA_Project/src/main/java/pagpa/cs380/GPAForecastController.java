package pagpa.cs380;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.util.StringConverter;
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
import java.util.Set;
import java.util.stream.Collectors;

public class GPAForecastController {
    @FXML private Label currentGPAText;
    @FXML private Label forecastGPAText;
    @FXML private Label currentTermGPAText;
    @FXML private Label forecastTermGPAText;
    @FXML private Label termLabel;
    @FXML private TableView<CourseGradeEntry> selectedCoursesTable;
    @FXML private TableColumn<CourseGradeEntry, String> courseCodeColumn;
    @FXML private TableColumn<CourseGradeEntry, String> courseNameColumn;
    @FXML private TableColumn<CourseGradeEntry, Double> creditsColumn;
    @FXML private TableColumn<CourseGradeEntry, String> expectedGradeColumn;
    @FXML private TableColumn<CourseGradeEntry, String> statusColumn;
    @FXML private Label studentNameLabel;

    private Student currentStudent;
    private ObservableList<CourseGradeEntry> coursesWithGrades = FXCollections.observableArrayList();
    private Map<Course, List<StudentGrade>> forecastGrades = new HashMap<>();
    private Registrar registrar = Registrar.getCurrent();
    private String currentTerm;
    
    // Class to represent a course with a grade in the table
    public static class CourseGradeEntry {
        private Course course;
        private SimpleStringProperty gradeProperty;
        private SimpleStringProperty statusProperty;
        private boolean isComplete = false;
        
        public CourseGradeEntry(Course course, String grade, boolean isComplete) {
            this.course = course;
            this.gradeProperty = new SimpleStringProperty(grade);
            this.isComplete = isComplete;
            this.statusProperty = new SimpleStringProperty(isComplete ? "Completed" : "In Progress");
        }
        
        public Course getCourse() {
            return course;
        }
        
        public String getCourseNum() {
            return course.getCourseNum();
        }
        
        public String getCourseName() {
            return course.getCourseName();
        }
        
        public double getCourseCredits() {
            return course.getCourseCredits();
        }
        
        public String getExpectedGrade() {
            return gradeProperty.get();
        }
        
        public void setExpectedGrade(String grade) {
            if (!isComplete) { // Only allow changing grade if course is not complete
                gradeProperty.set(grade);
            }
        }
        
        public String getStatus() {
            return statusProperty.get();
        }
        
        public SimpleStringProperty expectedGradeProperty() {
            return gradeProperty;
        }
        
        public SimpleStringProperty statusProperty() {
            return statusProperty;
        }
        
        public boolean isComplete() {
            return isComplete;
        }
    }

    @FXML
    public void initialize() {
        // Initialize table columns
        courseCodeColumn.setCellValueFactory(new PropertyValueFactory<>("courseNum"));
        courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        creditsColumn.setCellValueFactory(new PropertyValueFactory<>("courseCredits"));
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        
        // Setup editable grade column from user
        expectedGradeColumn.setCellValueFactory(cellData -> cellData.getValue().expectedGradeProperty());
        expectedGradeColumn.setCellFactory(column -> new ComboBoxTableCell<CourseGradeEntry, String>(
            "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "D-", "F"
        ) {
            @Override
            public void startEdit() {
                CourseGradeEntry entry = getTableRow().getItem();
                if (entry == null || entry.isComplete()) {
                    // Don't allow editing for completed courses
                    return;
                }
                super.startEdit();
            }
        });
        
        // Handle grade change events
        expectedGradeColumn.setOnEditCommit(event -> {
            CourseGradeEntry entry = event.getRowValue();
            if (!entry.isComplete()) {
                entry.setExpectedGrade(event.getNewValue());
                updateForecastGrades();
                handleCalculate();
            }
        });
        
        // Make the grade column editable
        selectedCoursesTable.setEditable(true);
        
        // Setup row click event for course names
        setupCourseRowClickEvent();
        
        // Bind table to data
        selectedCoursesTable.setItems(coursesWithGrades);
        
        // Get current term
        currentTerm = registrar.getCurrentTerm();
        
        // Validate current term
        validateCurrentTerm();
    }

    public void setStudent(Student student) {
        this.currentStudent = student;
        if (student != null) {
            studentNameLabel.setText(student.getFirstName() + " " + student.getLastName());
            termLabel.setText("Current Term: " + currentTerm);
        updateCurrentGPA();
            loadCurrentCourses();
        }
    }

    private void updateCurrentGPA() {
        if (currentStudent != null) {
            // Calculate current cumulative GPA (without forecasts)
            double cumulativeGPA = registrar.calcGpaForStudent(currentStudent, false);
            currentGPAText.setText(String.format("Current Cumulative GPA: %.3f", cumulativeGPA));
            
            // Calculate current term GPA (without forecasts)
            double termGPA = registrar.calcTermGpa(currentStudent, currentTerm, false);
            currentTermGPAText.setText(String.format("Current %s GPA: %.3f", currentTerm, termGPA));
        }
    }

    private void loadCurrentCourses() {
        // Clear existing data
        coursesWithGrades.clear();
        forecastGrades.clear();
        
        // Get all courses for the current term
        Map<String, Course> courseMap = new HashMap<>();
        for (Course course : registrar.getCourses()) {
            if (course.getTerm().equals(currentTerm)) {
                courseMap.put(course.getCourseNum(), course);
            }
        }
        
        // Check which courses the student already has grades for
        Map<String, List<StudentGrade>> studentCourseGrades = new HashMap<>();
        for (StudentGrade grade : currentStudent.getGrades()) {
            Course course = grade.getCourse();
            if (course.getTerm().equals(currentTerm)) {
                String courseNum = course.getCourseNum();
                if (!studentCourseGrades.containsKey(courseNum)) {
                    studentCourseGrades.put(courseNum, new ArrayList<>());
                }
                studentCourseGrades.get(courseNum).add(grade);
            }
        }
        
        // Add entries for each course
        for (Course course : courseMap.values()) {
            List<StudentGrade> courseGrades = studentCourseGrades.getOrDefault(course.getCourseNum(), new ArrayList<>());
            
            // Check if course is complete (all assignments have grades)
            boolean isComplete = isCourseComplete(course, courseGrades);
            
            // Calculate current letter grade for the course
            String letterGrade = calculateCurrentLetterGrade(course, courseGrades);
            
            // Add entry to table
            CourseGradeEntry entry = new CourseGradeEntry(course, letterGrade, isComplete);
            coursesWithGrades.add(entry);
            
            // Create a copy of student grades for this course
            forecastGrades.put(course, new ArrayList<>(courseGrades));
        }
    }
    
    protected boolean isCourseComplete(Course course, List<StudentGrade> grades) {
        // Current term courses are always in-progress
        if (isCurrentTerm(course.getTerm())) {
            return false;
        }
        
        // Get all assignments for this course
        List<Assignment> assignments = course.getAssignments();
        
        // Create a set of assignment IDs that have earned grades
        Set<String> gradedAssignments = grades.stream()
            .filter(g -> g.isEarned())
            .map(g -> g.getAssignment().getAssignmentId())
            .collect(Collectors.toSet());
        
        // Check if all assignments have grades
        for (Assignment assignment : assignments) {
            if (!gradedAssignments.contains(assignment.getAssignmentId())) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Checks if the given term is the current term
     * @param term The term to check
     * @return True if the term is the current term
     */
    private boolean isCurrentTerm(String term) {
        if (term == null || currentTerm == null) {
            return false;
        }
        
        // Normalize terms for comparison - strip whitespace, make lowercase
        String normalizedTerm = term.trim().toLowerCase();
        String normalizedCurrentTerm = currentTerm.trim().toLowerCase();
        
        if (normalizedTerm.equals(normalizedCurrentTerm)) {
            return true;
        }
        
        // Handle common variations like "Fall 2024" vs "FA24" or "Fall24"
        if (normalizedCurrentTerm.contains("fall")) {
            return normalizedTerm.contains("fall") || normalizedTerm.contains("fa");
        } else if (normalizedCurrentTerm.contains("spring")) {
            return normalizedTerm.contains("spring") || normalizedTerm.contains("sp");
        } else if (normalizedCurrentTerm.contains("summer")) {
            return normalizedTerm.contains("summer") || normalizedTerm.contains("su");
        }
        
        // Check year match as fallback
        String currentYear = extractYear(currentTerm);
        String termYear = extractYear(term);
        return currentYear != null && currentYear.equals(termYear);
    }
    
    private String extractYear(String term) {
        if (term == null) {
            return null;
        }
        
        // Look for 4-digit or 2-digit year patterns
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\b(20\\d{2}|\\d{2})\\b");
        java.util.regex.Matcher matcher = pattern.matcher(term);
        
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return null;
    }
    
    private String calculateCurrentLetterGrade(Course course, List<StudentGrade> grades) {
        // If the student has no grades for this course yet, use their average 
        // performance from other courses as a reasonable prediction
        if (grades.isEmpty()) {
            double avgGrade = calculateStudentAverageGrade();
            return registrar.getLetterGrade(avgGrade);
        }
        
        // Calculate numeric grade based on existing grades
        double numericGrade = registrar.getCourseNumGrade(grades);
        
        // Convert to letter grade
        return registrar.getLetterGrade(numericGrade);
    }
    
    /**
     * Calculates the student's average numeric grade across all courses
     * to use as a more personalized default.
     * @return The average numeric grade, or 85.0 (B) if no grades exist
     */
    private double calculateStudentAverageGrade() {
        if (currentStudent == null || currentStudent.getGrades().isEmpty()) {
            return 85.0; // Default to B if no grades at all
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
            return 85.0; // Default to B if no earned grades
        }
        
        return totalGrade / gradeCount;
    }

    private void updateForecastGrades() {
        // Update the forecast grades based on the UI selections
        for (CourseGradeEntry entry : coursesWithGrades) {
            // Skip completed courses
            if (entry.isComplete()) {
                continue;
            }
            
            Course course = entry.getCourse();
            List<StudentGrade> grades = forecastGrades.getOrDefault(course, new ArrayList<>());
            
            // Clean up any previous forecast grades for this course
            grades.removeIf(grade -> grade.isForecast());
            
            // Create forecast grades for remaining assignments
            double numericGrade = convertLetterGradeToNumeric(entry.getExpectedGrade());
            
            // Create a set of assignment IDs that already have earned grades
            Set<String> gradedAssignments = grades.stream()
                .filter(g -> g.isEarned())
                .map(g -> g.getAssignment().getAssignmentId())
                .collect(Collectors.toSet());
            
            // Add forecast grades for all ungraded assignments
            boolean hasUngraded = false;
            for (Assignment assignment : course.getAssignments()) {
                // Check if student already has an earned grade for this assignment
                if (!gradedAssignments.contains(assignment.getAssignmentId())) {
                    // Create a new forecast grade
                    StudentGrade forecastGrade = new StudentGrade(currentStudent, course, assignment);
                    forecastGrade.setGrade(numericGrade);
                    forecastGrade.setGradeDate(LocalDate.now());
                    forecastGrade.setGradeStatus(StudentGrade.GRADE_FORECAST);
                    
                    grades.add(forecastGrade);
                    hasUngraded = true;
                }
            }
            
            // Log warning if there are no ungraded assignments but course is marked in-progress
            if (!hasUngraded && !grades.isEmpty()) {
                System.err.println("Warning: Course " + course.getCourseNum() + 
                    " is marked as in-progress but has all assignments graded.");
            }
            
            forecastGrades.put(course, grades);
        }
    }

    private double convertLetterGradeToNumeric(String letterGrade) {
        // Validate input
        if (letterGrade == null || letterGrade.trim().isEmpty()) {
            System.err.println("Warning: Empty letter grade provided, defaulting to B");
            return 85.0; // Default to B for null or empty input
        }
        
        // Normalize input by trimming and converting to uppercase
        String normalizedGrade = letterGrade.trim().toUpperCase();
        
        // Convert letter grade to numeric grade (0-100 scale)
        switch (normalizedGrade) {
            case "A": return 95.0;
            case "A-": return 91.0;
            case "B+": return 88.0;
            case "B": return 85.0;
            case "B-": return 81.0;
            case "C+": return 78.0;
            case "C": return 75.0;
            case "C-": return 71.0;
            case "D+": return 68.0;
            case "D": return 65.0;
            case "D-": return 61.0;
            case "F": return 55.0;
            default:
                // If an invalid grade is provided, log a warning and default to B
                System.err.println("Warning: Unrecognized letter grade '" + letterGrade + "', defaulting to B");
                return 85.0; // Default to B for unrecognized input
        }
    }

    @FXML
    private void handleBackButton() {
        try {
            App.switchScene("studentdash2");
            StudentdashController controller = (StudentdashController) App.getController();
            if (controller != null) {
                controller.setStudent(currentStudent);
                controller.finishInit();
            } else {
                System.err.println("Error: Could not get Student Dashboard controller");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        
        // Add all the student's original grades
        for (StudentGrade grade : currentStudent.getGrades()) {
            studentWithForecasts.getGrades().add(grade);
        }
        
        // Add all the forecast grades
        for (List<StudentGrade> grades : forecastGrades.values()) {
            for (StudentGrade grade : grades) {
                if (grade.isForecast()) {
                    studentWithForecasts.getGrades().add(grade);
                }
            }
        }
        
        // Calculate forecast GPAs
        double forecastGPA = registrar.calcGpaForStudent(studentWithForecasts, true);
        forecastGPAText.setText(String.format("Forecast Cumulative GPA: %.3f", forecastGPA));
        
        double forecastTermGPA = registrar.calcTermGpa(studentWithForecasts, currentTerm, true);
        forecastTermGPAText.setText(String.format("Forecast %s GPA: %.3f", currentTerm, forecastTermGPA));
    }

    @FXML
    private void handleClear() {
        // Reset grades to B for in-progress courses only
        for (CourseGradeEntry entry : coursesWithGrades) {
            if (!entry.isComplete()) {
                entry.setExpectedGrade("B");
            }
        }
        
        // Clear all forecast data completely
        cleanupAllForecastGrades();
        
        // Reset forecast GPA display
        forecastGPAText.setText("Forecast Cumulative GPA: --");
        forecastTermGPAText.setText("Forecast Term GPA: --");
    }
    
    /**
     * Removes all forecast grades from both the UI model and the student's grade list
     * to ensure there are no lingering effects from previous forecasts.
     */
    private void cleanupAllForecastGrades() {
        // Clear forecast data from our internal map
        for (Course course : forecastGrades.keySet()) {
            List<StudentGrade> grades = forecastGrades.get(course);
            if (grades != null) {
                grades.removeIf(grade -> grade.isForecast());
            }
        }
        
        // Also clean up any forecast grades that might be in the student's grade list
        // This ensures we don't have lingering forecast grades affecting calculations
        if (currentStudent != null) {
            currentStudent.getGrades().removeIf(grade -> grade.isForecast());
        }
    }

    /**
     * Validates the current term and warns if there are potential issues.
     * This helps identify configuration problems early.
     */
    private void validateCurrentTerm() {
        if (currentTerm == null || currentTerm.trim().isEmpty()) {
            System.err.println("ERROR: Current term is not set. GPA forecasting may not work correctly.");
            // Set a default term to prevent null pointer exceptions
            currentTerm = "Fall 2024";
        }
        
        // Check if there are courses for the current term
        boolean hasCurrentTermCourses = registrar.getCourses().stream()
            .anyMatch(course -> isCurrentTerm(course.getTerm()));
            
        if (!hasCurrentTermCourses) {
            System.err.println("WARNING: No courses found for the current term '" + currentTerm + 
                "'. GPA forecasting may not work as expected.");
        }
        
        // Log the term we're using
        System.out.println("Using current term: " + currentTerm);
    }

    /**
     * Sets up the click event for course names to navigate to assignment forecast page
     */
    private void setupCourseRowClickEvent() {
        // Set row factory with mouse click handler
        selectedCoursesTable.setRowFactory(tv -> {
            TableRow<CourseGradeEntry> row = new TableRow<>();
            
            // Add hover style to indicate clickable
            row.setOnMouseEntered(event -> {
                if (row.getItem() != null) {
                    row.getStyleClass().add("clickable-row");
                }
            });
            
            row.setOnMouseExited(event -> {
                row.getStyleClass().remove("clickable-row");
            });
            
            // Handle double click on row
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    CourseGradeEntry entry = row.getItem();
                    openAssignmentForecastPage(entry.getCourse());
                }
            });
            
            return row;
        });
    }
    
    /**
     * Opens the assignment forecast page for a specific course
     */
    private void openAssignmentForecastPage(Course course) {
        try {
            App.switchScene("assignmentforecast");
            AssignmentForecastController controller = (AssignmentForecastController) App.getController();
            if (controller != null) {
                controller.setStudentAndCourse(currentStudent, course);
            } else {
                System.err.println("Error: Could not get Assignment Forecast controller");
            }
        } catch (IOException e) {
            e.printStackTrace();
            
            // Show error dialog
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Failed to open assignment forecast page");
            alert.setContentText("An error occurred: " + e.getMessage());
            alert.showAndWait();
        }
    }
} 