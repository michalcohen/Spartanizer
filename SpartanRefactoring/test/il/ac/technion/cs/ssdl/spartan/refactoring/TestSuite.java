package il.ac.technion.cs.ssdl.spartan.refactoring;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An abstract representation of our test suite, which is represented in
 * directory tree.
 * 
 * @author Yossi Gil
 * @since 2014/05/24
 */
/**
 * @author yogi
 *
 */
public abstract class TestSuite {
	/**
	 * Folder in which all test cases are found
	 */
	public static final File location = new File("TestCases");
	static String readFile(final File f) {
		try {
			final BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			String line;
			final StringBuilder $ = new StringBuilder();
			while ((line = r.readLine()) != null)
				$.append(line).append(System.lineSeparator());
			r.close();
			return $.toString();
		} catch (final IOException e) {
			fail(e.toString());
			return null;
		}
	}
	static Spartanization makeSpartanizationObject(final File f) {
		return makeSpartanizationObject(f.getName());
	}

	static Spartanization makeSpartanizationObject(final String folderForClass) {
		final Class<?> c = asClass(folderForClass);
		assertNotNull(c);
		final Object $ = getInstance(c);
		assertNotNull($);
		return (Spartanization) $;
	}

	/**
	 * Instantiates a {@link Class} object if possible, otherwise generate an
	 * assertion failure
	 * 
	 * @param c
	 *          an arbitrary class object
	 * @return an instance of the parameter
	 */
	public static Object getInstance(final Class<?> c) {
		try {
			return c.newInstance();
		} catch (final SecurityException e) {
			error("Security exception in instantiating ", c, e);
		} catch (final ExceptionInInitializerError e) {
			error("Error in instantiating class", c, e);
		} catch (final InstantiationException e) {
			error("Nullary constructor threw an exception in class", c, e);
		} catch (final IllegalAccessException e) {
			error("Missing public constructor (probably) in class", c, e);
		}
		return null;
	}

	private static Spartanization error(final String message, final Class<?> c, final Throwable e) {
		System.err.println(message + " '" + c.getCanonicalName() + "' " + e.getMessage());
		return null;
	}

	/**
	 * Convert a canonical name of a class into a {@link Class} object, if
	 * possible, otherwise generate an assertion failure
	 * 
	 * @param name
	 *          the canonical name of some class
	 * @return the object representing this class
	 * @since 2014/05/23
	 */
	private static Class<?> asClass(final String name) {
		try {
			return Class.forName(name);
		} catch (final ClassNotFoundException e) {
			fail(name + ": class not found. " + e.getMessage());
			return null;
		}
	}

	/**
	 * An abstract class representing the concept of traversing the {@link #location}
	 * while generating test cases.
	 * @see TestSuite.Traverse.Files
	 * 	  @see TestSuite.Traverse.Directories
	 * 
	 * @author Yossi Gil
	 * @since 2014/05/24
	 */
	public static abstract class Traverse extends TestSuite {
		/**
		 * @return a collection of all test cases generated in the traversal
		 */
		public final Collection<Object[]> go() {
			final List<Object[]> $ = new ArrayList<Object[]>();
			for (final File f : location.listFiles())
				go($, f);
			return $;
		}
		/**
		 * Collect test cases from each file in {@link #location}
		 * 
		 * @param $
		 *          where to save the collected test cases
		 * @param f
		 *          an entry in {@link #location}
		 */
		public abstract void go(List<Object[]> $, final File f);
	}

	/**
	 **
	 * An abstract class to be extended and implemented by client, while overriding
	 * {@link #go(List, File)} as per customer's need.
	 * @see TestSuite.Traverse.Files
	 * @see TestSuite.Traverse
	 * 
	 * @author Yossi Gil
	 * @since 2014/05/24
	 */
	public static abstract class Directories extends TestSuite.Traverse {
		/**
		 * Adds a test case to the a collection of all test cases generated in the
		 * traversal
		 */
		@Override public final void go(final List<Object[]> $, final File f) {
			if (f.isDirectory()) {
				final Object[] c = makeCase(f);
				if (c != null)
					$.add(c);
			}
		}
		abstract Object[] makeCase(File d);
	}
	/**
	 **
	 * An abstract class to be extended and implemented by client, while overriding
	 * {@link #go(List, File)} as per customer's need.
	 * @see TestSuite.Traverse.Directories
	 * @see TestSuite.Traverse
	 * 
	 * @author Yossi Gil
	 * @since 2014/05/24
	 */
	public static abstract class Files extends TestSuite.Traverse {

		/* (non-Javadoc)
		 * @see il.ac.technion.cs.ssdl.spartan.refactoring.TestSuite.Traverse#go(java.util.List, java.io.File)
		 */
		@Override public void go(final List<Object[]> $, final File d) {
			final Spartanization s = makeSpartanizationObject(d);
			for (final File f : d.listFiles())
				if (f.isFile() && f.exists()) {
					final Object[] c = makeCase(s, d, f, f.getName());
					if (c != null)
						$.add(c);
				}
		}

		abstract Object[] makeCase(final Spartanization s, final File d, final File f, final String name);
	}
}
