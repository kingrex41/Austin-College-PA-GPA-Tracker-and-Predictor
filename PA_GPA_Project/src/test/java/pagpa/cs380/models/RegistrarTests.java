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

/* In this class i do the math myself to assert the correct values of the gpas but then make objects to test the registrar class */
public class RegistrarTests {
    
    private Registrar registrar;
    private Student student;
    private Course course1;
    private Course course2;
    
    @BeforeEach
    void setUp() {
        // Get the singleton instance of registrar
        registrar = Registrar.getCurrent();
        
        // Create a test student
        student = new Student(12345, "Test", "Student", "Advisor", "555-1234", "2024", false);
        
        // Create test courses
        course1 = new Course("Summer 2024", "COURSE101", "Test Course 1", 3.0, "Professor Smith");
        course2 = new Course("Summer 2024", "COURSE102", "Test Course 2", 4.0, "Professor Jones");
        
        // Create assignments for different weeks
        Assignment week1Assignment = new Assignment("Week 1 Assignment", 1, 0.25, false, false);
        Assignment week2Assignment = new Assignment("Week 2 Assignment", 2, 0.25, false, false);
        Assignment week3Assignment = new Assignment("Week 3 Assignment", 3, 0.25, false, false);
        Assignment week4Assignment = new Assignment("Week 4 Assignment", 4, 0.25, false, false);
        
        // Add assignments to courses
        course1.getAssignments().add(week1Assignment);
        course1.getAssignments().add(week2Assignment);
        course1.getAssignments().add(week3Assignment);
        course1.getAssignments().add(week4Assignment);
        
        course2.getAssignments().add(week1Assignment);
        course2.getAssignments().add(week2Assignment);
        course2.getAssignments().add(week3Assignment);
        course2.getAssignments().add(week4Assignment);
        
        // Add courses to registrar
        registrar.getCourses().add(course1);
        registrar.getCourses().add(course2);
        
        // Add student to registrar
        registrar.getStudents().add(student);
        
        // Clear any existing grades
        student.getGrades().clear();
        registrar.getGrades().clear();
    }
    
    @Test
    void testCalcCumulativeWeekGpa() {
        // Create student grades for different weeks
        StudentGrade grade1Course1 = new StudentGrade(student, course1, course1.getAssignments().get(0));
        grade1Course1.setGrade(90.0); // A- (3.7)
        grade1Course1.setGradeStatus(StudentGrade.GRADE_EARNED);
        grade1Course1.setGradeDate(LocalDate.now());
        
        StudentGrade grade1Course2 = new StudentGrade(student, course2, course2.getAssignments().get(0));
        grade1Course2.setGrade(85.0); // B (3.0)
        grade1Course2.setGradeStatus(StudentGrade.GRADE_EARNED);
        grade1Course2.setGradeDate(LocalDate.now());
        
        StudentGrade grade2Course1 = new StudentGrade(student, course1, course1.getAssignments().get(1));
        grade2Course1.setGrade(80.0); // B- (2.7)
        grade2Course1.setGradeStatus(StudentGrade.GRADE_EARNED);
        grade2Course1.setGradeDate(LocalDate.now());
        
        StudentGrade grade2Course2 = new StudentGrade(student, course2, course2.getAssignments().get(1));
        grade2Course2.setGrade(95.0); // A (4.0)
        grade2Course2.setGradeStatus(StudentGrade.GRADE_EARNED);
        grade2Course2.setGradeDate(LocalDate.now());
        
        // Add grades to student and registrar
        student.getGrades().add(grade1Course1);
        student.getGrades().add(grade1Course2);
        student.getGrades().add(grade2Course1);
        student.getGrades().add(grade2Course2);
        
        registrar.getGrades().add(grade1Course1);
        registrar.getGrades().add(grade1Course2);
        registrar.getGrades().add(grade2Course1);
        registrar.getGrades().add(grade2Course2);
        
        // Expected GPA for week 1 only (course1 = 3.7, course2 = 3.0)
        // (3.7 * 3.0 + 3.0 * 4.0) / (3.0 + 4.0) = 11.1 + 12.0 / 7.0 = 23.1 / 7.0 = 3.3 (approx)
        double expectedWeek1Gpa = 3.3;
        
        // For week 2, need to average both assignments for each course
        // Course 1: (90 + 80) / 2 = 85 (B = 3.0)
        // Course 2: (85 + 95) / 2 = 90 (A- = 3.7)
        // Combined GPA: (3.0 * 3.0 + 3.7 * 4.0) / (3.0 + 4.0) = 9.0 + 14.8 / 7.0 = 23.8 / 7.0 = 3.4 (approx)
        double expectedWeek2Gpa = 3.4;
        
        // Test week 1 (which should only include week 1 assignments)
        double week1Gpa = registrar.calcCumulativeWeekGpa(student, 1, false);
        assertEquals(expectedWeek1Gpa, week1Gpa, 0.1, "Week 1 cumulative GPA should be approximately " + expectedWeek1Gpa);
        
        // Test week 2 (which should include both week 1 and week 2 assignments)
        double week2Gpa = registrar.calcCumulativeWeekGpa(student, 2, false);
        assertEquals(expectedWeek2Gpa, week2Gpa, 0.1, "Week 2 cumulative GPA should be approximately " + expectedWeek2Gpa);
        
        // Test that week 1 calculation only includes week 1 assignments
        double week1OnlyGpa = registrar.calcWeekGpa(student, 1, false);
        assertEquals(expectedWeek1Gpa, week1OnlyGpa, 0.1, "Week 1 only GPA should be approximately " + expectedWeek1Gpa);
    }
} 