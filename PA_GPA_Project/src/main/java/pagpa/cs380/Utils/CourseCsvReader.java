package pagpa.cs380.Utils;

import pagpa.cs380.Models.Course;
import pagpa.cs380.Models.Assignment;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for reading course data from CSV files.
 * Matches the UML diagram exactly.
 */
public class CourseCsvReader {
    private static final String COURSE_DATA_PATH = "/pagpa/cs380/Data/course_credits.csv";
    private static final String COURSE_INFO_PATH = "/pagpa/cs380/Data/course_info.csv";

    /**
     * Reads course data from the CSV file.
     * @return List of Course objects
     */
    public List<Course> readCourseData() {
        List<Course> courses = new ArrayList<>();
        
        try (InputStream is = getClass().getResourceAsStream(COURSE_DATA_PATH);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            
            // Skip header --
            reader.readLine();
            
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 5) {
                    String term = data[0];
                    String courseCode = data[1];
                    String courseTitle = data[2];
                    double credits = Double.parseDouble(data[3]);
                    String professor = data[4];
                    List<Assignment> assignments = new ArrayList<>(); 
                    
                    courses.add(new Course(term, courseCode, courseTitle, credits, professor));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading course data: " + e.getMessage());
        }
        
        return courses;
    }
    
    /**
     * Reads assignment data from the course_info.csv file.
     * @return Map of course IDs to lists of Assignment objects
     */
    public Map<String, List<Assignment>> readAssignmentData() {
        Map<String, List<Assignment>> assignmentData = new HashMap<>();
        
        try (InputStream is = getClass().getResourceAsStream(COURSE_INFO_PATH);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            
            if (is == null) {
                System.err.println("Could not find course info file: " + COURSE_INFO_PATH);
                return assignmentData;
            }
            
            // Skip header line
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return assignmentData;
            }
            
            // Process each line
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 7) {
                    String courseId = data[0].trim();
                    String courseName = data[1].trim();
                    String assignmentType = data[2].trim();
                    double weight = Double.parseDouble(data[3].trim());
                    boolean isExam = Boolean.parseBoolean(data[4].trim());
                    boolean isKey = Boolean.parseBoolean(data[5].trim());
                    int week = Integer.parseInt(data[6].trim());
                    
                    // Create the assignment
                    Assignment assignment = new Assignment(assignmentType, week, weight, isExam, isKey);
                    
                    // Add to the map
                    if (!assignmentData.containsKey(courseId)) {
                        assignmentData.put(courseId, new ArrayList<>());
                    }
                    assignmentData.get(courseId).add(assignment);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading assignment data: " + e.getMessage());
        }
        
        return assignmentData;
    }
} 