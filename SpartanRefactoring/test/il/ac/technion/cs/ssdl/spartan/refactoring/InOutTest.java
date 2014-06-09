package il.ac.technion.cs.ssdl.spartan.refactoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.Scanner;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Run tests in which a specific transformation is not supposed to change the
 * input text
 *
 * @author Yossi Gil
 * @since 2014/05/24
 */
@RunWith(Parameterized.class)//
public class InOutTest extends AbstractParametrizedTest {
	/**
	 * A String determines whereas we are at the IN or OUT side of the test
	 * See TestCases test files for reference.
	 */
	final static String testKeyword = "<Test Result>";
	/**
	 * Suffix for test files.
	 */
	final static String testSuffix = ".test";
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
		assertNotNull("Cannot instantiate Spartanization object", spartanization);
		final CompilationUnit cu = makeAST(fIn);
		assertEquals(1, spartanization.findOpportunities(cu).size());

		final StringBuilder str = new StringBuilder(fIn.getName());
		final int testMarker = str.indexOf(testSuffix);
		if (testMarker > 0)
			assertEquals(readFile(makeOutFile(fOut)), rewrite(spartanization, cu, new Document(readFile(makeInFile(fIn)))).get());
		else
			assertEquals(readFile(fOut), rewrite(spartanization, cu, new Document(readFile(fIn))).get());



	}
	/**
	 * Generate test cases for this parameterized class.
	 *
	 * @return a collection of cases, where each case is an array of four objects,
	 *         the spartanization, the test case name, the input file, and the
	 *         output file.
	 */
	@Parameters(name = "{index}: {0} {1}")//
	public static Collection<Object[]> cases() {
		return new TestSuite.Files() {
			@Override Object[] makeCase(final Spartanization s, final File d, final File f, final String name) {
				if (name.endsWith(testSuffix) && fileToStringBuilder(f).indexOf(testKeyword) > 0)
					return  new Object[] { s, name, f, makeOutFile(f) };

				if (!name.endsWith(".in"))
					return null;
				final File fOut = new File(d, name.replaceAll("\\.in$", ".out"));
				return !fOut.exists() ? null : new Object[] { s, name.replaceAll("\\.in$", ""), f, fOut };
			}
		}.go();
	}


	enum TestDirection {In, Out}
	static File makeInFile(final File file){
		final StringBuilder str = new StringBuilder(fileToStringBuilder(file));
		final int testMarker = str.indexOf(testKeyword);
		if (testMarker > 0)
			str.delete(str.indexOf(testKeyword), str.length());
		return createTempFile(str, TestDirection.In, file);
	}
	static File makeOutFile(final File file){
		final StringBuilder str = new StringBuilder(fileToStringBuilder(file));
		final int testMarker = str.indexOf(testKeyword);
		if (testMarker > 0)
			str.delete(0, str.indexOf(testKeyword) + testKeyword.length() + (str.indexOf("\r\n") > 0 ? 2 : 1));

		return createTempFile(str, TestDirection.Out, file);
	}

	static File createTempFile (final StringBuilder str, final TestDirection direction, final File file){
		File $;
		try {
			if ( direction == TestDirection.In)
				$ = File.createTempFile(file.getName().replace(".", ""), ".in");
			else
				$ = File.createTempFile(file.getName().replace(".", ""), ".out");

			final RandomAccessFile fh = new RandomAccessFile ($, "rw");
			fh.writeBytes(str.toString());
			fh.close();
			$.deleteOnExit();

		} catch (final IOException e) {
			$ = file;
		}
		return $;
	}

	static StringBuilder fileToStringBuilder(final File file){
		try {
			return new StringBuilder( new Scanner(file).useDelimiter("\\Z").next());
		} catch (final Exception e) {
			return new StringBuilder("");
		}
	}

}
