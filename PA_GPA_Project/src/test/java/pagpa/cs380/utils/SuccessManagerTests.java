package pagpa.cs380.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import pagpa.cs380.Models.Assignment;
import pagpa.cs380.Models.Student;
import pagpa.cs380.Models.StudentGrade;
import pagpa.cs380.Models.SuccessStanding;
import pagpa.cs380.Utils.SuccessManager;


/**
 * Tests an instance of the SuccessManager to see if they derive the criteria correctly.
 */
class SuccessManagerTests {

	SuccessManager sm;
	
	@Test
	void test_student_dismissed() {
		
		sm = new SuccessManager();
		
		Student stu = Mockito.mock(Student.class);
		Mockito.when(stu.isDismissed()).thenReturn(true);

		// if the student has been dismissed, then 
		// we better see the DISMISSED success standing
		assertEquals(SuccessStanding.DISMISSED, sm.studentStanding(stu));
		
	}
	
	

	/**
	 * Test to verify that a student with a low gpa is considered at RISK due to GPA
	 */
	@Test
	void test_student_at_risk_due_to_gpa() {
		
		/*
		 * when interacting with the successmanager, this mock student will pretend
		 * to have a log gpa and therefore should be reported as at RISK
		 */
		Student stu = Mockito.mock(Student.class);
		Mockito.when(stu.isDismissed()).thenReturn(false);  // not dismissed
		Mockito.when(stu.getGpa()).thenReturn(2.3);			// but low gpa

		sm = new SuccessManager();
		
		// we better see the RISK success standing due to low gpa
		assertEquals(SuccessStanding.RISK, sm.studentStanding(stu));
		
	}
	
	
	/**
	 * Test to verify that a student with a low gpa is considered at RISK due having
	 * a failing exam as the third grade.
	 */
	@Test
	void test_student_at_risk_due_to_failing_exam_all_earned() {
		
		
		// but .... what if....
		
		Assignment as01 = Mockito.mock(Assignment.class);
		Mockito.when(as01.isExam()).thenReturn(false);  	// first assignment not an exam

		Assignment as02 = Mockito.mock(Assignment.class);  	// second assignment IS an exam
		Mockito.when(as02.isExam()).thenReturn(true);

		Assignment as03 = Mockito.mock(Assignment.class);  	// third assignment IS an exam also
		Mockito.when(as03.isExam()).thenReturn(true);

		
		/*
		 * first grade on first assignment is not an exam...so should be ignored
		 */
		StudentGrade sg01 = Mockito.mock(StudentGrade.class);
		Mockito.when(sg01.isEarned()).thenReturn(true);
		Mockito.when(sg01.getAssignment()).thenReturn(as01);
		
		
		/*
		 * second grade on second assignment IS an exam but is a good grade...so still not at risk
		 */
		StudentGrade sg02 = Mockito.mock(StudentGrade.class);   
		Mockito.when(sg02.isEarned()).thenReturn(true);
		Mockito.when(sg02.getAssignment()).thenReturn(as02);	// is an exam...
		Mockito.when(sg02.getGrade()).thenReturn(95.0); 		// with passing grade

		/*
		 * third grade on third assignment IS an exam but is a BAD grade...so RISK criteria
		 * should be satisfied and our manager should return AT RISK when finding this grade
		 */
		StudentGrade sg03 = Mockito.mock(StudentGrade.class);
		Mockito.when(sg03.isEarned()).thenReturn(true);
		Mockito.when(sg03.getAssignment()).thenReturn(as03);	// is an exam assignment
		Mockito.when(sg03.getGrade()).thenReturn(63.0); 		// with failing grade
		
		
		
		Student stu = Mockito.mock(Student.class);
		Mockito.when(stu.isDismissed()).thenReturn(false);  // not dismissed
		Mockito.when(stu.getGpa()).thenReturn(3.5);			// good gpa
		
		// but what happens when the student has these assignment grades trained above.
		Mockito.when(stu.getGrades()).thenReturn(Stream.of(sg01,sg02,sg03).toList());
		
		
		sm = new SuccessManager();
		
		// we better see the RISK success standing
		assertEquals(SuccessStanding.RISK, sm.studentStanding(stu));
		
	}
	
