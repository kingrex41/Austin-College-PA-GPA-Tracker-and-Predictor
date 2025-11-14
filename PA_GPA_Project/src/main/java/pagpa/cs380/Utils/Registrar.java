package pagpa.cs380.Utils;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import pagpa.cs380.Models.Student;
import pagpa.cs380.Models.Assignment;
import pagpa.cs380.Models.Course;
import pagpa.cs380.Models.Grade;
import pagpa.cs380.Models.StudentGrade;

/**
 * Singleton class that manages all data and calculations for the application.
 * This class follows the UML design by maintaining lists of students, courses,
 * and grades, and providing methods for GPA calculations and risk level distribution.
 */
public class Registrar {
	private static Registrar current = null;   
	
	// data collections 
	private List<Student> students;
	private List<Course> courses;
	private List<StudentGrade> grades;
	private int curCohort;
	
	// CSV readers
	private final StudentCsvReader studentReader;
	private final CourseCsvReader courseReader;
	private final GradeCsvReader gradeReader;
	
	private Random rand = new Random();
	
	/**
	 * Protected constructor for singleton pattern.
	 * Initializes all data by reading from CSV files.
	 */
	protected Registrar() {
		// Initialize readers
		studentReader = new StudentCsvReader();
		courseReader = new CourseCsvReader();
		gradeReader = new GradeCsvReader();
		
		// Initialize collections
		students = new ArrayList<>();
		courses = new ArrayList<>();
		grades = new ArrayList<>();
		curCohort = 2025; // Default current cohort
		
		loadAllData();
		
	
	}
	
