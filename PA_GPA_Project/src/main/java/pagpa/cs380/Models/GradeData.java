package pagpa.cs380.Models;

/**
 * Represents grade data read from CSV files.
 * This class is used as an intermediate representation before converting to StudentGrade objects.
 */
public class GradeData {
    private String studentId;
    private String courseCode;
    private String assignmentType;
    private String gradeType;
    private String gradeValue;

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getAssignmentType() { return assignmentType; }
    public void setAssignmentType(String assignmentType) { this.assignmentType = assignmentType; }

    public String getGradeType() { return gradeType; }
    public void setGradeType(String gradeType) { this.gradeType = gradeType; }

    public String getGradeValue() { return gradeValue; }
    public void setGradeValue(String gradeValue) { this.gradeValue = gradeValue; }
} 