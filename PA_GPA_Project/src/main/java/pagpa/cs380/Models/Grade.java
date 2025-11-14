package pagpa.cs380.Models;

public class Grade {

    private Double grade;
    private Double gradeWeight;
    private String gradeDate;
    private String gradeType;
    
    /**
     * Constructs the Grade class with the given parameters --> 
     * 
     * @param grade --> the grade earned on the assignment 
     * @param gradeWeight --> the weight of the grade in regard to other assignments
     * @param gradeDate --> date the grade was assigned
     * @param gradeType --> type of grade (exam, assignment, etc - double chk meaning)
     */

    public Grade(Double grade, Double gradeWeight, String gradeDate, String gradeType) {
        this.grade = grade;
        this.gradeWeight = gradeWeight;
        this.gradeDate = gradeDate;
        this.gradeType = gradeType;
    }



    public Double getGrade() {
        return grade;
    }

    public Double getGradeWeight() {
        return gradeWeight;
    }

    public String getGradeDate() {
        return gradeDate;
    }

    public String getGradeType() {
        return gradeType;
    }

}
