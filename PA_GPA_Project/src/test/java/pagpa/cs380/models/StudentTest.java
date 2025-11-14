package pagpa.cs380.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import pagpa.cs380.Models.Student;

public class StudentTest {
	
	// FOR REFERENCE STUDENT CONSTRUCTOR public Student(String name, double gpa, double liveGpa, String riskLevel, String advisor, String phone, int cohort) 
	
	@Test
	public void testStudentGetters() {
		
		//  Student(int acid, String firstName, String lastName, String advisor, String phone, String cohort, boolean dismissed) 
		Student stu1 = new Student(123456, "Mark", "John", "Dr. Patel", "9721230045", "2025", false);

		assertEquals(stu1.getFirstName(), "Mark");
		assertEquals(stu1.getLastName(), "John");
		assertEquals(stu1.getAdvisor(), "Dr. Patel");
		assertEquals(stu1.getPhone(), "9721230045");
		assertEquals(stu1.getCohort(), "2025");
		assertEquals(false, stu1.isDismissed());
		
		//all worked
	}
	
	@Test
	public void testStudentSetters() {
		
		Student stu1 = new Student(123456, "Mark", "John", "Dr. Patel", "9721230045", "2025", false);		

		stu1.setFirstName("Mark John");
		assertEquals(stu1.getFirstName(), "Mark John");
		
		stu1.setFirstName("West");
		assertEquals(stu1.getLastName(), "West");
		
//		stu1.setGpa(3.0);
//		assertEquals(stu1.getGpa(), 3.0);
//		
//		stu1.setGpa(2.4);
//		assertEquals(stu1.getLiveGpa(), 2.4);
		
		stu1.setAdvisor("Dr. Patel");
		assertEquals(stu1.getAdvisor(), "Dr. Patel");
		
		stu1.setPhone("9721230045");
		assertEquals(stu1.getPhone(), "9721230045");
		
		stu1.setCohort("2025");
		assertEquals(stu1.getCohort(), "2025");
		
		stu1.setDismissed(true);
		assertTrue(stu1.isDismissed());
		
		//all worked
	}
	
}
