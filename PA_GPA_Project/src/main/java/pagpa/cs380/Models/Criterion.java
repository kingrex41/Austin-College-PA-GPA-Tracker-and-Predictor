package pagpa.cs380.Models;

import java.util.function.Predicate;

import pagpa.cs380.Utils.SuccessManager;

/**
 * An instance of this class encapsulates a single criteria
 * that can be tested.   It is useful in representing the criteria
 * for establishing a students success standing.
 * 
 * @param <T>
 */
public class Criterion<T>  {
	
	/**
	 * Convenience factory method to create a new criterion instance based on a student.
	 * Use this when you want a new criteria to be evaluated against a student.
	 * 
	 * @see SuccessManager
	 * 
	 * @param str
	 * @param spred
	 * @return
	 */
	public static Criterion<Student> studentCriterion(String str, Predicate<Student> spred ) {
		return new Criterion<Student>(str,spred);
	}
	
	
	Predicate<T> criterion;
	String title;
	
	public Criterion( String displayStr, Predicate<T> pred) {
		this.criterion = pred;
		this.title = displayStr;
	}
	
	public boolean test( T subject) {
		return criterion.test(subject );
	}

	public Predicate<T> getCriterion() {
		return criterion;
	}

	public void setCriterion(Predicate<T> criterion) {
		this.criterion = criterion;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String toString() {
		return this.title;
	}
	
	

}
