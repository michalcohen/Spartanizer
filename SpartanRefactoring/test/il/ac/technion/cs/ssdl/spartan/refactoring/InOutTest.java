package il.ac.technion.cs.ssdl.spartan.refactoring;

import static il.ac.technion.cs.ssdl.spartan.utils.Funcs.objects;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
  private static final String WHITES = "\\s+";
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
  @Parameter(value = 2) public File input;
  /**
   * Where the expected output can be found?
   */
  @Parameter(value = 3) public File output;

  /**
   * Runs a parameterized test case, based on the instance variables of this
   * instance
   */
  @Test public void go() {
    assertNotNull("Cannot instantiate Spartanization object", spartanization);
    final CompilationUnit cu = makeAST(makeInFile(input));
    assertEquals(cu.toString(), 1, spartanization.findOpportunities(cu).size());
    final boolean properSuffix = input.getName().endsWith(testSuffix);
    assertTrue(similar(!properSuffix ? readFile(output) : readFile(TestSuite.makeOutFile(output)),
        rewrite(spartanization, cu, new Document(!properSuffix ? readFile(input) : readFile(TestSuite.makeInFile(input)))).get()));
  }

  private static boolean similar(final String expected, final String actual) {
    return expected.equals(actual) || almostSame(expected, actual);
  }

  private static boolean almostSame(final String expected, final String actual) {
    assertEquals(compressSpaces(expected), compressSpaces(actual));
    return true;
  }

  private static String compressSpaces(final String s) {
    String $ = s//
        .replaceAll("(?m)^[ \t]*\r?\n", "") // Remove empty lines
        .replaceAll("[ \t]+", " ") // Squeeze whites
        .replaceAll("[ \t]+$", "") // Remove trailing spaces
        .replaceAll("^[ \t]+$", "") // No space at line beginnings
        ;
    for (final String operator : new String[] { ",", "\\+", "-", "\\*", "\\|", "\\&", "%", "\\(", "\\)", "^" })
      $ = $ //
      .replaceAll(WHITES + operator, operator) // Preceding whites
      .replaceAll(operator + WHITES, operator) // Trailing whites
      ;
    return $;
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
      @Override Object[] makeCase(final Spartanization s, final File folder, final File input, final String name) {
        if (name.endsWith(testSuffix) && 0 < fileToStringBuilder(input).indexOf(testKeyword))
          return objects(s, name, input, makeOutFile(input));
        if (!name.endsWith(".in"))
          return null;
        final File output = new File(folder, name.replaceAll("\\.in$", ".out"));
        return !output.exists() ? null : objects(s, name.replaceAll("\\.in$", ""), input, output);
      }
    }.go();
  }
}
