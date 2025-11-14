package pagpa.cs380.Models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a student's grade in a course, including the course, assignment, and grade details.
 */
public class StudentGrade {
	
	public static final String GRADE_EMPTY = "empty";
	public static final String GRADE_FORECAST = "forecast";
	public static final String GRADE_EARNED = "earned";
	
	
    private Student student;
    private Course course;
    private Assignment assignment;
    private double grade;
    private LocalDate gradeDate;
    private String gradeStatus;
    private List<RemGrade> remGrades;
    
    /**
     * Constructs the Student Grade class with the given parameters --> 
     * 
     * @param student --> the student's name
     * @param course --> the name of the course
     * @param assignment --> assignment name
     * 
     * 
     */
    
    public StudentGrade(Student student, Course course, Assignment assignment) {
        this.student = student;
        this.course = course;
        this.assignment = assignment;
        this.remGrades = new ArrayList<>();
    }
    
    
    /**
     * @return true if current student grade is earned
     */
    public boolean isEarned() {
    	return GRADE_EARNED.equals(this.getGradeStatus());
    }

    /**
     * @return true if current student grade is a forecast grade
     */
    public boolean isForecast() {
    	return GRADE_FORECAST.equals(this.getGradeStatus());
    }
    
    /**
     * @return true if current student grade is currently empty
     */
    public boolean isEmpty() {
    	return GRADE_EMPTY.equals(this.getGradeStatus());
    }

    
    /**
     * Adds a remediation grade with the specified details.
     */
    public void addRemediation(double grade, LocalDate date, String note) {
        RemGrade remGrade = new RemGrade(grade, date, note);
        remGrades.add(remGrade);
    }

    // Getters and setters
    public Student getStudent() { return student; }
    public Course getCourse() { return course; }
    public Assignment getAssignment() { return assignment; }
    public double getGrade() { return grade; }
    public LocalDate getGradeDate() { return gradeDate; }
    public String getGradeStatus() { return gradeStatus; }
    public List<RemGrade> getRemGrades() { return remGrades; }

    public void setGrade(double grade) { this.grade = grade; }
    public void setGradeDate(LocalDate gradeDate) { this.gradeDate = gradeDate; }
    public void setGradeStatus(String gradeStatus) { this.gradeStatus = gradeStatus; }


	@Override
	public String toString() {
		return "StudentGrade [student=" + student.getAcid() + ", course=" + course.getCourseNum() + ", assignment=" + assignment.getAssignmentId() + ", grade="
				+ grade + ", gradeDate=" + gradeDate + ", gradeStatus=" + gradeStatus + "]";
	}
    
    
}

