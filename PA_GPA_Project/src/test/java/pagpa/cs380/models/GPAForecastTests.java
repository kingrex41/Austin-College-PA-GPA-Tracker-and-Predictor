package pagpa.cs380.models;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pagpa.cs380.Models.Assignment;
import pagpa.cs380.Models.Course;
import pagpa.cs380.Models.Student;
import pagpa.cs380.Models.StudentGrade;
import pagpa.cs380.Utils.Registrar;

public class GPAForecastTests {
    
    private Registrar registrar;
    private Student student;
    private Course completedCourse; // Summer 2024 course (set to completed for testing purposes)
    private Course inProgressCourse; // Fall 2024 course (set to in progress for testing purposes)
    
    // Math done by hand for testing purposes (triple checked) 
    @BeforeEach
    void setUp() {
        // Get the current registrar
        registrar = Registrar.getCurrent();
        
        // Create a test student similar to Jordan Lee in program
        student = new Student(1060, "Jordan", "Lee", "Dr. Smith", "555-1234", "2027", false);
        
        // Create test courses
        completedCourse = new Course("Summer 2024", "PAED520", "Summer Course", 3.0, "Professor Smith");
        inProgressCourse = new Course("Fall 2024", "PAED530", "Fall Course", 4.0, "Professor Jones");
        
        // Create assignments for summer course (completed)
        Assignment summerAssignment1 = new Assignment("Summer Assignment 1", 1, 0.5, false, false);
        Assignment summerAssignment2 = new Assignment("Summer Assignment 2", 2, 0.5, false, false);
        
        // Create assignments for fall course (in progress)
        Assignment fallAssignment1 = new Assignment("Fall Assignment 1", 5, 0.33, false, false);
        Assignment fallAssignment2 = new Assignment("Fall Assignment 2", 6, 0.33, false, false);
        Assignment fallAssignment3 = new Assignment("Fall Assignment 3", 7, 0.34, false, false);
        
        // Add assignments to courses
        completedCourse.getAssignments().add(summerAssignment1);
        completedCourse.getAssignments().add(summerAssignment2);
        
        inProgressCourse.getAssignments().add(fallAssignment1);
        inProgressCourse.getAssignments().add(fallAssignment2);
        inProgressCourse.getAssignments().add(fallAssignment3);
        
        // Add courses to registrar
        registrar.getCourses().add(completedCourse);
        registrar.getCourses().add(inProgressCourse);
        
        // Add student to registrar
        registrar.getStudents().add(student);
        
        // Clear any existing grades
        student.getGrades().clear();
        registrar.getGrades().clear();
        
        // Add completed grades for summer course
        StudentGrade summerGrade1 = new StudentGrade(student, completedCourse, summerAssignment1);
        summerGrade1.setGrade(90.0); // A-
        summerGrade1.setGradeStatus(StudentGrade.GRADE_EARNED);
        summerGrade1.setGradeDate(LocalDate.now().minusMonths(2));
        
        StudentGrade summerGrade2 = new StudentGrade(student, completedCourse, summerAssignment2);
        summerGrade2.setGrade(85.0); // B
        summerGrade2.setGradeStatus(StudentGrade.GRADE_EARNED);
        summerGrade2.setGradeDate(LocalDate.now().minusMonths(1));
        
        // Add partial grades for fall course (only first assignment has a grade)
        StudentGrade fallGrade1 = new StudentGrade(student, inProgressCourse, inProgressCourse.getAssignments().get(0));
        fallGrade1.setGrade(88.0); // B+
        fallGrade1.setGradeStatus(StudentGrade.GRADE_EARNED);
        fallGrade1.setGradeDate(LocalDate.now().minusWeeks(2));
        
        // Add grades to student and registrar
        student.getGrades().add(summerGrade1);
        student.getGrades().add(summerGrade2);
        student.getGrades().add(fallGrade1);
        
        registrar.getGrades().add(summerGrade1);
        registrar.getGrades().add(summerGrade2);
        registrar.getGrades().add(fallGrade1);
    }
    
    @Test
    void testCurrentGPA() {
        // Calculate expected current GPA
        // Summer course: (90 + 85) / 2 = 87.5 (B+) = 3.3 GPA points
        // Fall course (only one assignment): 88.0 (B+) = 3.3 GPA points
        // Combined: (3.3 * 3.0 + 3.3 * 4.0) / 7.0 = 3.3
        double expectedCurrentGPA = 3.3;
        
        double actualCurrentGPA = registrar.calcGpaForStudent(student, false);
        assertEquals(expectedCurrentGPA, actualCurrentGPA, 0.1, "Current GPA calculation should match expected value");
    }
    
    @Test
    void testCurrentTermGPA() {
        // Calculate current term GPA (Fall 2024)
        // Only one assignment graded: 88.0 (B+) = 3.3 GPA points
        double expectedFallTermGPA = 3.3;
        
        double actualFallTermGPA = registrar.calcTermGpa(student, "Fall 2024", false);
        assertEquals(expectedFallTermGPA, actualFallTermGPA, 0.1, "Current Fall term GPA calculation should match expected value");
    }
    
    @Test
    void testCompletedTermGPA() {
        // Calculate completed term GPA (Summer 2024)
        // (90 + 85) / 2 = 87.5 (B+) = 3.3 GPA points
        double expectedSummerTermGPA = 3.3;
        
        double actualSummerTermGPA = registrar.calcTermGpa(student, "Summer 2024", false);
        assertEquals(expectedSummerTermGPA, actualSummerTermGPA, 0.1, "Summer term GPA calculation should match expected value");
    }
    
    @Test
    void testForecastGPA() {
        // Add forecast grades for remaining fall assignments
        StudentGrade fallGrade2 = new StudentGrade(student, inProgressCourse, inProgressCourse.getAssignments().get(1));
        fallGrade2.setGrade(95.0); // A
        fallGrade2.setGradeStatus(StudentGrade.GRADE_FORECAST);
        fallGrade2.setGradeDate(LocalDate.now());
        
        StudentGrade fallGrade3 = new StudentGrade(student, inProgressCourse, inProgressCourse.getAssignments().get(2));
        fallGrade3.setGrade(95.0); // A
        fallGrade3.setGradeStatus(StudentGrade.GRADE_FORECAST);
        fallGrade3.setGradeDate(LocalDate.now());
        
        // Add forecast grades to student
        student.getGrades().add(fallGrade2);
        student.getGrades().add(fallGrade3);
        
        // Calculate expected forecast term GPA
        // Fall course with forecasts: (88 + 95 + 95) / 3 ≈ 92.7 (A-) = 3.7 GPA points
        double expectedForecastTermGPA = 3.7;
        
        // Calculate expected forecast cumulative GPA
        // Summer course: 3.3 GPA points with 3.0 credits
        // Fall course with forecasts: 3.7 GPA points with 4.0 credits
        // Combined: (3.3 * 3.0 + 3.7 * 4.0) / 7.0 = 9.9 + 14.8 / 7.0 = 24.7 / 7.0 ≈ 3.5
        double expectedForecastCumulativeGPA = 3.5;
        
        // Test forecast term GPA
        double actualForecastTermGPA = registrar.calcTermGpa(student, "Fall 2024", true);
        assertEquals(expectedForecastTermGPA, actualForecastTermGPA, 0.1, "Forecast term GPA should match expected value");
        
        // Test forecast cumulative GPA
        double actualForecastCumulativeGPA = registrar.calcGpaForStudent(student, true);
        assertEquals(expectedForecastCumulativeGPA, actualForecastCumulativeGPA, 0.1, "Forecast cumulative GPA should match expected value");
    }
} 