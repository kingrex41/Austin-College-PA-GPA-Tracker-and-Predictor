package pagpa.cs380.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import pagpa.cs380.Models.Course;
import pagpa.cs380.Utils.CourseCsvReader;
import pagpa.cs380.Models.Assignment;

import java.util.List;
import java.util.Map;
/**
 * Unit tests for CourseCsvReader.
 */
public class CourseCsvReaderTest {

/*	Verifies that the CSV returns not null 
 * and that each course is populated
 */
    @Test
    void testReadCourseDataExists() {
        CourseCsvReader reader = new CourseCsvReader();
        List<Course> courses = reader.readCourseData();

        // Basic existence checks
        assertNotNull(courses, "readCourseData() should not return null");
        assertFalse(courses.isEmpty(), "There should be at least one Course");

        // Check that the first course has its required fields populated
        Course first = courses.get(0);
        assertNotNull(first.getTerm(),     "Course.term should not be null");
        assertNotNull(first.getCourseCredits(),"Course.courseCode should not be null");
        assertNotNull(first.getCourseName(),"Course.courseTitle should not be null");
        assertTrue(first.getCourseCredits() >= 0, "Course.credits should be non‐negative");
        
    }

/*   Testing to see if readAssignmentData() returns a non null map
 * 	 and each assignment is populated 
 */
    
    @Test
    void testReadAssignmentDataExists() {
        CourseCsvReader reader = new CourseCsvReader();
        Map<String, List<Assignment>> assignmentData = reader.readAssignmentData();

        // Basic existence checks
        assertNotNull(assignmentData, "readAssignmentData() should not return null");
        assertFalse(assignmentData.isEmpty(), "There should be at least one entry in the map");

        // Picking the first course ID and its assignments
        String someCourseId = assignmentData.keySet().iterator().next();
        List<Assignment> assignments = assignmentData.get(someCourseId);

        assertNotNull(assignments, "List of assignments for a course ID should not be null");
        assertFalse(assignments.isEmpty(), "There should be at least one Assignment for that course");

        // Checking required fields on the first assignment
        Assignment a = assignments.get(0);
        assertNotNull(a.getAssignmentId(), "Assignment.assignmentId should not be null");
        assertTrue(a.getWeek() > 0,        "Assignment.week should be positive");
        assertTrue(a.getWeight() >= 0,     "Assignment.weight should be non‐negative");
        
    }
}

