package pagpa.cs380.Models;

import java.util.LinkedList;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import pagpa.cs380.Utils.Registrar;
import pagpa.cs380.Utils.SuccessManager;

/**
 * Represents a student as shown in the UML diagram.
 * A virtual attribute (like gpa) is not a physical instance variable but still has a similar getter
 * method. The getter method calls a computational method to derive the current value.
 */
public class Student {
	
	//line to test

	// variables used in constructor
	private int acid;  // AC ID #
	private String firstName;  
	private String lastName;   
	private String advisor;
	private String phone;
	private String cohort; //keep as string or else the drop down on all students does not allow "All Students"
	private List<StudentGrade> grades;
	private boolean dismissed;
	
	private final BooleanProperty selected = new SimpleBooleanProperty(false);
	
	//variables not used on constructor
	//private double gpa;
	//private double liveGpa;
	
	//private String riskLevel;

	/**
     * Constructs the Grade class with the given parameters --> 
     * 
     * @param acid --> the student's Austin College ID number
     * @param firstName --> the first name of the student
     * @param lastName --> the last name of student
     * @param advisor --> the name of the student's academic coach
     * @param phone --> the phone number of the student
     * @param cohort --> the cohort the student is in - (ex. class of 2026;2026 for the current one)
     * @param dismissed --> true or false --> for whether the student is dismissed or a current student
     * 
     */
	
	public Student(int acid, String firstName, String lastName, String advisor, String phone, String cohort, boolean dismissed) {
		 this.acid = acid;	 
		 this.firstName = firstName;
		 this.lastName = lastName;
		 this.advisor = advisor;
		 this.phone = phone;
		 this.cohort = cohort;
		 this.dismissed = dismissed;
		 
		 grades = new LinkedList<StudentGrade>();
 }

/*
 * GETTERS
 */
	 // Getters for variables in constructor
	 public int getAcid() { return acid; }
	 public String getFirstName() { return firstName; }
	 public String getLastName() { return lastName; }
	 public String getAdvisor() { return advisor; }
	 public String getPhone() { return phone; }
	 public String getCohort() { return cohort; }
	 public boolean isDismissed() { return dismissed; }
	 public List<StudentGrade> getGrades() { return grades; }
	 
	 
	 public double getGpa() { return this.calcGpa(false); }
	 public double getLiveGpa() { return this.calcGpa(true); }
	 public String getRiskLevel() {
		 
		 SuccessManager mgr = new SuccessManager();
		 
		 return mgr.studentStanding(this).toString();
		 
		 }
	 
	 // getters needed for check box in All Students view
	 public BooleanProperty selectedProperty() { return selected; }
	 public boolean isSelected() { return selected.get(); }
	 public void setSelected(boolean selected) { this.selected.set(selected); }

    /*
	 * calcGpa calculates gpa based on
	 * whether forecasted grades are being used, etc. 
	 */
	public double calcGpa(boolean includeForecast) {
		if (grades == null || grades.isEmpty()) {
			System.err.println("no grades found for student with ACID: " + this.getAcid());
			return 0.0;
		}

	return Registrar.getCurrent().calcGpaForStudent(this, includeForecast);
}
	
/*
 * SETTERS
 */
public void setGrades(List<StudentGrade> grades) { this.grades = grades; }

public void setFirstName(String string) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'setFirstName'");
}

public void setGpa(double d) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'setGpa'");
}

public void setAdvisor(String string) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'setAdvisor'");
}

public void setPhone(String string) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'setPhone'");
}

public void setCohort(String string) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'setCohort'");
}

public void setDismissed(boolean dismissed) {
	this.dismissed = dismissed;
}

@Override
public String toString() {
	return "Student [acid=" + acid + ", firstName=" + firstName + ", lastName=" + lastName + ", advisor=" + advisor
			+ ", phone=" + phone + ", cohort=" + cohort + ", dismissed=" + dismissed
			+ ", selected=" + selected + ", gpa=" + this.getGpa() + ", liveGpa=" + this.getLiveGpa() + ", riskLevel=" + this.getRiskLevel() + "]";
}

}
