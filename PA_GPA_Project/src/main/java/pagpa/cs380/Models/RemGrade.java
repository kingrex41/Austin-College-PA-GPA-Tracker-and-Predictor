package pagpa.cs380.Models;

import java.time.LocalDate;

/**
 * Represents a remediation grade for a student's assignment.
 * This is a separate class as shown in the UML diagram.
 */
public class RemGrade {
    private Double gradeValue;
    private LocalDate gradeDate;
    private String gradeNote;

    public RemGrade(Double gradeValue, LocalDate gradeDate, String gradeNote) {
        this.gradeValue = gradeValue;
        this.gradeDate = gradeDate;
        this.gradeNote = gradeNote;
    }

    // Getters
    public Double getGradeValue() { return gradeValue; }
    public LocalDate getGradeDate() { return gradeDate; }
    public String getGradeNote() { return gradeNote; }
} 