	/**
	 * Loads all data from CSV files.
	 */
	private void loadAllData() {
		try {
			// Load students
			students = studentReader.readStudentsOfCohort();
			
			// Load courses
			courses = courseReader.readCourseData();
			
			// Load assignments and add them to courses
			loadAssignmentsFromCsv();
			
			// Load grades and convert to StudentGrade objects
			Map<String, List<Grade>> gradeData = gradeReader.readGradeData();
			convertGradeData(gradeData);
			
		} catch (Exception e) {
			System.err.println("Error loading data: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads assignment data from course_info.csv and adds assignments to the appropriate courses.
	 */
	private void loadAssignmentsFromCsv() {
		// Use the course reader to load assignment data
		try {
			Map<String, List<Assignment>> assignmentData = courseReader.readAssignmentData();
			
			// Add assignments to courses
			for (Course course : courses) {
				String courseId = course.getCourseNum();
				List<Assignment> assignments = assignmentData.get(courseId);
				if (assignments != null) {
					course.getAssignments().addAll(assignments);
				}
			}
		} catch (Exception e) {
			System.err.println("Error loading assignment data: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	
	private void convertGradeData(Map<String, List<Grade>> gradeData) {
		// Clear any existing grades
		grades.clear();
		for (Student student : students) {
			student.getGrades().clear();
		}
		
		if (gradeData == null || gradeData.isEmpty()) {
			return;
		}
		
		// Create a map from student ID to Student object for faster lookup
		Map<String, Student> studentMap = new HashMap<>();
		for (Student student : students) {
			String studentId = String.valueOf(student.getAcid());
			studentMap.put(studentId, student);
		}
		
		// Create a map from course number to Course object for faster lookup
		Map<String, Course> courseMap = new HashMap<>();
		for (Course course : courses) {
			courseMap.put(course.getCourseNum(), course);
		}
		
		// Process each student's grades from the CSV file
		for (Map.Entry<String, List<Grade>> entry : gradeData.entrySet()) {
			String studentId = entry.getKey();
			List<Grade> studentGrades = entry.getValue();
			
			// Find the matching student
			Student student = studentMap.get(studentId);
			
			if (student == null) {
				continue;
			}
			
			// For each grade, find matching assignment and create StudentGrade
			for (Grade gradeObj : studentGrades) {
				// Get the header from grade's grade type (which stores the assignment name in format "PAED 520 - Exam #1")
				String header = gradeObj.getGradeType();
				String[] parts = header.split(" - ");
				
				if (parts.length == 2) {
					String courseNum = parts[0].trim(); // "PAED 520"
					String assignmentName = parts[1].trim(); // "Exam #1"
					
					Course course = courseMap.get(courseNum);
					
					if (course != null) {
						// Find or create the assignment
						Assignment assignment = null;
						for (Assignment a : course.getAssignments()) {
							if (a.getAssignmentId().equals(assignmentName)) {
								assignment = a;
								break;
							}
						}
						
						// If assignment doesn't exist, create it
						if (assignment == null) {
							boolean isExam = assignmentName.toLowerCase().contains("exam");
							assignment = new Assignment(assignmentName, 1, 0.25, isExam, true);
							course.getAssignments().add(assignment);
						}
						
						// Create the student grade
						StudentGrade sg = new StudentGrade(student, course, assignment);
						sg.setGrade(gradeObj.getGrade());
						sg.setGradeStatus(StudentGrade.GRADE_EARNED);
						
						try {
							// Try to parse the date from the grade
							String dateStr = gradeObj.getGradeDate();
							if (dateStr != null && !dateStr.isEmpty()) {
								sg.setGradeDate(LocalDate.parse(dateStr));
							} else {
								sg.setGradeDate(LocalDate.now());
							}
						} catch (Exception e) {
							sg.setGradeDate(LocalDate.now());
						}
						
						grades.add(sg);
						student.getGrades().add(sg);
					}
				}
			}
		}
	}
	
	/**
	 * Returns the singleton instance of Registrar.
	 */
	public static Registrar getCurrent() {
		if (current == null) {
			current = new Registrar();
		}
		return current;
	}
	
	/**
	 * Calculates the average GPA of all students.
	 */
	public double calculateAverageGPA() {
		if (students.isEmpty()) {
			return 0.0;
		}
		
		double totalGPA = 0.0;
		for (Student student : students) {
			totalGPA += student.getGpa();
		}
		
		System.out.println("Yes, we are calling this"); //this is being called for button, but the math is off and is showing as 78, down not make sense why 
		
		return totalGPA / students.size();
	}
	
	/**
	 * Converts a letter grade to GPA points on a 4.0 scale.
	 * @param letterGrade The letter grade (A, A-, B+, etc.)
	 * @return The corresponding GPA points
	 */
	private double letterGradeToPoints(String letterGrade) {
		switch (letterGrade) {
			case "A": return 4.0;
			case "A-": return 3.7;
			case "B+": return 3.3;
			case "B": return 3.0;
			case "B-": return 2.7;
			case "C+": return 2.3;
			case "C": return 2.0;
			case "C-": return 1.7;
			case "D+": return 1.3;
			case "D": return 1.0;
			case "D-": return 0.7;
			case "F": return 0.0;
			default: return 0.0;
		}
	}
	
	/**
	 * Calculates GPA for a specific student.
	 * @param student The student for whom to calculate GPA
	 * @param includeForecast Whether to include forecasted grades
	 * @return The calculated GPA on a 4.0 scale
	 */
	public double calcGpaForStudent(Student student, boolean includeForecast) {
		List<StudentGrade> grades = student.getGrades();
		if (grades == null || grades.isEmpty()) {
			return 0.0;
		}

		// Group grades by course to calculate course grades first
		Map<Course, List<StudentGrade>> courseGrades = new HashMap<>();
		for (StudentGrade grade : grades) {
			if (!includeForecast && grade.isForecast()) {
				continue;
			}
			if (grade.isEmpty()) {
				continue;
			}
			
			Course course = grade.getCourse();
			if (!courseGrades.containsKey(course)) {
				courseGrades.put(course, new ArrayList<>());
			}
			courseGrades.get(course).add(grade);
		}
		
		double totalCredits = 0.0;
		double weightedSum = 0.0;
		
		// Calculate GPA using letter grades converted to points
		for (Map.Entry<Course, List<StudentGrade>> entry : courseGrades.entrySet()) {
			Course course = entry.getKey();
			List<StudentGrade> gradeList = entry.getValue();
			
			// Get numeric grade for the course
			double courseNumericGrade = getCourseNumGrade(gradeList);
			
			// Convert to letter grade
			String letterGrade = getLetterGrade(courseNumericGrade);
			
			// Convert letter grade to GPA points
			double gradePoints = letterGradeToPoints(letterGrade);
			
			double credits = course.getCourseCredits();
			
			totalCredits += credits;
			weightedSum += gradePoints * credits;
		}
		
		return totalCredits == 0 ? 0.0 : weightedSum / totalCredits;
	}
	
	/**
	 * Calculates the distribution of risk levels among students.
	 */
	public Map<String, Integer> calculateRiskLevelDistribution() {
		Map<String, Integer> distribution = new HashMap<>();
		
		for (Student student : students) {
			String riskLevel = student.getRiskLevel();
			distribution.put(riskLevel, distribution.getOrDefault(riskLevel, 0) + 1);
		}
		
		return distribution;
	}
	
	

	// Calculates the GPA for a student for the summer term
	public double calcSummerGpa(Student student, boolean includeForecast) {
		return calcGpaForTerm(student, includeForecast, "summer");
	}
	
	

	public double calcGpaForTerm(Student student, boolean includeForecast, String term) {
		
		List<StudentGrade> grades = student.getGrades();
		if (grades == null || grades.isEmpty()) {
			return 0.0;
		}

		// Group grades by course to calculate course grades first
		Map<Course, List<StudentGrade>> courseGrades = new HashMap<>();
		for (StudentGrade grade : grades) {
			if (!includeForecast && grade.isForecast()) {
				continue;
			}
			if (grade.isEmpty()) {
				continue;
			}
			
			Course course = grade.getCourse();
			// Only include courses from summer term
			if (course.getTerm().toLowerCase().contains(term)) {
				if (!courseGrades.containsKey(course)) {
					courseGrades.put(course, new ArrayList<>());
				}
				courseGrades.get(course).add(grade);
			}
		}
		
		double totalCredits = 0.0;
		double weightedSum = 0.0;
		
		// Calculate GPA using letter grades converted to points
		for (Map.Entry<Course, List<StudentGrade>> entry : courseGrades.entrySet()) {
			Course course = entry.getKey();
			List<StudentGrade> gradeList = entry.getValue();
			
			// Get numeric grade for the course
			double courseNumericGrade = getCourseNumGrade(gradeList);
			
			// Convert to letter grade
			String letterGrade = getLetterGrade(courseNumericGrade);
			
			// Convert letter grade to GPA points
			double gradePoints = letterGradeToPoints(letterGrade);
			
			double credits = course.getCourseCredits();
			
			totalCredits += credits;
			weightedSum += gradePoints * credits;
		}
		
		return totalCredits == 0 ? 0.0 : weightedSum / totalCredits;
	}
	
	
	
	// Calculates the GPA for a student for the Fall term
	public double calcFallGpa(Student student, boolean includeForecast) {
		return calcGpaForTerm(student, includeForecast, "summer");
	}

	
	// Calculates the GPA for a student for the Spring term
	public double calcSpringGpa(Student student, boolean includeForecast) {
		return calcGpaForTerm(student, includeForecast, "spring");
	}
	
	
	
	/**
	 * Derives and returns the list of all possible cohorts from the student
	 * data.   Can be used the page controllers to populate combo box pull downs.
	 * 
	 * @return
	 */
	public List<String> allCohorts() {
		
		if (this.getStudents()==null) return new LinkedList<String>();
		
		// for each student, get their cohort, remove dups, collect into list.
		return this.getStudents().stream().map(stu -> stu.getCohort()).distinct().toList();
	}
	
	/**
	 * Calculates the weighted numeric grade for a course given a list of StudentGrade objects.
	 * @param grades List of StudentGrade objects for the course
	 * @return The weighted numeric grade (0-100)
	 */
	public double getCourseNumGrade(List<StudentGrade> grades) {
		if (grades == null || grades.isEmpty()) {
			return 0.0;
		}
		
		double totalWeight = 0.0;
		double weightedSum = 0.0;
		for (StudentGrade grade : grades) {
			Assignment assignment = grade.getAssignment();
			Double value = grade.getGrade();
			if (assignment != null && value != null) {
				double weight = assignment.getWeight();
				totalWeight += weight;
				weightedSum += value * weight;
			}
		}
		
		if (totalWeight == 0.0) {
			return 0.0;
		}
		
		double weightedAverage = weightedSum / totalWeight;
		return weightedAverage;
	}
	

	/**
	 * Returns the letter grade for a course given the numeric grade.
	 * @param numericGrade The numeric grade (0-100)
	 * @return The letter grade as a String
	 */

	public String getLetterGrade(double numericGrade) {
		if (numericGrade >= 93.0) {
			return "A";
		} else if (numericGrade >= 90.0) {
			return "A-";
		} else if (numericGrade >= 87.0) {
			return "B+";
		} else if (numericGrade >= 83.0) {
			return "B";
		} else if (numericGrade >= 80.0) {
			return "B-";
		} else if (numericGrade >= 77.0) {
			return "C+";
		} else if (numericGrade >= 73.0) {
			return "C";
		} else if (numericGrade >= 70.0) {
			return "C-";
		} else if (numericGrade >= 67.0) {
			return "D+";
		} else if (numericGrade >= 63.0) {
			return "D";
		} else if (numericGrade >= 60.0) {
			return "D-";
		} else {
			return "F";
		}
	}

	
	/**
	 * Given a numeric grade,  converts to the associated grade point.
	 * 
	 * @param numericGrade
	 * @return
	 */
	public double gradePointFromGrade(double numericGrade) {
		if (numericGrade >= 93.0) {
			return 4.0;
		} else if (numericGrade >= 90.0) {
			return 3.7;
		} else if (numericGrade >= 87.0) {
			return 3.3;
		} else if (numericGrade >= 83.0) {
			return 3.0;
		} else if (numericGrade >= 80.0) {
			return 2.7;
		} else if (numericGrade >= 77.0) {
			return 2.3;
		} else if (numericGrade >= 73.0) {
			return 2.0;
		} else if (numericGrade >= 70.0) {
			return 1.7;
		} else if (numericGrade >= 67.0) {
			return 1.3;
		} else if (numericGrade >= 63.0) {
			return 1.0;
		} else if (numericGrade >= 60.0) {
			return 0.7;
		} else {
			return 0.0;
		}
	}
	
	// Getters
	public List<Student> getStudents() { return students; }
	public List<Course> getCourses() { return courses; }
	public List<StudentGrade> getGrades() { return grades; }
	public int getCurCohort() { return curCohort; }
	
	// Setter for testing
	protected static void setCurrent(Registrar r) {
		current = r;
	}

	
	
	// Calculates the GPA for a student for a specific week
	public double calcWeekGpa(Student student, int week, boolean includeForecast) {
		List<StudentGrade> grades = student.getGrades();
		if (grades == null || grades.isEmpty()) {
			return 0.0;
		}
		
		// Group grades by course to calculate course grades first
		Map<Course, List<StudentGrade>> courseGrades = new HashMap<>();
		boolean hasGradesForWeek = false;
		
		// First, collect all grades for each course for this specific week
		for (StudentGrade grade : grades) {
			if (!includeForecast && grade.isForecast()) {
				continue;
			}
			if (grade.isEmpty()) {
				continue;
			}
			
			Course course = grade.getCourse();
			Assignment assignment = grade.getAssignment();
			
			// Only include assignments from this week
			if (assignment != null && assignment.getWeek() == week) {
				if (!courseGrades.containsKey(course)) {
					courseGrades.put(course, new ArrayList<>());
				}
				courseGrades.get(course).add(grade);
				hasGradesForWeek = true;
			}
		}
		
		// If no grades for this specific week, return 0.0
		if (!hasGradesForWeek) {
			return 0.0;
		}
		
		double totalCredits = 0.0;
		double weightedSum = 0.0;
		
		// Calculate GPA for each course in this week
		for (Map.Entry<Course, List<StudentGrade>> entry : courseGrades.entrySet()) {
			Course course = entry.getKey();
			List<StudentGrade> gradeList = entry.getValue();
			
			// Get numeric grade for the course's assignments in this week
			double courseNumericGrade = getCourseNumGrade(gradeList);
			
			// Convert to letter grade
			String letterGrade = getLetterGrade(courseNumericGrade);
			
			// Convert letter grade to GPA points
			double gradePoints = letterGradeToPoints(letterGrade);
			
			// Get course credits
			double credits = course.getCourseCredits();
			
			// Add to weighted sum
			totalCredits += credits;
			weightedSum += gradePoints * credits;
		}
		
		// If no credits found for this week, return 0.0
		if (totalCredits == 0.0) {
			return 0.0;
		}
		
		// Calculate and return GPA
		return weightedSum / totalCredits;
	}
	
	/**
	 * Calculates the cumulative GPA for a student up to and including a specific week.
	 * This includes all grades from week 1 through the specified week.
	 * 
	 * @param student The student for whom to calculate the GPA
	 * @param week The week up to which to include grades (inclusive)
	 * @param includeForecast Whether to include forecasted grades
	 * @return The calculated GPA on a 4.0 scale
	 */
	public double calcCumulativeWeekGpa(Student student, int week, boolean includeForecast) {
		List<StudentGrade> grades = student.getGrades();
		if (grades == null || grades.isEmpty()) {
			return 0.0;
		}
		
		// Group grades by course to calculate course grades first
		Map<Course, List<StudentGrade>> courseGrades = new HashMap<>();
		boolean hasGradesForWeek = false;
		
		// Collect all grades for each course up to and including this week
		for (StudentGrade grade : grades) {
			if (!includeForecast && grade.isForecast()) {
				continue;
			}
			if (grade.isEmpty()) {
				continue;
			}
			
			Course course = grade.getCourse();
			Assignment assignment = grade.getAssignment();
			
			// Only include assignments up to and including this week
			if (assignment != null && assignment.getWeek() <= week) {
				if (!courseGrades.containsKey(course)) {
					courseGrades.put(course, new ArrayList<>());
				}
				courseGrades.get(course).add(grade);
				hasGradesForWeek = true;
			}
		}
		
		// If no grades up to this week, return 0.0
		if (!hasGradesForWeek) {
			return 0.0;
		}
		
		double totalCredits = 0.0;
		double weightedSum = 0.0;
		
		// Calculate GPA for each course up to this week
		for (Map.Entry<Course, List<StudentGrade>> entry : courseGrades.entrySet()) {
			Course course = entry.getKey();
			List<StudentGrade> gradeList = entry.getValue();
			
			// Get numeric grade for the course's assignments up to this week
			double courseNumericGrade = getCourseNumGrade(gradeList);
			
			// Convert to letter grade
			String letterGrade = getLetterGrade(courseNumericGrade);
			
			// Convert letter grade to GPA points
			double gradePoints = letterGradeToPoints(letterGrade);
			
			// Get course credits
			double credits = course.getCourseCredits();
			
			// Add to weighted sum
			totalCredits += credits;
			weightedSum += gradePoints * credits;
		}
		
		// If no credits found up to this week, return 0.0
		if (totalCredits == 0.0) {
			return 0.0;
		}
		
		// Calculate and return GPA
		return weightedSum / totalCredits;
	}
	
	// Returns a list of all weeks in the academic year
	public List<Integer> getAllWeeks() {
		// Get the maximum week number from course data
		int maxWeek = 0;
		for (Course course : courses) {
			for (Assignment assignment : course.getAssignments()) {
				if (assignment.getWeek() > maxWeek) {
					maxWeek = assignment.getWeek();
				}
			}
		}
		
		// Generate list of weeks from 1 to maxWeek
		List<Integer> weeks = new ArrayList<>();
		for (int i = 1; i <= maxWeek; i++) {
			weeks.add(i);
		}
		return weeks;
	}
	
	// Returns a mapping of week numbers to their associated term
	public Map<Integer, String> getWeekToTermMapping() {
		Map<Integer, String> weekToTerm = new HashMap<>();
		
		// Summer term: weeks 1-4
		for (int i = 1; i <= 4; i++) {
			weekToTerm.put(i, "Summer 2024");
		}
		
		// Fall term: weeks 5-8
		for (int i = 5; i <= 8; i++) {
			weekToTerm.put(i, "Fall 2024");
		}
		
		// Spring term: weeks 9-12
		for (int i = 9; i <= 12; i++) {
			weekToTerm.put(i, "Spring 2025");
		}
		
		return weekToTerm;
	}
	
	// Get a list of weekly GPAs for a student across all weeks
	public List<Double> getWeeklyGpas(Student student, boolean includeForecast) {
		// We have 12 weeks total (4 Summer, 4 Fall, 4 Spring)
		int totalWeeks = 12;
		List<Double> weeklyGpas = new ArrayList<>();
		
		// First, check which weeks have assignments for this student
		Map<Integer, Boolean> weeksWithAssignments = new HashMap<>();
		
		for (StudentGrade grade : student.getGrades()) {
			if (grade.getAssignment() != null) {
				int assignmentWeek = grade.getAssignment().getWeek();
				weeksWithAssignments.put(assignmentWeek, true);
			}
		}
		
		// Calculate GPA for each week from 1 to 12
		for (int week = 1; week <= totalWeeks; week++) {
			// Skip weeks with no assignments
			if (!weeksWithAssignments.containsKey(week)) {
				weeklyGpas.add(0.0);
				continue;
			}
			
			// Calculate the cumulative GPA up to and including this week
			double gpa = calcCumulativeWeekGpa(student, week, includeForecast);
			weeklyGpas.add(gpa);
		}
		
		return weeklyGpas;
	}

	/**
	 * This method looks at the current date and derives the term.  
	 * 
	 * @return
	 */
	public String getCurrentTerm() {
		// Derive this from current date
		java.time.Month month = java.time.LocalDate.now().getMonth();
		int year = java.time.LocalDate.now().getYear();
		
		String prefix = switch (month) {
			case FEBRUARY, MARCH, APRIL, MAY -> "Spring ";
			case JUNE, JULY, AUGUST -> "Summer ";
			default -> "Fall ";
		};
		
		return prefix + String.valueOf(year);
	}


	/**
	 * Yet another way to compute the live term gpa for a specific student for a specific
	 * term based on their grades.  May include forecast grades if needed.
	 * 
	 * @param s  a handle on the current student
	 * @param term  a specific term (like FA25)
	 * @param includeForecast should we included forecasted grades
	 * @return
	 */
	public double calcTermGpa(Student s, String term, boolean includeForecast) {
		
		// narrow grades to only those in the specified term for the specified student
		// earned 
		List<StudentGrade> grades = s.getGrades().stream()
				.filter(sg->sg.getCourse().getTerm().equals(term))
				.filter(sg-> (sg.isEarned()) || (sg.isForecast()&&includeForecast) )
				.toList();
		
		// at very start of the term, might be all empty grades, no term gpa
		if (grades.isEmpty()) return 0.0;
		
		//System.err.println("Grades for "+s.getFirstName()+" "+s.getLastName());
		//grades.stream().forEach(System.err::println);  // debug trace
		
		// Derive the list of courses for the current student
		List<Course> courses = grades.stream()
				.map(sg -> sg.getCourse())
				.distinct()
				.toList();
		
		// courses.stream().forEach(System.err::println); debug trace
		
		double totalCredit = 0.0;
		double weightedSum = 0.0;
		
		for (Course crs: courses) {

			List<StudentGrade> crsGrades = grades.stream().filter(sg->sg.getCourse()==crs).toList();
			
			// calculate the current grade for the course based on our filtered down students grade
			// for the current course
			Optional<Double> crsGrade = crs.calcCourseGrade(crsGrades);
			
			if (crsGrade.isPresent()) {
				double credit = crs.getCourseCredits();
				totalCredit += credit;
				weightedSum += credit*crsGrade.orElse(0.0);
			}
		}

		// taking care to handle case where no assignments have an earned
		// grade.  this should be impossible given early logic after filtering above
		if (totalCredit == 0.0) return 0.0;

		// compute grade (percentage) and map to grade point.
		return this.gradePointFromGrade(weightedSum/totalCredit); 
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
