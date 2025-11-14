package pagpa.cs380;

/**
 * All controllers in our app should extend this class giving our
 * controllers a second phase of initialization useful when switching
 * between pages.
 */
public abstract class PaController {

	public void finishInit() {
		// the default is to do nothing.  Our controllers can override
		// if some computation is needed.
		System.err.println("no final initializagtion ");
	}
}
