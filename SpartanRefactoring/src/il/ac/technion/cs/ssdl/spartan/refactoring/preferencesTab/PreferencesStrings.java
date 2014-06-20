package il.ac.technion.cs.ssdl.spartan.refactoring.preferencesTab;

/**
 * @author OFIR
 *
 *         Manages strings for preferences page
 */
public final class PreferencesStrings {
	/**
	 * Preferences page strings
	 */
	@SuppressWarnings("javadoc")
	public static class Strings {
		public static final String description = "Select for each tranformation suggestion if they will be shown\n\n";
	}

	/**
	 * Preferences page layout strings such as combo-boxes
	 */
	@SuppressWarnings("javadoc")
	public static class Layout {
		public static final String[][] optBothLiterals = {
			{ Options.RepositionLiterals, Options.RepositionLiterals },
			{ Options.DoNotRepositionLiterals,
				Options.DoNotRepositionLiterals } };

		public static final String[][] optRightLiteral = {
			{ Options.RepositionAllRightLiterals,
				Options.RepositionAllRightLiterals },
				{ Options.RepositionAllButBoolAndNull,
					Options.RepositionAllButBoolAndNull },
					{ Options.DoNotRepositionRightLiterals,
						Options.DoNotRepositionRightLiterals } };

	}

	/**
	 * Option strings for preferences page
	 */
	@SuppressWarnings("javadoc")
	public static class Options {
		public static final String ComboBothLiterals = "Reposition operand literals";
		public static final String ComboRightLiterals = "Reposition right-operand literals";
		public static final String RepositionAllRightLiterals = "Reposition all right literals";
		public static final String RepositionAllButBoolAndNull = "Reposition all except boolean and null literals";
		public static final String DoNotRepositionRightLiterals = "Do not reposition right operand literals";
		public static final String RepositionLiterals = "Reposition literals";
		public static final String DoNotRepositionLiterals = "Do not reposition literals at all";
	}
}
