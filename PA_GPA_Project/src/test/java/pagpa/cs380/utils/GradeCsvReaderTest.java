package pagpa.cs380.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import pagpa.cs380.Models.Grade;
import pagpa.cs380.Utils.GradeCsvReader;

import java.util.List;
import java.util.Map;

/**
 * Unit tests for GradeCsvReader.
 */
public class GradeCsvReaderTest {

    /**
     * Tests that readGradeData() returns a valid, populated map of grades.
     */
    @Test
    void testReadGradeDataExists() {

        GradeCsvReader reader = new GradeCsvReader();
        Map<String, List<Grade>> gradeData = reader.readGradeData();

        // Assert: the returned map is not null
        assertNotNull(gradeData, "readGradeData() should not return null");
        // Assert: the map contains at least one student ID
        assertFalse(gradeData.isEmpty(), "There should be at least one student with grades");


        String someStudentId = gradeData.keySet().iterator().next();
        List<Grade> grades = gradeData.get(someStudentId);

        // Assert: the list of grades is not null
        assertNotNull(grades, "List of grades for a student should not be null");
        // Assert: there is at least one Grade in the list
        assertFalse(grades.isEmpty(), "There should be at least one Grade for that student");

 
        Grade first = grades.get(0);
        assertNotNull(first, "Grade object should not be null");
        assertTrue(first.getGrade() >= 0,      "Grade value should be non-negative");
        assertTrue(first.getGradeWeight()     >= 0,      "Grade weight should be non-negative");
    }
}

