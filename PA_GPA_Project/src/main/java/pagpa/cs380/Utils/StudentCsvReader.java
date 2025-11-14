package pagpa.cs380.Utils;
import pagpa.cs380.Models.Course;
import pagpa.cs380.Models.Student;
import pagpa.cs380.Models.Assignment;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StudentCsvReader {
	
	private static final String STUDENT_INFO_PATH =  "/pagpa/cs380/Data/studentData.csv";
	
	List<Student> readStudentsOfCohort (){
		
		List<Student> students = new ArrayList<>();
		
        try (InputStream is = getClass().getResourceAsStream(STUDENT_INFO_PATH);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
               
               // Skip header
               reader.readLine();
               
               String line;
               while ((line = reader.readLine()) != null) {
                   String[] data = line.split(",");
                   if (data.length >= 6) {
                	   int acId = Integer.parseInt(data[0].trim());;
                       String firstName = data[1].trim();
                       String lastName = data[2].trim();
                       String advisor = data[3].trim();
                       String phone = data[4].trim();
                       String cohort = data[5].trim();
                       String dismissedStr = data[6].trim().toUpperCase();
                     
                       students.add(new Student(acId, firstName, lastName, advisor, phone, cohort, "Y".equals(dismissedStr)));
                   }
               }
		
		
	} catch (NumberFormatException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
        return students;
		
	}
}


