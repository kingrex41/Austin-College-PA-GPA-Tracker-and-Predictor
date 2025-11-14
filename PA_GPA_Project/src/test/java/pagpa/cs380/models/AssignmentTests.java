package pagpa.cs380.models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import pagpa.cs380.Models.Assignment;

public class AssignmentTests {

	 /**
     * Verify that the parameterized constructor initializes all fields correctly
     * and that each getter returns the expected value.
     */
	
	@Test
    void testParameterizedConstructorAndGetters() {
        Assignment a = new Assignment("A1", 3, 15.5, true, false);
        assertEquals("A1", a.getAssignmentId(), "assignmentId should match");
        assertEquals(3, a.getWeek(), "week should match");
        assertEquals(15.5, a.getWeight(), 1e-9, "weight should match");
        assertTrue(a.isExam(), "exam flag should be true");
        assertFalse(a.isKey(), "key flag should be false");
    }

	/**
     * Verify that the default constructor sets all fields to their Java defaults:
     * null for object, 0 for numeric, and false for booleans.
     */
	
    @Test
    void testDefaultConstructorDefaults() {
        Assignment a = new Assignment();
        assertNull(a.getAssignmentId(), "default assignmentId should be null");
        assertEquals(0, a.getWeek(), "default week should be 0");
        assertEquals(0.0, a.getWeight(), 1e-9, "default weight should be 0.0");
        assertFalse(a.isExam(), "default exam flag should be false");
        assertFalse(a.isKey(), "default key flag should be false");
    }
    

    
//     When only exam is true, isExamOrKey() should return true.
    
    @Test
    void testIsExamOrKey_whenExamOnly() {
        Assignment a = new Assignment("E1", 1, 10.0, true, false);
        assertTrue(a.isExamOrKey(), "should be true when exam is true");
    }

    
//     When only key is true, isExamOrKey() should return true.
     
    @Test
    void testIsExamOrKey_whenKeyOnly() {
        Assignment a = new Assignment("K1", 1, 10.0, false, true);
        assertTrue(a.isExamOrKey(), "should be true when key is true");
    }

    
//     When neither exam nor key is true, isExamOrKey() should return false.
   
    @Test
    void testIsExamOrKey_whenNeither() {
        Assignment a = new Assignment("N1", 1, 10.0, false, false);
        assertFalse(a.isExamOrKey(), "should be false when both exam and key are false");
    }

    
//      When both exam and key are true, isExamOrKey() should return true.
    
    @Test
    void testIsExamOrKey_whenBoth() {
        Assignment a = new Assignment("B1", 1, 10.0, true, true);
        assertTrue(a.isExamOrKey(), "should be true when both exam and key are true");
    }

}
