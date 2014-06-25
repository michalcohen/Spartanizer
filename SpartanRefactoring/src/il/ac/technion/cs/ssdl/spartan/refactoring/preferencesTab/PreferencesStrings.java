package il.ac.technion.cs.ssdl.spartan.refactoring.preferencesTab;

/**
 * @author Ofir Elmakias <code><elmakias [at] outlook.com></code> (original /
 *         2014/6/21)
 * @author Tomer Zeltzer <code><tomerr90 [at] gmail.com></code> (original /
 *         2014/6/21)
 * @since 2014/6/21
 * 
 *        Manages strings for preferences page
 */
public final class PreferencesStrings {
	/**
	 * Preferences page strings
	 */
	@SuppressWarnings("javadoc") public static class Strings {
		public static final String description = "Select for each type of tranformation suggestion if it will be shown\n\n";
	}

	/**
	 * Strings for Shortest Operand First positioning options
	 */
	public static final String atEnd = " at End";
	@SuppressWarnings("javadoc") public static final String atStart = " at Start";
	@SuppressWarnings("javadoc") public static final String Shortest = "By Shortest Length";

	/**
	 * Strings Null positioning options
	 */
	public static final String NullAtEnd = "Null" + atEnd;
	@SuppressWarnings("javadoc") public static final String NullAtStart = "Null" + atStart;
	@SuppressWarnings("javadoc") public static final String[][] NullOptions = { { NullAtEnd, NullAtEnd },
			{ NullAtStart, NullAtStart }, { Shortest, Shortest } };

	/**
	 * Strings for Boolean Literals positioning options
	 */
	public static final String BoolAtEnd = "Boolean Literal" + atEnd;
	@SuppressWarnings("javadoc") public static final String BoolAtStart = "Boolean Literal" + atStart;
	@SuppressWarnings("javadoc") public static final String[][] BoolOptions = { { BoolAtEnd, BoolAtEnd },
			{ BoolAtStart, BoolAtStart }, { Shortest, Shortest } };

	/**
	 * Strings for Numeric Literals positioning options
	 */
	public static final String NumAtEnd = "Numeric Literal" + atEnd;
	@SuppressWarnings("javadoc") public static final String NumAtStart = "Numeric Literal" + atStart;
	@SuppressWarnings("javadoc") public static final String[][] NumOptions = { { NumAtEnd, NumAtEnd },
			{ NumAtStart, NumAtStart }, { Shortest, Shortest } };
}
