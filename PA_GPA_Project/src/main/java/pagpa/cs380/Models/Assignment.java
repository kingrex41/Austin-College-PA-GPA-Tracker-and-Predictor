package pagpa.cs380.Models;

/**
 * Represents a specific assignment in a course, as shown in the UML diagram.
 */


// priv variables needed for the assignment class 
public class Assignment {
    private String assignmentId;
    private int week;
    private double weight;
    private boolean exam;
    private boolean key;

    /**
     * Constructs the Assignment class with the given parameters --> 
     * (for documentation/readability sake let us do this for each class)
     * 
     * @param assignmentId --> identification for the assignment
     * @param week --> week the assignment is completed
     * @param weight --> the weight of the assignment in the course grade
     * @param exam --> true if the assignment is an exam
     * @param key --> true if the assignment is a key assignment
     */
    
    public Assignment(String assignmentId, int week, double weight, boolean exam, boolean key) {
        this.assignmentId = assignmentId;
        this.week = week;
        this.weight = weight;
        this.exam = exam;
        this.key = key;
    }
    
    /**
     * Default constructor useful for testing and some other frameworks.
     */
    public Assignment() {
    }
    
   

    // Getters for each param as listed above
    public String getAssignmentId() {
        return assignmentId;
    }

    public int getWeek() {
        return week;
    }

    public double getWeight() {
        return weight;
    }

    public boolean isExam() {
        return exam;
    }

    public boolean isKey() {
        return key;
    }

    /**
     * Method that returns true if the assignment is either an exam or a key assignment.
     */
    public boolean isExamOrKey() {
        return exam || key;
    }
}

