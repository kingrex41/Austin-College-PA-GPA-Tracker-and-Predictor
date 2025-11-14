package pagpa.cs380.Utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import pagpa.cs380.Models.Criterion;
import pagpa.cs380.Models.RemGrade;
import pagpa.cs380.Models.Student;
import pagpa.cs380.Models.StudentGrade;
import pagpa.cs380.Models.SuccessStanding;

import java.util.stream.Collectors;

/**
 * An instance of this class can help us compute the success standing
 * of a student.   The main algorithm is <code>studentStanding(student)</code>.  This
 * helper object will evaluate the current student and return the standing
 * of that student.
 * <p>
 * Each success standing level is defined by a collection of predicates.  We say a 
 * student is "at" that certain level if it fails all the criteria at lower levels. At 
 * a specific level if any one of the defining criteria are satisfied, then we found
 * that success level for that student.   For example, if a student does not satisfy
 * any of the criteria for DISMISSED or PROBATION and satisfies any one of the criteria
 * for RISK, the student is considered at RISK.
 * </p>
 * @see SuccessStanding
 * @see Criterion
 * 
 */
public class SuccessManager {

	/**
	 * We keep the standing level criteria in a map.  For each level
	 * we manage a list of java predicates that define the criteria 
	 * for that level.
	 */
	Map<SuccessStanding, List<Criterion<Student>>> criteriaMap;
	
	
	/**
	 * As the primary constructor we initializes the list of criteria
	 * for each of the standing levels.  Private helper methods let us
	 * focus on the criteria for each.   
	 */
	public SuccessManager() {
		
		// a list of criteria for each level of standing 
		criteriaMap = new HashMap< SuccessStanding, List< Criterion<Student>> >();
		
		
		criteriaMap.put(SuccessStanding.DISMISSED, this.dismissedCriteria());
		
		// not dismissed, but may be on probation
		criteriaMap.put(SuccessStanding.PROBATION, this.probationCriteria());

		// not on probation, but maybe at risk
		criteriaMap.put(SuccessStanding.RISK, this.riskCriteria());
		
		// not at risk, but maybe all good
		criteriaMap.put(SuccessStanding.GOOD, this.goodCriteria());  
		
	}



	/**
	 * We build a list of predicates that must all be true if the student is considered
	 * in good standing.   We can use lambda expressions (assuming a student parameter) when
	 * the criteria is super simple (a one liner) or or an anonymous inline class allowing
	 * use to use a complex algorithm.   These predicates must return a true if the test is
	 * satisfied or false if the test fails.
	 * <p>
	 * A predicate is a computation that returns a true or false.  In this case, it is an 
	 * condition imposed on a student.   All our predicates must accept a student and must
	 * return a true or false value.
	 * </p>
	 * 
	 * @return a list of predicates on a student
	 */
	private List<Criterion<Student>> goodCriteria() {
		List<Criterion<Student>> goodCriteriaLst = new LinkedList<Criterion<Student>>();

		/*
		 * a student in good standing must not be dismissed. here we
		 */
		
		goodCriteriaLst.add(
				
				Criterion.studentCriterion("Good cumulative GPA", s -> s.getGpa() >= 3.0 || s.getGpa()==0.0 )

				);   // students cumulative gpa is b or above
		
	
		goodCriteriaLst.add(new Criterion<Student>("Good term GPA so far", new Predicate<Student>() {
			@Override
			public boolean test(Student s) {
				String term = Registrar.getCurrent().getCurrentTerm();
				Double tgpa = Registrar.getCurrent().calcTermGpa(s,term, false);
				return tgpa==0.0 || tgpa > 3.0;
				
			}
			
		}));
		
		return goodCriteriaLst;
	}
	
	
	/**
	 * builds and returns the list of criteria for a student being at PROBATION
	 * success level.  The student does not qualify for DISMISSED, but one of the
	 * criteria in this list might be satisfied.
	 * 
	 * NOTE: right now its an empty list and someone on this team needs to 
	 * finish.
	 * <ul>
	 * <li>the GPA at the end of the prior semester is below a 2.75 </li>
	 * <li>presence of any exam or key assigment without successful remediation</li>
	 * <li>student conduct; low professional standard</li>
	 * </ul>
	 * 
	 * 
	 * Don't think we have the ability to make a list for this yet^
	 * 
	 * @return
	 */
	private List<Criterion<Student>> probationCriteria() {
		List< Criterion<Student> > criteriaList = new LinkedList< Criterion<Student> >();
		
		
		Criterion<Student> c1 = Criterion.studentCriterion("Low cGPA", s -> s.getGpa() < 2.75 && s.getGpa()>0.0 );
		criteriaList.add( c1 ); 
		
		criteriaList.add(new Criterion<Student>("Unremediated Failed Exam or Key Assignment", new Predicate<Student>() {
			@Override
			public boolean test(Student s) {

				// this criteria is based on the student's grades
				List<StudentGrade> grades = s.getGrades();

				// searching for earned exam grade considered failing
				for (StudentGrade sg : grades ) {
					
					if (sg.isEarned()) {
						if (sg.getAssignment().isExamOrKey()) {
							if (sg.getGrade()<70.0) {
								List<RemGrade> rGrades = sg.getRemGrades();
								if (rGrades == null) return true;
								if (rGrades.isEmpty()) return true;
								if (rGrades.stream().anyMatch(rg->rg.getGradeValue()>=70.0)) {
									return true;
								}
							}
								
						}
					}
				}
									
				// if we get here none were found (we did not exit early)
				return false;
			}}));
		
		//TODO professionalism?   
		
		//TODO core competency exam? 
		
		return criteriaList;
	}
	
