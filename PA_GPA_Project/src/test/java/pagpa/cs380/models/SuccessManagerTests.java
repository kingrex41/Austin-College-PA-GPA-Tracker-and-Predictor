package pagpa.cs380.models;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

import pagpa.cs380.Models.Assignment;
import pagpa.cs380.Models.Grade;
import pagpa.cs380.Models.Student;
import pagpa.cs380.Models.StudentGrade;
import pagpa.cs380.Models.SuccessStanding;
import pagpa.cs380.Utils.SuccessManager;
import pagpa.cs380.Models.Course;

public class SuccessManagerTests {

	private SuccessManager manager;
	
	@BeforeEach
	void setUp() {
		manager = new SuccessManager();
	}
	
//	Test to see if a student that is labeled as dismissed will return that they are dismissed
	@Test
	void testDismissedStudent() {
		Student s = new Student(0, "John", "Doe", "Nguyen", "911", "2027", true) {
			@Override 
			public boolean isDismissed() {return true;}
			@Override
			public double getGpa() {return 50.47;}
			
		};
		
		s.setGrades(Collections.emptyList());
		
		assertEquals(
	            SuccessStanding.DISMISSED, manager.studentStanding(s), "Student marked dismissed should return DISMISSED");
	}
	
//	Test to see if a student that has a GPA 2.75 is marked as AT RISK
	@Test
	void testRiskLowGpaCriteria() {
		Student s = new Student(0, "John", "Doe", "Nguyen", "911", "2027", true){
		@Override 
		public boolean isDismissed() {return false;}
		@Override
		public double getGpa() {return 75.0;}
	};
	
	s.setGrades(Collections.emptyList());
	
	assertEquals(
            SuccessStanding.RISK, manager.studentStanding(s), "Student with GPA <80 should be at RISK");
	};
	
	
//	Test to see if a student has a good standing 
	 @Test
	    void testGoodStanding() {
		 Student s = new Student(0, "John", "Doe", "Nguyen", "911", "2027", false) {
	            @Override public double getGpa() { return 85.0; }
	        };
	        s.setGrades(Collections.emptyList());

	        assertEquals(
	            SuccessStanding.GOOD,manager.studentStanding(s), "GPA >= 80 with no failing grades should return GOOD");
	    }
	 

//	 Test to see if when there is no criteria labeled on student, if it will return UNKNOWN
	 @Test
	 void testWhenCriteriaUnkown() {
		 manager.setCriteriaMap(Collections.emptyMap());
		 Student s = new Student(0, "John", "Doe", "Nguyen", "911", "2027", false) {
			 @Override
			 public double getGpa() { return 85.0;}
			 };
			 s.setGrades(Collections.emptyList());
			 
			 assertEquals( SuccessStanding.UNKNOWN, manager.studentStanding(s), "With no criteria labeled on student, should return UNKNOWN");
	 }
	
}
