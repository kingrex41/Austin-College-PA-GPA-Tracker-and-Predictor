package pagpa.cs380.Utils;

import pagpa.cs380.Models.Grade;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Utility class for reading grade data from CSV files.
 * Matches the UML diagram exactly.
 */
public class GradeCsvReader {
    private static final String GRADES_PATH = "/pagpa/cs380/Data/mockGrades_new.csv";

    /**
     * Reads grade data from the CSV file.
     * @return Map of student IDs to their list of grades
     */
    public Map<String, List<Grade>> readGradeData() {
        Map<String, List<Grade>> gradeData = new HashMap<>();
        
        try (InputStream is = getClass().getResourceAsStream(GRADES_PATH);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
             
            if (is == null) {
                return gradeData;
            }
            
            // Read header line
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return gradeData;
            }
            
            // Process each line in the CSV
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue; // Skip empty lines
                }
                
                String[] data = line.split(",");
                if (data.length < 6) { // Need at least id, term, courseNum, assignID, status, grade
                    continue;
                }
                
                // Parse data according to new format
                String studentId = data[0].trim();
                String term = data[1].trim();
                String courseNum = data[2].trim();
                String assignId = data[3].trim();
                String status = data[4].trim();
                
                // Only process earned grades
                if ("earned".equalsIgnoreCase(status)) {
                    try {
                        double gradeValue = Double.parseDouble(data[5].trim());
                        String gradeDate = data.length > 6 && !data[6].trim().isEmpty() ? data[6].trim() : "2024-01-01";
                        
                        // Create the assignment name in the format expected by other code: "PAED 520 Foundations of Medicine - Exam #1"
                        String assignmentName = courseNum + " - " + assignId;
                        
                        // Create the grade
                        double weight = 1.0; // Default weight
                        Grade grade = new Grade(gradeValue, weight, gradeDate, assignmentName);
                        
                        // Add to the map
                        if (!gradeData.containsKey(studentId)) {
                            gradeData.put(studentId, new ArrayList<>());
                        }
                        gradeData.get(studentId).add(grade);
                    } catch (NumberFormatException e) {
                        // Skip grades that can't be parsed
                    }
                }
            }
            
        } catch (IOException e) {
            // Log exception in production code
        }
        
        return gradeData;
    }
} 