	/**
	 * builds and returns the list of criteria for a student being at RISK level.
	 * <p>
	 * A student is a RISK if none of the criteria for the lower levels apply and
	 * <ul>
	 * <li>cumulative GPA dips below threshold</li>
	 * <li>low live term GPA below threshold</li>
	 * <li>one or more exam grades < 70.0</li>
	 * <li>three of more exam or key assignment grades < 75.0</li>
	 * <li>five or more exam or key assignment grades < 80.0</li>
	 * </ul>
	 * </p>
	 * @return
	 */
	private List< Criterion<Student>   > riskCriteria() {

		
		List< Criterion<Student> > criteriaList = new LinkedList< Criterion<Student> >();

		/*
		 * a student in RISK standing has a cumulative GPA under a desired
		 * threshold 
		 */
		Criterion<Student> c1 = Criterion.studentCriterion("Low cGPA", s -> s.getGpa() < 2.75 && s.getGpa()>0.0 );
		criteriaList.add( c1 ); 

		
		/**
		 * Student's GPA this current term is concerning (less than 3.0) 
		 */
		criteriaList.add(new Criterion<Student>("Low Term GPA ", new Predicate<Student>() {
			@Override
			public boolean test(Student s) {
				String term = Registrar.getCurrent().getCurrentTerm();
				Double tgpa = Registrar.getCurrent().calcTermGpa(s,term, false);
				return tgpa>0.0 && tgpa < 3.0;
				
			}
			
		}));
		

		/* 
		 * has a least one failing exam grade. 
		 *  
		 * NOTE: Here I am showing anonymous
		 * inline subclass extending the Predicate class with a test method on a student.  
		 * your
		 * complex algorithm will eventually return true or false depending on
		 * if we satisfy the predicate or not.
		 */
		criteriaList.add(new Criterion<Student>("Failed Exam or Key Assignment", new Predicate<Student>() {
			@Override
			public boolean test(Student s) {

				// this criteria is based on the student's grades
				List<StudentGrade> grades = s.getGrades();

				// searching for earned exam grade considered failing
				for (StudentGrade sg : grades ) {
					
					if (sg.isEarned()) {
						if (sg.getAssignment().isExamOrKey()) {
							if (sg.getGrade()<70.0)
								return true;
						}
					}
				}
									
				// if we get here none were found (we did not exit early)
				return false;
			}}));
		
		
		
		

		/*
		 * ... OR ...
		 * 
		 * 3 or more exam or key assignments in low C range.  Here I am showing how
		 * to use an anonymous inline approach using the streams api, avoiding
		 * loops and if statements.  Java's functional programming support is cool.
		 */
		criteriaList.add(new Criterion<Student>("At least 3 exam grades C- or lower",	new Predicate<Student>() {
			@Override
			public boolean test(Student s) {
				return s.getGrades().stream()
					.filter(sg->sg.isEarned())
					.filter(sg->sg.getAssignment().isExam())  // look at only exam or key assignments
					.filter(sg->sg.getGrade() < 75.0)  // that are under 75.0
					.count()>=3;
				
			}
			
		}));

		/*
		 * 5 or more exam or key assignments in below B range.  Here we use a combination
		 * of lambda expression and the streams api. we streamify the grades for the current
		 * student, then filter down the grade list to only those student grades are exams or 
		 * key assignments.  once filtered, we count them and compare to our required threshold.
		 * The student satisfies the criteria for being at RISK if this evaluates to true
		 */
		
		criteriaList.add( new Criterion<Student>("At least 5 exam grades lower than B",
				
				s -> s.getGrades().stream()
				.filter(sg->sg.isEarned())  // look at only exam or key assignments				
				.filter(sg->sg.getAssignment().isExam())
				.filter(sg->sg.getGrade() < 80.0)  // that are under 80.0
				.count()>=5)
				
				);
		
		
		// TODO  other criteria for RISK needs to be added here; someone finish
		
		return criteriaList;
	}
	
