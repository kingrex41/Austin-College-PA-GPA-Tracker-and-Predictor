package pagpa.cs380.Models;

/**
 * Represents the various standing states by which a PA student
 * might be characterized.   See Java's enumerated type.
 */
public enum SuccessStanding {

	DISMISSED("Dismissed"),
	PROBATION("Academic Probation"),
	RISK("Close Monitoring"),
    GOOD("Meets Expectations"),
    UNKNOWN ("Unknown Standing");   // should not be possible!
	
	private final String displayName;
	
	SuccessStanding(String dname) {
		this.displayName = dname;
	}
	
	@Override
	public String toString() {
		return displayName;
	}
}
