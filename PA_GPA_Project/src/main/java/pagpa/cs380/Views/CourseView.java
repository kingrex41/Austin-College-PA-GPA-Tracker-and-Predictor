/**
  * The purpose of this class is to show the courses
  * that each student is taking in each semester. It
  * will be able to be accessed through the student dashboard.
  * The table will display the course name, professor, course credit,
  * and the student's grade.
  */

package pagpa.cs380.Views;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import pagpa.cs380.App;
import pagpa.cs380.StudentdashController;
import pagpa.cs380.Models.Course;
import pagpa.cs380.Models.Student;
import pagpa.cs380.Models.StudentGrade;
import pagpa.cs380.Utils.Registrar;

public class CourseView implements Initializable {
	
	private Registrar registrar = Registrar.getCurrent();

    @FXML
    private TableView<Course> courseTable;
    
    @FXML
    private TableColumn<Course, String> termColumn;
    
    @FXML
    private TableColumn<Course, String> courseNumColumn;
    
    @FXML
    private TableColumn<Course, String> courseNameColumn;
    
    @FXML
    private TableColumn<Course, String> profColumn;
    
    @FXML
    private TableColumn<Course, Double> creditColumn;
    
    @FXML 
    private TableColumn<Course, Course> gradeColumn; 
    
    @FXML 
    private TextField searchCourse;
    
    @FXML 
    private ComboBox<String> semesterComboBox; 
    
    private FilteredList<Course> filteredList;
    
    private static Student currViewedStudent; // handle on the current student that is being viewed in the student dashboard

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		/*
		 * setting up the course view table
		 */
		termColumn.setCellValueFactory(new PropertyValueFactory<>("term"));
		courseNumColumn.setCellValueFactory(new PropertyValueFactory<>("courseNum"));
		courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        profColumn.setCellValueFactory(new PropertyValueFactory<>("instructor"));
        creditColumn.setCellValueFactory(new PropertyValueFactory<>("courseCredits"));
        
        // Set up grade column to show the student's grade for each course
        gradeColumn.setCellValueFactory(param -> new javafx.beans.property.SimpleObjectProperty<>(param.getValue()));
        gradeColumn.setCellFactory(column -> {
            return new TableCell<Course, Course>() {
                @Override
                protected void updateItem(Course course, boolean empty) {
                    super.updateItem(course, empty);
                    
                    if (empty || course == null || currViewedStudent == null) {
                        setText(null);
                        return;
                    }
                    
                    // Find the student's grade for this course
                    double courseGrade = calculateStudentCourseGrade(course);
                    if (courseGrade > 0) {
                        // show letter grade
                        String letterGrade = registrar.getLetterGrade(courseGrade);
                        setText(letterGrade);
                    } else {
                        setText("No Grade");
                    }
                }
            };
        });
        
        // Set up the combo box for the semester
        semesterComboBox.getItems().addAll("All", "Summer 2024", "Fall 2024", "Spring 2025");
        
        // Initialize with default value
        semesterComboBox.setValue("All");
        
        // Add listeners
        searchCourse.textProperty().addListener((obs, oldVal, newVal) -> updateFilters());
        semesterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateFilters());
	}
	
	/**
	 * Calculate a student's numeric grade for a specific course
	 * @param course The course to calculate the grade for
	 * @return The calculated numeric grade or 0 if no grade
	 */
	private double calculateStudentCourseGrade(Course course) {
	    if (currViewedStudent == null) {
	        return 0.0;
	    }
	    
	    List<StudentGrade> grades = currViewedStudent.getGrades();
	    if (grades == null || grades.isEmpty()) {
	        return 0.0;
	    }
	    
	    // Create a list of grades for this specific course
	    List<StudentGrade> courseGrades = new ArrayList<>();
	    for (StudentGrade grade : grades) {
	        if (grade.getCourse().getCourseNum().equals(course.getCourseNum())) {
	            courseGrades.add(grade);
	        }
	    }
	    
	    // If no grades for this course, return 0
	    if (courseGrades.isEmpty()) {
	        return 0.0;
	    }
	    
	    // Calculate weighted average grade
	    return registrar.getCourseNumGrade(courseGrades);
	}
	
	/**
	 * This will be called when entering the Course View page
	 * from the Student dashboard to store the student info and update the course list
	 */
	public static void setCurrStudent(Student stu) {
		currViewedStudent = stu;
	}
	
	/**
	 * Refresh the course table with the current student's courses
	 * Called after currViewedStudent is set
	 */
	public void refreshCourseList() {
	    if (currViewedStudent == null) {
	        return;
	    }
	    
	    // Get all courses the student is taking based on their grades
	    List<Course> studentCourses = new ArrayList<>();
	    Map<String, Course> uniqueCourses = new HashMap<>();
	    
	    // Extract unique courses from student's grades
	    for (StudentGrade grade : currViewedStudent.getGrades()) {
	        Course course = grade.getCourse();
	        String courseId = course.getCourseNum();
	        if (!uniqueCourses.containsKey(courseId)) {
	            uniqueCourses.put(courseId, course);
	            studentCourses.add(course);
	        }
	    }
	    
	    // Update table with the student's courses
	    ObservableList<Course> courseList = FXCollections.observableArrayList(studentCourses);
	    filteredList = new FilteredList<>(courseList, s -> true);
	    courseTable.setItems(filteredList);
	    
	    // Apply initial filters
	    updateFilters();
	}
	
	// switch back to the student dashboard
	@FXML
	private void switchToStudentDash() throws IOException {
		try {
            // Load the FXML file directly
            FXMLLoader loader = new FXMLLoader(App.class.getResource("studentdash2.fxml"));
            Parent root = loader.load();
            
            // Get the controller from the loader
            StudentdashController controller = loader.getController();
            
            // Use the stored student reference
            if (currViewedStudent != null) {
                controller.setStudent(currViewedStudent);
                controller.finishInit();
            }
            
            // Set the scene's root to our newly loaded FXML with initialized controller
            App.scene.setRoot(root);
            
        } catch (Exception e) {
            // Fall back to the standard scene switch if our custom approach fails
            App.switchScene("studentdash2");
        }
	}
	
	/*
	 * Update filters based on search text and semester selection
	 */
    private void updateFilters() {
        if (filteredList == null) {
            return;
        }
        
        filteredList.setPredicate(course -> {
        	// gets value of the combo box and the search field to read it
            String term = semesterComboBox.getValue();
            String search = searchCourse.getText();

            // Filter by term
            if (term != null && !term.equals("All") && !term.equals(course.getTerm())) {
            	return false;
            }
            
            // Filter by search text (course number, name, or instructor)
            if (search != null && !search.isEmpty() &&
            	!(course.getCourseName().toLowerCase().contains(search.toLowerCase()) ||
            	  course.getCourseNum().toLowerCase().contains(search.toLowerCase()) ||
            	  course.getInstructor().toLowerCase().contains(search.toLowerCase()))) {
            	return false;
            }
            
            return true;
        });
    }
    
    @FXML
    private void switchToMainMenu(ActionEvent event) throws IOException {
        App.switchScene("primary");
    }
    
    @FXML
    private void switchBackToAllStudents() throws IOException {
    	System.out.println("Switching BACK to All Students view");
        App.switchScene("allstudents");
    }
}
