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
	@SuppressWarnings("javadoc")
	public static class Strings {
		public static final String description = "Select for each type of tranformation suggestion if it will be shown\n\n";
	}

	/**
	 * Preferences page layout strings such as combo-boxes
	 */
	@SuppressWarnings("javadoc")
	public static class Layout {
		public static final String[][] optBothLiterals = {
				{ RepositionLiterals, RepositionLiterals },
				{ DoNotRepositionLiterals, DoNotRepositionLiterals } };

		public static final String[][] optRightLiteral = {
				{ RepositionAllRightLiterals, RepositionAllRightLiterals },
				{ RepositionAllButBoolAndNull, RepositionAllButBoolAndNull },
				{ DoNotRepositionRightLiterals, DoNotRepositionRightLiterals } };

	}

	/**
	 * Strings that represent the chosen option for each repositioning option of ShortestOperandFirst
	 */
	public static final String ComboBothLiterals = "Reposition operand literals";
	@SuppressWarnings("javadoc")
	public static final String ComboRightLiterals = "Reposition right-operand literals";
	@SuppressWarnings("javadoc")
	public static final String RepositionAllRightLiterals = "Reposition all right literals";
	@SuppressWarnings("javadoc")
	public static final String RepositionAllButBoolAndNull = "Reposition all except boolean and null literals";
	@SuppressWarnings("javadoc")
	public static final String DoNotRepositionRightLiterals = "Do not reposition right operand literals";
	@SuppressWarnings("javadoc")
	public static final String RepositionLiterals = "Reposition literals";
	@SuppressWarnings("javadoc")
	public static final String DoNotRepositionLiterals = "Do not reposition literals at all";
}
