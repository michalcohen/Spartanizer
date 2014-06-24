package il.ac.technion.cs.ssdl.spartan.refactoring;

import il.ac.technion.cs.ssdl.spartan.refactoring.ShortestOperand.RepositionLiterals;
import il.ac.technion.cs.ssdl.spartan.refactoring.ShortestOperand.RepositionRightLiteral;
import il.ac.technion.cs.ssdl.spartan.refactoring.preferencesTab.PreferencesFile;
import il.ac.technion.cs.ssdl.spartan.refactoring.preferencesTab.PreferencesStrings.Options;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Boris van Sosin <code><boris.van.sosin [at] gmail.com></code> (v2)
 * @author Ofir Elmakias <code><elmakias [at] outlook.com></code> (original /
 *         30.05.2014) (v3)
 * @author Tomer Zeltzer <code><tomerr90 [at] gmail.com></code> (original /
 *         30.05.2014) (v3)
 * @since 2013/07/01
 */
public enum All {
	@SuppressWarnings("javadoc")
	ComparisonWithBoolean(new ComparisonWithBoolean()), //
	@SuppressWarnings("javadoc")
	ForwardDeclaration(new ForwardDeclaration()), //
	@SuppressWarnings("javadoc")
	InlineSingleUse(new InlineSingleUse()), //
	@SuppressWarnings("javadoc")
	RenameReturnVariableToDollar(new RenameReturnVariableToDollar()), //
	@SuppressWarnings("javadoc")
	ShortestBranchFirst(new ShortestBranchFirst()), //
	@SuppressWarnings("javadoc")
	ShortestOperand(new ShortestOperand()), //
	@SuppressWarnings("javadoc")
	Ternarize(new Ternarize()), //
	;

	private final Spartanization value;

	private All(final Spartanization value) {
		this.value = value;
	}

	/**
	 * @return Spartanization class rule instance
	 */
	public Spartanization value() {
		return value;
	}

	/**
	 * @param c
	 *            Spartanization rule
	 * @return Spartanization class rule instance
	 */
	@SuppressWarnings("unchecked")
	//
	public static <T extends Spartanization> T findInstance(
			final Class<? extends T> c) {
		for (final All a : All.values()) {
			final Spartanization $ = a.value();
			if ($.getClass().equals(c))
				return (T) $;
		}
		return null;
	}

	/**
	 * @return Iteration over all Spartanization class instances
	 */
	public static Iterable<Spartanization> allAvailableSpartanizations() {
		return new Iterable<Spartanization>() {
			@Override
			public Iterator<Spartanization> iterator() {
				return new Iterator<Spartanization>() {
					int next;

					@Override
					public boolean hasNext() {
						return next < values().length;
					}

					@Override
					public Spartanization next() {
						return values()[next++].value();
					}

					@Override
					public void remove() {
						return;
					}
				};
			}
		};
	}

	private static String ignoreRuleStr = "false";

	private static final Map<String, Spartanization> all = new HashMap<>();

	private static void put(final Spartanization s) {
		all.put(s.toString(), s);
	}

	private static boolean ignored(final String sparta) {
		return 0 <= sparta.indexOf(ignoreRuleStr);
	}

	private static void assignRulesOptions(final String[] str) {
		final ShortestOperand shortestOperandInstance = (ShortestOperand) ShortestOperand.value;
		if (str == null || shortestOperandInstance == null)
			return;
		for (final String line : str) {
			// There must be a way to make it looks good, it's looks similar to
			// the case with o.equals() and the in() function but it's not the
			// same case...
			if (line == null)
				continue;
			if (line.contains(Options.RepositionAllRightLiterals))
				shortestOperandInstance
						.setRightLiteralRule(RepositionRightLiteral.All);
			if (line.contains(Options.RepositionAllButBoolAndNull))
				shortestOperandInstance
						.setRightLiteralRule(RepositionRightLiteral.AllButBooleanAndNull);
			if (line.contains(Options.DoNotRepositionRightLiterals))
				shortestOperandInstance
						.setRightLiteralRule(RepositionRightLiteral.None);
			if (line.contains(Options.RepositionLiterals))
				shortestOperandInstance
						.setBothLiteralsRule(RepositionLiterals.All);
			if (line.contains(Options.DoNotRepositionLiterals))
				shortestOperandInstance
						.setBothLiteralsRule(RepositionLiterals.None);
		}
	}

	/**
	 * Resets the enumeration with the current values from the preferences file.
	 * Letting the rules notification decisions be updated without restarting
	 * eclipse.
	 */
	public static void reset() {
		all.clear();
		final int offset = PreferencesFile.getSpartanTitle().length;
		final String[] str = PreferencesFile.phrasePrefFile();
		final boolean useAll = str == null;
		int i = 0;
		for (final Spartanization rule : allAvailableSpartanizations()) {
			if (useAll || str != null && str.length >= i + offset
					&& !ignored(str[i + offset]))
				put(rule);
			i++;
		}

		assignRulesOptions(str);
		put(new SimplifyLogicalNegation());
	}

	/**
	 * @param name
	 *            the name of the spartanization
	 * @return an instance of the spartanization
	 */
	public static Spartanization get(final String name) {
		return all.get(name);
	}

	/**
	 * @return all the registered spartanization refactoring objects
	 */
	public static Iterable<Spartanization> all() {
		return all.values();
	}

	/**
	 * @return all the registered spartanization refactoring objects names
	 */
	public static List<String> allRulesNames() {
		final List<String> $ = new ArrayList<>();
		for (final Spartanization rule : allAvailableSpartanizations())
			if (rule != null)
				$.add(rule.getName());
		return $;
	}
}
