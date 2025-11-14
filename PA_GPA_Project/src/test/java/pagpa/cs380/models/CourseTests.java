package pagpa.cs380.models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import pagpa.cs380.Models.Assignment;
import pagpa.cs380.Models.Course;

public class CourseTests {

	/**
	 * Tests the primary constructor.  all initialization should be good
	 * and the course starts with no assignments.
	 */
	@Test
	void test_constructor_no_assignmets() {
		
	    // Course(String term, String courseNum, String courseName, double courseCredits, String teacher, Assignment ... assigns )
	
		Course c = new Course("FA25", "CS*120*A", "Intermediate Programming", 1.0, "Block");
		assertNotNull(c);
		
		assertEquals("FA25",c.getTerm());
		assertEquals("CS*120*A", c.getCourseNum());
		assertEquals("Intermediate Programming", c.getCourseName());
		assertEquals(1.0, c.getCourseCredits(),1E-10);
		assertEquals("Block",c.getInstructor());
		
		assertTrue(c.getAssignments().isEmpty());
		
	}

	
	/**
	 * Tests the constructor with additional assignments.  This constructor is
	 * useful during later testings when we can inject trained mock assignments into
	 * the course to testing gpa calculations.
	 */
	@Test
	void test_constructor_with_assignments() {
		
		Assignment as01 = new Assignment();
		Assignment as02 = new Assignment();
		
	    // Course(String term, String courseNum, String courseName, double courseCredits, String teacher, Assignment ... assigns )
	
		Course c = new Course("FA25", "CS*120*A", "Intermediate Programming", 1.0, "Block", as01, as02);
		assertNotNull(c);
		
		assertEquals("FA25",c.getTerm());
		assertEquals("CS*120*A", c.getCourseNum());
		assertEquals("Intermediate Programming", c.getCourseName());
		assertEquals(1.0, c.getCourseCredits(),1E-10);
		assertEquals("Block",c.getInstructor());
		
		assertEquals(2, c.getAssignments().size()); 
				
		
	}
	
	

}
