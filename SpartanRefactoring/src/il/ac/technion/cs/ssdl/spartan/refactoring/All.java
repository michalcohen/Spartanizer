package il.ac.technion.cs.ssdl.spartan.refactoring;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * @author Boris van Sosin <code><boris.van.sosin [at] gmail.com></code> (v2)
 * @author Ofir Elmakias <code><elmakias [at] outlook.com></code> (original /
 *         30.05.2014) (v3)
 * @author Tomer Zeltzer <code><tomerr90 [at] gmail.com></code> (original /
 *         30.05.2014) (v3)
 * @since 2013/07/01
 */
@SuppressWarnings("null") public enum All {
	;
	private static final Map<String, Spartanization> all = new HashMap<String, Spartanization>();
	private static final Spartanization[] rules = { //
	new ComparisonWithBoolean(), //
	    new ForwardDeclaration(), //
	    new InlineSingleUse(), //
	    new RenameReturnVariableToDollar(), //
	    new ShortestBranchFirst(), //
	    new ShortestOperand(), //
	    new Ternarize(), //
	    null };
	private static void put(final Spartanization s) {
		all.put(s.toString(), s);
	}
	private static String[] phrasePrefFile() {
		Scanner sc;
		String[] arr = null;
		try {
			sc = new Scanner(new File(Spartanization.getPrefFilePath()));
			final List<String> lines = new ArrayList<String>();
			while (sc.hasNextLine())
				lines.add(sc.nextLine());
			arr = lines.toArray(new String[0]);
			sc.close();
		} catch (final FileNotFoundException e) {
			// TODO Add existence check
			e.printStackTrace();
		}
		return arr;
	}
	private static boolean ignored(final String sparta) {
		return sparta.indexOf("false") >= 0;
	}
	static {
		reset();
	}
	/**
	 * Resets the enumeration with the current values from the preferences file.
	 * Letting the rules notification decisions be updated without restarting
	 * eclipse.
	 */
	public static void reset() {
		final String[] str = phrasePrefFile();
		final int offset = Spartanization.getSpartanTitle().length;
		all.clear();
		final boolean useAll = (str == null);
		for (int i = 0; i < rules.length - 1; i++)
			if (useAll || !ignored(str[i + offset]))
				put(rules[i]);
		put(new SimplifyLogicalNegation());
	}
	/**
	 * @param name
	 *          the name of the spartanization
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
}
