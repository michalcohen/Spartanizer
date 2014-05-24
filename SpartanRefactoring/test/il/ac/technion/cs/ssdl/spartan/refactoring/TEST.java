package il.ac.technion.cs.ssdl.spartan.refactoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import il.ac.technion.cs.ssdl.spartan.utils.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author Yossi Gil
 * @since 2014/05/24
 */
public class TEST {

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

	static CompilationUnit makeAST(final File f) {
		return (CompilationUnit) Utils.makeParser(readFile(f)).createAST(null);
	}

	static Document rewrite(final Spartanization s, final CompilationUnit cu, final Document d) {
		try {
			s.createRewrite(cu, null).rewriteAST(d, null).apply(d);
			return d;
		} catch (final MalformedTreeException e) {
			fail(e.getMessage());
		} catch (final IllegalArgumentException e) {
			fail(e.getMessage());
		} catch (final BadLocationException e) {
			fail(e.getMessage());
		}
		return null;
	}

	static Spartanization makeSpartanizationObject(final File f) {
		return makeSpartanizationObject(f.getName());
	}
	static Spartanization makeSpartanizationObject(final String folderForClass) {
		final Class<?> c = asClass(folderForClass);
		assertNotNull(c);
		final Object o = getInstance(c);
		assertNotNull(o);
		return (Spartanization) o;
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
	static Class<?> asClass(final String name) {
		try {
			return Class.forName(name);
		} catch (final ClassNotFoundException e) {
			fail(name + ": class not found. " + e.getMessage());
			return null;
		}
	}

	/**
	 * Instantiates a {@link Class} object if possible, otherwise generate an
	 * assertion failure
	 * 
	 * @param c
	 *          an arbitrary class object
	 * @return an instance of the parameter
	 */
	static Object getInstance(final Class<?> c) {
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
	 * Tests that each directory in our test suite is a name of valid
	 * {@link Spartanization} class.
	 * 
	 * @author Yossi Gil
	 * @since 2014/05/24
	 */
	@RunWith(Parameterized.class)//
	public static class SpartanizationClassForFolderExists {
		/**
		 * A name of a folder whose name should represent a {@link Spartanization}
		 * class
		 */
		@Parameter(value = 0) public String folderForClass;
		/**
		 * Tests that {@link #folderForClass} is a valid class name
		 */
		@Test public void validClassName() {
			makeSpartanizationObject(folderForClass);
		}
		/**
		 * @return a collection of cases, where each case is an array of length 1
		 *         containing the name of a in the test suite
		 */
		@Parameters(name = "{index}: {0}")//
		public static Collection<Object[]> cases() {
			return new ListDirectories() {
				@Override Object[] makeCase(final File d) {
					return new Object[] { d.getName() };
				}
			}.go();
		}
	}

	/**
	 * Run tests in which a specific transformation is not supposed to change the
	 * input text
	 * 
	 * @author Yossi Gil
	 * @since 2014/05/24
	 */
	@RunWith(Parameterized.class)//
	public static class InOutTest {
		/**
		 * An object describing the required transformation
		 */
		@Parameter(value = 0) public Spartanization spartanization;
		/**
		 * The name of the specific test for this transformation
		 */
		@Parameter(value = 1) public String name;
		/**
		 * Where the input text can be found
		 */
		@Parameter(value = 2) public File fIn;
		/**
		 * Where the expected output can be found?
		 */
		@Parameter(value = 3) public File fOut;

		/**
		 * Runs a parameterized test case, based on the instance variables of this
		 * instance
		 */
		@Test public void go() {
			final CompilationUnit cu = makeAST(fIn);
			assertNotNull("Cannot instantiate Spartanization object", spartanization);
			assertEquals(1, spartanization.findOpportunities(cu).size());
			assertEquals(readFile(fOut), rewrite(spartanization, cu, new Document(readFile(fIn))).get());
		}

		/**
		 * Generate test cases for this parameterized class.
		 * 
		 * @return a collection of cases, where each case is an array of four
		 *         objects, the spartanization, the test case name, the input file,
		 *         and the output file.
		 */
		@Parameters(name = "{index}: {0} {1}")//
		public static Collection<Object[]> cases() {
			return new ListFiles() {
				@Override Object[] makeCase(final Spartanization s, final File d, final File f, final String name) {
					if (!name.endsWith(".in"))
						return null;
					final File fOut = new File(d, name.replaceAll("\\.in$", ".out"));
					if (!fOut.exists())
						return null;
					return new Object[] { s, name.replaceAll("\\.in$", ""), f, fOut };
				}
			}.go();
		}
	}
	/**
	 * Test cases in which the transformation should not do anything
	 * 
	 * @author Yossi Gil
	 * @since 2014/05/24
	 */
	@RunWith(Parameterized.class)//
	public static class Unchanged {
		/**
		 * An object describing the required transformation
		 */
		@Parameter(value = 0) public Spartanization spartanization;
		/**
		 * The name of the specific test for this transformation
		 */
		@Parameter(value = 1) public String name;
		/**
		 * Where the input text can be found
		 */
		@Parameter(value = 2) public File fIn;
		/**
		 * Runs a parameterized test case, based on the instance variables of this
		 * instance
		 */
		@Test public void go() {
			final CompilationUnit cu = makeAST(fIn);
			assertNotNull("Cannot instantiate Spartanization object", spartanization);
			assertEquals(0, spartanization.findOpportunities(cu).size());
			assertEquals(readFile(fIn), rewrite(spartanization, cu, new Document(readFile(fIn))).get());
		}
		/**
		 * @return a collection of cases, where each cases is an array of three
		 *         objects, the spartanization, the test case name, and the input
		 *         file
		 */
		@Parameters(name = "{index}: {0} {1}")//
		public static Collection<Object[]> cases() {
			return new ListFiles() {
				@Override Object[] makeCase(final Spartanization s, final File d, final File f, final String name) {
					if (!name.endsWith(".in"))
						return null;
					if (new File(d, name.replaceAll("\\.in$", ".out")).exists())
						return null;
					return new Object[] { s, name.replaceAll("\\.in$", ""), f };
				}
			}.go();
		}
	}
}

abstract class ListDirectories extends TEST {
	/**
	 * Folder in which all test cases are found
	 */
	private static final File testCasesLocation = new File("TestCases");
	/**
	 * @return a collection of all test cases generated in the traversal
	 */
	public Collection<Object[]> go() {
		final List<Object[]> $ = new ArrayList<Object[]>();
		for (final File d : testCasesLocation.listFiles())
			if (d.isDirectory()) {
				final Object[] c = makeCase(d);
				if (c != null)
					$.add(c);
			}
		return $;
	}
	abstract Object[] makeCase(File d);
}

abstract class ListFiles extends TEST {
	/**
	 * Folder in which all test cases are found
	 */
	private static final File testCasesLocation = new File("TestCases");

	/**
	 * @return a collection of all test cases generated in the traversal
	 */
	public Collection<Object[]> go() {
		final List<Object[]> $ = new ArrayList<Object[]>();
		for (final File d : testCasesLocation.listFiles())
			if (d.isDirectory())
				$.addAll(go(d));
		return $;
	}

	private Collection<Object[]> go(final File d) {
		return go(makeSpartanizationObject(d), d);
	}

	private Collection<Object[]> go(final Spartanization s, final File d) {
		final List<Object[]> $ = new ArrayList<Object[]>();
		for (final File f : d.listFiles())
			if (f.isFile() && f.exists()) {
				final Object[] testCase = makeCase(s, d, f, f.getName());
				if (null != testCase)
					$.add(testCase);
			}
		return $;
	}
	abstract Object[] makeCase(final Spartanization s, final File d, final File f, final String name);
}