	/**
	 * Test to verify that a student with a low gpa is considered at RISK due having
	 * a failing exam as the third grade.
	 */
	@Test
	void test_student_at_risk_due_to_three_key_grades_too_low_all_earned() {
		
		
		// but .... what if....
		
		Assignment as01 = Mockito.mock(Assignment.class);
		Mockito.when(as01.isExamOrKey()).thenReturn(false);

		Assignment as02 = Mockito.mock(Assignment.class);
		Mockito.when(as02.isExamOrKey()).thenReturn(true);
		
		Assignment as03 = Mockito.mock(Assignment.class);  	// third assignment IS an exam also
		Mockito.when(as03.isExamOrKey()).thenReturn(true);
		
		Assignment as04 = Mockito.mock(Assignment.class);  	// fourth assignment IS an exam also
		Mockito.when(as04.isExamOrKey()).thenReturn(true);
		
		
		/*
		 * first grade on first assignment exam or key...so should be ignored
		 */
		StudentGrade sg01 = Mockito.mock(StudentGrade.class);   // not a violation
		Mockito.when(sg01.isEarned()).thenReturn(true);		
		Mockito.when(sg01.getAssignment()).thenReturn(as01);
		Mockito.when(sg01.getGradeStatus()).thenReturn(StudentGrade.GRADE_EARNED);
		
		/*
		 * second grade on second assignment IS an exam but is a good grade...so still not at risk
		 */
		StudentGrade sg02 = Mockito.mock(StudentGrade.class);   // not a violation
		Mockito.when(sg02.isEarned()).thenReturn(true);
		Mockito.when(sg02.getAssignment()).thenReturn(as02);	// is an exam...
		Mockito.when(sg02.getGrade()).thenReturn(95.0); 		// with passing grade

		/*
		 * grade on third assignment IS an exam but is a LOW grade...not failing
		 */
		StudentGrade sg03 = Mockito.mock(StudentGrade.class);
		Mockito.when(sg03.isEarned()).thenReturn(true);
		Mockito.when(sg03.getAssignment()).thenReturn(as03);	// is an exam or key assignment
		Mockito.when(sg03.getGrade()).thenReturn(72.0); 		// with low C grade

		/*
		 * grade on fourth assignment is NOT an exam but is a LOW key grade...not failing
		 */
		StudentGrade sg04 = Mockito.mock(StudentGrade.class);
		Mockito.when(sg04.isEarned()).thenReturn(true);
		Mockito.when(sg04.getAssignment()).thenReturn(as04);	// is an exam assignment
		Mockito.when(sg04.getGrade()).thenReturn(73.0); 		// with low C grade

		
		/*
		 * train our mock student to report having these grades to our success manager.
		 * this student is not dismissed and has a good gpa
		 */
		Student stu = Mockito.mock(Student.class);
		Mockito.when(stu.isDismissed()).thenReturn(false);  // not dismissed
		Mockito.when(stu.getGpa()).thenReturn(3.5);			// good gpa
		
		// but what happens when the student has numerous LOW grades
		// sg01 and sg02 are fine,   sg03,g04 are LOW
		// so in this scenario we have 3 low in the list and 3 fine in our list
		Mockito.when(stu.getGrades()).thenReturn(Stream.of(sg01,sg02,sg03,sg04,sg01,sg03).toList());
		
		sm = new SuccessManager();
		
		// we better see the RISK success standing
		assertEquals(SuccessStanding.RISK, sm.studentStanding(stu));
		
	}
	
	/**
	 * Test to verify that a student with a low gpa is considered at RISK due having
	 * a failing exam as the third grade.
	 */
	@Test
	void test_student_good_with_many_grades() {
		
		
		// but .... what if....
		
		Assignment as01 = Mockito.mock(Assignment.class);
		Mockito.when(as01.isExamOrKey()).thenReturn(false);

		Assignment as02 = Mockito.mock(Assignment.class);
		Mockito.when(as02.isExamOrKey()).thenReturn(true);
		
		Assignment as03 = Mockito.mock(Assignment.class);  	// third assignment IS an exam also
		Mockito.when(as03.isExamOrKey()).thenReturn(true);
		
		Assignment as04 = Mockito.mock(Assignment.class);  	// fourth assignment IS an exam also
		Mockito.when(as04.isExamOrKey()).thenReturn(true);
		
		
		/*
		 * first grade on first assignment exam or key...so should be ignored
		 */
		StudentGrade sg01 = Mockito.mock(StudentGrade.class);   // not a violation
		Mockito.when(sg01.isEarned()).thenReturn(true);		
		Mockito.when(sg01.getAssignment()).thenReturn(as01);
		Mockito.when(sg01.getGradeStatus()).thenReturn(StudentGrade.GRADE_EARNED);
		
		/*
		 * second grade on second assignment IS an exam but is a good grade...so still not at risk
		 */
		StudentGrade sg02 = Mockito.mock(StudentGrade.class);   // not a violation
		Mockito.when(sg02.isEarned()).thenReturn(true);
		Mockito.when(sg02.getAssignment()).thenReturn(as02);	// is an exam...
		Mockito.when(sg02.getGrade()).thenReturn(95.0); 		// with passing grade

		/*
		 * grade on third assignment IS an exam but is a LOW grade...not failing
		 */
		StudentGrade sg03 = Mockito.mock(StudentGrade.class);
		Mockito.when(sg03.isEarned()).thenReturn(true);
		Mockito.when(sg03.getAssignment()).thenReturn(as03);	// is an exam or key assignment
		Mockito.when(sg03.getGrade()).thenReturn(80.0); 		// with low C grade

		/*
		 * grade on fourth assignment is NOT an exam but is a LOW key grade...not failing
		 */
		StudentGrade sg04 = Mockito.mock(StudentGrade.class);
		Mockito.when(sg04.isEarned()).thenReturn(true);
		Mockito.when(sg04.getAssignment()).thenReturn(as04);	// is an exam assignment
		Mockito.when(sg04.getGrade()).thenReturn(81.0); 		// with low C grade

		
		/*
		 * train our mock student to report having these grades to our success manager.
		 * this student is not dismissed and has a good gpa
		 */
		Student stu = Mockito.mock(Student.class);
		Mockito.when(stu.isDismissed()).thenReturn(false);  // not dismissed
		Mockito.when(stu.getGpa()).thenReturn(3.5);			// good gpa
		
		// but what happens when the student has no low grades
		Mockito.when(stu.getGrades()).thenReturn(Stream.of(sg01,sg02,sg03,sg04,sg01,sg03).toList());
		
		sm = new SuccessManager();
		
		// we better see the RISK success standing
		assertEquals(SuccessStanding.GOOD, sm.studentStanding(stu));
		
	}

}
