package pagpa.cs380;

import pagpa.cs380.Models.Student;
import pagpa.cs380.Utils.Registrar;

import java.util.List;

/**
 * Simple test class to print out term GPAs for all students
 */
public class GpaTest {
    
    public static void main(String[] args) {
        // Get the Registrar instance
        Registrar registrar = Registrar.getCurrent();
        
        // Get all students
        List<Student> students = registrar.getStudents();
        
        System.out.println("===========================================");
        System.out.println("STUDENT GPA REPORT");
        System.out.println("===========================================");
        
        // Print GPA information for each student
        for (Student student : students) {
            System.out.println("\nStudent: " + student.getFirstName() + " " + student.getLastName() + 
                               " (ID: " + student.getAcid() + ")");
            System.out.println("-------------------------------------------");
            
            // Calculate GPAs
            double overallGpa = registrar.calcGpaForStudent(student, false);
            double summerGpa = registrar.calcSummerGpa(student, false);
            double fallGpa = registrar.calcFallGpa(student, false);
            double springGpa = registrar.calcSpringGpa(student, false);
            
            // Print formatted GPA values
            System.out.printf("Overall GPA: %.2f\n", overallGpa);
            System.out.printf("Summer GPA:  %.2f\n", summerGpa);
            System.out.printf("Fall GPA:    %.2f\n", fallGpa);
            System.out.printf("Spring GPA:  %.2f\n", springGpa);
        }
        
        System.out.println("\n===========================================");
    }
} 