	/**
	 * What is the criteria for being dismissed?   They have been marked
	 * as dismissed.  No other.  
	 * 
	 * @return
	 */
	private List<Criterion<Student>> dismissedCriteria() {
		
		List< Criterion<Student> > criteriaList = new LinkedList< Criterion<Student> >();

		/*
		 * a student in dismissed standing must be marked manually as dismissed
		 */
		criteriaList.add( 
				Criterion.studentCriterion("Student is dismissed", s -> s.isDismissed()));   // student is not dismissed

		return criteriaList;
	}
	
	
	
	/**
	 * Returns a list o criteria 
	 * @param stu
	 * @return
	 */
	public List<Criterion<Student>> standingCriteria( Student stu) {
		
		Enum<SuccessStanding> level = this.studentStanding(stu);
		
		List<Criterion<Student>> criteria = this.criteriaMap.get(level);
		
		return criteria;
	}
	
	
	
	/**
	 * Computes the success standing of a student. We process them
	 * in "bad" to "good" order. So if the student is at a given
	 * standing value (like RISK), this implies they are NOT at
	 * PROBATION or DISMISSED.  None of the criteria for those lower
	 * levels must have satisfied (all evaluated to false). 
	 *   
	 * @param s the current student
	 */
	public Enum<SuccessStanding> studentStanding(Student s) {
		
		for (Enum<SuccessStanding> standing : SuccessStanding.values()) {
			
			if (standing == SuccessStanding.UNKNOWN) break;
			
			// fetch the criteria list for the current standing level
			List<Criterion<Student>> criteriaList = criteriaMap.get(standing);
			
			// skip standings without criteria for now, but show warning to 
			if (criteriaList == null || criteriaList.isEmpty()) {
				System.err.println("["+standing+"] has no criteria at this time. Someone fix this.");
				continue;
			}
			

			// if any of the criteria predicates in the current list evaluate to true, 
			// the student is at the current success standing 
			if (criteriaList.stream().anyMatch(pred -> ((Criterion<Student>) pred).test(s))) {
				
				return standing;
			}
			
			// otherwise we look at the next level up.
		}
		
		return SuccessStanding.UNKNOWN;  // should never happen if we code criteria right
	}

	public Map<SuccessStanding, List<Criterion<Student>>> getCriteriaMap() {
		return criteriaMap;
	}

	public void setCriteriaMap(Map<SuccessStanding, List<Criterion<Student>>> criteriaMap) {
		this.criteriaMap = criteriaMap;
	}

}
