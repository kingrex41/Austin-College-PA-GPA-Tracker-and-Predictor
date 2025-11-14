package pagpa.cs380.Models;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Represents a course --> with term, num, name/title, credits, and graded assignments.
 * as shown in the UML diagram
 */

// priv variables needed for the course class --> 
public class Course {
    private String term;       		// like FA25
    private String courseNum;  		// like CS*120
    private String courseName; 		// Intermediate Computer Program
    private double courseCredits;   // like 0.75 or 1.0
    private String instructor;		
    
    private List<Assignment> assignments;  // course grades are based on a collection of assignments

    /**
     * Constructs a new Course.
     *
     * @param term --> the academic term (ex for us it would be "class of 2026" or "2026"; would differ in the future)
     * @param courseName --> the name of the course
     * @param courseCredits --> the number of credits the course is worth (the weight of the course)
     * @param assignments --> the list of assignments the course has
     * @param courseCode --> course code as in PAED 520, used for identification
     * @param teacher --> unique name of instructor teaching the course
     */
    
    public Course(String term, String courseNum, String courseName, double courseCredits, String teacher, Assignment ... assigns ) {
        this.term = term;
        this.courseNum = courseNum;
        this.courseName = courseName;
        this.courseCredits = courseCredits;
        this.assignments = new LinkedList<Assignment>();
        this.instructor = teacher;        
        
        this.assignments.addAll(List.of(assigns));  // if provided, populate list of assignments too
    
    }

    /**
     * Calculates the final grade for the course using a weighted average
     *
     * @param grades --> the list of student grades for this course
     * the following returns the weighted average grade, or empty if no valid grades
     */
    
    public Optional<Double> calcCourseGrade(List<StudentGrade> grades) {
        if (grades == null || grades.isEmpty()) {
            return Optional.empty();
        }

        double totalWeight = 0.0;
        double weightedSum = 0.0;

        for (StudentGrade grade : grades) {
            Assignment assignment = grade.getAssignment();
            Double value = grade.getGrade();

            // Checking if grade or assignment is null
            if (assignment != null && value != null) {
                double weight = assignment.getWeight();
                totalWeight += weight;
                weightedSum += value * weight;
            }
        }

        if (totalWeight == 0.0) {
            return Optional.empty();
        }

        return Optional.of(weightedSum / totalWeight);
    }

    // Getters
    public String getTerm() {
        return term;
    }

    public String getCourseName() {
        return courseName;
    }

    public double getCourseCredits() {
        return courseCredits;
    }

    public List<Assignment> getAssignments() {
        return assignments;
    }

	public String getCourseNum() {
		return courseNum;
	}
	
	/* setter methods for testing only (not public) */

	/**
	 * for testing only
	 */
	protected void setTerm(String term) {
		this.term = term;
	}

	/**
	 * for testing only
	 */
	protected void setCourseNum(String courseNum) {
		this.courseNum = courseNum;
	}

	/**
	 * for testing only
	 */
	protected void setCourseName(String courseName) {
		this.courseName = courseName;
	}

	/**
	 * for testing only
	 */
	protected void setCourseCredits(double courseCredits) {
		this.courseCredits = courseCredits;
	}

	/**
	 * for testing only
	 */
	protected void setAssignments(List<Assignment> assignments) {
		this.assignments = assignments;
	}


	public String getInstructor() {
		return instructor;
	}

	/**
	 * for testing only
	 */
	protected void setInstructor(String instructor) {
		this.instructor = instructor;
	}
}

