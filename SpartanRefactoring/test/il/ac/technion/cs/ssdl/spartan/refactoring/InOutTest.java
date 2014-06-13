package il.ac.technion.cs.ssdl.spartan.refactoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.Collection;

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
		final CompilationUnit cu = makeAST(makeInFile(fIn));
		assertEquals(1, spartanization.findOpportunities(cu).size());

		final StringBuilder str = new StringBuilder(fIn.getName());
		final int testMarker = str.indexOf(testSuffix);
		if (testMarker > 0)
			assertEquals(readFile(TestSuite.makeOutFile(fOut)), rewrite(spartanization, cu, new Document(readFile(TestSuite.makeInFile(fIn)))).get());
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
				if (name.endsWith(testSuffix) && 0 < fileToStringBuilder(f).indexOf(testKeyword))
					return  new Object[] { s, name, f, makeOutFile(f) };

				if (!name.endsWith(".in"))
					return null;
				final File fOut = new File(d, name.replaceAll("\\.in$", ".out"));
				return !fOut.exists() ? null : new Object[] { s, name.replaceAll("\\.in$", ""), f, fOut };
			}
		}.go();
	}
}
